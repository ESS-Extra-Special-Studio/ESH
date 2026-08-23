package uk.co.extraspecialstudio.esh.integration;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import uk.co.extraspecialstudio.esh.api.EshApi;
import uk.co.extraspecialstudio.esh.api.EshSection;
import uk.co.extraspecialstudio.esh.api.EshWindowSpec;
import uk.co.extraspecialstudio.esl.mod.EslMods;

import java.lang.reflect.Method;

/**
 * Soft-registers ESG authoring leaves when {@code extraspecialgui} is installed.
 * ESH does not compile against ESG screen classes.
 */
public final class EsgHubBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ESG_MOD_ID = "extraspecialgui";
    private static final String ESG_SCREENS = "uk.co.extraspecialstudio.esg.EsgScreens";

    private EsgHubBridge() {
    }

    public static void registerIfPresent() {
        if (!EslMods.isLoaded(ESG_MOD_ID)) {
            return;
        }
        try {
            Class<?> screens = Class.forName(ESG_SCREENS);
            Method builder = screens.getMethod("builder", Screen.class);
            Method library = screens.getMethod("assetLibrary", Screen.class);
            Method profiles = screens.getMethod("profiles", Screen.class);

            EshApi.registerModWindow(EshWindowSpec.builder(ESG_MOD_ID, "gui_builder", "GUI Builder")
                .section(EshSection.ESS)
                .author("Extra Special Studio")
                .group("esg", "ESG")
                .subtitle("Compose ESC screens · live preview")
                .sortOrder(23)
                .factory(parent -> invokeScreen(builder, parent))
                .build());

            EshApi.registerModWindow(EshWindowSpec.builder(ESG_MOD_ID, "asset_library", "Asset Library")
                .section(EshSection.ESS)
                .author("Extra Special Studio")
                .group("esg", "ESG")
                .subtitle("Browse ESC bricks")
                .sortOrder(24)
                .factory(parent -> invokeScreen(library, parent))
                .build());

            EshApi.registerModWindow(EshWindowSpec.builder(ESG_MOD_ID, "profiles", "Profiles")
                .section(EshSection.ESS)
                .author("Extra Special Studio")
                .group("esg", "ESG")
                .subtitle("Saved GUI profiles")
                .sortOrder(25)
                .factory(parent -> invokeScreen(profiles, parent))
                .build());
        } catch (Throwable ex) {
            LOGGER.warn("ESG is loaded but hub registration failed: {}", ex.toString());
        }
    }

    private static Screen invokeScreen(Method factory, Screen parent) {
        try {
            return (Screen) factory.invoke(null, parent);
        } catch (Throwable ex) {
            LOGGER.warn("ESG screen factory failed, returning to parent: {}", ex.toString());
            return parent;
        }
    }
}
