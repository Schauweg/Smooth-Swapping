package dev.shwg.smoothswapping.mixin;

import dev.shwg.shwgconfig.util.ModLoaderHelper;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmoothSwappingMixinPlugin implements IMixinConfigPlugin {

    // own logger to not load main class too early
    private static final Logger LOGGER = LoggerFactory.getLogger("SmoothSwapping");

    // new compats via ".mixin.compat.package", "modId"
    private static final Map<String, String> COMPAT_MIXINS = Map.of(
            ".mixin.compat.sophisticatedcore.", "sophisticatedcore"
    );

    private final Map<String, Boolean> modPresence = new HashMap<>();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (Map.Entry<String, String> compat : COMPAT_MIXINS.entrySet()) {
            if (mixinClassName.contains(compat.getKey())) {
                return modPresence.computeIfAbsent(compat.getValue(), SmoothSwappingMixinPlugin::isModPresent);
            }
        }
        return true;
    }

    private static boolean isModPresent(String modId) {
        try {
            boolean present = ModLoaderHelper.isModPresentEarly(modId);
            if (present) LOGGER.info("Enabling compatibility for '{}'", modId);
            return present;
        } catch (Throwable t) {
            LOGGER.warn("Could not check whether '{}' is installed, its compatibility mixins are disabled: {}", modId, t.toString());
            LOGGER.debug("Stack trace for the failed '{}' check", modId, t);
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
