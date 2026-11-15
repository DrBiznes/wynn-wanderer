package me.jamino.wynnWanderer.config.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.jamino.wynnWanderer.WynnWanderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Only loaded by Mod Menu. The config screen needs Cloth Config, so without it Mod Menu gets the default factory
 * and shows no config button, and the settings are changed in config/wynn-wanderer.toml instead.
 */
@Environment(EnvType.CLIENT)
public class WynnWandererModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!WynnWanderer.isClothConfigLoaded()) {
            return ModMenuApi.super.getModConfigScreenFactory();
        }
        // Kept in a lambda so the Cloth Config classes are only loaded when the screen is opened
        return parent -> ClothConfigScreen.create(parent);
    }
}
