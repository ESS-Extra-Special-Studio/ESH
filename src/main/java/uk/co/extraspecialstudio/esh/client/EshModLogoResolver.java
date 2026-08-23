package uk.co.extraspecialstudio.esh.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves a mod logo/icon for ESH badges with caching and safe fallbacks.
 * Prefers in-pack textures, then {@code mods.toml} {@code logoFile} (jar-root logos
 * are registered as dynamic textures — same source Forge's mod list uses).
 * <p>
 * First call returns a placeholder and schedules load for the next tick so hub open
 * does not hitch on jar I/O. Dynamic textures are released on {@link #clearCache()}.
 */
public final class EshModLogoResolver {
    private static final Map<String, Optional<ResourceLocation>> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();
    private static final Map<ResourceLocation, DynamicTexture> DYNAMICS = new ConcurrentHashMap<>();
    private static final ResourceLocation FALLBACK = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/unknown_pack.png");
    private static volatile boolean dirty;

    private EshModLogoResolver() {
    }

    public static ResourceLocation resolve(String modId) {
        if (modId == null || modId.isBlank()) {
            return FALLBACK;
        }
        Optional<ResourceLocation> hit = CACHE.get(modId);
        if (hit != null) {
            return hit.orElse(FALLBACK);
        }
        requestLoad(modId);
        return FALLBACK;
    }

    public static ResourceLocation resolveOrNull(String modId) {
        if (modId == null || modId.isBlank()) {
            return null;
        }
        Optional<ResourceLocation> hit = CACHE.get(modId);
        if (hit != null) {
            return hit.orElse(null);
        }
        requestLoad(modId);
        return null;
    }

    /** True once after any deferred logo finishes (hub invalidates group cache). */
    public static boolean consumeDirty() {
        if (!dirty) {
            return false;
        }
        dirty = false;
        return true;
    }

    private static void requestLoad(String modId) {
        if (!LOADING.add(modId)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            LOADING.remove(modId);
            CACHE.putIfAbsent(modId, Optional.empty());
            return;
        }
        // Defer past the current frame so first paint stays light.
        mc.tell(() -> {
            try {
                Optional<ResourceLocation> packHit = lookupPackTextures(modId);
                if (packHit.isPresent()) {
                    CACHE.put(modId, packHit);
                    dirty = true;
                    LOADING.remove(modId);
                    return;
                }
            } catch (Exception e) {
                // fall through to jar path
            }
            CompletableFuture
                .supplyAsync(() -> readJarLogoBytes(modId))
                .thenAccept(jarBytes -> mc.execute(() -> {
                    try {
                        if (jarBytes != null) {
                            CACHE.put(modId, uploadDynamic(modId, jarBytes));
                        } else {
                            CACHE.put(modId, Optional.empty());
                        }
                        dirty = true;
                    } finally {
                        LOADING.remove(modId);
                    }
                }));
        });
    }

    private static Optional<ResourceLocation> lookupPackTextures(String modId) {
        String[] candidates = {
            modId + ":logo.png",
            modId + ":textures/logo.png",
            modId + ":textures/gui/logo.png",
            modId + ":textures/gui/icon.png",
            modId + ":icon.png",
            modId + ":textures/icon.png",
            modId + ":pack.png"
        };
        for (String c : candidates) {
            ResourceLocation rl = tryParse(c);
            if (rl != null && resourceExists(rl)) {
                return Optional.of(rl);
            }
        }

        try {
            return ModList.get().getModContainerById(modId).flatMap(container -> {
                IModInfo info = container.getModInfo();
                String logoFile = info.getLogoFile().orElse(null);
                if (logoFile == null || logoFile.isBlank()) {
                    Object prop = info.getModProperties().get("logoFile");
                    if (prop instanceof String s && !s.isBlank()) {
                        logoFile = s;
                    }
                }
                if (logoFile == null || logoFile.isBlank()) {
                    return Optional.empty();
                }
                String normalized = logoFile.replace('\\', '/');
                String fileName = normalized.contains("/")
                    ? normalized.substring(normalized.lastIndexOf('/') + 1)
                    : normalized;
                ResourceLocation assetGuess = tryParse(modId + ":textures/gui/" + fileName);
                if (assetGuess != null && resourceExists(assetGuess)) {
                    return Optional.of(assetGuess);
                }
                assetGuess = tryParse(modId + ":" + normalized);
                if (assetGuess != null && resourceExists(assetGuess)) {
                    return Optional.of(assetGuess);
                }
                return Optional.empty();
            });
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static byte[] readJarLogoBytes(String modId) {
        try {
            return ModList.get().getModContainerById(modId).map(container -> {
                IModInfo info = container.getModInfo();
                String logoFile = info.getLogoFile().orElse(null);
                if (logoFile == null || logoFile.isBlank()) {
                    Object prop = info.getModProperties().get("logoFile");
                    if (prop instanceof String s && !s.isBlank()) {
                        logoFile = s;
                    }
                }
                if (logoFile == null || logoFile.isBlank()) {
                    return null;
                }
                String normalized = logoFile.replace('\\', '/');
                IModFile modFile = info.getOwningFile().getFile();
                String[] parts = normalized.split("/");
                Path path = modFile.findResource(parts);
                if (path == null || !Files.exists(path)) {
                    return null;
                }
                try {
                    return Files.readAllBytes(path);
                } catch (Exception e) {
                    return null;
                }
            }).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static Optional<ResourceLocation> uploadDynamic(String modId, byte[] bytes) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || bytes == null || bytes.length == 0) {
            return Optional.empty();
        }
        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                "extraspecialhub", "dynamic_mod_logo/" + modId.toLowerCase(Locale.ROOT)
            );
            mc.getTextureManager().register(id, texture);
            DYNAMICS.put(id, texture);
            return Optional.of(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static boolean resourceExists(ResourceLocation rl) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return false;
            }
            ResourceManager rm = mc.getResourceManager();
            return rm.getResource(rl).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    private static ResourceLocation tryParse(String path) {
        try {
            return ResourceLocation.parse(path);
        } catch (Exception e) {
            return null;
        }
    }

    public static ResourceLocation fallback() {
        return FALLBACK;
    }

    /** Call if resource packs reload and logos may have changed. */
    public static void clearCache() {
        CACHE.clear();
        LOADING.clear();
        Minecraft mc = Minecraft.getInstance();
        for (ResourceLocation id : DYNAMICS.keySet()) {
            if (mc != null) {
                mc.getTextureManager().release(id);
            }
        }
        DYNAMICS.clear();
        dirty = true;
    }
}
