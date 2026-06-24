package dev.shwg.smoothswapping.config;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.Vec2;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
    private boolean toggleMod = true;
    private int animationSpeed = 100;
    private float[][] curvePoints = new float[][]{};
    private boolean moddedScreenCompatibilityEnabled = true;
    private List<ModdedScreenCompatibilityEntry> moddedScreenCompatibilityEntries = loadDefaultModdedScreenCompatibilityEntries();

    public int getAnimationSpeed() {
        return animationSpeed;
    }
    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    public float getAnimationSpeedFormatted() {
        return animationSpeed / 100F;
    }

    public List<CatmullRomWidget.CatmullRomSpline> getSplines() {
        return CatmullRomWidget.splinesFromPoints(getCurvePoints());
    }

    public void setCurvePoints(List<Vec2> points) {
        float[][] curvePoints = new float[points.size() - 4][2];

        for (int i = 2; i < points.size() - 2; i++) {
            Vec2 p = points.get(i);
            curvePoints[i - 2][0] = (float) p.v[0];
            curvePoints[i - 2][1] = (float) p.v[1];
        }
        this.curvePoints = curvePoints;
    }

    public List<Vec2> getCurvePoints() {

        List<Vec2> points = new ArrayList<>();

        points.add(new Vec2(0, 0));
        points.add(new Vec2(0, 0));

        for (float[] curvePoint : curvePoints) {
            points.add(new Vec2(curvePoint[0], curvePoint[1]));
        }

        points.add(new Vec2(1, 1));
        points.add(new Vec2(1, 1));

        return points;
    }

    public boolean getToggleMod(){
        return toggleMod;
    }

    public void setToggleMod(Boolean value) {
        toggleMod = value;
    }

    public boolean isModdedScreenCompatibilityEnabled() {
        return moddedScreenCompatibilityEnabled;
    }

    public List<ModdedScreenCompatibilityEntry> getModdedScreenCompatibilityEntries() {
        if (moddedScreenCompatibilityEntries == null) {
            return List.of();
        }
        return moddedScreenCompatibilityEntries;
    }

    private static List<ModdedScreenCompatibilityEntry> loadDefaultModdedScreenCompatibilityEntries() {
        try (InputStreamReader reader = new InputStreamReader(
                Config.class.getResourceAsStream("/assets/smoothswapping/default_modded_screen_compatibility_entries.json"),
                StandardCharsets.UTF_8
        )) {
            ModdedScreenCompatibilityEntry[] entries = SmoothSwapping.GSON.fromJson(reader, ModdedScreenCompatibilityEntry[].class);
            return entries == null ? List.of() : new ArrayList<>(Arrays.asList(entries));
        } catch (Exception e) {
            return List.of();
        }
    }

    public static class ModdedScreenCompatibilityEntry {
        private String id;
        private boolean enabled = true;
        private List<String> screenClasses = List.of();
        private List<String> menuClasses = List.of();
        private String slotPolicy = "vanilla_like";
        private List<String> itemSlotClasses = List.of();
        private boolean includePlayerInventorySlots;
        private List<String> playerInventorySlotClasses = List.of();
        private long clickAnimationWindowMs = -1L;
        private int maxChangedSlots = -1;

        public ModdedScreenCompatibilityEntry() {
        }

        public ModdedScreenCompatibilityEntry(
                String id,
                boolean enabled,
                List<String> screenClasses,
                List<String> menuClasses,
                String slotPolicy,
                List<String> itemSlotClasses,
                boolean includePlayerInventorySlots,
                List<String> playerInventorySlotClasses,
                long clickAnimationWindowMs,
                int maxChangedSlots
        ) {
            this.id = id;
            this.enabled = enabled;
            this.screenClasses = screenClasses;
            this.menuClasses = menuClasses;
            this.slotPolicy = slotPolicy;
            this.itemSlotClasses = itemSlotClasses;
            this.includePlayerInventorySlots = includePlayerInventorySlots;
            this.playerInventorySlotClasses = playerInventorySlotClasses;
            this.clickAnimationWindowMs = clickAnimationWindowMs;
            this.maxChangedSlots = maxChangedSlots;
        }

        public String getId() {
            return id == null || id.isBlank() ? "configured" : id;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public List<String> getScreenClasses() {
            return screenClasses == null ? List.of() : screenClasses;
        }

        public List<String> getMenuClasses() {
            return menuClasses == null ? List.of() : menuClasses;
        }

        public String getSlotPolicy() {
            return slotPolicy == null || slotPolicy.isBlank() ? "vanilla_like" : slotPolicy;
        }

        public List<String> getItemSlotClasses() {
            return itemSlotClasses == null ? List.of() : itemSlotClasses;
        }

        public boolean shouldIncludePlayerInventorySlots() {
            return includePlayerInventorySlots;
        }

        public List<String> getPlayerInventorySlotClasses() {
            return playerInventorySlotClasses == null ? List.of() : playerInventorySlotClasses;
        }

        public long getClickAnimationWindowMs() {
            return clickAnimationWindowMs;
        }

        public int getMaxChangedSlots() {
            return maxChangedSlots;
        }
    }
}
