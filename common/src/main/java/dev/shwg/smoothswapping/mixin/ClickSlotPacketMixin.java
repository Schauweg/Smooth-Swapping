package dev.shwg.smoothswapping.mixin;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.SwapUtil;
import dev.shwg.smoothswapping.config.ConfigManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotPacketMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(int syncId, int revision, short slot, byte button, SlotActionType actionType, Int2ObjectMap<ItemStackHash> modifiedStacks, ItemStackHash cursor, CallbackInfo cbi) {
        if (!ConfigManager.getConfig().getToggleMod())
            return;
        //remove swap when stack gets moved before it arrived
        SmoothSwapping.swaps.remove((int) slot);

        if ((actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) && modifiedStacks.size() > 1 && MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
            assert MinecraftClient.getInstance().player != null;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            ScreenHandler screenHandler = player.currentScreenHandler;

            if (slot >= 0 && slot < screenHandler.slots.size()) {
                Slot mouseHoverSlot = screenHandler.getSlot(slot);

                if (actionType == SlotActionType.QUICK_MOVE && !mouseHoverSlot.canTakePartial(player)) {

                    ItemStackHash newMouseStackHash = modifiedStacks.get(slot);
                    ItemStack oldMouseStack = smooth_Swapping$getSafeOldStack(slot);

                    //only if new items are less or equal (crafting table output for example)
                    if (oldMouseStack != null && newMouseStackHash instanceof ItemStackHash.Impl newMouseStackImpl && (newMouseStackImpl.count() - oldMouseStack.getCount() <= 0)) {
                        SmoothSwapping.clickSwapStack = slot;
                    }
                } else if (actionType == SlotActionType.SWAP) {
                    SmoothSwapping.clickSwap = true;

                    for (Map.Entry<Integer, ItemStackHash> stackEntry : modifiedStacks.int2ObjectEntrySet()) {
                        int destinationSlotID = stackEntry.getKey();

                        if (destinationSlotID >= 0 && destinationSlotID < screenHandler.slots.size() && destinationSlotID != slot) {
                            Slot destinationSlot = screenHandler.getSlot(destinationSlotID);

                            ItemStack destinationOldStack = smooth_Swapping$getSafeOldStack(destinationSlotID);

                            if (!mouseHoverSlot.canTakePartial(player) && destinationSlot.canTakePartial(player)) {
                                if (destinationOldStack.isEmpty()) {
                                    SwapUtil.addI2IInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getStack().getCount());
                                }
                            } else if (mouseHoverSlot.canTakePartial(player) && destinationSlot.canTakePartial(player)) {
                                if (destinationSlot.hasStack()) {
                                    SwapUtil.addI2IInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getStack().getCount());
                                }
                                if (mouseHoverSlot.hasStack()) {
                                    SwapUtil.addI2IInventorySwap(slot, destinationSlot, mouseHoverSlot, false, mouseHoverSlot.getStack().getCount());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Unique
    private ItemStack smooth_Swapping$getSafeOldStack(int slot) {
        DefaultedList<ItemStack> oldStacks = SmoothSwapping.oldStacks;
        if (oldStacks == null) {
            oldStacks = DefaultedList.of();
            SmoothSwapping.oldStacks = oldStacks;
        }
        if (slot < 0 || slot >= oldStacks.size()) {
            return ItemStack.EMPTY;
        }
        return oldStacks.get(slot);
    }
}
