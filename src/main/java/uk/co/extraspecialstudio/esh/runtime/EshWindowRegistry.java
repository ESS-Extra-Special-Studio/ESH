package uk.co.extraspecialstudio.esh.runtime;

import uk.co.extraspecialstudio.esh.EshMod;
import uk.co.extraspecialstudio.esh.api.EshSection;
import uk.co.extraspecialstudio.esh.api.EshWindowSpec;
import uk.co.extraspecialstudio.esh.client.EshModsListMode;
import uk.co.extraspecialstudio.esh.client.EshModLogoResolver;
import uk.co.extraspecialstudio.esl.registry.EslRegistry;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubGroup;
import uk.co.extraspecialstudio.extraspecial.esc.ui.layout.EscHubLeaf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores registered hub windows.
 */
public final class EshWindowRegistry {
    private static final EshWindowRegistry INSTANCE = new EshWindowRegistry();

    private static final ResourceLocation ESS_BADGE =
        ResourceLocation.fromNamespaceAndPath("extraspecialhub", "textures/gui/ess_badge.png");
    private static final ResourceLocation ESG_BADGE =
        ResourceLocation.fromNamespaceAndPath("extraspecialhub", "textures/gui/esg_badge.png");

    private final EslRegistry<EshWindowSpec> windows = new EslRegistry<>();

    private EshWindowRegistry() {
    }

    public static EshWindowRegistry get() {
        return INSTANCE;
    }

    public void register(EshWindowSpec spec) {
        if (spec.section() == EshSection.MODS && isStackCore(spec)) {
            EshMod.LOGGER.warn(
                "Rejected ESH Mods registration for stack core {} — use ESS / Utility.",
                spec.registryKey()
            );
            return;
        }
        String key = spec.registryKey();
        if (windows.contains(key)) {
            EshMod.LOGGER.warn("Replacing ESH window {}", key);
            windows.registerOrReplace(key, spec);
        } else {
            windows.register(key, spec);
        }
        EshMod.LOGGER.info("Registered ESH window {} [{}] author={} enabled={}",
            key, spec.section(), spec.author(), spec.enabled());
    }

    /** Stack cores must not register under Mods. */
    private static boolean isStackCore(EshWindowSpec spec) {
        String modId = spec.modId() == null ? "" : spec.modId().trim().toLowerCase(Locale.ROOT);
        return STACK_CORE_MOD_IDS.contains(modId);
    }

    private static final java.util.Set<String> STACK_CORE_MOD_IDS = java.util.Set.of(
        "extraspecialhub",
        "extraspecialcore",
        "extraspecialgui",
        "extraspeciallib"
    );

    public EshWindowSpec find(String registryKey) {
        return windows.get(registryKey).orElse(null);
    }

    public List<EshWindowSpec> forSection(EshSection section) {
        List<EshWindowSpec> list = new ArrayList<>();
        for (EshWindowSpec s : windows.values()) {
            if (s.section() == section) {
                list.add(s);
            }
        }
        list.sort(Comparator
            .comparingInt(EshWindowSpec::sortOrder)
            .thenComparing(EshWindowSpec::author, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(s -> s.title().getString(), String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public List<EscHubGroup> groupsForSection(EshSection section, EshModsListMode mode, String search) {
        List<EshWindowSpec> specs = filterSearch(forSection(section), search);
        // Mods + Utility: Author / Title / Group filter modes.
        if (section == EshSection.MODS || section == EshSection.UTILITY) {
            return groupMods(specs, mode == null ? EshModsListMode.BY_AUTHOR : mode, section);
        }
        // ESS: curated explicit groups (search chrome not shown).
        return groupByExplicitGroup(specs, section);
    }

    /** @deprecated use overload with mode/search */
    public List<EscHubGroup> groupsForSection(EshSection section) {
        return groupsForSection(section, EshModsListMode.BY_AUTHOR, "");
    }

    private static List<EshWindowSpec> filterSearch(List<EshWindowSpec> specs, String search) {
        if (search == null || search.isBlank()) {
            return specs;
        }
        String q = search.toLowerCase(Locale.ROOT).trim();
        List<EshWindowSpec> out = new ArrayList<>();
        for (EshWindowSpec s : specs) {
            if (s.author().toLowerCase(Locale.ROOT).contains(q)
                || s.title().getString().toLowerCase(Locale.ROOT).contains(q)
                || s.modId().toLowerCase(Locale.ROOT).contains(q)
                || s.subtitle().toLowerCase(Locale.ROOT).contains(q)
                || s.groupTitle().toLowerCase(Locale.ROOT).contains(q)) {
                out.add(s);
            }
        }
        return out;
    }

    private static List<EscHubGroup> groupMods(List<EshWindowSpec> specs, EshModsListMode mode, EshSection section) {
        return switch (mode) {
            case BY_AUTHOR -> groupByAuthor(specs);
            case BY_TITLE -> groupFlatByTitle(specs);
            case BY_GROUP -> groupByExplicitGroup(specs, section);
        };
    }

    private static List<EscHubGroup> groupByAuthor(List<EshWindowSpec> specs) {
        Map<String, List<EshWindowSpec>> buckets = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (EshWindowSpec spec : specs) {
            buckets.computeIfAbsent(spec.author(), k -> new ArrayList<>()).add(spec);
        }
        List<EscHubGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<EshWindowSpec>> e : buckets.entrySet()) {
            List<EshWindowSpec> leaves = new ArrayList<>(e.getValue());
            leaves.sort(Comparator.comparing(s -> s.title().getString(), String.CASE_INSENSITIVE_ORDER));
            // Author rows: studio → ESS badge; else first leaf groupIcon; else first-leaf inherit.
            ResourceLocation groupIcon = isStudioAuthor(e.getKey())
                ? ESS_BADGE
                : firstGroupIcon(leaves);
            groups.add(new EscHubGroup(
                "author:" + e.getKey(),
                e.getKey(),
                toLeaves(leaves),
                groupIcon
            ));
        }
        return groups;
    }

    /** Folder badge for ESS stack groups — leaves themselves stay icon-less (tree branches). */
    private static ResourceLocation essFolderIcon(String groupId, List<EshWindowSpec> leaves) {
        ResourceLocation explicit = firstGroupIcon(leaves);
        if (explicit != null) {
            return explicit;
        }
        return switch (groupId == null ? "" : groupId) {
            case "ess" -> ESS_BADGE;
            case "esl" -> EshModLogoResolver.resolve("extraspeciallib");
            case "esc" -> EshModLogoResolver.resolve("extraspecialcore");
            case "esg" -> esgFolderIcon();
            case "esh" -> EshModLogoResolver.resolve("extraspecialhub");
            default -> leaves.isEmpty() ? ESS_BADGE : EshModLogoResolver.resolve(leaves.get(0).modId());
        };
    }

    private static ResourceLocation esgFolderIcon() {
        ResourceLocation live = EshModLogoResolver.resolveOrNull("extraspecialgui");
        return live != null ? live : ESG_BADGE;
    }

    private static ResourceLocation firstGroupIcon(List<EshWindowSpec> leaves) {
        for (EshWindowSpec s : leaves) {
            if (s.groupIcon() != null) {
                return s.groupIcon();
            }
        }
        return null;
    }

    private static boolean isStudioAuthor(String author) {
        String a = author == null ? "" : author.trim().toLowerCase(Locale.ROOT);
        return a.equals("extra special studio") || a.equals("extraspecialstudio");
    }

    private static List<EscHubGroup> groupFlatByTitle(List<EshWindowSpec> specs) {
        List<EshWindowSpec> sorted = new ArrayList<>(specs);
        sorted.sort(Comparator.comparing(s -> s.title().getString(), String.CASE_INSENSITIVE_ORDER));
        return List.of(new EscHubGroup("all", "All mods (A–Z)", toLeaves(sorted)));
    }

    private static List<EscHubGroup> groupByExplicitGroup(List<EshWindowSpec> specs, EshSection section) {
        boolean treeNav = section == EshSection.ESS;
        Map<String, List<EshWindowSpec>> buckets = new LinkedHashMap<>();
        Map<String, String> titles = new LinkedHashMap<>();
        List<EshWindowSpec> sorted = new ArrayList<>(specs);
        sorted.sort(Comparator
            .comparingInt(EshWindowSpec::sortOrder)
            .thenComparing(s -> s.title().getString(), String.CASE_INSENSITIVE_ORDER));
        for (EshWindowSpec spec : sorted) {
            String gid = spec.groupId().isBlank() ? spec.registryKey() : spec.groupId();
            buckets.computeIfAbsent(gid, k -> new ArrayList<>()).add(spec);
            titles.putIfAbsent(gid, spec.groupId().isBlank() ? spec.title().getString() : spec.groupTitle());
        }
        // Group order = lowest sortOrder in the group (then title). Not A–Z — ESS uses stack order.
        List<Map.Entry<String, List<EshWindowSpec>>> entries = new ArrayList<>(buckets.entrySet());
        entries.sort(Comparator
            .comparingInt((Map.Entry<String, List<EshWindowSpec>> e) ->
                e.getValue().stream().mapToInt(EshWindowSpec::sortOrder).min().orElse(0))
            .thenComparing(e -> titles.get(e.getKey()), String.CASE_INSENSITIVE_ORDER));
        List<EscHubGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<EshWindowSpec>> e : entries) {
            List<EshWindowSpec> leaves = new ArrayList<>(e.getValue());
            leaves.sort(Comparator
                .comparingInt(EshWindowSpec::sortOrder)
                .thenComparing(s -> s.title().getString(), String.CASE_INSENSITIVE_ORDER));
            groups.add(new EscHubGroup(
                e.getKey(),
                titles.get(e.getKey()),
                toLeaves(leaves, treeNav),
                treeNav ? essFolderIcon(e.getKey(), leaves) : firstGroupIcon(leaves)
            ));
        }
        return groups;
    }

    private static List<EscHubLeaf> toLeaves(List<EshWindowSpec> specs) {
        return toLeaves(specs, false);
    }

    private static List<EscHubLeaf> toLeaves(List<EshWindowSpec> specs, boolean treeNav) {
        List<EscHubLeaf> leaves = new ArrayList<>();
        for (EshWindowSpec s : specs) {
            ResourceLocation icon = treeNav
                ? null
                : (s.icon() != null ? s.icon() : EshModLogoResolver.resolve(s.modId()));
            leaves.add(new EscHubLeaf(
                s.registryKey(),
                s.title().getString(),
                s.subtitle(),
                icon,
                s.enabled(),
                s.recommendHint()
            ));
        }
        return leaves;
    }
}
