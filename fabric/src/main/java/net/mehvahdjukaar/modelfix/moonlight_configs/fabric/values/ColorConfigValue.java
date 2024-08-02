package net.mehvahdjukaar.modelfix.moonlight_configs.fabric.values;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.modelfix.ModelFix;
import net.mehvahdjukaar.modelfix.moonlight_configs.ConfigBuilder;

public class ColorConfigValue extends IntConfigValue {

    public ColorConfigValue(String name, int defaultValue) {
        super(name, defaultValue, 0, 0xffffff);
    }

    @Override
    public void loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                String s = element.get(this.name).getAsString();
             //   if (ConfigBuilder.COLOR_CHECK.test(s)) return;
                //if not valid it defaults
                this.value = defaultValue;
            } catch (Exception ignored) {
            }
            ModelFix.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
        } else {
            ModelFix.LOGGER.warn("Config file had missing entry {}", this.name);
        }
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = defaultValue;
        object.addProperty(this.name, Integer.toHexString(this.value));
    }


}
