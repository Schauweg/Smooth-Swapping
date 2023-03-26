package schauweg.smoothswapping.mixin;

import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
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
import schauweg.smoothswapping.swaps.ItemToCursorInventorySwap;

import static schauweg.smoothswapping.SmoothSwapping.ASSUME_CURSOR_STACK_SLOT_INDEX;
import static schauweg.smoothswapping.SwapUtil.*;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderGuiItemModel", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(MatrixStack matrices, ItemStack stack, int x, int y, BakedModel model, CallbackInfo cbi) {

        //Fix so hotbar doesn't get rendered
        Vector3f z = new Vector3f();
        matrices.peek().getPositionMatrix().getColumn(3, z);
        if (z.round().x <= 0 && !(client.currentScreen instanceof ConfigScreen))
            return;


        try {
            doSwap(stack, matrices, model, x, y, cbi);
        } catch (Exception e) {
            SwapUtil.reset();
        }
    }

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderOverlay(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo cbi) {

        Vector3f z = new Vector3f();
        matrices.peek().getPositionMatrix().getColumn(3, z);
        if (z.round().x <= 0 && !(client.currentScreen instanceof ConfigScreen))
            return;

        try {
            doOverlayRender((ItemRenderer) (Object) this, matrices, stack, renderer, x, y, cbi);
        } catch (Exception e) {
            SwapUtil.reset();
        }
    }


    private void doSwap(ItemStack stack, MatrixStack matrices, BakedModel model, int x, int y, CallbackInfo cbi) throws StackOverflowError {

        float lastFrameDuration = client.getLastFrameDuration();
        ItemRenderer renderer = (ItemRenderer) (Object) this;
        int index = getSlotIndex(stack);

        // TODO proper fix (https://github.com/Schauweg/Smooth-Swapping/issues/4)
        if (SmoothSwapping.swaps.containsKey(index)) {
            //Get all swaps for one slot


            List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);

            boolean renderDestinationSlot = true;

            //render all swaps for one slot
            for (int i = 0; i < swapList.size(); i++) {
                InventorySwap swap = swapList.get(i);

                swap.setRenderDestinationSlot(swap.isChecked());

                if (!swap.renderDestinationSlot()) {
                    renderDestinationSlot = false;
                }

                //render swap
                //LOGGER.info("render i2i swap, stack hash: " + stack.hashCode());

                renderSwap(renderer, swap, lastFrameDuration, stack.copy(), x, y, matrices.peek().getPositionMatrix(), model);

                if (hasArrived(swap)) {
                    setRenderToTrue(swapList);
                    swapList.remove(swap);
                }
            }

            //whether the destination slot should be rendered
            if (renderDestinationSlot) {
                renderer.renderGuiItemIcon(matrices, stack.copy(), x, y);
            }
            if (swapList.size() == 0)
                SmoothSwapping.swaps.remove(index);

            cbi.cancel();
        } else if (SmoothSwapping.swaps.containsKey(ASSUME_CURSOR_STACK_SLOT_INDEX)) {
            List<InventorySwap> swapList = SmoothSwapping.swaps
                    .get(ASSUME_CURSOR_STACK_SLOT_INDEX)
                    .stream().filter(swap -> !((ItemToCursorInventorySwap) swap).isArrived())
                    .toList();

            if (!swapList.isEmpty()) {
                if (swapListIndexOf(swapList, (swap) -> ((ItemToCursorInventorySwap) swap).getCopiedStackHash() == stack.hashCode()) == -1) {
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    ScreenHandler handler = null;
                    if (player != null) handler = player.currentScreenHandler;
                    //LOGGER.info("cursor stack hash: " + handler.getCursorStack().hashCode());

                    for (InventorySwap inventorySwap : swapList) { // assign initial renders
                        ItemToCursorInventorySwap swap = (ItemToCursorInventorySwap) inventorySwap;

                        if (!swap.isStartedRender()) {
                            //LOGGER.info("i2c start render " + swap.getSwapItem() +  ", hash=" + swap.getSwapItem().hashCode());
                            swap.setStartedRender(true);
                        } else if (!swap.isArrived()) {
                            if (handler != null) {
                                DefaultedList<ItemStack> inventoryStacks = handler.getStacks();
                                //LOGGER.info("render stack: [" + stack +  ", " + stack.hashCode() + "], inventory stacks:" + sb);
                                //LOGGER.info("target stack hash: " + swap.getTargetStackHash() + ", cursor stack hash: " + handler.getCursorStack().hashCode());
                                if (swap.getTargetStackHash() == -1 || handler.getCursorStack().hashCode() == swap.getTargetStackHash()) {
                                    if (!inventoryStacks.contains(stack)) { // now rendering cursor stack from parent
                                        ItemStack copiedStack = swap.getSwapItem().copy();
                                        swap.setCopiedStackHash(copiedStack.hashCode());
                                        if (swap.getTargetStackHash() == -1)
                                            swap.setTargetStackHash(stack.hashCode());
                                        //LOGGER.info("i2c insert render on " + stack + " to render " + swap.getSwapItem() + ", hash=" + swap.getSwapItem().hashCode());
                                        renderSwap(renderer, swap, lastFrameDuration, copiedStack, x, y, matrices.peek().getPositionMatrix(), model);

                                        if (hasArrived(swap)) swap.setArrived(true);
                                    }
                                } else {
                                    swap.setArrived(true);
                                    //LOGGER.info("cursor stack has changed, enforcing to arrive");
                                }

                            }
                        }
                    }
                } /*else LOGGER.info("i2c real render: " + stack + ", hash=" + stack.hashCode());*/
            }

            if (swapList.stream().allMatch(swap -> ((ItemToCursorInventorySwap) swap).isArrived()))
                SmoothSwapping.swaps.remove(ASSUME_CURSOR_STACK_SLOT_INDEX);
        }
    }


    private static int swapListIndexOf(List<InventorySwap> list, Function<InventorySwap, Boolean> prediction) {
        for (int i = 0; i < list.size(); i++) {
            if (prediction.apply(list.get(i))) return i;
        }
        return -1;
    }

    private void doOverlayRender(ItemRenderer itemRenderer, MatrixStack matrices, ItemStack stack, TextRenderer renderer, int x, int y, CallbackInfo cbi) throws StackOverflowError {
        int index = getSlotIndex(stack);

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

                if (swap.getAmount() > 1 || stack.isItemBarVisible()) {
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

                    textMatrices.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
                    textMatrices.translate(renderX, -renderY, 350);

                    if (stack.isItemBarVisible())
                        itemRenderer.renderGuiItemOverlay(textMatrices, renderer, stack.copy(), x, y, null);
                    else
                        itemRenderer.renderGuiItemOverlay(textMatrices, renderer, stack.copy(), x, y, amount);

                    immediate.draw();
                    textMatrices.pop();
                }

            }

            if (renderToSlot && stackCount > 1) {
                itemRenderer.renderGuiItemOverlay(matrices, renderer, stack.copy(), x, y, String.valueOf(stackCount));
            }
            cbi.cancel();
        }
    }

    private static void renderSwap(ItemRenderer itemRenderer, InventorySwap swap, float lastFrameDuration, ItemStack stack, int xPos, int yPos, Matrix4f positionMatrix, BakedModel model) {
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

        matrices.translate((float) xPos, (float) yPos, 100.0F);
        matrices.translate(8.0F, 8.0F, 0.0F);
        matrices.multiplyPositionMatrix(positionMatrix);
        matrices.multiplyPositionMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
        matrices.scale(16.0F, 16.0F, 16.0F);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        boolean bl = !model.isSideLit();
        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        }

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        matrixStack.translate(renderX / 16, renderY / 16, 5);
        RenderSystem.applyModelViewMatrix();
        itemRenderer.renderItem(stack, ModelTransformationMode.GUI, false, new MatrixStack(), immediate, 15728880, OverlayTexture.DEFAULT_UV, model);
        immediate.draw();
        RenderSystem.enableDepthTest();
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }

        matrices.pop();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();

        double speed = swap.getDistance() / 10 * config.getAnimationSpeedFormatted();

        swap.setX(x + lastFrameDuration * speed * Math.cos(angle));
        swap.setY(y + lastFrameDuration * speed * Math.sin(angle));

        matrices.pop();
    }
}
