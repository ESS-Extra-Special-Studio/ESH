package uk.co.extraspecialstudio.esh.api;

/**
 * Top-level ESH hub sections (tab order = declaration order).
 */
public enum EshSection {
    ESS,
    /** Ops / power-user tools (search + filter; studio + third-party). */
    UTILITY,
    /** Gameplay / settings menus (studio + third-party). */
    MODS;

    public String displayName() {
        return switch (this) {
            case ESS -> "ESS";
            case UTILITY -> "Utility";
            case MODS -> "Mods";
        };
    }

    public String subtitle() {
        return switch (this) {
            case ESS -> "Studio docs & stack";
            case UTILITY -> "Ops & tools · search";
            case MODS -> "Installed mod menus";
        };
    }
}
