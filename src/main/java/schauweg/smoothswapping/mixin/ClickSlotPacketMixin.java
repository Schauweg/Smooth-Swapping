package schauweg.smoothswapping.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.config.ConfigManager;

import java.util.*;

import static schauweg.smoothswapping.SwapUtil.addI2IInventorySwap;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotPacketMixin {

    @Shadow
    @Final
    private SlotActionType actionType;

    @Shadow
    @Final
    private Int2ObjectMap<ItemStack> modifiedStacks;

    //id of slot that got clicked/hovered over
    @Shadow
    @Final
    private int slot;

    @Inject(method = "<init>(IIIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("TAIL"))
    public void onInit(CallbackInfo cbi) {
        if (!ConfigManager.getConfig().getToggleMod())
            return;

        //remove swap when stack gets moved before it arrived
        SmoothSwapping.swaps.remove(slot);

        if ((actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) && modifiedStacks.size() > 1 && MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
            assert MinecraftClient.getInstance().player != null;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            ScreenHandler screenHandler = player.currentScreenHandler;
            Slot mouseHoverSlot = screenHandler.getSlot(slot);

            if (actionType == SlotActionType.QUICK_MOVE && !mouseHoverSlot.canTakePartial(player)) {

                ItemStack newMouseStack = modifiedStacks.get(slot);
                ItemStack oldMouseStack = SmoothSwapping.oldStacks.get(slot);

                //only if new items are less or equal (crafting table output for example)
                if (newMouseStack == null || newMouseStack.getCount() - oldMouseStack.getCount() <= 0) {
                    SmoothSwapping.clickSwapStack = slot;
                }

            } else if (actionType == SlotActionType.SWAP) {
                SmoothSwapping.clickSwap = true;
                for (Map.Entry<Integer, ItemStack> stackEntry : modifiedStacks.int2ObjectEntrySet()) {
                    int destinationSlotID = stackEntry.getKey();
                    if (destinationSlotID != slot) {
                        Slot destinationSlot = screenHandler.getSlot(destinationSlotID);
                        SmoothSwapping.swaps.remove(destinationSlotID);
                        //if mouse slot is output slot(crafting slot for example) and old destination stack is empty
                        if (!mouseHoverSlot.canTakePartial(player) && destinationSlot.canTakePartial(player) && SmoothSwapping.oldStacks.get(destinationSlotID).isEmpty()) {
                            addI2IInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getStack().getCount());
                        } else if (mouseHoverSlot.canTakePartial(player) && destinationSlot.canTakePartial(player)) {
                            if (destinationSlot.hasStack()) {
                                addI2IInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getStack().getCount());
                            }
                            if (mouseHoverSlot.hasStack()) {
                                addI2IInventorySwap(slot, destinationSlot, mouseHoverSlot, false, mouseHoverSlot.getStack().getCount());
                            }
                        }
                    }
                }
            }
        }
    }
}
