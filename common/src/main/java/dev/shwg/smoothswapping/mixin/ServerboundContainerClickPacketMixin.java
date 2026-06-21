package dev.shwg.smoothswapping.mixin;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.SwapUtil;
import dev.shwg.smoothswapping.config.ConfigManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerboundContainerClickPacket.class)
public class ServerboundContainerClickPacketMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(int syncId, int revision, short slot, byte button, ContainerInput containerInput, Int2ObjectMap<HashedStack> modifiedStacks, HashedStack cursor, CallbackInfo cbi) {
        if (!ConfigManager.getConfig().getToggleMod())
            return;
        //remove swap when stack gets moved before it arrived
        SmoothSwapping.swaps.remove((int) slot);

        if ((containerInput == ContainerInput.QUICK_MOVE || containerInput == ContainerInput.SWAP) && modifiedStacks.size() > 1 && Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen) {
            assert Minecraft.getInstance().player != null;
            LocalPlayer player = Minecraft.getInstance().player;
            AbstractContainerMenu screenHandler = player.containerMenu;

            if (slot >= 0 && slot < screenHandler.slots.size()) {
                Slot mouseHoverSlot = screenHandler.getSlot(slot);

                if (containerInput == ContainerInput.QUICK_MOVE && !mouseHoverSlot.allowModification(player)) {

                    HashedStack newMouseStackHash = modifiedStacks.get(slot);
                    ItemStack oldMouseStack = smooth_Swapping$getSafeOldStack(slot);

                    //only if new items are less or equal (crafting table output for example)
                    if (oldMouseStack != null && newMouseStackHash instanceof HashedStack.ActualItem newMouseStackImpl && (newMouseStackImpl.count() - oldMouseStack.getCount() <= 0)) {
                        SmoothSwapping.clickSwapStack = slot;
                    }
                } else if (containerInput == ContainerInput.SWAP) {
                    SmoothSwapping.clickSwap = true;

                    for (Map.Entry<Integer, HashedStack> stackEntry : modifiedStacks.int2ObjectEntrySet()) {
                        int destinationSlotID = stackEntry.getKey();

                        if (destinationSlotID >= 0 && destinationSlotID < screenHandler.slots.size() && destinationSlotID != slot) {
                            Slot destinationSlot = screenHandler.getSlot(destinationSlotID);

                            ItemStack destinationOldStack = smooth_Swapping$getSafeOldStack(destinationSlotID);

                            if (!mouseHoverSlot.allowModification(player) && destinationSlot.allowModification(player)) {
                                if (destinationOldStack.isEmpty()) {
                                    SwapUtil.addI2IInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getItem().getCount());
                                }
                            } else if (mouseHoverSlot.allowModification(player) && destinationSlot.allowModification(player)) {
                                if (destinationSlot.hasItem()) {
                                    SwapUtil.addI2IInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getItem().getCount());
                                }
                                if (mouseHoverSlot.hasItem()) {
                                    SwapUtil.addI2IInventorySwap(slot, destinationSlot, mouseHoverSlot, false, mouseHoverSlot.getItem().getCount());
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
        NonNullList<ItemStack> oldStacks = SmoothSwapping.oldStacks;
        if (oldStacks == null) {
            oldStacks = NonNullList.create();
            SmoothSwapping.oldStacks = oldStacks;
        }
        if (slot < 0 || slot >= oldStacks.size()) {
            return ItemStack.EMPTY;
        }
        return oldStacks.get(slot);
    }
}
