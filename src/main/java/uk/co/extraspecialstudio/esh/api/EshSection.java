package uk.co.extraspecialstudio.esh.api;

/**
 * Top-level ESH hub sections (tab order = declaration order).
 */
public enum EshSection {
    ESS,
    PANTHEON,
    /** Ops / power-user tools (search + filter; studio + third-party). */
    UTILITY,
    /** Third-party / other authors' menus (search + filter). */
    MODS;

    public String displayName() {
        return switch (this) {
            case ESS -> "ESS";
            case PANTHEON -> "PantheonAPI";
            case UTILITY -> "Utility";
            case MODS -> "Other Mods";
        };
    }

    public String subtitle() {
        return switch (this) {
            case ESS -> "Studio docs & stack";
            case PANTHEON -> "Pantheon Hub + dock";
            case UTILITY -> "Ops & tools · search";
            case MODS -> "Third-party menus";
        };
    }
}
