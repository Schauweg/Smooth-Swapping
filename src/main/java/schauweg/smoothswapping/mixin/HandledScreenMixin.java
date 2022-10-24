package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.SmoothSwapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    private Screen currentScreen = null;

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo cbi) {
//        MinecraftClient client = MinecraftClient.getInstance();
//
//        if (client.player == null || client.player.currentScreenHandler == null) {
//            return;
//        }
//
//        DefaultedList<ItemStack> stacks = client.player.currentScreenHandler.getStacks();
//
//        Screen screen = client.currentScreen;
//        if (currentScreen != screen) {
//            SmoothSwapping.swaps.clear();
//            addAll(SmoothSwapping.oldStacks, stacks);
//            currentScreen = screen;
//        }
//
////        Map<Integer, ItemStack> changedStacks = getChangedStacks(SmoothSwapping.oldStacks, stacks);
////
////        if (changedStacks.size() > 0){
////
////
////
////            addAll(SmoothSwapping.oldStacks, stacks);
////        }
//
//        if (!areStacksEqual(SmoothSwapping.oldStacks, stacks)) {
//            addAll(SmoothSwapping.oldStacks, stacks);
//        }
    }


    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    public void onTick(CallbackInfo cbi) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.player.currentScreenHandler == null) {
            return;
        }

        DefaultedList<ItemStack> stacks = client.player.currentScreenHandler.getStacks();

        Screen screen = client.currentScreen;
        if (currentScreen != screen) {
            SmoothSwapping.swaps.clear();
            addAll(SmoothSwapping.oldStacks, stacks);
            currentScreen = screen;
        }

        Map<Integer, ItemStack> changedStacks = getChangedStacks(SmoothSwapping.oldStacks, stacks);
        if (changedStacks.size() > 0){
            System.out.println(changedStacks);

            //TODO calculate difference between stacks and decide what slots move to which


            addAll(SmoothSwapping.oldStacks, stacks);
        }

        if (!areStacksEqual(SmoothSwapping.oldStacks, stacks)) {
            addAll(SmoothSwapping.oldStacks, stacks);
        }
    }

    private Map<Integer, ItemStack> getChangedStacks(DefaultedList<ItemStack> oldStacks, DefaultedList<ItemStack> newStacks) {
        Map<Integer, ItemStack> changedStacks = new HashMap<>();
        for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
            ItemStack newStack = newStacks.get(slotID);
            ItemStack oldStack = oldStacks.get(slotID);
            if (!ItemStack.areEqual(oldStack, newStack)) {
                changedStacks.put(slotID, newStack.copy());
            }
        }
        return changedStacks;
    }

    private boolean areStacksEqual(DefaultedList<ItemStack> oldStacks, DefaultedList<ItemStack> newStacks) {
        if (oldStacks == null || newStacks == null || (oldStacks.size() != newStacks.size())) {
            return false;
        } else {
            for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
                ItemStack newStack = newStacks.get(slotID);
                ItemStack oldStack = oldStacks.get(slotID);
                if (!ItemStack.areEqual(oldStack, newStack)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addAll(DefaultedList<ItemStack> oldStacks, DefaultedList<ItemStack> newStacks) {
        oldStacks.clear();
        newStacks.stream().map(ItemStack::copy).forEach(oldStacks::add);
    }
}
