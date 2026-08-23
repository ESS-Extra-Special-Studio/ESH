package uk.co.extraspecialstudio.esh.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.InputConstants;
import uk.co.extraspecialstudio.esh.api.EshSection;
import uk.co.extraspecialstudio.esh.api.EshWindowSpec;
import uk.co.extraspecialstudio.esh.EshMod;
import uk.co.extraspecialstudio.esh.runtime.EshWindowRegistry;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSound;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscUiCue;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscColorRole;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscQualityMode;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemeConfigs;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemeManager;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemeResolved;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemeSwatch;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscBackground;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscButtonBar;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscColorPickerScreen;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscCrtLayer;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscPanel;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscRect;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscScreen;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscSearchBox;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscText;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypeRole;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypography;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscUiStyle;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscTransition;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscTransitionDriver;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubGroup;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubLayout;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubLayoutContext;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubLayoutId;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubLayouts;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubLeaf;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubSection;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscAccordion;

import java.util.ArrayList;
import java.util.List;

/**
 * F9 Extra Special Hub — ESC-driven wow layouts (Carousel default).
 */
public final class EshHubScreen extends EscScreen {

    private EscHubLayoutContext hubCtx;
    private EscHubLayout layout;
    private EscRect hubBounds = new EscRect(0, 0, 0, 0);
    private EscRect footerBounds = new EscRect(0, 0, 0, 0);
    private EscSearchBox searchBox;
    private EshModsListMode listMode = EshModsListMode.BY_AUTHOR;
    private String searchQuery = "";
    private int savedSectionIndex;
    private String savedExpandedGroupId = "";
    private final EscTransitionDriver openTransition = new EscTransitionDriver();
    private final List<Button> footerButtons = new ArrayList<>();
    private final List<FooterBtn> footerDefs = new ArrayList<>();
    /** {@code -1} = accordion / section nav; {@code >= 0} = footer strip index. */
    private int footerFocus = -1;

    public EshHubScreen() {
        super(EscText.literal("Extra Special Hub"), EscUiStyle.active(EshMod.MOD_ID));
        openTransition.restart(EscTransition.SCALE);
        EshHubSession.begin();
        this.savedSectionIndex = EshHubSession.savedSectionIndex();
        this.savedExpandedGroupId = EshHubSession.savedExpandedGroupId();
    }

    @Override
    protected void buildLayout() {
        clearWidgets();
        setStyle(EscUiStyle.active(EshMod.MOD_ID));
        EscRect content = contentRect();
        EscRect title = EscPanel.titleBar(content, style);

        // Footer list first so we know whether one or two rows are needed before sizing the hub.
        EscThemeResolved theme = EscThemeManager.resolve(EshMod.MOD_ID);
        EscHubLayoutId layoutId = readLayoutId();
        java.util.List<FooterBtn> footer = buildFooterDefs(theme, layoutId);
        int pad = 4;
        int gap = 3;
        int footerH = EscButtonBar.fitDenseHeight(
            content.width(), footer.size(), EscButtonBar.DEFAULT_HEIGHT, pad, gap, EscButtonBar.DEFAULT_ROW_GAP);
        footerBounds = new EscRect(content.x(), content.bottom() - footerH, content.width(), footerH);

        // Hub fills from title → footer; Other Mods + Utility search lives in the detail pane.
        hubBounds = new EscRect(
            content.x(),
            title.bottom() + 2,
            content.width(),
            Math.max(40, footerBounds.y() - title.bottom() - 8)
        );

        List<EscHubSection> sections = buildSections();
        hubCtx = new EscHubLayoutContext(
            sections,
            this::groupsForSectionId,
            this::openLeaf,
            style,
            font
        );
        hubCtx.setHostDrawsChrome(true);
        // ESS stack docs: tree branches for sub-items instead of repeating mod logos.
        hubCtx.setCuratedTreeNav("ESS"::equals);
        hubCtx.setLayoutId(layoutId);
        hubCtx.setSectionIndex(savedSectionIndex);
        if (!savedExpandedGroupId.isBlank()) {
            hubCtx.setExpandedGroupId(savedExpandedGroupId);
        }
        refreshHeaderTips();
        layout = EscHubLayouts.create(hubCtx.layoutId());
        layout.reset(hubCtx);

        searchBox = null;
        hubCtx.setDetailTopInset(0);
        if (isSearchableSectionSelected()) {
            EscRect detail = layout.detailBounds(hubCtx, hubBounds);
            int searchPad = 6;
            int searchH = EscSearchBox.recommendedHeight(font);
            int filterW = 150;
            EscRect searchBounds = new EscRect(
                detail.x() + searchPad,
                detail.y() + searchPad,
                Math.max(80, detail.width() - filterW - searchPad * 3),
                searchH
            );
            searchBox = addSearchBox(searchBounds, EscText.literal("Search"), EscText.literal("Search author / mod…"));
            searchBox.setValue(searchQuery);
            searchBox.setResponder(value -> {
                searchQuery = value == null ? "" : value;
                if (hubCtx != null) {
                    hubCtx.invalidateGroupsCache();
                    refreshHeaderTips();
                }
            });
            addButton(EscText.literal(listMode.label()),
                new EscRect(searchBounds.right() + searchPad, detail.y() + searchPad, filterW, 20),
                b -> {
                    EscSound.play(EscUiCue.SELECT);
                    listMode = listMode.next();
                    refreshHubKeepingSection();
                });
            hubCtx.setDetailTopInset(searchH + searchPad * 2);
        }

        footerButtons.clear();
        footerDefs.clear();
        EscRect[] slots = EscButtonBar.fitDense(
            footerBounds, footer.size(), EscButtonBar.DEFAULT_HEIGHT, pad, gap, EscButtonBar.DEFAULT_ROW_GAP);
        for (int i = 0; i < footer.size() && i < slots.length; i++) {
            FooterBtn fb = footer.get(i);
            footerDefs.add(fb);
            footerButtons.add(addButton(EscText.literal(fb.label), slots[i], fb.onPress));
        }
        if (footerFocus >= 0 && footerFocus < footerButtons.size()) {
            applyFooterFocus(footerFocus);
        } else {
            footerFocus = -1;
        }
    }

    private java.util.List<FooterBtn> buildFooterDefs(EscThemeResolved theme, EscHubLayoutId layoutId) {
        java.util.List<FooterBtn> footer = new java.util.ArrayList<>();
        footer.add(new FooterBtn("Layout: " + layoutId.displayName(), b -> cycleLayout(), null));
        if (EscThemeManager.canPlayerCustomize()) {
            footer.add(new FooterBtn(
                EscThemeConfigs.playerScopeLabel(),
                b -> {
                    EscSound.play(EscUiCue.SELECT);
                    EscThemeConfigs.PLAYER_APPLY_GLOBAL.set(!EscThemeConfigs.playerApplyGlobal());
                    EscThemeManager.invalidateCache();
                    refreshHubKeepingSection();
                },
                null));
            footer.add(new FooterBtn("Border: " + theme.borderLabel(),
                b -> openColorPicker(EscColorRole.BORDER), EscColorRole.BORDER));
            footer.add(new FooterBtn("Text: " + theme.textLabel(),
                b -> openColorPicker(EscColorRole.TEXT), EscColorRole.TEXT));
            footer.add(new FooterBtn("Accent: " + theme.accentLabel(),
                b -> openColorPicker(EscColorRole.ACCENT), EscColorRole.ACCENT));
        } else if (EscThemeManager.isPackOverrideActive()) {
            footer.add(new FooterBtn("Theme: Pack locked", b -> {
                EscSound.play(EscUiCue.BACK);
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                        EscText.literal("Pack/server theme override is locked — colours are not personal on this world."),
                        true);
                }
            }, null));
        }
        footer.add(new FooterBtn("Q: " + EscThemeConfigs.qualityMode().name(), b -> {
            EscSound.play(EscUiCue.SELECT);
            EscThemeConfigs.QUALITY_MODE.set(EscThemeConfigs.qualityMode().next().name());
            EscThemeManager.invalidateCache();
            refreshHubKeepingSection();
        }, null));
        footer.add(new FooterBtn("Motion " + (EscThemeConfigs.reducedMotion() ? "OFF" : "ON"), b -> {
            EscSound.play(EscUiCue.SELECT);
            EscThemeConfigs.REDUCED_MOTION.set(!EscThemeConfigs.reducedMotion());
            EscThemeManager.invalidateCache();
            refreshHubKeepingSection();
        }, null));
        footer.add(new FooterBtn("BG: " + EscThemeConfigs.backdropStyle().label(), b -> {
            EscSound.play(EscUiCue.SELECT);
            EscThemeConfigs.BACKDROP_STYLE.set(EscThemeConfigs.backdropStyle().next().name());
            if (EscThemeConfigs.qualityMode() != EscQualityMode.HIGH) {
                EscThemeConfigs.QUALITY_MODE.set(EscQualityMode.HIGH.name());
            }
            EscThemeManager.invalidateCache();
            refreshHubKeepingSection();
        }, null));
        footer.add(new FooterBtn("Close", b -> onClose(), null));
        return footer;
    }

    /** Other Mods + Utility share search / Author·Title·Group filter chrome. */
    private boolean isSearchableSectionSelected() {
        EshSection section = selectedEshSection();
        return section == EshSection.MODS || section == EshSection.UTILITY;
    }

    private EshSection selectedEshSection() {
        if (hubCtx != null && hubCtx.selectedSection() != null) {
            try {
                return EshSection.valueOf(hubCtx.selectedSection().id());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        try {
            return EshSection.values()[Math.floorMod(savedSectionIndex, EshSection.values().length)];
        } catch (Exception e) {
            return null;
        }
    }

    private record FooterBtn(String label, net.minecraft.client.gui.components.Button.OnPress onPress, EscColorRole colorRole) {
    }

    private void openColorPicker(EscColorRole role) {
        EscSound.play(EscUiCue.SELECT);
        if (!EscThemeManager.canPlayerCustomize()) {
            return;
        }
        if (!EscThemeConfigs.PLAYER_CUSTOM.get()) {
            seedPlayerSwatchesFromBase();
            EscThemeConfigs.PLAYER_CUSTOM.set(true);
        }
        EscColorPickerScreen.openPlayerSlot(this, role);
    }

    private void cycleSwatch(EscColorRole role) {
        EscSound.play(EscUiCue.SELECT);
        if (!EscThemeManager.canPlayerCustomize()) {
            return;
        }
        if (!EscThemeConfigs.PLAYER_CUSTOM.get()) {
            seedPlayerSwatchesFromBase();
            EscThemeConfigs.PLAYER_CUSTOM.set(true);
        }
        EscThemeConfigs.cyclePlayerSwatch(role);
        EscThemeManager.invalidateCache();
        refreshHubKeepingSection();
    }

    private void seedPlayerSwatchesFromBase() {
        var base = EscThemeManager.resolveBase(EshMod.MOD_ID);
        EscThemeConfigs.PLAYER_BORDER.set(closestSwatch(base.borderArgb(), true).ordinal());
        EscThemeConfigs.PLAYER_TEXT.set(closestSwatch(base.textTitleRgb(), false).ordinal());
        EscThemeConfigs.PLAYER_ACCENT.set(closestAccent(base.accentArgb()).ordinal());
        EscThemeConfigs.clearPlayerCustomColor(EscColorRole.BORDER);
        EscThemeConfigs.clearPlayerCustomColor(EscColorRole.TEXT);
        EscThemeConfigs.clearPlayerCustomColor(EscColorRole.ACCENT);
    }

    private static EscThemeSwatch closestSwatch(int color, boolean border) {
        int rgb = color & 0xFFFFFF;
        EscThemeSwatch best = EscThemeSwatch.WHITE;
        int bestDist = Integer.MAX_VALUE;
        for (EscThemeSwatch s : EscThemeSwatch.values()) {
            int c = border ? (s.borderArgb() & 0xFFFFFF) : s.titleRgb();
            int dist = colourDist(rgb, c);
            if (dist < bestDist) {
                bestDist = dist;
                best = s;
            }
        }
        return best;
    }

    private static EscThemeSwatch closestAccent(int color) {
        int rgb = color & 0xFFFFFF;
        EscThemeSwatch best = EscThemeSwatch.WHITE;
        int bestDist = Integer.MAX_VALUE;
        for (EscThemeSwatch s : EscThemeSwatch.values()) {
            int dist = colourDist(rgb, s.accentArgb() & 0xFFFFFF);
            if (dist < bestDist) {
                bestDist = dist;
                best = s;
            }
        }
        return best;
    }

    private static int colourDist(int a, int b) {
        int dr = ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
        int dg = ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
        int db = (a & 0xFF) - (b & 0xFF);
        return dr * dr + dg * dg + db * db;
    }

    private void refreshHeaderTips() {
        EscHubSection sel = hubCtx.selectedSection();
        if (sel != null && EshSection.MODS.name().equals(sel.id())) {
            hubCtx.setHeaderTips(List.of(
                "Other Mods — third-party menus only. Search / Filter above the list.",
                "Studio tools live under ESS / PantheonAPI / Utility."
            ));
        } else if (sel != null && EshSection.UTILITY.name().equals(sel.id())) {
            hubCtx.setHeaderTips(List.of(
                "Utility — ops & power-user tools. Search / Filter above the list.",
                "Third-party utility mods register here with EshSection.UTILITY."
            ));
        } else if (sel != null && EshSection.PANTHEON.name().equals(sel.id())) {
            hubCtx.setHeaderTips(List.of(
                "Open Pantheon Hub, then pick Hermes / Aegis from the dock.",
                "Path: F9 → PantheonAPI → Pantheon Hub → Hermes"
            ));
        } else if (EscThemeManager.isPlayerThemeLocked()) {
            hubCtx.setHeaderTips(List.of(
                "Pack/server theme override is locked — Border/Text/Accent are disabled here.",
                "Layout, Q, Motion, and BG stay personal on this PC only."
            ));
        } else {
            hubCtx.setHeaderTips(List.of(
                "Layout / colours / Q / Motion / BG save to this PC only — never the server.",
                "Apply to: ESH = F9 only · Apply to: ALL = every ESC UI on this machine."
            ));
        }
    }

    private List<EscHubSection> buildSections() {
        List<EscHubSection> list = new ArrayList<>();
        for (EshSection section : EshSection.values()) {
            list.add(new EscHubSection(
                section.name(),
                section.displayName(),
                section.subtitle(),
                EshWindowRegistry.get().forSection(section).size(),
                EshSectionIcons.forSection(section)
            ));
        }
        return list;
    }

    private List<EscHubGroup> groupsForSectionId(String sectionId) {
        try {
            EshSection section = EshSection.valueOf(sectionId);
            if (section == EshSection.MODS || section == EshSection.UTILITY) {
                return EshWindowRegistry.get().groupsForSection(section, listMode, searchQuery);
            }
            // ESS / Pantheon stay curated — no search chrome.
            return EshWindowRegistry.get().groupsForSection(section, EshModsListMode.BY_AUTHOR, "");
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    private void openLeaf(EscHubLeaf leaf) {
        rememberSection();
        EshHubSession.rememberHubState(savedSectionIndex, savedExpandedGroupId);
        EshWindowSpec spec = EshWindowRegistry.get().find(leaf.id());
        if (spec == null || minecraft == null) {
            return;
        }
        Screen child = spec.factory().apply(this);
        if (child != null && child != this) {
            EshHubSession.markHosted(child);
            minecraft.setScreen(child);
        }
    }

    private EscHubLayoutId readLayoutId() {
        try {
            return EscHubLayoutId.valueOf(EshClientConfig.LAYOUT_ID.get());
        } catch (Exception e) {
            return EscHubLayoutId.CAROUSEL;
        }
    }

    private void cycleLayout() {
        rememberSection();
        EscHubLayoutId next = hubCtx.layoutId().next();
        hubCtx.setLayoutId(next);
        EshClientConfig.LAYOUT_ID.set(next.name());
        EscSound.play(EscUiCue.LAYOUT_SWITCH);
        openTransition.restart(EscTransition.PUSH_H);
        refreshHubKeepingSection();
    }

    private void rememberSection() {
        if (hubCtx != null) {
            savedSectionIndex = hubCtx.sectionIndex();
            savedExpandedGroupId = hubCtx.expandedGroupId();
        }
    }

    private void refreshHubKeepingSection() {
        rememberSection();
        if (searchBox != null) {
            searchQuery = searchBox.getValue();
        }
        clearWidgets();
        buildLayout();
    }

    @Override
    public void tick() {
        super.tick();
        openTransition.tick();
        if (hubCtx != null && EshModLogoResolver.consumeDirty()) {
            hubCtx.invalidateGroupsCache();
        }
        if (layout != null && hubCtx != null) {
            int before = hubCtx.sectionIndex();
            layout.tick(hubCtx);
            if (hubCtx.sectionIndex() != before) {
                refreshHeaderTips();
                refreshHubKeepingSection();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        setStyle(EscUiStyle.active(EshMod.MOD_ID));
        EscRect content = contentRect();
        EscBackground.render(graphics, content, style.theme());
        float alpha = openTransition.alpha();
        int ox = openTransition.offsetX(content.width());
        int oy = openTransition.offsetY(content.height());
        EscTypography.drawCentered(graphics, font, "EXTRA SPECIAL HUB",
            content.x() + content.width() / 2 + ox, content.y() + 6 + oy, style, EscTypeRole.TITLE, alpha);
        EscTypography.draw(graphics, font, "Arrows / WASD · Enter · Down to footer · LMB colour picker · RMB cycle · F9 close",
            content.x() + ox, content.y() + 18 + oy, style, EscTypeRole.META, content.width(), alpha);
        if (layout != null && hubCtx != null) {
            // Tips refresh on section/search change — not every frame.
            layout.render(graphics, hubCtx, hubBounds, mouseX, mouseY, partialTick);
        }
        EscPanel.renderPanel(graphics, footerBounds, style);
        // Accent lip on skinned footer
        int lip = (0xAA << 24) | (style.accentColor() & 0xFFFFFF);
        graphics.fill(footerBounds.x(), footerBounds.y(), footerBounds.right(), footerBounds.y() + 2, lip);
        EscCrtLayer.render(graphics, content, style.theme());
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Right-click colour footer buttons = cycle presets (left-click opens picker).
        if (button == 1) {
            for (int i = 0; i < footerButtons.size() && i < footerDefs.size(); i++) {
                Button b = footerButtons.get(i);
                FooterBtn def = footerDefs.get(i);
                if (def.colorRole() != null
                    && mouseX >= b.getX() && mouseX < b.getX() + b.getWidth()
                    && mouseY >= b.getY() && mouseY < b.getY() + b.getHeight()) {
                    cycleSwatch(def.colorRole());
                    return true;
                }
            }
        }
        if (layout != null && hubCtx != null) {
            int before = hubCtx.sectionIndex();
            if (layout.mouseClicked(hubCtx, hubBounds, mouseX, mouseY, button)) {
                if (hubCtx.sectionIndex() != before) {
                    refreshHubKeepingSection();
                } else {
                    refreshHeaderTips();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (layout != null && hubCtx != null && layout.mouseScrolled(hubCtx, hubBounds, mouseX, mouseY, scrollY)) {
            refreshHeaderTips();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (footerFocus >= 0) {
            if (keyPressedFooter(keyCode)) {
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (layout != null && hubCtx != null) {
            int before = hubCtx.sectionIndex();
            if (layout.keyPressed(hubCtx, keyCode, scanCode, modifiers)) {
                if (hubCtx.sectionIndex() != before) {
                    refreshHubKeepingSection();
                } else {
                    refreshHeaderTips();
                }
                return true;
            }
            if (keyCode == InputConstants.KEY_DOWN || keyCode == InputConstants.KEY_S) {
                if (enterFooterFocus(0)) {
                    EscSound.play(EscUiCue.MOVE);
                    return true;
                }
            }
            if (keyCode == InputConstants.KEY_UP || keyCode == InputConstants.KEY_W) {
                EscSound.play(EscUiCue.UNAVAILABLE);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean keyPressedFooter(int keyCode) {
        if (keyCode == InputConstants.KEY_UP || keyCode == InputConstants.KEY_W) {
            leaveFooterFocus();
            EscSound.play(EscUiCue.MOVE);
            return true;
        }
        if (keyCode == InputConstants.KEY_DOWN || keyCode == InputConstants.KEY_S) {
            EscSound.play(EscUiCue.UNAVAILABLE);
            return true;
        }
        if (keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_A) {
            cycleFooterFocus(-1);
            return true;
        }
        if (keyCode == InputConstants.KEY_RIGHT || keyCode == InputConstants.KEY_D) {
            cycleFooterFocus(1);
            return true;
        }
        if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_SPACE) {
            if (footerFocus >= 0 && footerFocus < footerButtons.size()) {
                Button b = footerButtons.get(footerFocus);
                return b.mouseClicked(b.getX() + b.getWidth() * 0.5, b.getY() + b.getHeight() * 0.5, 0);
            }
            return true;
        }
        return false;
    }

    private boolean enterFooterFocus(int index) {
        if (footerButtons.isEmpty()) {
            return false;
        }
        if (hubCtx != null) {
            hubCtx.setDetailCursor(-1);
        }
        applyFooterFocus(Math.floorMod(index, footerButtons.size()));
        return true;
    }

    private void leaveFooterFocus() {
        footerFocus = -1;
        setFocused(null);
        if (hubCtx != null && hubCtx.selectedSection() != null) {
            // Land on last accordion row when coming up from footer.
            EscAccordion.moveFocus(hubCtx, hubCtx.selectedSection().id(), -1);
        }
    }

    private void cycleFooterFocus(int delta) {
        if (footerButtons.isEmpty()) {
            return;
        }
        int next = Math.floorMod(footerFocus + delta, footerButtons.size());
        applyFooterFocus(next);
        EscSound.play(EscUiCue.MOVE);
    }

    private void applyFooterFocus(int index) {
        footerFocus = index;
        Button b = footerButtons.get(index);
        setFocused(b);
        b.setFocused(true);
    }

    @Override
    public void onClose() {
        EscSound.play(EscUiCue.CLOSE);
        EshHubSession.end();
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }
}
