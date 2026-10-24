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
        switch (this) {
            case ESS:
                return "ESS";
            case UTILITY:
                return "Utility";
            case MODS:
                return "Mods";
            default:
                return name();
        }
    }

    public String subtitle() {
        switch (this) {
            case ESS:
                return "Studio docs & stack";
            case UTILITY:
                return "Ops & tools · search";
            case MODS:
                return "Installed mod menus";
            default:
                return "";
        }
    }
}
