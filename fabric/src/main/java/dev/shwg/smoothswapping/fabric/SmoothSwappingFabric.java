package dev.shwg.smoothswapping.fabric;

import dev.shwg.smoothswapping.SmoothSwapping;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class SmoothSwappingFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SmoothSwapping.init();
    }
}