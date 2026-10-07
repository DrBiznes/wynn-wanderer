package me.jamino.wynnWanderer.wynntils;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.utils.type.RenderElementType;

/**
 * The Wynntils feature that owns the territory title overlay. Registering it with Wynntils lets the
 * overlay be moved and toggled from the Wynntils overlay manager like any built-in overlay.
 * <p>
 * Wynntils derives translation keys from the class names, see the "feature.wynntils.wynnWanderer"
 * keys in the language file before renaming this class or the overlay.
 */
@ConfigCategory(Category.OVERLAYS)
public class WynnWandererFeature extends Feature {
    private static WynnWandererFeature instance;

    @RegisterOverlay(renderType = RenderElementType.TITLE)
    private final TerritoryTitleOverlay territoryTitleOverlay = new TerritoryTitleOverlay();

    public WynnWandererFeature() {
        super(ProfileDefault.ENABLED);
        instance = this;
    }

    /**
     * @return The feature registered with Wynntils, or null if it has not been (or could not be) registered
     */
    public static WynnWandererFeature getInstance() {
        return instance;
    }

    public TerritoryTitleOverlay getTerritoryTitleOverlay() {
        return territoryTitleOverlay;
    }
}
