package me.jamino.wynnWanderer.mixin;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.FeatureManager;
import me.jamino.wynnWanderer.WynnWanderer;
import me.jamino.wynnWanderer.wynntils.WynnWandererFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wynntils has no API for other mods to add features, so we register ours right after Wynntils
 * registers its own. This runs before Wynntils loads its config, which means our overlay is
 * saved, loaded and positioned exactly like the built-in ones.
 */
@Mixin(value = FeatureManager.class, remap = false)
public abstract class FeatureManagerMixin {
    @Shadow
    private void registerFeature(Feature feature) {}

    @Inject(method = "init()V", at = @At("TAIL"))
    private void wynnWanderer$registerFeature(CallbackInfo ci) {
        try {
            registerFeature(new WynnWandererFeature());
        } catch (Throwable t) {
            WynnWanderer.LOGGER.error("Failed to register the Wynn Wanderer overlay with Wynntils", t);
        }
    }
}
