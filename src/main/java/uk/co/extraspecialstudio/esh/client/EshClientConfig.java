package uk.co.extraspecialstudio.esh.client;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client preferences for ES Hub chrome (layout only — colours live in ESC theme).
 */
public final class EshClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> LAYOUT_ID;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
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
