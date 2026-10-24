package uk.co.extraspecialstudio.esh.client;

import net.minecraft.client.util.InputMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import uk.co.extraspecialstudio.esh.EshMod;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSound;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscUiCue;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscButtons;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscOwnsBackNav;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscText;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscUiStyle;

@Mod.EventBusSubscriber(modid = EshMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EshClientEvents {
    public static final KeyBinding OPEN_HUB = new KeyBinding(
        "key.extraspecialhub.open",
        KeyConflictContext.UNIVERSAL,
        InputMappings.Type.KEYSYM,
        GLFW.GLFW_KEY_F9,
        "key.categories.extraspecialhub"
    );

    private static final int HUB_BTN_W = 72;
    private static final int HUB_BTN_H = 20;

    private EshClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EshClientEvents::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.registerKeyBinding(OPEN_HUB);
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getResourceManager() instanceof IReloadableResourceManager) {
                ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(
                    (IResourceManagerReloadListener) manager -> EshModLogoResolver.clearCache()
                );
            }
        });
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
     * Propagate “opened from hub” onto child screens (CTL S2C panel, nested menus, etc.).
     */
    @SubscribeEvent
    public static void onScreenOpening(GuiOpenEvent event) {
        Screen next = event.getGui();
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
     * Screens that own their own back nav opt out via {@link uk.co.extraspecialstudio.esh.api.EshOwnsHubNav} / {@link EscOwnsBackNav}.
     */
    @SubscribeEvent
    public static void onScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (screen instanceof EshHubScreen || screen instanceof EscOwnsBackNav || !EshHubSession.isHosted(screen)) {
            return;
        }
        java.util.List<Widget> widgets = event.getWidgetList();
        int[] placement = hubButtonPlacement(screen, widgets);
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
        event.addWidget(hub);
    }

    /**
     * Align with the hosted screen's top-band Close control: same {@code y} and height, left inset.
     */
    private static int[] hubButtonPlacement(Screen screen, java.util.List<Widget> widgets) {
        int topBand = Math.min(56, Math.max(28, screen.height / 8));
        Widget anchor = null;
        for (Widget widget : widgets) {
            if (widget.y > topBand || widget.getHeight() <= 0) {
                continue;
            }
            if (widget.x + widget.getWidth() / 2 < screen.width / 2) {
                continue;
            }
            if (anchor == null || widget.x < anchor.x) {
                anchor = widget;
            }
        }

        int w = HUB_BTN_W;
        int h = anchor != null ? anchor.getHeight() : HUB_BTN_H;
        int x = 4;
        int y = anchor != null ? anchor.y : 8;

        if (overlapsAny(widgets, x, y, w, h, null)) {
            for (int attempt = 0; attempt < 6; attempt++) {
                y += h + 4;
                if (!overlapsAny(widgets, x, y, w, h, null)) {
                    break;
                }
            }
        }

        if (overlapsAny(widgets, x, y, w, h, null)) {
            x = 4;
            y = 4;
            h = HUB_BTN_H;
        }
        return new int[]{x, y, w, h};
    }

    private static boolean overlapsAny(java.util.List<Widget> widgets, int x, int y, int w, int h, Widget ignore) {
        int x1 = x + w;
        int y1 = y + h;
        for (Widget widget : widgets) {
            if (widget == ignore) {
                continue;
            }
            int wx1 = widget.x + widget.getWidth();
            int wy1 = widget.y + widget.getHeight();
            if (x < wx1 && x1 > widget.x && y < wy1 && y1 > widget.y) {
                return true;
            }
        }
        return false;
    }
}
