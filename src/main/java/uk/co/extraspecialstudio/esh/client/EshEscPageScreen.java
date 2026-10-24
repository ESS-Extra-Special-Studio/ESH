package uk.co.extraspecialstudio.esh.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import uk.co.extraspecialstudio.esh.EshMod;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSound;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscUiCue;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscBackground;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscButtonBar;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscCrtLayer;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscPanel;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscRect;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscScreen;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscText;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypeRole;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypography;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscUiStyle;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscTransition;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscTransitionDriver;

/**
 * Shared ESC-chrome page for ESH docs / showcase / tools — backdrop, CRT, panel, open transition.
 */
public abstract class EshEscPageScreen extends EscScreen {
    protected final Screen parent;
    private final EscTransitionDriver openTransition = new EscTransitionDriver();
    protected EscRect panelBounds = new EscRect(0, 0, 0, 0);
    protected EscRect footerBounds = new EscRect(0, 0, 0, 0);

    protected EshEscPageScreen(Component title, Screen parent) {
        this(title, parent, EscTransition.FADE);
    }

    protected EshEscPageScreen(Component title, Screen parent, EscTransition open) {
        super(title, EscUiStyle.active(EshMod.MOD_ID));
        this.parent = parent;
        openTransition.restart(open == null ? EscTransition.FADE : open);
    }

    @Override
    protected void buildLayout() {
        clearWidgets();
        setStyle(EscUiStyle.active(EshMod.MOD_ID));
        EscRect content = contentRect();
        EscRect title = EscPanel.titleBar(content, style);
        footerBounds = EscPanel.footer(content, style);
        panelBounds = new EscRect(
            content.x(),
            title.bottom() + 4,
            content.width(),
            Math.max(40, footerBounds.y() - title.bottom() - 8)
        );
        int fy = footerBounds.y() + EscButtonBar.DEFAULT_PAD;
        int[] leftPreferred = footerLeftPreferredWidths();
        EscRect[] slots = EscButtonBar.splitOrEqual(
            footerBounds,
            leftPreferred,
            new int[]{80},
            fy,
            EscButtonBar.DEFAULT_HEIGHT
        );
        int leftN = leftPreferred.length;
        if (leftN > 0 && slots.length >= leftN) {
            EscRect[] leftSlots = new EscRect[leftN];
            System.arraycopy(slots, 0, leftSlots, 0, leftN);
            placeFooterLeft(leftSlots);
        }
        EscRect backSlot = slots.length > 0 ? slots[slots.length - 1]
            : new EscRect(footerBounds.right() - 84, fy, 80, EscButtonBar.DEFAULT_HEIGHT);
        addButton(EscText.literal("Back"), backSlot, b -> {
            EscSound.play(EscUiCue.BACK);
            onClose();
        });
    }

    /**
     * Preferred widths for footer controls left of Back.
     * Empty = Back alone on the right. Overflow packs into an equal strip with Back.
     */
    protected int[] footerLeftPreferredWidths() {
        return new int[0];
    }

    /** Place left-side footer controls into slots from {@link EscButtonBar#splitOrEqual}. */
    protected void placeFooterLeft(EscRect[] slots) {
    }

    protected abstract void renderPage(GuiGraphics g, EscRect panel, float alpha);

    @Override
    public void tick() {
        super.tick();
        openTransition.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        setStyle(EscUiStyle.active(EshMod.MOD_ID));
        EscRect content = contentRect();
        EscBackground.render(graphics, content, style.theme());
        float alpha = openTransition.alpha();
        float scale = openTransition.scale();
        int oy = openTransition.offsetY(12);
        int ox = openTransition.offsetX(content.width());

        EscTypography.drawCentered(graphics, font, getTitle().getString(),
            content.x() + content.width() / 2 + ox, content.y() + 6 + oy, style, EscTypeRole.TITLE, alpha);

        EscRect panel = scaledRect(panelBounds, scale);
        EscPanel.renderPanel(graphics, panel, style);
        // Accent strip under title edge of panel
        int strip = Math.max(20, Math.min(120, Math.round(panel.width() * 0.22f * alpha)));
        int accent = (Math.round(180 * alpha) << 24) | (style.accentColor() & 0xFFFFFF);
        graphics.fill(panel.x() + 10, panel.y() + 2, panel.x() + 10 + strip, panel.y() + 4, accent);

        renderPage(graphics, panel, alpha);
        EscPanel.renderPanel(graphics, footerBounds, style);
        EscCrtLayer.render(graphics, content, style.theme());
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static EscRect scaledRect(EscRect base, float scale) {
        if (scale >= 0.999f) {
            return base;
        }
        int w = Math.max(40, Math.round(base.width() * scale));
        int h = Math.max(40, Math.round(base.height() * scale));
        int cx = base.x() + base.width() / 2;
        int cy = base.y() + base.height() / 2;
        return new EscRect(cx - w / 2, cy - h / 2, w, h);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE || keyCode == InputConstants.KEY_BACKSPACE) {
            EscSound.play(EscUiCue.BACK);
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
