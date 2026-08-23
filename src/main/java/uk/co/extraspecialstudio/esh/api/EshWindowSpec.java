package uk.co.extraspecialstudio.esh.api;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Function;

/**
 * Registration payload for a window hosted inside ES Hub.
 */
public final class EshWindowSpec {
    private final String modId;
    private final String windowId;
    private final Component title;
    private final EshSection section;
    private final String author;
    private final Function<Screen, Screen> factory;
    private final String groupId;
    private final String groupTitle;
    private final String subtitle;
    private final int sortOrder;
    private final ResourceLocation icon;
    /** Optional badge for the accordion group this leaf belongs to (overrides first-leaf inherit). */
    private final ResourceLocation groupIcon;

    private EshWindowSpec(Builder b) {
        this.modId = b.modId;
        this.windowId = b.windowId;
        this.title = b.title;
        this.section = b.section;
        this.author = b.author;
        this.factory = b.factory;
        this.groupId = b.groupId;
        this.groupTitle = b.groupTitle;
        this.subtitle = b.subtitle;
        this.sortOrder = b.sortOrder;
        this.icon = b.icon;
        this.groupIcon = b.groupIcon;
    }

    public String modId() {
        return modId;
    }

    public String windowId() {
        return windowId;
    }

    public String registryKey() {
        return modId + "/" + windowId;
    }

    public Component title() {
        return title;
    }

    public EshSection section() {
        return section;
    }

    public String author() {
        return author;
    }

    public Function<Screen, Screen> factory() {
        return factory;
    }

    public String groupId() {
        return groupId;
    }

    public String groupTitle() {
        return groupTitle.isBlank() ? title.getString() : groupTitle;
    }

    public String subtitle() {
        return subtitle;
    }

    public int sortOrder() {
        return sortOrder;
    }

    /** Explicit icon, or null to auto-resolve from modId. */
    public ResourceLocation icon() {
        return icon;
    }

    /**
     * Explicit group badge for IconPolicy. When any leaf in a group sets this, the group
     * uses it instead of inheriting the first leaf's icon.
     */
    public ResourceLocation groupIcon() {
        return groupIcon;
    }

    public static Builder builder(String modId, String windowId, Component title) {
        return new Builder(modId, windowId, title);
    }

    public static Builder builder(String modId, String windowId, String title) {
        return new Builder(modId, windowId, Component.literal(title));
    }

    public static final class Builder {
        private final String modId;
        private final String windowId;
        private final Component title;
        private EshSection section = EshSection.MODS;
        private String author = "Unknown";
        private Function<Screen, Screen> factory;
        private String groupId = "";
        private String groupTitle = "";
        private String subtitle = "";
        private int sortOrder = 0;
        private ResourceLocation icon;
        private ResourceLocation groupIcon;

        private Builder(String modId, String windowId, Component title) {
            this.modId = Objects.requireNonNull(modId, "modId");
            this.windowId = Objects.requireNonNull(windowId, "windowId");
            this.title = Objects.requireNonNull(title, "title");
        }

        public Builder section(EshSection section) {
            this.section = Objects.requireNonNull(section, "section");
            return this;
        }

        public Builder author(String author) {
            this.author = author == null || author.isBlank() ? "Unknown" : author;
            return this;
        }

        public Builder factory(Function<Screen, Screen> factory) {
            this.factory = Objects.requireNonNull(factory, "factory");
            return this;
        }

        public Builder group(String groupId, String groupTitle) {
            this.groupId = groupId == null ? "" : groupId;
            this.groupTitle = groupTitle == null ? "" : groupTitle;
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle == null ? "" : subtitle;
            return this;
        }

        public Builder sortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder icon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        /** Badge for the accordion group row (see EscHubIconPolicy / ESH docs). */
        public Builder groupIcon(ResourceLocation groupIcon) {
            this.groupIcon = groupIcon;
            return this;
        }

        public EshWindowSpec build() {
            if (factory == null) {
                throw new IllegalStateException("factory is required");
            }
            return new EshWindowSpec(this);
        }
    }
}
