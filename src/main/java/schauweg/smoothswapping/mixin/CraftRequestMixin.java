package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftRequestC2SPacket.class)
public class CraftRequestMixin {

    @Inject(method = "<init>(ILnet/minecraft/recipe/Recipe;Z)V", at = @At("TAIL"))
    public void inInit(int syncId, Recipe<?> recipe, boolean craftAll, CallbackInfo cbi){

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;


//        System.out.println("Craft Request " + " " + craftAll);
        
    }

}
