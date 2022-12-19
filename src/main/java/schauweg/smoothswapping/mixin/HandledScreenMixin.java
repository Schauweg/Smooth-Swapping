package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.SwapStacks;
import schauweg.smoothswapping.SwapUtil;
import schauweg.smoothswapping.Vec2;
import schauweg.smoothswapping.config.ConfigManager;

import java.util.*;

import static schauweg.smoothswapping.SmoothSwapping.*;
import static schauweg.smoothswapping.SwapUtil.getCount;
import static schauweg.smoothswapping.SwapUtil.updateStacks;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {


    @Shadow
    @Final
    protected ScreenHandler handler;

    @Shadow
    protected int x, y;

    private Screen currentScreen = null;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo cbi) {
        if (!ConfigManager.getConfig().getToggleMod())
            return;

        @SuppressWarnings("rawtypes")
        HandledScreen handledScreen = (HandledScreen) (Object) this;

        if (handledScreen instanceof CreativeInventoryScreen) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.player.currentScreenHandler == null) {
            return;
        }

        currentStacks = client.player.currentScreenHandler.getStacks();

        Screen screen = client.currentScreen;

        if (clickSwap) {
            clickSwap = false;
            updateStacks(currentStacks, oldStacks);
            return;
        }

        if (currentScreen != screen) {
            SmoothSwapping.swaps.clear();
            updateStacks(currentStacks, oldStacks);
            currentScreen = screen;
            return;
        }

        Map<Integer, ItemStack> changedStacks = getChangedStacks(SmoothSwapping.oldStacks, currentStacks);
        if (changedStacks.size() > 1 && !clickSwap) {

            List<SwapStacks> moreStacks = new ArrayList<>();
            List<SwapStacks> lessStacks = new ArrayList<>();

            int totalAmount = 0;
            for (Map.Entry<Integer, ItemStack> stackEntry : changedStacks.entrySet()) {
                int slotID = stackEntry.getKey();
                ItemStack newStack = stackEntry.getValue();
                ItemStack oldStack = SmoothSwapping.oldStacks.get(slotID);

                //whether the stack got more items or less and if slot is output slot
                if (getCount(newStack) > getCount(oldStack) && handler.getSlot(slotID).canTakePartial(MinecraftClient.getInstance().player)) {
                    moreStacks.add(new SwapStacks(slotID, oldStack, newStack, getCount(oldStack) - getCount(newStack)));
                    totalAmount += getCount(newStack) - getCount(oldStack);
                } else if (getCount(newStack) < getCount(oldStack) && handler.getSlot(slotID).canTakePartial(MinecraftClient.getInstance().player) && SmoothSwapping.clickSwapStack == null) {
                    lessStacks.add(new SwapStacks(slotID, oldStack, newStack, getCount(oldStack) - getCount(newStack)));
                }
            }
            if (SmoothSwapping.clickSwapStack != null){
                lessStacks.clear();
                ItemStack newStack = handler.getSlot(clickSwapStack).getStack();
                ItemStack oldStack = SmoothSwapping.oldStacks.get(clickSwapStack);
                lessStacks.add(new SwapStacks(clickSwapStack, oldStack, newStack, totalAmount));
                SmoothSwapping.clickSwapStack = null;
            }
            if (moreStacks.isEmpty()) {
                SwapUtil.assignI2CSwaps(client, lessStacks, new Vec2(mouseX - x, mouseY - y), handler);
            } else {
                SwapUtil.assignI2ISwaps(moreStacks, lessStacks, handler);
            }
        }

        if (!areStacksEqual(SmoothSwapping.oldStacks, currentStacks)) {
            updateStacks(currentStacks, oldStacks);
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



}
