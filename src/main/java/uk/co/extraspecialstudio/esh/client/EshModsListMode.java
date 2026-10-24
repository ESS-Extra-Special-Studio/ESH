package uk.co.extraspecialstudio.esh.client;

/**
 * How searchable hub lists (Other Mods + Utility) are organised.
 */
public enum EshModsListMode {
    BY_AUTHOR,
    BY_TITLE,
    BY_GROUP;

    public String label() {
        switch (this) {
            case BY_AUTHOR:
                return "Filter: Author";
            case BY_TITLE:
                return "Filter: Title";
            case BY_GROUP:
                return "Filter: Group";
            default:
                return name();
        }
    }

    public EshModsListMode next() {
        EshModsListMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
