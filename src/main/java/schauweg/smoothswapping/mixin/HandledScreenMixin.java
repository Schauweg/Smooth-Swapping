package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.OldStack;
import schauweg.smoothswapping.SmoothSwapping;

import java.util.List;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    private Screen currentScreen = null;

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo cbi) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.player.currentScreenHandler == null) {
            return;
        }

        DefaultedList<ItemStack> stacks = client.player.currentScreenHandler.getStacks();

        Screen screen = client.currentScreen;
        if (currentScreen != screen) {
            SmoothSwapping.swaps.clear();
            SmoothSwapping.addAllOldItems(SmoothSwapping.oldStacks, stacks);
            currentScreen = screen;
        }

        if (!areStacksEqual(SmoothSwapping.oldStacks, stacks)) {
            SmoothSwapping.addAllOldItems(SmoothSwapping.oldStacks, stacks);
        }
    }

    private boolean areStacksEqual(List<OldStack> oldStacks, DefaultedList<ItemStack> newStacks) {
        if (oldStacks == null || newStacks == null || (oldStacks.size() != newStacks.size())) {
            return false;
        } else {
            for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
                ItemStack newStack = newStacks.get(slotID);
                OldStack oldStack = oldStacks.get(slotID);
                if (!newStack.getItem().equals(oldStack.getItem()) || newStack.getCount() != oldStack.getCount()) {
                    return false;
                }
            }
        }
        return true;
    }
}
