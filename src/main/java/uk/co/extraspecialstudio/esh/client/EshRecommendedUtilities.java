package uk.co.extraspecialstudio.esh.client;

import net.neoforged.fml.ModList;
import uk.co.extraspecialstudio.esh.api.EshApi;
import uk.co.extraspecialstudio.esh.api.EshSection;
import uk.co.extraspecialstudio.esh.api.EshWindowSpec;
import uk.co.extraspecialstudio.esh.runtime.EshWindowRegistry;

/**
 * Seeds Utility badges for studio tools even when those mods are not installed.
 * Live mods replace the same registry keys when they register.
 */
public final class EshRecommendedUtilities {
    private EshRecommendedUtilities() {
    }

    public static void seed() {
        seedIfMissing(
            "calmtheleaks",
            "panel",
            "Calm The Leaks",
            "Recommended · not installed",
            "Install Calm The Leaks (CTL) for leak diagnostics. CurseForge / Modrinth: calm-the-leaks."
        );
        seedIfMissing(
            "whatlib",
            "scanner",
            "WhatLIB?",
            "Recommended · not installed",
            "Install WhatLIB for dependency trees and orphan review. CurseForge / Modrinth: whatlib."
        );
        seedIfMissing(
            "hermes",
            "settings",
            "Hermes",
            "Recommended · not installed",
            "Install Hermes for keybind remapping. CurseForge / Modrinth: hermes."
        );
        seedIfMissing(
            "aegis_accord",
            "settings",
            "Aegis Accord",
            "Recommended · not installed",
            "Install Aegis Accord for ally protection & targeting. CurseForge / Modrinth: aegis-accord."
        );
    }

    private static void seedIfMissing(String modId, String windowId, String title, String subtitle, String hint) {
        String key = modId + "/" + windowId;
        if (EshWindowRegistry.get().find(key) != null) {
            return;
        }
        if (ModList.get().isLoaded(modId)) {
            return;
        }
        EshApi.registerModWindow(EshWindowSpec.builder(modId, windowId, title)
            .section(EshSection.UTILITY)
            .author("Extra Special Studio")
            .subtitle(subtitle)
            .sortOrder(900)
            .recommended(hint)
            .build());
    }
}
