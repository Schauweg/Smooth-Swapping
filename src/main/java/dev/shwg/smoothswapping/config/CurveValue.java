package dev.shwg.smoothswapping.config;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import dev.shwg.shwgconfig.api.values.CustomJsonSerializable;
import dev.shwg.smoothswapping.config.widgets.CatmullRomSpline;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurveValue extends ConfigValue<List<Vec2>> implements CustomJsonSerializable {

    private List<CatmullRomSpline> splines;

    public CurveValue(List<Vec2> defaultValue) {
        super(List.copyOf(defaultValue));
        this.splines = CatmullRomSpline.buildFrom(this.value);
    }

    public static CurveValue of(List<Vec2> defaultPoints) {
        List<Vec2> full = new ArrayList<>();
        full.add(Vec2.ZERO);
        full.addAll(defaultPoints);
        full.add(Vec2.ONE);
        return new CurveValue(full);
    }

    public static CurveValue of(Vec2... points) {
        return of(Arrays.asList(points));
    }

    public static CurveValue empty() {
        return new CurveValue(new ArrayList<>(List.of(Vec2.ZERO, Vec2.ONE)));
    }

    @Override
    public void setValue(List<Vec2> newValue) {
        super.setValue(List.copyOf(newValue));
        this.splines = CatmullRomSpline.buildFrom(this.value);
    }

    @Override
    public List<Vec2> getValue() {
        return new ArrayList<>(this.value);
    }

    @Override
    public List<Vec2> getDefault() {
        return new ArrayList<>(this.defaultValue);
    }

    public List<CatmullRomSpline> getSplines() {
        return splines;
    }

    public double ease(double t) {
        return CatmullRomSpline.ease(t, splines);
    }

    @Override
    protected boolean isValid(List<Vec2> newValue) {
        if (newValue == null || newValue.isEmpty()) {
            return false;
        }
        if (!newValue.get(0).equals(Vec2.ZERO) || !newValue.get(newValue.size() - 1).equals(Vec2.ONE) || newValue.size() < 2) {
            return false;
        }
        return true;
    }

    @Override
    public JsonElement toJsonElement() {
        JsonArray array = new JsonArray();
        for (int i = 1; i < value.size() - 1; i++) {
            Vec2 point = value.get(i);
            JsonArray pair = new JsonArray();
            pair.add(point.x);
            pair.add(point.y);
            array.add(pair);
        }
        return array;
    }

    @Override
    public void loadFromJson(JsonElement element) {
        List<Vec2> loaded = new ArrayList<>();
        loaded.add(Vec2.ZERO);
        for (JsonElement entry : element.getAsJsonArray()) {
            JsonArray pair = entry.getAsJsonArray();
            loaded.add(new Vec2(
                    Mth.clamp(pair.get(0).getAsFloat(), 0, 1),
                    Mth.clamp(pair.get(1).getAsFloat(), 0, 1))
            );
        }
        loaded.add(Vec2.ONE);
        setValue(loaded);
    }
}
