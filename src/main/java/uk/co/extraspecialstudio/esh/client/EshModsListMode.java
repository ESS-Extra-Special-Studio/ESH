package uk.co.extraspecialstudio.esh.client;

/**
 * How searchable hub lists (Other Mods + Utility) are organised.
 */
public enum EshModsListMode {
    BY_AUTHOR,
    BY_TITLE,
    BY_GROUP;

    public String label() {
        return switch (this) {
            case BY_AUTHOR -> "Filter: Author";
            case BY_TITLE -> "Filter: Title";
            case BY_GROUP -> "Filter: Group";
        };
    }

    public EshModsListMode next() {
        EshModsListMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
