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
import org.slf4j.Logger;
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
			
			doSwap(client, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, zOffset, cbi);
		}
	}
	
	@Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "HEAD"), cancellable = true)
	private void onRenderOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (zOffset < 100) return; //fix so hotbar won't be affected
		
		doOverlayRender((ItemRenderer) (Object) this, stack, renderer, x, y, ci);
	}
	
	private void doSwap(MinecraftClient client, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, float zOffset, CallbackInfo ci) {
		
		float lastFrameDuration = client.getLastFrameDuration();
		ItemRenderer renderer = client.getItemRenderer();
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
					
					if (!swap.isChecked() && ItemStack.areItemsEqual(SmoothSwapping.oldStacks.get(index), stack)) {
						swap.setChecked(true);
						swap.setRenderDestinationSlot(true);
					} else if (!swap.isChecked()) {
						swap.setChecked(true);
					}
					
					
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
			e.printStackTrace();
			SmoothSwapping.swaps.remove(index);
		}
	}
	
	private void doOverlayRender(ItemRenderer itemRenderer, ItemStack stack, TextRenderer renderer, int x, int y, CallbackInfo cbi) {
		int index = getSlotIndex(stack);
        
        // TODO proper fix (https://github.com/Schauweg/Smooth-Swapping/issues/4)
        try {
            if (SmoothSwapping.swaps.containsKey(index)) {
                List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);
                int stackCount = stack.getCount();
                boolean renderToSlot = true;
        
                for (InventorySwap swap : swapList) {
                    stackCount -= swap.getAmount();
                    if (!swap.renderDestinationSlot()) {
                        renderToSlot = false;
                    }
            
                    if (swap.getAmount() > 1) {
                        String amount = String.valueOf(swap.getAmount());
                        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                
                        MatrixStack textStack = new MatrixStack();
                        textStack.push();
                        textStack.translate(-swap.getX(), -swap.getY(), zOffset + 250);
                        renderer.draw(amount, (float) (x + 19 - 2 - renderer.getWidth(amount)), (float) (y + 6 + 3), 16777215, true, textStack.peek().getPositionMatrix(), immediate, false, 0, 15728880);
                        immediate.draw();
                        textStack.pop();
                    }
            
                }
        
                if (renderToSlot && stackCount > 1) {
                    itemRenderer.renderGuiItemOverlay(renderer, stack.copy(), x, y, String.valueOf(stackCount));
                }
                cbi.cancel();
            }
        } catch (StackOverflowError e) {
			e.printStackTrace();
            SmoothSwapping.swaps.remove(index);
        }
	}
	
	private static void renderSwap(ItemRenderer itemRenderer, InventorySwap swap, float lastFrameDuration, ItemStack stack, boolean leftHanded, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, float zOffset) {
		Config config = ConfigManager.getConfig();
		MatrixStack matrices = new MatrixStack();
		matrices.push();
		
		double x = swap.getX();
		double y = swap.getY();
		float angle = swap.getAngle();
		
		float progress = SwapUtil.map((float) Math.hypot(x, y), 0, (float) swap.getDistance(), 0.95f, 0.05f);
		
		matrices.translate(-x / 16, y / 16, zOffset - 145); //zOffset 5
		
		itemRenderer.renderItem(stack, ModelTransformation.Mode.GUI, leftHanded, matrices, vertexConsumers, light, overlay, model);
		
		float ease = SwapUtil.getEase(config, progress);
		
		double speed = swap.getDistance() / 10 * ease * config.getAnimationSpeedFormatted();
		
		swap.setX(x + lastFrameDuration * speed * Math.cos(angle));
		swap.setY(y + lastFrameDuration * speed * Math.sin(angle));
		
		matrices.pop();
	}
	
}
