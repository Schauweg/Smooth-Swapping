package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.InventorySwap;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.SwapUtil;
import schauweg.smoothswapping.config.Config;
import schauweg.smoothswapping.config.ConfigManager;

import java.util.List;

import static schauweg.smoothswapping.SwapUtil.*;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {


    @Shadow
    public float zOffset;

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo cbi) {
        if (zOffset < 100) return; //fix so hotbar won't be affected

        if (renderMode == ModelTransformation.Mode.GUI) {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null)
                return;

            doSwap(client, (ItemRenderer) (Object) this, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, cbi);
        }
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderOverlay(TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel, CallbackInfo cbi) {
        if (zOffset < 100) return; //fix so hotbar won't be affected

        doOverlayRender((ItemRenderer) (Object) this, stack, renderer, x, y, cbi);
    }

    private void doSwap(MinecraftClient client, ItemRenderer itemRenderer, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo cbi) {

        float lastFrameDuration = client.getLastFrameDuration();

        int index = getSlotIndex(stack);

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
                renderSwap(itemRenderer, swap, lastFrameDuration, stack.copy(), leftHanded, vertexConsumers, light, overlay, model);


                if (hasArrived(swap)) {
                    setRenderToTrue(swapList);
                    swapList.remove(swap);
                }

            }

            if (renderToSlot)
                itemRenderer.renderItem(stack.copy(), renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);

            if (swapList.size() == 0)
                SmoothSwapping.swaps.remove(index);

            cbi.cancel();
        }
    }

    private void doOverlayRender(ItemRenderer itemRenderer, ItemStack stack, TextRenderer renderer, int x, int y, CallbackInfo cbi) {
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
                itemRenderer.renderGuiItemOverlay(renderer, stack.copy(), x, y, String.valueOf(stackCount));
            }

            cbi.cancel();
        }
    }

    private static void renderSwap(ItemRenderer itemRenderer, InventorySwap swap, float lastFrameDuration, ItemStack stack, boolean leftHanded, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
        Config config = ConfigManager.getConfig();
        MatrixStack matrices = new MatrixStack();
        matrices.push();

        double x = swap.getX();
        double y = swap.getY();
        float angle = swap.getAngle();

        matrices.translate(-x / 16, y / 16, 0);

        itemRenderer.renderItem(stack, ModelTransformation.Mode.GUI, leftHanded, matrices, vertexConsumers, light, overlay, model);

        float ease = SwapUtil.getEase(config, x, y, swap.getDistance());

        double speed = swap.getDistance() / 10 * ease * config.getAnimationSpeedFormatted();

        swap.setX(x + lastFrameDuration * speed * Math.cos(angle));
        swap.setY(y + lastFrameDuration * speed * Math.sin(angle));

        matrices.pop();
    }

}
