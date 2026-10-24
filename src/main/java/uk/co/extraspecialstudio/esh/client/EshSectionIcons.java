package uk.co.extraspecialstudio.esh.client;

import net.minecraft.util.ResourceLocation;
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
        switch (section) {
            case ESS:
                return new ResourceLocation("extraspecialhub", "textures/gui/ess_badge.png");
            case UTILITY:
                return new ResourceLocation("minecraft", "textures/item/iron_pickaxe.png");
            case MODS:
                return new ResourceLocation("minecraft", "textures/item/comparator.png");
            default:
                return new ResourceLocation("minecraft", "textures/misc/unknown_pack.png");
        }
    }
}
