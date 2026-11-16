/*? if fabric {*/
package dev.shwg.smoothswapping.config;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.shwg.smoothswapping.SmoothSwapping;


public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SmoothSwapping.configManager::getScreen;
    }
}
/*?}*/
