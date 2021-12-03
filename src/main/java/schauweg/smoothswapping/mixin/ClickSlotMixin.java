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
import schauweg.smoothswapping.InventorySwap;
import schauweg.smoothswapping.SmoothSwapping;

import java.util.*;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotMixin {

    @Shadow @Final private SlotActionType actionType;

    @Shadow @Final private Int2ObjectMap<ItemStack> modifiedStacks;

    //id of slot that got clicked/hovered over
    @Shadow @Final private int slot;

    private void addInventorySwap(int index, Slot fromSlot, Slot toSlot, boolean checked, int amount) {
        List<InventorySwap> swaps = SmoothSwapping.swaps.getOrDefault(index, new ArrayList<>());
        swaps.add(new InventorySwap(fromSlot, toSlot, checked, amount));
        SmoothSwapping.swaps.put(index, swaps);
    }

    @Inject(method = "<init>(IIIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("TAIL"))
    public void onInit(CallbackInfo cbi){
        //remove swap when stack gets moved before it arrived
        SmoothSwapping.swaps.remove(slot);

        if ((actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) && modifiedStacks.size() > 1 && MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
            assert MinecraftClient.getInstance().player != null;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            ScreenHandler screenHandler = player.currentScreenHandler;

            System.out.println(modifiedStacks);

            Slot mouseSlot = screenHandler.getSlot(slot);
            if (actionType == SlotActionType.QUICK_MOVE) {
                for (Map.Entry<Integer, ItemStack> stackEntry : modifiedStacks.int2ObjectEntrySet()) {
                    int destinationSlotID = stackEntry.getKey();
                    if (destinationSlotID != slot) {
                        Slot destinationSlot = screenHandler.getSlot(destinationSlotID);
                        ItemStack oldStack = SmoothSwapping.oldStacks.get(destinationSlotID);

                        //If destination slot can take items from player and destination slot is empty or has same item in it
                        if (destinationSlot.canTakePartial(player) && (oldStack.isEmpty() || oldStack.isItemEqual(SmoothSwapping.oldStacks.get(slot)))) {
                            int oldDestinationCount = SmoothSwapping.oldStacks.get(destinationSlot.id).getCount();
                            int newDestinationCount = destinationSlot.getStack().getCount();
                            addInventorySwap(destinationSlotID, mouseSlot, destinationSlot, false, newDestinationCount - oldDestinationCount);
                        }
                    }
                }
            } else if (actionType == SlotActionType.SWAP) {
                for (Map.Entry<Integer, ItemStack> stackEntry : modifiedStacks.int2ObjectEntrySet()) {
                    int destinationSlotID = stackEntry.getKey();
                    if (destinationSlotID != slot) {
                        Slot destinationSlot = screenHandler.getSlot(destinationSlotID);
                        SmoothSwapping.swaps.remove(destinationSlotID);
                        //if mouse slot is output slot(crafting slot for example) and old destination stack is empty
                        if (!mouseSlot.canTakePartial(player) && destinationSlot.canTakePartial(player) && SmoothSwapping.oldStacks.get(destinationSlotID).isEmpty()){
                            addInventorySwap(destinationSlotID, mouseSlot, destinationSlot, true, destinationSlot.getStack().getCount());
                        } else if (mouseSlot.canTakePartial(player) && destinationSlot.canTakePartial(player)){
                            if (destinationSlot.hasStack()){
                                addInventorySwap(destinationSlotID, mouseSlot, destinationSlot, true, destinationSlot.getStack().getCount());
                            }
                            if (mouseSlot.hasStack()){
                                addInventorySwap(slot, destinationSlot, mouseSlot, true, mouseSlot.getStack().getCount());
                            }
                        }
                    }
                }
            }
        }
    }

}
