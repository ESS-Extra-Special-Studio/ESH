package uk.co.extraspecialstudio.esh.client;

import net.minecraft.resources.Identifier;
import uk.co.extraspecialstudio.esh.api.EshSection;

/**
 * Section identity icons for hub cards / classic tabs.
 */
public final class EshSectionIcons {
    private EshSectionIcons() {
    }

    public static Identifier forSection(EshSection section) {
        if (section == null) {
            return Identifier.fromNamespaceAndPath("minecraft", "textures/misc/unknown_pack.png");
        }
        return switch (section) {
            case ESS -> Identifier.fromNamespaceAndPath("extraspecialhub", "textures/gui/ess_badge.png");
            case UTILITY -> Identifier.fromNamespaceAndPath("minecraft", "textures/item/iron_pickaxe.png");
            case MODS -> Identifier.fromNamespaceAndPath("minecraft", "textures/item/comparator.png");
        };
    }
}
