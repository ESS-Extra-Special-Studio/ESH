package uk.co.extraspecialstudio.esh.client;

import net.minecraft.resources.ResourceLocation;
import uk.co.extraspecialstudio.esh.api.EshSection;

/**
 * Section identity icons for hub cards / classic tabs.
 */
public final class EshSectionIcons {
    private EshSectionIcons() {
    }

    public static ResourceLocation forSection(EshSection section) {
        if (section == null) {
            return new ResourceLocation("minecraft", "textures/misc/unknown_pack.png");
        }
        return switch (section) {
            case ESS -> new ResourceLocation("extraspecialhub", "textures/gui/ess_badge.png");
            case UTILITY -> new ResourceLocation("minecraft", "textures/item/iron_pickaxe.png");
            case MODS -> new ResourceLocation("minecraft", "textures/item/comparator.png");
        };
    }
}
