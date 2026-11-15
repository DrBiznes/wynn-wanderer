package me.jamino.wynnWanderer.config.gui;

import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.config.WynnWandererConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Builds the config screen with Cloth Config. Cloth Config does not save anything itself here, it only edits the
 * live config and asks the mod to save it, so the mod works the same with or without it.
 * <p>
 * Do not touch this class unless {@link WynnWanderer#isClothConfigLoaded()} is true, it can not be used without
 * Cloth Config.
 */
public final class ClothConfigScreen {
    private static boolean registered;

    private ClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigHolder<ClothConfigData> holder = holder();
        // Pick up the current settings in case the live config was replaced since the screen was last opened
        holder.load();
        return AutoConfigClient.getConfigScreen(ClothConfigData.class, parent).get();
    }

    private static synchronized ConfigHolder<ClothConfigData> holder() {
        if (!registered) {
            AutoConfig.register(ClothConfigData.class, (definition, configClass) -> new LiveConfigSerializer());
            registered = true;
        }
        return AutoConfig.getConfigHolder(ClothConfigData.class);
    }

    private static class LiveConfigSerializer implements ConfigSerializer<ClothConfigData> {
        @Override
        public void serialize(ClothConfigData data) {
            data.copyTo(WynnWanderer.getConfig());
            WynnWanderer.saveConfig();
        }

        @Override
        public ClothConfigData deserialize() {
            return ClothConfigData.of(WynnWanderer.getConfig());
        }

        @Override
        public ClothConfigData createDefault() {
            return ClothConfigData.of(new WynnWandererConfig());
        }
    }
}
