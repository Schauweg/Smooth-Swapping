package schauweg.smoothswapping.mixin;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.config.CatmullRomWidget;
import schauweg.smoothswapping.config.ConfigScreen;
import schauweg.smoothswapping.swaps.InventorySwap;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.SwapUtil;
import schauweg.smoothswapping.config.Config;
import schauweg.smoothswapping.config.ConfigManager;

import static schauweg.smoothswapping.SwapUtil.*;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {


    @Shadow
    public float zOffset;

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo cbi) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (zOffset < 100 && !(client.currentScreen instanceof ConfigScreen))
            return; //fix so hotbar won't be affected


        if (renderMode == ModelTransformation.Mode.GUI) {
            if (client.player == null && !(client.currentScreen instanceof ConfigScreen))
                return;

            doSwap(client, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, zOffset, cbi);
        }
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo cbi) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (zOffset < 100 && !(client.currentScreen instanceof ConfigScreen))
            return; //fix so hotbar won't be affected

        doOverlayRender((ItemRenderer) (Object) this, stack, renderer, x, y, cbi);
    }

    private void doSwap(MinecraftClient client, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, float zOffset, CallbackInfo ci) {

        float lastFrameDuration = client.getLastFrameDuration();
        ItemRenderer renderer = (ItemRenderer) (Object) this;
        int index = getSlotIndex(stack);

        // TODO proper fix (https://github.com/Schauweg/Smooth-Swapping/issues/4)
        try {
            if (SmoothSwapping.swaps.containsKey(index)) {
                //Get all swaps for one slot
                List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);

                boolean renderDestinationSlot = true;

                //render all swaps for one slot
                for (int i = 0; i < swapList.size(); i++) {
                    InventorySwap swap = swapList.get(i);

                    swap.setRenderDestinationSlot(index != -1 && swap.isChecked());

                    if (!swap.renderDestinationSlot()) {
                        renderDestinationSlot = false;
                    }

                    //render swap
                    renderSwap(renderer, swap, lastFrameDuration, stack.copy(), leftHanded, vertexConsumers, light, overlay, model, zOffset);

                    if (hasArrived(swap)) {
                        setRenderToTrue(swapList);
                        swapList.remove(swap);
                    }
                }

                //whether the destination slot should be rendered
                if (renderDestinationSlot) {
                    renderer.renderItem(stack.copy(), renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
                }
                if (swapList.size() == 0)
                    SmoothSwapping.swaps.remove(index);

                ci.cancel();
            }
        } catch (StackOverflowError e) {
            SmoothSwapping.LOGGER.warn("StackOverflowError just happened while trying to render an item swap. This message is a reminder to properly fix an issue #4 described on SmoothSwapping's GitHub");
            SmoothSwapping.swaps.remove(index);
        }
    }

    private void doOverlayRender(ItemRenderer itemRenderer, ItemStack stack, TextRenderer renderer, int x, int y, CallbackInfo cbi) {
        int index = getSlotIndex(stack);

        // TODO proper fix (https://github.com/Schauweg/Smooth-Swapping/issues/4)
        try {
            if (SmoothSwapping.swaps.containsKey(index)) {
                List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);
                Config config = ConfigManager.getConfig();
                int stackCount = stack.getCount();
                boolean renderToSlot = true;

                for (InventorySwap swap : swapList) {

                    if (!ItemStack.areItemsEqual(stack, swap.getSwapItem())) {
                        SmoothSwapping.swaps.remove(index);
                        return;
                    }

                    stackCount -= swap.getAmount();
                    if (!swap.renderDestinationSlot()) {
                        renderToSlot = false;
                    }

                    if (swap.getAmount() > 1) {
                        String amount = String.valueOf(swap.getAmount());
                        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

                        MatrixStack textMatrices = new MatrixStack();
                        textMatrices.push();

                        double swapX = swap.getX();
                        double swapY = swap.getY();
                        double angle = swap.getAngle();

                        double progress = 1D - SwapUtil.map(Math.hypot(swapX, swapY), 0, swap.getDistance(), 1D, 0D);

                        List<CatmullRomWidget.CatmullRomSpline> splines = config.getSplines();


                        double ease = CatmullRomWidget.getProgress(progress, splines);

                        double renderX = -swap.getStartX() - (Math.cos(angle) * swap.getDistance() * ease);
                        double renderY = swap.getStartY() + (Math.sin(angle) * swap.getDistance() * ease);

                        textMatrices.translate(renderX, -renderY, zOffset + 250);

                        renderer.draw(amount, (float) (x + 19 - 2 - renderer.getWidth(amount)), (float) (y + 6 + 3), 16777215, true, textMatrices.peek().getPositionMatrix(), immediate, false, 0, 15728880);
                        immediate.draw();
                        textMatrices.pop();
                    }

                }

                if (renderToSlot && stackCount > 1) {
                    itemRenderer.renderGuiItemOverlay(renderer, stack.copy(), x, y, String.valueOf(stackCount));
                }
                cbi.cancel();
            }
        } catch (StackOverflowError e) {
            SmoothSwapping.LOGGER.warn("StackOverflowError just happened while trying to render an overlay. This message is a reminder to properly fix an issue #4 described on SmoothSwapping's GitHub");
            SmoothSwapping.swaps.remove(index);
        }
    }

    private static void renderSwap(ItemRenderer itemRenderer, InventorySwap swap, float lastFrameDuration, ItemStack stack, boolean leftHanded, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, float zOffset) {
        Config config = ConfigManager.getConfig();
        MatrixStack matrices = new MatrixStack();
        matrices.push();

        double x = swap.getX();
        double y = swap.getY();
        double angle = swap.getAngle();

        double progress = 1D - SwapUtil.map(Math.hypot(x, y), 0, swap.getDistance(), 1D, 0D);

        List<CatmullRomWidget.CatmullRomSpline> splines = config.getSplines();

        double ease = CatmullRomWidget.getProgress(progress, splines);

        double renderX = -swap.getStartX() - Math.cos(angle) * swap.getDistance() * ease;
        double renderY = swap.getStartY() + Math.sin(angle) * swap.getDistance() * ease;

        matrices.translate(renderX / 16, renderY / 16, 5); //zOffset 5

        itemRenderer.renderItem(stack, ModelTransformation.Mode.GUI, leftHanded, matrices, vertexConsumers, light, overlay, model);

        double speed = swap.getDistance() / 10 * config.getAnimationSpeedFormatted();

        swap.setX(x + lastFrameDuration * speed * Math.cos(angle));
        swap.setY(y + lastFrameDuration * speed * Math.sin(angle));

        matrices.pop();
    }
}
