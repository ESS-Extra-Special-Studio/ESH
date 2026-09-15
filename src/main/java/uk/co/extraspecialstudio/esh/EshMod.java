package uk.co.extraspecialstudio.esh;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import uk.co.extraspecialstudio.esh.api.EshApi;
import uk.co.extraspecialstudio.esh.api.EshWindowSpec;
import uk.co.extraspecialstudio.esh.client.EshClientConfig;
import uk.co.extraspecialstudio.esh.client.EshClientEvents;
import uk.co.extraspecialstudio.esh.client.EshRecommendedUtilities;
import uk.co.extraspecialstudio.esh.docs.EssDocsWindows;
import uk.co.extraspecialstudio.esh.json.EshJsonWindowLoader;

/**
 * ES Hub — F9 ecosystem shell for Extra Special Studio.
 * Theme / chrome colours are owned by ESC ({@code extraspecialcore-theme-*.toml}).
 */
@Mod(EshMod.MOD_ID)
public final class EshMod {
    public static final String MOD_ID = "extraspecialhub";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EshMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EshClientConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            EshClientEvents.register(modBus);
            EssDocsWindows.registerBuiltins();
            EshJsonWindowLoader.loadFromConfig();
            EshRecommendedUtilities.seed();
        }
        LOGGER.info("ES Hub (ESH) loaded — F9 opens the hub");
    }

    /** Convenience for early registrations from other mods' constructors (client). */
    public static void registerWindow(EshWindowSpec spec) {
        EshApi.registerModWindow(spec);
    }
}
