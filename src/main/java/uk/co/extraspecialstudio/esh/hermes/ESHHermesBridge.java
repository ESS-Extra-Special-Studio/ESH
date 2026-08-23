package uk.co.extraspecialstudio.esh.hermes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridge for Hermes default modifier profiles / complex defaults.
 * Hermes registers into ESH Utility separately; this holds shared defaults.
 */
public final class ESHHermesBridge {
    private static final Map<String, Object> DEFAULTS = new ConcurrentHashMap<>();

    private ESHHermesBridge() {
    }

    /**
     * Register a named Hermes default payload (profile id → opaque config object).
     * Hermes interprets the object; ESH only stores it for discovery.
     */
    public static void registerHermesDefault(String profileId, Object defaultConfig) {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("profileId");
        }
        if (defaultConfig == null) {
            throw new IllegalArgumentException("defaultConfig");
        }
        DEFAULTS.put(profileId, defaultConfig);
    }

    public static Object getHermesDefault(String profileId) {
        return DEFAULTS.get(profileId);
    }

    public static Map<String, Object> allDefaults() {
        return Map.copyOf(DEFAULTS);
    }
}
