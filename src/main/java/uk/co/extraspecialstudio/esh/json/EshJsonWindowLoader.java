package uk.co.extraspecialstudio.esh.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import uk.co.extraspecialstudio.esh.api.EshApi;
import uk.co.extraspecialstudio.esh.api.EshSection;
import uk.co.extraspecialstudio.esh.api.EshWindowSpec;
import uk.co.extraspecialstudio.esh.docs.EssDocsWindows;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads optional hub window definitions from {@code config/extraspecialhub/windows/*.json}.
 * <p>
 * JSON cannot invent arbitrary GUIs — each entry must use one of:
 * <ul>
 *   <li>{@code docs}: string array — opens a simple doc screen</li>
 *   <li>{@code runCommand}: chat command without leading slash (client sends as command)</li>
 *   <li>{@code screenClass}: fully-qualified {@code Screen} class with {@code (Screen parent)} ctor</li>
 * </ul>
 */
public final class EshJsonWindowLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private EshJsonWindowLoader() {
    }

    public static void loadFromConfig() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("extraspecialhub").resolve("windows");
        try {
            Files.createDirectories(dir);
            Path example = dir.resolve("_example_window.json");
            if (!Files.exists(example)) {
                Files.writeString(example, exampleJson());
            }
        } catch (IOException e) {
            LOGGER.warn("Could not create ESH windows config dir", e);
            return;
        }
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                .filter(p -> !p.getFileName().toString().startsWith("_"))
                .forEach(EshJsonWindowLoader::loadFile);
        } catch (IOException e) {
            LOGGER.warn("Failed listing ESH window JSON files", e);
        }
    }

    private static void loadFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return;
            if (root.has("windows") && root.get("windows").isJsonArray()) {
                for (JsonElement el : root.getAsJsonArray("windows")) {
                    if (el.isJsonObject()) registerOne(el.getAsJsonObject(), path);
                }
            } else {
                registerOne(root, path);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load ESH window JSON {}", path, e);
        }
    }

    private static void registerOne(JsonObject o, Path path) {
        String modId = str(o, "modId", "json");
        String windowId = str(o, "windowId", path.getFileName().toString().replace(".json", ""));
        String title = str(o, "title", windowId);
        String author = str(o, "author", "JSON");
        String subtitle = str(o, "subtitle", "");
        String groupId = str(o, "groupId", "");
        String groupTitle = str(o, "groupTitle", "");
        int sortOrder = o.has("sortOrder") ? o.get("sortOrder").getAsInt() : 100;
        EshSection section = parseSection(str(o, "section", "MODS"));

        EshWindowSpec.Builder b = EshWindowSpec.builder(modId, windowId, title)
            .section(section)
            .author(author)
            .subtitle(subtitle)
            .sortOrder(sortOrder)
            .group(groupId, groupTitle);
        String iconPath = str(o, "icon", "");
        if (!iconPath.isBlank()) {
            try {
                b.icon(new net.minecraft.resources.ResourceLocation(iconPath));
            } catch (Exception ignored) {
            }
        }

        if (o.has("docs") && o.get("docs").isJsonArray()) {
            List<String> lines = new ArrayList<>();
            JsonArray arr = o.getAsJsonArray("docs");
            for (JsonElement el : arr) {
                lines.add(el.getAsString());
            }
            b.factory(parent -> EssDocsWindows.openDoc(parent, title, lines));
        } else if (o.has("runCommand")) {
            String cmd = o.get("runCommand").getAsString();
            b.factory(parent -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.connection.sendCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
                }
                return parent;
            });
        } else if (o.has("screenClass")) {
            String className = o.get("screenClass").getAsString();
            b.factory(parent -> openScreenClass(className, parent));
        } else {
            LOGGER.warn("ESH JSON {} has no docs/runCommand/screenClass — skipped", path);
            return;
        }
        EshApi.registerModWindow(b.build());
        LOGGER.info("Registered ESH window from JSON {}", path.getFileName());
    }

    private static Screen openScreenClass(String className, Screen parent) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getConstructor(Screen.class);
            Object inst = ctor.newInstance(parent);
            if (inst instanceof Screen screen) {
                return screen;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to open screenClass {}", className, e);
        }
        return parent;
    }

    private static EshSection parseSection(String raw) {
        try {
            return EshSection.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return EshSection.MODS;
        }
    }

    private static String str(JsonObject o, String key, String def) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : def;
    }

    private static String exampleJson() {
        return """
            {
              "modId": "example_pack",
              "windowId": "readme",
              "title": "Example JSON Window",
              "section": "MODS",
              "author": "Pack Author",
              "subtitle": "Loaded from config/extraspecialhub/windows",
              "docs": [
                "Rename this file (drop the leading _) to enable it.",
                "Use docs[], runCommand, or screenClass (Screen(Screen) ctor).",
                "Java mods should prefer EshApi.registerModWindow(...)."
              ]
            }
            """;
    }
}
