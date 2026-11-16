package dev.shwg.smoothswapping;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.api.ClientTickDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.shwg.shwgconfig.api.ConfigManager;
import dev.shwg.smoothswapping.config.SmoothSwappingConfig;

/*? if >=1.20.5 { */
import net.minecraft.core.component.DataComponents;
/*?}*/

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
/^? if 1.20.6 || >= 1.21.6  { ^/
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
/^? } ^/
*//*?}*/

/*? if neoforge {*/
/*import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
*//*?}*/

/*? if forgeLike { */
/*import dev.shwg.shwgconfig.ShwgConfig;

@Mod(SmoothSwapping.MOD_ID)
*//*?}*/
/*~ if fabric '{' -> 'implements ModInitializer {' */
public class SmoothSwapping implements ModInitializer {

    public static final String MOD_ID = "smooth_swapping";
    public static final Logger LOGGER = LoggerFactory.getLogger("SmoothSwapping");
    public static final SmoothSwappingConfig CONFIG = new SmoothSwappingConfig();
    public static ConfigManager configManager;

    /*? if forge {*/
    /*/^~ if 1.20.6 || >= 1.21.6 '()' -> '(FMLJavaModLoadingContext context)' ^/
    public SmoothSwapping(FMLJavaModLoadingContext context) {
        init();
        /^~ if 1.20.6 || >= 1.21.6 'null' -> 'context' ^/
        ShwgConfig.forgeInit(configManager, context);
    }
    *//*?}*/

    /*? if neoforge {*/
    /*public SmoothSwapping(IEventBus modEventBus, ModContainer modContainer) {
        init();
        ShwgConfig.neoForgeInit(configManager, modEventBus, modContainer);
    }
    *//*?}*/

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        init();
    }
    /*?}*/

    private void init() {
        configManager = ConfigManager.of(CONFIG, "smooth_swapping.json");
        configManager.load();

        ClientTickDispatcher.registerGUI(keyEvent -> {
            if (keyEvent.matches(InputConstants.KEY_LALT)) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    /*? if >=1.20.5 { */
                    /*~ if >=1.21.5 '.items.forEach' -> '.forEach'*/
                    player.getInventory().forEach(stack -> System.out.println(
                            stack + " bundle=" + stack.get(DataComponents.BUNDLE_CONTENTS)
                                    + " container=" + stack.get(DataComponents.CONTAINER))
                    );
                    /*?} else {*/
                    /*player.getInventory().items.forEach(stack -> {
                                if (stack.getItem() != Items.AIR) {
                                    System.out.println(stack + ", tags: " + stack.getTag());
                                }
                            }
                    );
                    *//*?}*/
                }
            }
        });
    }
}
