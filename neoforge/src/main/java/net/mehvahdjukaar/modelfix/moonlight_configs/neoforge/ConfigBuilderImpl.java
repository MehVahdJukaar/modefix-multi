package net.mehvahdjukaar.modelfix.moonlight_configs.neoforge;

import net.mehvahdjukaar.modelfix.moonlight_configs.ConfigBuilder;
import net.mehvahdjukaar.modelfix.moonlight_configs.ConfigType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.http.annotation.Experimental;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    private final List<ModConfigSpec.ConfigValue<?>> requireGameRestart = new ArrayList<>();
    private boolean currentGameRestart;
    private ModConfigSpec.ConfigValue<?> currentValue;
    private final List<ValueWrapper<?, ?>> specialValues = new ArrayList<>();

    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ModConfigSpec.Builder builder;

    private final Deque<String> cat = new ArrayDeque<>();

    public ConfigBuilderImpl(ResourceLocation name, ConfigType type) {
        super(name, type);
        this.builder = new ModConfigSpec.Builder();
    }

    @Override
    public String currentCategory() {
        return cat.peekFirst();
    }


    @Override
    public ConfigSpecWrapper build() {
        return new ConfigSpecWrapper(this.getName(), this.builder.build(), this.type,
                this.changeCallback, this.requireGameRestart, specialValues);
    }

    @Override
    public ConfigBuilderImpl push(String category) {
        builder.push(category);
        cat.push(category);
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        builder.pop();
        cat.pop();
        return this;
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        maybeAddTranslationString(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        maybeAddTranslationString(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }


    @Experimental
    @Override
    public Supplier<Float> define(String name, float defaultValue, float min, float max) {
        maybeAddTranslationString(name);

        var value = builder.defineInRange(name, defaultValue, min, max);

        this.currentValue = value;
        maybeAddGameRestart();

        var wrapper = new ValueWrapper<Float, Double>(value) {
            @Override
            Float map(Double value) {
                return value.floatValue();
            }
        };
        specialValues.add(wrapper);

        return wrapper;
    }


    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue, validator);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    public <T> Supplier<T> define(String name, Supplier<T> defaultValue, Predicate<Object> validator) {
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue, validator);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        maybeAddTranslationString(name);
        var value = builder.defineList(name, defaultValue, predicate);
        this.currentValue = value;
        maybeAddGameRestart();
        return () -> (List<String>) value.get();
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        maybeAddTranslationString(name);
        var value = builder.defineEnum(name, defaultValue);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    private void maybeAddGameRestart() {
        if (currentGameRestart && currentValue != null) {
            requireGameRestart.add(currentValue);
            currentGameRestart = false;
            currentValue = null;
        }
    }

    @Override
    public ConfigBuilder gameRestart() {
        this.currentGameRestart = true;
        maybeAddGameRestart();
        return this;
    }

    @Override
    public ConfigBuilder worldReload() {
        builder.worldRestart();
        return this;
    }

    @Override
    public ConfigBuilder comment(String comment) {
        builder.comment(comment); //.translationKey(getTranslationName());
        //TODO: choose. either add a translation or a comment literal not both
        return super.comment(comment);
    }

    // wrapper class for special configs. ugly and hacky just to allow cachind as defualt config entries arent extendable
    public abstract static class ValueWrapper<T, C> implements Supplier<T> {
        private final ModConfigSpec.ConfigValue<C> original;
        private T cachedValue = null;

        ValueWrapper(ModConfigSpec.ConfigValue<C> original) {
            this.original = original;
        }

        abstract T map(C value);

        public void clearCache() {
            cachedValue = null;
        }

        public T get() {
            if (cachedValue == null) {
                cachedValue = map(original.get());
            }
            return cachedValue;
        }
    }
}
