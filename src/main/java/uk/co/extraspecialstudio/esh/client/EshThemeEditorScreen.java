package uk.co.extraspecialstudio.esh.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSound;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscUiCue;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscEffectPreset;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscFrameStyle;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscQualityMode;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscTheme;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemeConfigs;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemeManager;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemePreset;
import uk.co.extraspecialstudio.extraspecial.esc.theme.EscThemeValidator;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscBackground;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscButtonBar;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscCard;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscCrtLayer;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscPanel;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscRect;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscText;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypeRole;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypography;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscUiStyle;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscTransition;

import java.util.List;
import java.util.Locale;

/**
 * Live ESH theme tuner — dual preview, effect/presentation knobs, export.
 * Uses ESC theming primitives; Apply stores the ESH application theme.
 */
public final class EshThemeEditorScreen extends EshEscPageScreen {
    private EscTheme working;
    private int presetIndex;
    private int effectIndex;

    public EshThemeEditorScreen(Screen parent) {
        super(EscText.literal("ESH Theme Tuner"), parent, EscTransition.SCALE);
        this.working = EscThemeManager.resolve("extraspecialhub").theme();
        this.presetIndex = clampPresetIndex(working);
        this.effectIndex = 0;
    }

    private static int clampPresetIndex(EscTheme theme) {
        String name = theme.name() == null ? "" : theme.name().replace("Pack:", "");
        EscThemePreset p = EscThemePreset.byName(name);
        return p.ordinal();
    }

    @Override
    protected void buildLayout() {
        clearWidgets();
        setStyle(EscUiStyle.fromTheme(working));
        EscRect content = contentRect();
        EscRect title = EscPanel.titleBar(content, style);
        // Two equal button rows + padding (pad + row + gap + row + pad).
        int footerH = EscButtonBar.DEFAULT_PAD * 2
            + EscButtonBar.DEFAULT_HEIGHT * 2
            + EscButtonBar.DEFAULT_ROW_GAP;
        footerBounds = new EscRect(content.x(), content.bottom() - footerH, content.width(), footerH);
        panelBounds = new EscRect(
            content.x(),
            title.bottom() + 4,
            content.width(),
            Math.max(40, footerBounds.y() - title.bottom() - 8)
        );

        EscRect[][] rows = EscButtonBar.equalRows(footerBounds, new int[]{5, 7}, EscButtonBar.DEFAULT_HEIGHT);
        EscRect[] row0 = rows[0];
        EscRect[] row1 = rows[1];

        addButton(EscText.literal("Preset: " + EscThemePreset.values()[presetIndex].label()),
            row0[0], b -> cyclePreset());
        addButton(EscText.literal("Effect: " + EscEffectPreset.values()[effectIndex].name()),
            row0[1], b -> cycleEffect());
        addButton(EscText.literal("Frame: " + working.frameStyle().name()),
            row0[2], b -> cycleFrame());
        addButton(EscText.literal("Q: " + EscThemeConfigs.qualityMode().name()),
            row0[3], b -> cycleQuality());
        addButton(EscText.literal("Motion " + (EscThemeConfigs.reducedMotion() ? "OFF" : "ON")),
            row0[4], b -> {
                EscSound.play(EscUiCue.SELECT);
                EscThemeConfigs.REDUCED_MOTION.set(!EscThemeConfigs.reducedMotion());
                rebuild();
            });

        addButton(EscText.literal("Op " + pct(working.panelOpacity())),
            row1[0], b -> nudgeOpacity());
        addButton(EscText.literal("Sh " + pct(working.shadowStrength())),
            row1[1], b -> nudgePresentation(0));
        addButton(EscText.literal("Gl " + pct(working.glowStrength())),
            row1[2], b -> nudgePresentation(1));
        addButton(EscText.literal("CRT " + pct(working.crtIntensity())),
            row1[3], b -> nudgePresentation(2));
        addButton(EscText.literal("Export"),
            row1[4], b -> exportClipboard());
        if (EscThemeManager.canPlayerCustomize()) {
            addButton(EscText.literal("Apply"),
                row1[5], b -> applyWorking());
        } else {
            addButton(EscText.literal("Locked"),
                row1[5], b -> {
                    EscSound.play(EscUiCue.BACK);
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                            EscText.literal("Pack/server theme is locked — Apply is disabled."), true);
                    }
                });
        }
        addButton(EscText.literal("Back"),
            row1[6], b -> {
                EscSound.play(EscUiCue.BACK);
                onClose();
            });
    }

    @Override
    protected void renderPage(GuiGraphics g, EscRect panel, float alpha) {
        // Unused — full render overridden below.
    }

    private static String pct(float v) {
        return String.format(Locale.ROOT, "%d%%", Math.round(v * 100f));
    }

    private void cyclePreset() {
        EscSound.play(EscUiCue.SELECT);
        EscThemePreset[] all = EscThemePreset.values();
        presetIndex = (presetIndex + 1) % all.length;
        EscThemePreset selected = all[presetIndex];
        working = EscEffectPreset.values()[effectIndex].apply(selected.theme());
        if (selected.preferredBackdrop() != null) {
            EscThemeConfigs.BACKDROP_STYLE.set(selected.preferredBackdrop().name());
            EscThemeConfigs.QUALITY_MODE.set(EscQualityMode.HIGH.name());
            EscThemeConfigs.REDUCED_MOTION.set(false);
        }
        rebuild();
    }

    private void cycleEffect() {
        EscSound.play(EscUiCue.SELECT);
        EscEffectPreset[] all = EscEffectPreset.values();
        effectIndex = (effectIndex + 1) % all.length;
        working = all[effectIndex].apply(EscThemePreset.values()[presetIndex].theme());
        rebuild();
    }

    private void cycleFrame() {
        EscSound.play(EscUiCue.SELECT);
        EscFrameStyle[] all = EscFrameStyle.values();
        EscFrameStyle next = all[(working.frameStyle().ordinal() + 1) % all.length];
        working = working.withPresentation(
            working.panelOpacity(), next,
            working.shadowStrength(), working.glowStrength(), working.crtIntensity(), working.motionScale()
        );
        rebuild();
    }

    private void cycleQuality() {
        EscSound.play(EscUiCue.SELECT);
        EscQualityMode next = EscThemeConfigs.qualityMode().next();
        EscThemeConfigs.QUALITY_MODE.set(next.name());
        rebuild();
    }

    private void nudgeOpacity() {
        EscSound.play(EscUiCue.SELECT);
        float next = working.panelOpacity() + 0.05f;
        if (next > 1.001f) next = 0.55f;
        working = working.withPresentation(
            next, working.frameStyle(),
            working.shadowStrength(), working.glowStrength(), working.crtIntensity(), working.motionScale()
        );
        rebuild();
    }

    /** 0=shadow, 1=glow, 2=crt */
    private void nudgePresentation(int which) {
        EscSound.play(EscUiCue.SELECT);
        float shadow = working.shadowStrength();
        float glow = working.glowStrength();
        float crt = working.crtIntensity();
        switch (which) {
            case 0 -> {
                shadow += 0.1f;
                if (shadow > 1.001f) shadow = 0f;
            }
            case 1 -> {
                glow += 0.1f;
                if (glow > 1.001f) glow = 0f;
            }
            default -> {
                crt += 0.1f;
                if (crt > 1.001f) crt = 0f;
            }
        }
        working = working.withPresentation(
            working.panelOpacity(), working.frameStyle(), shadow, glow, crt, working.motionScale()
        );
        rebuild();
    }

    private void applyWorking() {
        if (!EscThemeManager.canPlayerCustomize()) {
            EscSound.play(EscUiCue.BACK);
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    EscText.literal("Pack/server theme is locked — Apply is disabled."), true);
            }
            return;
        }
        EscSound.play(EscUiCue.CONFIRM);
        working = EscThemeValidator.clamp(working);
        // In-memory application layer for ESH on this client only — never writes COMMON/pack.toml.
        EscThemeManager.registerApplicationTheme("extraspecialhub", working);
        List<String> issues = EscThemeValidator.validate(working);
        if (!issues.isEmpty() && minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(EscText.literal("Theme notes: " + issues.get(0)), true);
        } else if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(
                EscText.literal("Applied ESH theme on this PC (client only)"), true);
        }
        EscThemeConfigs.PLAYER_CUSTOM.set(true);
        rebuild();
    }

    private void exportClipboard() {
        EscSound.play(EscUiCue.NOTIFICATION);
        String export = "preset=" + EscThemePreset.values()[presetIndex].name()
            + " effect=" + EscEffectPreset.values()[effectIndex].name()
            + " backdrop=" + EscThemeConfigs.backdropStyle().name()
            + " frame=" + working.frameStyle().name()
            + " opacity=" + String.format(Locale.ROOT, "%.2f", working.panelOpacity())
            + " shadow=" + String.format(Locale.ROOT, "%.2f", working.shadowStrength())
            + " glow=" + String.format(Locale.ROOT, "%.2f", working.glowStrength())
            + " crt=" + String.format(Locale.ROOT, "%.2f", working.crtIntensity())
            + " motion=" + String.format(Locale.ROOT, "%.2f", working.motionScale())
            + " border=#" + hex(working.borderArgb())
            + " accent=#" + hex(working.accentArgb());
        if (minecraft != null) {
            minecraft.keyboardHandler.setClipboard(export);
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(EscText.literal("Theme export copied"), true);
            }
        }
    }

    private static String hex(int argb) {
        return String.format(Locale.ROOT, "%06X", argb & 0xFFFFFF);
    }

    private void rebuild() {
        clearWidgets();
        buildLayout();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        setStyle(EscUiStyle.fromTheme(working));
        EscRect content = contentRect();
        EscBackground.render(graphics, content, working);
        EscTypography.drawCentered(graphics, font, "ESH THEME TUNER",
            content.x() + content.width() / 2, content.y() + 6, style, EscTypeRole.TITLE, 1f);
        EscTypography.draw(graphics, font,
            EscThemeManager.canPlayerCustomize()
                ? "Client prefs only · Apply skins ESH on this PC · Export copies knobs for pack.toml"
                : "Pack/server theme locked · preview + Export still work · Apply disabled",
            content.x(), content.y() + 20, style, EscTypeRole.META, content.width(), 1f);

        EscRect preview = panelBounds.width() > 0 ? panelBounds : new EscRect(
            content.x(), content.y() + 36, content.width(), Math.max(80, content.height() - 110));
        EscPanel.renderPanel(graphics, preview, style);

        int cardW = Math.min(220, (preview.width() - 48) / 2);
        EscRect focused = new EscRect(preview.x() + 16, preview.y() + 16, cardW, 72);
        EscRect muted = new EscRect(preview.x() + 24 + cardW, preview.y() + 16, cardW, 72);
        EscCard.render(graphics, font, focused, style, working.name(), "Focused card", "HOT", 1f);
        EscCard.render(graphics, font, muted, style, working.name(), "Muted card", "DIM", 0.2f);

        EscTypography.draw(graphics, font,
            "frame " + working.frameStyle().name()
                + " · bg " + EscThemeConfigs.backdropStyle().label()
                + " · op " + pct(working.panelOpacity())
                + " · sh " + pct(working.shadowStrength())
                + " · gl " + pct(working.glowStrength())
                + " · crt " + pct(working.crtIntensity()),
            preview.x() + 16, preview.y() + 100, style, EscTypeRole.BODY, preview.width() - 32, 1f);

        List<String> issues = EscThemeValidator.validate(working);
        int y = preview.y() + 118;
        for (String issue : issues) {
            EscTypography.draw(graphics, font, "• " + issue, preview.x() + 16, y, style, EscTypeRole.META, preview.width() - 32, 1f);
            y += font.lineHeight + 2;
        }
        EscCrtLayer.render(graphics, content, working);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
