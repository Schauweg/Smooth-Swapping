package schauweg.smoothswapping.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlotMixin {

    @Shadow @Final private SlotActionType actionType;

    @Shadow @Final private Int2ObjectMap<ItemStack> modifiedStacks;

    @Shadow @Final private int slot;

    private void addInventorySwap(int index, Slot fromSlot, Slot toSlot, boolean checked) {
        List<InventorySwap> swaps = SmoothSwapping.swaps.getOrDefault(index, new ArrayList<>());
        int oldToCount = SmoothSwapping.oldStacks.get(toSlot.id).getCount();
        int newToCount = toSlot.getStack().getCount();
        swaps.add(new InventorySwap(fromSlot, toSlot, checked, newToCount - oldToCount));
        SmoothSwapping.swaps.put(index, swaps);
    }

    @Inject(method = "<init>(IIIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("TAIL"))
    public void onInit(CallbackInfo cbi){
        //remove swap when stack gets moved before it arrived
        SmoothSwapping.swaps.remove(slot);
        if ((actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) && modifiedStacks.size() > 1 && MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
            assert MinecraftClient.getInstance().player != null;
            ScreenHandler screenHandler = MinecraftClient.getInstance().player.currentScreenHandler;
            Slot fromSlot = screenHandler.getSlot(slot);
            if (actionType == SlotActionType.QUICK_MOVE) {
                for (Map.Entry<Integer, ItemStack> stackEntry : modifiedStacks.int2ObjectEntrySet()) {
                    if (stackEntry.getKey() != slot) {
                        Slot toSlot = screenHandler.getSlot(stackEntry.getKey());
                        addInventorySwap(stackEntry.getKey(), fromSlot, toSlot, false);
                    }
                }
            } else if (actionType == SlotActionType.SWAP) {
                for (Map.Entry<Integer, ItemStack> stackEntry : modifiedStacks.int2ObjectEntrySet()) {
                    int toSlotID = stackEntry.getKey();
                    if (toSlotID != slot) {
                        Slot toSlot = screenHandler.getSlot(toSlotID);
                        if (toSlot.hasStack())
                            addInventorySwap(toSlotID, fromSlot, toSlot,  true);
                        if (fromSlot.hasStack())
                            addInventorySwap(slot, toSlot, fromSlot, true);
                    }
                }
            }
        }
    }

}
