package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.InventorySwap;
import schauweg.smoothswapping.SmoothSwapping;

import java.util.List;

import static java.lang.Math.PI;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {


    @Shadow
    public float zOffset;

    @Shadow
    public abstract void renderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model);

    @Shadow
    public abstract void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel);

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo cbi) {
        if (zOffset < 100) return; //fix so hotbar won't be affected

        if (renderMode == ModelTransformation.Mode.GUI) {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null)
                return;

            int index = getSlotIndex(stack);

            float delta = client.getTickDelta();
            float lastFrameDuration = client.getLastFrameDuration();

            //if there is a swap happening for a certain slot
            if (SmoothSwapping.swaps.containsKey(index)) {
                //Get all swaps for one slot
                List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);

                boolean renderToSlot = true;

                //render all swaps for one slot
                for (int i = 0; i < swapList.size(); i++) {
                    InventorySwap swap = swapList.get(i);

                    if (!swap.isChecked() && ItemStack.areItemsEqual(SmoothSwapping.oldStacks.get(index), stack)) {
                        swap.setChecked(true);
                        swap.setRenderToSlot(true);
                    } else if (!swap.isChecked()) {
                        swap.setChecked(true);
                    }

                    if (!swap.isRenderToSlot()) {
                        renderToSlot = false;
                    }

                    //render swap
                    renderSwap(swap, delta, lastFrameDuration, stack.copy(), leftHanded, vertexConsumers, light, overlay, model);


                    if (hasArrived(swap)) {
                        setRenderToTrue(swapList);
                        swapList.remove(swap);
                    }

                }

                if (renderToSlot) {
                    this.renderItem(stack.copy(), renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
                }

                if (swapList.size() == 0) {
                    SmoothSwapping.swaps.remove(index);
                }
                cbi.cancel();
            }
        }
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderOverlay(TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel, CallbackInfo cbi) {
        if (zOffset < 100) return; //fix so hotbar won't be affected

        int index = getSlotIndex(stack);
        if (SmoothSwapping.swaps.containsKey(index)) {
            List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);
            int stackCount = stack.getCount();
            boolean renderToSlot = true;

            for (InventorySwap swap : swapList) {
                stackCount -= swap.getAmount();
                if (!swap.isRenderToSlot()) {
                    renderToSlot = false;
                }
            }

            if (renderToSlot && stackCount > 1) {
                this.renderGuiItemOverlay(renderer, stack.copy(), x, y, String.valueOf(stackCount));
            }

            cbi.cancel();
        }

    }

    private void renderSwap(InventorySwap swap, float delta, float lastFrameDuration, ItemStack stack, boolean leftHanded, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {

        MatrixStack matrices = new MatrixStack();
        matrices.push();

        double x = swap.getX();
        double y = swap.getY();
        float angle = swap.getAngle();

        matrices.translate(-x / 16, y / 16, 0);
        this.renderItem(stack, ModelTransformation.Mode.GUI, leftHanded, matrices, vertexConsumers, light, overlay, model);

        float lastUpdate = swap.getLastUpdate();
        if (lastUpdate != delta) {

            double speed = swap.getDistance() / 10;

            swap.setX(x + lastFrameDuration * speed * Math.cos(angle));
            swap.setY(y + lastFrameDuration * speed * Math.sin(angle));
            swap.setLastUpdate(delta);
        }
        matrices.pop();
    }

    private boolean hasArrived(InventorySwap swap) {
        int quadrant = getQuadrant(swap.getAngle());
        double x = swap.getX();
        double y = swap.getY();
        if (quadrant == 0 && x > 0 && y > 0) {
            return true;
        } else if (quadrant == 1 && x < 0 && y > 0) {
            return true;
        } else if (quadrant == 2 && x < 0 && y < 0) {
            return true;
        } else return quadrant == 3 && x > 0 && y < 0;
    }

    private int getSlotIndex(ItemStack stack) {
        ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
        DefaultedList<ItemStack> stacks = handler.getStacks();
        return stacks.indexOf(stack);
    }

    private void setRenderToTrue(List<InventorySwap> swapList) {
        for (InventorySwap swap : swapList) {
            swap.setRenderToSlot(true);
        }
    }

    private int getQuadrant(float angle) {
        return (int) (Math.floor(2 * angle / PI) % 4 + 4) % 4;
    }
}
