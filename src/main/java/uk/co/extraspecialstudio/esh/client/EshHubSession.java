package uk.co.extraspecialstudio.esh.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscSound;
import uk.co.extraspecialstudio.extraspecial.esc.sound.EscUiCue;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tracks screens opened from Extra Special Hub (and children of those screens)
 * so ESH can offer a universal {@code ← Hub} return without each mod wiring it.
 */
public final class EshHubSession {
    private static final Set<Screen> HOSTED = Collections.newSetFromMap(new WeakHashMap<>());

    private static int savedSectionIndex;
    private static String savedExpandedGroupId = "";
    private static boolean active;

    private EshHubSession() {
    }

    /** Call when the hub is shown from F9 / Hub button. */
    public static void begin() {
        active = true;
    }

    /** Call when the hub is dismissed to gameplay (Close / F9 while on hub). */
    public static void end() {
        active = false;
        HOSTED.clear();
    }

    public static boolean isActive() {
        return active;
    }

    public static void rememberHubState(int sectionIndex, String expandedGroupId) {
        savedSectionIndex = sectionIndex;
        savedExpandedGroupId = expandedGroupId == null ? "" : expandedGroupId;
    }

    public static int savedSectionIndex() {
        return savedSectionIndex;
    }

    public static String savedExpandedGroupId() {
        return savedExpandedGroupId;
    }

    public static void markHosted(Screen screen) {
        if (screen == null || screen instanceof EshHubScreen) {
            return;
        }
        HOSTED.add(screen);
        active = true;
    }

    public static boolean isHosted(Screen screen) {
        return screen != null && HOSTED.contains(screen);
    }

    /** Re-open the hub, restoring the last section / expanded group when possible. */
    public static void openHub() {
        EscSound.play(EscUiCue.BACK);
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        active = true;
        mc.setScreen(new EshHubScreen());
    }
}
