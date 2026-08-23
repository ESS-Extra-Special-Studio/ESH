package uk.co.extraspecialstudio.esh.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client preferences for ES Hub chrome (layout only — colours live in ESC theme).
 */
public final class EshClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> LAYOUT_ID;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("hub");
        LAYOUT_ID = b
            .comment("Hub layout profile: CAROUSEL, CONTROL_CENTRE, DASHBOARD, ORBITAL, CLASSIC")
            .define("layoutId", "CAROUSEL");
        b.pop();
        SPEC = b.build();
    }

    private EshClientConfig() {
    }
}
