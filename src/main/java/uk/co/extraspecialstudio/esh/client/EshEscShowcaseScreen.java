package uk.co.extraspecialstudio.esh.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSound;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSounds;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscComponentSkin;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscEffectPreset;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscFrameStyle;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscTheme;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscCard;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscPanel;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscRect;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscText;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypeRole;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypography;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscUiStyle;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscFocusVisual;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscTransition;

/**
 * Live gallery of ESC frames, focus phases, skins, effects, and chrome.
 */
public final class EshEscShowcaseScreen extends EshEscPageScreen {
    private int page;
    private int menuPreview = 1;

    public EshEscShowcaseScreen(Screen parent) {
        super(EscText.literal("ESC Showcase"), parent, EscTransition.SCALE);
    }

    @Override
    protected int[] footerLeftPreferredWidths() {
        if (page == 4) {
            return new int[]{170, 28, 90, 28};
        }
        return new int[]{170};
    }

    @Override
    protected void placeFooterLeft(EscRect[] slots) {
        if (slots.length == 0) {
            return;
        }
        addButton(EscText.literal("Page: " + pageLabel()), slots[0], b -> {
            page = (page + 1) % 5;
            clearWidgets();
            buildLayout();
        });
        if (page == 4 && slots.length >= 4) {
            addButton(EscText.literal("◀"), slots[1], b -> {
                menuPreview = menuPreview <= 1 ? EscSounds.MENU_BANK_SIZE : menuPreview - 1;
                EscSound.playMenu(menuPreview);
                clearWidgets();
                buildLayout();
            });
            addButton(EscText.literal("Play #" + menuPreview), slots[2], b -> EscSound.playMenu(menuPreview));
            addButton(EscText.literal("▶"), slots[3], b -> {
                menuPreview = menuPreview >= EscSounds.MENU_BANK_SIZE ? 1 : menuPreview + 1;
                EscSound.playMenu(menuPreview);
                clearWidgets();
                buildLayout();
            });
        }
    }

    private String pageLabel() {
        return switch (page) {
            case 0 -> "Frames";
            case 1 -> "Focus + Skins";
            case 2 -> "Effects";
            case 3 -> "Live chrome";
            default -> "Menu bank";
        };
    }

    @Override
    protected void renderPage(GuiGraphics g, EscRect panel, float alpha) {
        EscTypography.draw(g, font, pageLabel().toUpperCase() + " — click Page to cycle",
            panel.x() + 10, panel.y() + 10, style, EscTypeRole.META, panel.width() - 20, alpha);
        switch (page) {
            case 0 -> renderFrames(g, panel, alpha);
            case 1 -> renderFocusSkins(g, panel, alpha);
            case 2 -> renderEffects(g, panel, alpha);
            case 3 -> renderLive(g, panel, alpha);
            default -> renderMenuBank(g, panel, alpha);
        }
    }

    private void renderMenuBank(GuiGraphics g, EscRect panel, float alpha) {
        EscTypography.draw(g, font, "Menu Sound bank — audition 1–20, bind later via EscUiCueMap",
            panel.x() + 10, panel.y() + 30, style, EscTypeRole.BODY, panel.width() - 20, alpha);
        EscTypography.draw(g, font, "Now previewing: MENU SOUND " + menuPreview,
            panel.x() + 10, panel.y() + 50, style, EscTypeRole.TITLE, panel.width() - 20, alpha);
        EscTypography.draw(g, font, "Footer: ◀ / Play / ▶   ·   Hub cues stay silent until you assign",
            panel.x() + 10, panel.y() + 72, style, EscTypeRole.META, panel.width() - 20, alpha);

        int cols = 10;
        int gap = 4;
        int cellW = (panel.width() - 20 - gap * (cols - 1)) / cols;
        int cellH = 28;
        int y0 = panel.y() + 100;
        for (int i = 1; i <= EscSounds.MENU_BANK_SIZE; i++) {
            int col = (i - 1) % cols;
            int row = (i - 1) / cols;
            EscRect cell = new EscRect(
                panel.x() + 10 + col * (cellW + gap),
                y0 + row * (cellH + gap),
                cellW,
                cellH
            );
            boolean sel = i == menuPreview;
            EscPanel.fill(g, cell, style.panelFillColor());
            EscPanel.border(g, cell, sel ? style.focusBorderColor() : style.panelBorderColor(), sel ? 2 : 1);
            EscTypography.drawCentered(g, font, String.valueOf(i),
                cell.x() + cell.width() / 2, cell.y() + 8, style,
                sel ? EscTypeRole.TITLE : EscTypeRole.BODY, alpha);
        }
    }

    private void renderFrames(GuiGraphics g, EscRect panel, float alpha) {
        EscFrameStyle[] frames = EscFrameStyle.values();
        int cols = Math.min(3, frames.length);
        int gap = 8;
        int cellW = (panel.width() - 20 - gap * (cols - 1)) / cols;
        int cellH = 56;
        for (int i = 0; i < frames.length; i++) {
            int col = i % cols;
            int row = i / cols;
            EscRect cell = new EscRect(
                panel.x() + 10 + col * (cellW + gap),
                panel.y() + 30 + row * (cellH + gap),
                cellW,
                cellH
            );
            EscUiStyle local = EscUiStyle.fromTheme(style.theme().withPresentation(
                style.theme().panelOpacity(), frames[i],
                Math.max(0.45f, style.theme().shadowStrength()),
                Math.max(0.4f, style.theme().glowStrength()),
                style.theme().crtIntensity(), style.theme().motionScale()
            ));
            EscPanel.renderPanel(g, cell, local);
            float sweep = (System.currentTimeMillis() % 1600L) / 1600f;
            EscPanel.animatedBorderSweep(g, cell,
                EscFocusVisual.applyAlpha(local.accentColor() | 0xFF000000, 0.85f * alpha), sweep, 0.6f);
            EscTypography.drawCentered(g, font, frames[i].name(),
                cell.x() + cell.width() / 2, cell.y() + cell.height() / 2 - 4, local, EscTypeRole.SUBTITLE, alpha);
        }
    }

    private void renderFocusSkins(GuiGraphics g, EscRect panel, float alpha) {
        float[] focuses = {0.15f, 0.45f, 0.75f, 1f};
        String[] labels = {"MUTED", "IDLE", "NEAR", "FOCUS"};
        int cardW = Math.min(150, (panel.width() - 40) / 4);
        int y = panel.y() + 30;
        for (int i = 0; i < focuses.length; i++) {
            EscRect card = new EscRect(panel.x() + 10 + i * (cardW + 8), y, cardW, 72);
            EscCard.renderIdentity(g, font, card, style, labels[i], "focus " + focuses[i], String.valueOf(i + 1),
                focuses[i], EshSectionIcons.forSection(
                    switch (i) {
                        case 0 -> uk.co.extraspecialstudio.esh.api.EshSection.ESS;
                        case 1 -> uk.co.extraspecialstudio.esh.api.EshSection.UTILITY;
                        default -> uk.co.extraspecialstudio.esh.api.EshSection.MODS;
                    }));
        }

        EscComponentSkin[] skins = EscComponentSkin.values();
        int sy = y + 88;
        EscTypography.draw(g, font, "COMPONENT SKINS", panel.x() + 10, sy, style, EscTypeRole.META, panel.width() - 20, alpha);
        sy += 14;
        int skinW = Math.min(110, (panel.width() - 30) / Math.max(1, skins.length));
        for (int i = 0; i < skins.length; i++) {
            EscTheme skinned = skins[i].apply(style.theme());
            EscUiStyle local = EscUiStyle.fromTheme(skinned);
            EscRect card = new EscRect(panel.x() + 10 + i * (skinW + 4), sy, skinW, 52);
            EscCard.render(g, font, card, local, skins[i].name(), "skin", "", 0.9f);
        }
    }

    private void renderEffects(GuiGraphics g, EscRect panel, float alpha) {
        EscEffectPreset[] presets = EscEffectPreset.values();
        int cols = 3;
        int gap = 8;
        int cellW = (panel.width() - 20 - gap * (cols - 1)) / cols;
        int cellH = 74;
        for (int i = 0; i < presets.length; i++) {
            int col = i % cols;
            int row = i / cols;
            EscTheme themed = presets[i].apply(style.theme()).withName(presets[i].name());
            EscUiStyle local = EscUiStyle.fromTheme(themed);
            EscRect cell = new EscRect(
                panel.x() + 10 + col * (cellW + gap),
                panel.y() + 30 + row * (cellH + gap),
                cellW,
                cellH
            );
            EscCard.render(g, font, cell, local, presets[i].name(),
                "crt " + String.format("%.2f", themed.crtIntensity()),
                themed.frameStyle().name(), 0.98f);
        }
    }

    private void renderLive(GuiGraphics g, EscRect panel, float alpha) {
        EscTypography.draw(g, font, "Footer / buttons use EscChromeButton · panel CRT from active theme",
            panel.x() + 10, panel.y() + 30, style, EscTypeRole.BODY, panel.width() - 20, alpha);

        EscRect demo = new EscRect(panel.x() + 10, panel.y() + 52, Math.min(320, panel.width() - 20), 70);
        EscPanel.renderPanel(g, demo, style);
        float sweep = (System.currentTimeMillis() % 1400L) / 1400f;
        EscPanel.animatedBorderSweep(g, demo,
            EscFocusVisual.applyAlpha(style.accentColor() | 0xFF000000, 0.95f), sweep, 0.8f);
        EscTypography.drawCentered(g, font, "ANIMATED BORDER + DEPTH",
            demo.x() + demo.width() / 2, demo.y() + 28, style, EscTypeRole.TITLE, alpha);

        EscRect a = new EscRect(panel.x() + 10, panel.y() + 136, 140, 72);
        EscRect b = new EscRect(panel.x() + 160, panel.y() + 136, 140, 72);
        EscCard.renderIdentity(g, font, a, style, "FOCUSED", "identity card", "OK", 1f,
            EshSectionIcons.forSection(uk.co.extraspecialstudio.esh.api.EshSection.ESS));
        EscCard.renderIdentity(g, font, b, style, "MUTED", "identity card", "…", 0.2f,
            EshSectionIcons.forSection(uk.co.extraspecialstudio.esh.api.EshSection.MODS));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (page == 4 && button == 0 && panelBounds.contains(mouseX, mouseY)) {
            int cols = 10;
            int gap = 4;
            int cellW = (panelBounds.width() - 20 - gap * (cols - 1)) / cols;
            int cellH = 28;
            int y0 = panelBounds.y() + 100;
            for (int i = 1; i <= EscSounds.MENU_BANK_SIZE; i++) {
                int col = (i - 1) % cols;
                int row = (i - 1) / cols;
                EscRect cell = new EscRect(
                    panelBounds.x() + 10 + col * (cellW + gap),
                    y0 + row * (cellH + gap),
                    cellW,
                    cellH
                );
                if (cell.contains(mouseX, mouseY)) {
                    menuPreview = i;
                    EscSound.playMenu(menuPreview);
                    clearWidgets();
                    buildLayout();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
