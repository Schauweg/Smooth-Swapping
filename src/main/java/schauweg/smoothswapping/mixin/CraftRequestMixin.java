package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.*;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(CraftRequestC2SPacket.class)
public abstract class CraftRequestMixin {

    @Inject(method = "<init>(ILnet/minecraft/recipe/Recipe;Z)V", at = @At("TAIL"))
    public void inInit(int syncId, Recipe<?> recipe, boolean craftAll, CallbackInfo cbi) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ScreenHandler handler = client.player.currentScreenHandler;




        if (handler instanceof AbstractRecipeScreenHandler<?> recipeHandler) {

            int gridWidth = recipeHandler.getCraftingWidth();
            int gridHeight = recipeHandler.getCraftingHeight();
            int gridOutputSlot = recipeHandler.getCraftingResultSlotIndex();
//            int craftSlotCount = recipeHandler.getCraftingSlotCount();

            getSlotIds(gridWidth, gridHeight, gridOutputSlot, recipe);



//            if (recipe instanceof ShapedRecipe) {
//                ShapedRecipe sr = (ShapedRecipe) recipe;
//                int recipeWidth = sr.getWidth();
//                int recipeHeight = sr.getHeight();
//
//                for (int i = 0; i < gridWidth * gridHeight; i++) {
//                    if ((i % 3) >= recipeWidth) {
//                        continue;
//                    }
//                    if (recipeWidth == 1) {
//                        //i + 1;
//                    } else if (recipeHeight == 1) {
//
//                    }
//
//
//                    System.out.println(i);
//                }
//            }
        }

    }

    private List<Integer> getSlotIds(int gridWidth, int gridHeight, int gridOutputSlot, Recipe<?> recipe) {
        int width = gridWidth;
        int height = gridHeight;
        Iterator<Ingredient> inputs = recipe.getIngredients().iterator();
        List<Integer> integers = new ArrayList<>();

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            width = shapedRecipe.getWidth();
            height = shapedRecipe.getHeight();
        }

        int shapedRecipe = 0;

        for (int i = 0; i < gridHeight; ++i) {
            if (shapedRecipe == gridOutputSlot) {
                ++shapedRecipe;
            }

            if (integers.size() >= recipe.getIngredients().size()){
                break;
            }

            boolean outOfBounds = (float) height < (float) gridHeight / 2.0F;
            int flooredHeight = MathHelper.floor((float) gridHeight / 2.0F - (float) height / 2.0F);
            if (outOfBounds && flooredHeight > i) {
                shapedRecipe += gridWidth;
                ++i;
            }

            for (int j = 0; j < gridWidth; ++j) {
                if (!inputs.hasNext()) {
                    break;
                }

                outOfBounds = (float) width < (float) gridWidth / 2.0F;
                flooredHeight = MathHelper.floor((float) gridWidth / 2.0F - (float) width / 2.0F);
                int width1 = width;
                boolean bl2 = j < width;
                if (outOfBounds) {
                    width1 = flooredHeight + width;
                    bl2 = flooredHeight <= j && j < flooredHeight + width;
                }

                if (bl2) {
                    if (integers.size() >= recipe.getIngredients().size()){
                        break;
                    } else {
                        integers.add(shapedRecipe);
                    }
                } else if (width1 == j) {
                    shapedRecipe += gridWidth - j;
                    break;
                }

                ++shapedRecipe;
            }
        }
        return integers;
    }

}
