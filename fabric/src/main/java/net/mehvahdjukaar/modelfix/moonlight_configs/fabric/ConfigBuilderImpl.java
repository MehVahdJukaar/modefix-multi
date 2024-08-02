package net.mehvahdjukaar.modelfix.moonlight_configs.fabric;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.modelfix.moonlight_configs.ConfigBuilder;
import net.mehvahdjukaar.modelfix.moonlight_configs.ConfigType;
import net.mehvahdjukaar.modelfix.moonlight_configs.fabric.ConfigSubCategory;
import net.mehvahdjukaar.modelfix.moonlight_configs.fabric.FabricConfigSpec;
import net.mehvahdjukaar.modelfix.moonlight_configs.fabric.values.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.http.annotation.Experimental;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.mehvahdjukaar.modelfix.moonlight_configs.ConfigSpec.getReadableName;

/**
 * Author: MehVahdJukaar
 */
public class ConfigBuilderImpl extends ConfigBuilder {

    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ConfigSubCategory mainCategory = new ConfigSubCategory(this.getName().getNamespace());

    private final Deque<ConfigSubCategory> categoryStack = new ArrayDeque<>();

    public ConfigBuilderImpl(ResourceLocation name, ConfigType type) {
        super(name, type);
        categoryStack.push(mainCategory);
    }

    //doesn't load it immediately. happens after registration to mimic forge
    @NotNull
    public FabricConfigSpec build() {
        assert categoryStack.size() == 1;
        return new FabricConfigSpec(this.getName(), mainCategory, this.type, this.changeCallback);
    }

    @Override
    public String currentCategory() {
        return categoryStack.peek().getName();
    }

    @Override
    public ConfigBuilderImpl push(String translation) {
        var cat = new ConfigSubCategory(translation);
        categoryStack.peek().addEntry(cat);
        categoryStack.push(cat);
        return this;
    }


    @Override
    public ConfigBuilderImpl pop() {
        assert categoryStack.size() != 1;
        categoryStack.pop();
        return this;
    }

    private void doAddConfig(String name, ConfigValue<?> config) {
        config.setTranslationKey(this.translationKey(name));
        maybeAddTranslationString(name);
        var tooltipKey = this.tooltipKey(name);
        if (this.comments.containsKey(tooltipKey)) {
            config.setDescriptionKey(tooltipKey);
        }

        this.categoryStack.peek().addEntry(config);
        if (this.categoryStack.size() <= 1) throw new AssertionError();
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        var config = new BoolConfigValue(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }


    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        var config = new DoubleConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Experimental
    @Override
    public Supplier<Float> define(String name, float defaultValue, float min, float max) {
        var config = new FloatConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        var config = new IntConfigValue(name, defaultValue, min, max);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        var config = new StringConfigValue(name, defaultValue, validator);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        var config = new ListStringConfigValue<>(name, (List<String>) defaultValue, predicate);
        doAddConfig(name, config);
        return config;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        var config = new EnumConfigValue<>(name, defaultValue);
        doAddConfig(name, config);
        return config;
    }



    @Override
    protected void maybeAddTranslationString(String name) {
        comments.put(this.translationKey(name), getReadableName(name));
        super.maybeAddTranslationString(name);
    }

    //NYI
    @Override
    public ConfigBuilder gameRestart() {
        return this;
    }

    @Override
    public ConfigBuilder worldReload() {
        return this;
    }

    @Override
    public ConfigBuilder comment(String comment) {
        return super.comment(comment);
    }
}
