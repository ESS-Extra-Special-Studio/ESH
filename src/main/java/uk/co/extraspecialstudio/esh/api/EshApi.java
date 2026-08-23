package uk.co.extraspecialstudio.esh.api;

import uk.co.extraspecialstudio.esh.runtime.EshWindowRegistry;

/**
 * Public one-line registration API for ES Hub windows.
 */
public final class EshApi {

    private EshApi() {
    }

    public static void registerModWindow(EshWindowSpec spec) {
        EshWindowRegistry.get().register(spec);
    }

    /**
     * Re-open Extra Special Hub (client). Safe no-op on dedicated server.
     * Prefer this over {@code setScreen(null)} when leaving an ESH-hosted menu.
     */
    public static void openHub() {
        uk.co.extraspecialstudio.esh.client.EshHubSession.openHub();
    }
}
