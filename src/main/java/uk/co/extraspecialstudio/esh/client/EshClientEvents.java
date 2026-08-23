package uk.co.extraspecialstudio.esh.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import uk.co.extraspecialstudio.esh.EshMod;
import uk.co.extraspecialstudio.esh.api.EshOwnsHubNav;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSound;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscUiCue;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscButtons;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscOwnsBackNav;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscText;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscUiStyle;

@Mod.EventBusSubscriber(modid = EshMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EshClientEvents {
    public static final KeyMapping OPEN_HUB = new KeyMapping(
        "key.extraspecialhub.open",
        KeyConflictContext.UNIVERSAL,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F9,
        "key.categories.extraspecialhub"
    );

    private static final int HUB_BTN_W = 72;
    private static final int HUB_BTN_H = 20;

    private EshClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EshClientEvents::onRegisterKeys);
        modBus.addListener(EshClientEvents::onRegisterReloadListeners);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> EshModLogoResolver.clearCache());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_HUB.consumeClick()) {
            if (mc.screen instanceof EshHubScreen) {
                EscSound.play(EscUiCue.CLOSE);
                EshHubSession.end();
                mc.setScreen(null);
            } else {
                EshHubSession.begin();
                mc.setScreen(new EshHubScreen());
            }
        }
    }

    /**
     * Propagate “opened from hub” onto child screens (Pantheon → Hermes, CTL S2C panel, etc.).
     */
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen next = event.getNewScreen();
        if (next == null || next instanceof EshHubScreen) {
            return;
        }
        Screen cur = Minecraft.getInstance().screen;
        if (cur instanceof EshHubScreen || EshHubSession.isHosted(cur)) {
            EshHubSession.markHosted(next);
        }
    }

    /**
     * Inject {@code ← Hub} on every hosted mod menu so Close-to-game screens can still return to ESH.
     * Screens that own their own back nav opt out via {@link EshOwnsHubNav} / {@link EscOwnsBackNav}.
     */
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof EshHubScreen || screen instanceof EscOwnsBackNav || !EshHubSession.isHosted(screen)) {
            return;
        }
        int[] placement = hubButtonPlacement(screen);
        EscUiStyle style = EscUiStyle.active(EshMod.MOD_ID);
        Button hub = EscButtons.button(
            EscText.literal("← Hub"),
            placement[0],
            placement[1],
            placement[2],
            placement[3],
            style,
            b -> EshHubSession.openHub()
        );
        event.addListener(hub);
    }

    /**
     * Align with the hosted screen's top-band Close control: same {@code y} and height, left inset.
     */
    private static int[] hubButtonPlacement(Screen screen) {
        int topBand = Math.min(56, Math.max(28, screen.height / 8));
        AbstractWidget anchor = null;
        for (GuiEventListener child : screen.children()) {
            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }
            if (widget.getY() > topBand || widget.getHeight() <= 0) {
                continue;
            }
            if (widget.getX() + widget.getWidth() / 2 < screen.width / 2) {
                continue;
            }
            if (anchor == null || widget.getX() < anchor.getX()) {
                anchor = widget;
            }
        }

        int w = HUB_BTN_W;
        int h = anchor != null ? anchor.getHeight() : HUB_BTN_H;
        int x = 4;
        int y = anchor != null ? anchor.getY() : 8;

        if (overlapsAny(screen, x, y, w, h, null)) {
            for (int attempt = 0; attempt < 6; attempt++) {
                y += h + 4;
                if (!overlapsAny(screen, x, y, w, h, null)) {
                    break;
                }
            }
        }

        if (overlapsAny(screen, x, y, w, h, null)) {
            x = 4;
            y = 4;
            h = HUB_BTN_H;
        }
        return new int[]{x, y, w, h};
    }

    private static boolean overlapsAny(Screen screen, int x, int y, int w, int h, AbstractWidget ignore) {
        int x1 = x + w;
        int y1 = y + h;
        for (GuiEventListener child : screen.children()) {
            if (!(child instanceof AbstractWidget widget) || widget == ignore) {
                continue;
            }
            int wx1 = widget.getX() + widget.getWidth();
            int wy1 = widget.getY() + widget.getHeight();
            if (x < wx1 && x1 > widget.getX() && y < wy1 && y1 > widget.getY()) {
                return true;
            }
        }
        return false;
    }
}
