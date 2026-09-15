package uk.co.extraspecialstudio.esh.docs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import uk.co.extraspecialstudio.esh.api.EshApi;
import uk.co.extraspecialstudio.esh.api.EshSection;
import uk.co.extraspecialstudio.esh.api.EshWindowSpec;
import uk.co.extraspecialstudio.esh.client.EshEscPageScreen;
import uk.co.extraspecialstudio.esh.client.EshEscShowcaseScreen;
import uk.co.extraspecialstudio.esh.client.EshThemeEditorScreen;
import uk.co.extraspecialstudio.esh.integration.EsgHubBridge;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscImage;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscRect;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscText;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypeRole;
import uk.co.extraspecialstudio.extraspecial.esc.ui.EscTypography;
import uk.co.extraspecialstudio.extraspecial.esc.ui.anim.EscTransition;

import java.util.List;

/**
 * Built-in ESS wiki + showcase windows for the ESS section.
 * Stack leaves are grouped ESS → ESL → ESC → ESG (optional) → ESH.
 * Reserved slots (ESA / ESN) live on a single Roadmap leaf under ESS — not separate installable pages.
 */
public final class EssDocsWindows {

    private EssDocsWindows() {
    }

    public static Screen openDoc(Screen parent, String title, List<String> lines) {
        return new DocScreen(parent, title, lines, null);
    }

    public static Screen openDoc(Screen parent, String title, List<String> lines, ResourceLocation badge) {
        return new DocScreen(parent, title, lines, badge);
    }

    public static void registerBuiltins() {
        List<String> overviewLines = List.of(
            "One sentence",
            "ESC is the GUI OS, ESH is the start menu, ESL is libc, ESG is the optional IDE.",
            "",
            "What it is",
            "Extra Special Studio (ESS) is the umbrella for our Minecraft platform mods.",
            "JAR / modIds stay long (extraspeciallib, extraspecialcore, extraspecialhub, extraspecialgui).",
            "APIs and hub labels use shorthand: ESL, ESC, ESH, ESG.",
            "",
            "How to use it (players)",
            "Press F9 → Extra Special Hub.",
            "ESS — studio docs and demos.",
            "Hermes / Aegis — Utility leaves when those mods are installed.",
            "Utility — ops tools (CTL, WhatLIB?, …) with search / filter.",
            "Mods — menus other authors register (search / filter).",
            "Footer — Layout, colours, Q, Motion, BG, Close (this PC only unless pack locks theme).",
            "",
            "Install",
            "Runtime UIs need ESL + ESC. F9 needs ESH. ESG is optional (authors). WhatLIB? is optional Utility.",
            "",
            "Coming later",
            "See Roadmap under ESS — ESA / ESN are reserved slots, not shipping mods.",
            "",
            "Developers: open For Mod Authors for Esl* / Esc* / Esh* / Esg* methods."
        );
        EshApi.registerModWindow(EshWindowSpec.builder("extraspecialhub", "overview", "ESS Overview")
            .section(EshSection.ESS)
            .author("Extra Special Studio")
            .group("ess", "ESS")
            .subtitle("What the stack is")
            .sortOrder(0)
            .factory(parent -> openDoc(parent, "ESS Overview", overviewLines))
            .build());

        register("esl", "ESL", "Backend foundation", "esl", "ESL", 10, List.of(
            "What it is",
            "ESL — Extra Special LIB (modId: extraspeciallib).",
            "The shared backend foundation for the ES stack. No GUI, no screens, no render loops.",
            "",
            "What it does",
            "Gives ESC, ESH, and other studio mods common helpers: named registries, Forge config builders, mod-list checks, and small string utilities — so every layer does not reinvent the same glue.",
            "",
            "How to use it (players)",
            "You do not open ESL. Install it as a dependency when you run ESC / ESH / other ES mods. There is nothing to configure day-to-day.",
            "",
            "How to use it (developers)",
            "Depend on extraspeciallib. Import uk.co.extraspecialstudio.esl.* types. Call ESL from common or client code; never put screens or draw calls here.",
            "",
            "Methods",
            "EslRegistry<T>",
            "  register(id, value) — add a named entry (fails if duplicate)",
            "  registerOrReplace(id, value) — upsert",
            "  get(id) — Optional lookup",
            "  contains(id) · values() · keys()",
            "  clear() — wipe (tests / reload)",
            "",
            "EslConfig",
            "  begin(rootComment) — start a ModConfigSpec.Builder",
            "  push(builder, section, commentLines…) / pop(builder) — nested sections",
            "",
            "EslMods",
            "  isLoaded(modId) — soft dependency check",
            "  getDisplayName(modId) — Optional display name",
            "  runIfLoaded(modId, runnable) — run only when present",
            "  acceptIfLoaded(modId, consumer, value) — same, with a value",
            "",
            "EslStrings",
            "  isBlank(s) · nullToEmpty(s) · emptyToNull(s)"
        ));

        register("esc", "ESC", "GUI framework", "esc", "ESC", 20, List.of(
            "What it is",
            "ESC — Extra Special Core (modId: extraspecialcore).",
            "The GUI / presentation framework for Extra Special Studio. Layouts, panels, buttons, themes, sound cues, transitions, hub layout primitives.",
            "",
            "What it does",
            "Lets studio (and other) mods build consistent screens without each reinventing chrome.",
            "Themes stack: ESC defaults → pack/server → per-application (modId) → player personal colours (hub-scoped by default, or all ESC if you allow it).",
            "Quality (Q) and Motion gate expensive FX so players can keep looks or keep FPS.",
            "ESC does not own gameplay rules (waves, loot, radio stations, …).",
            "",
            "How to use it (players)",
            "Open ESH (F9). Use the footer: Colours, Border / Text / Accent, Q, Motion, BG.",
            "ESS → ESC: Showcase, Quality & Motion. ESS → ESG (optional): GUI Builder, Asset Library, Profiles when extraspecialgui is installed.",
            "Try ESC Showcase under the ESC group, and ESH Theme Tuner under ESH, for live frames / skins / hub colours.",
            "See Quality & Motion for what each Q / Motion / BG setting actually turns on.",
            "",
            "How to use it (developers)",
            "Depend on extraspecialcore (+ extraspeciallib). Build screens with EscScreen and Esc* helpers. Pass your modId into theme resolve so player “Hub only” colours do not leak into unrelated UIs.",
            "Declarative GUIs: save JSON profiles under config/extraspecialcore/gui-profiles/<modid>/ or assets/<modid>/esc/gui/. Open at runtime with EscGui.open(parent, \"mymod:settings\").bind(...).action(...).open().",
            "Visual authoring: install optional ESG (extraspecialgui). Use ESS → ESG → GUI Builder (live preview, same ESC bricks as runtime).",
            "",
            "Methods",
            "Screens & chrome",
            "  EscScreen — base screen with EscUiStyle, content rect, addButton / addSearchBox",
            "  EscPanel.renderPanel / fill / border — panels and geometry",
            "  EscButtons / EscText / EscTypography / EscCard / EscImage — widgets & type",
            "  EscSound.play(EscUiCue.*) — UI cues",
            "",
            "Themes",
            "  EscUiStyle.active() / active(applicationId) — current style",
            "  EscThemeManager.resolve(applicationId) — full resolved theme + flags",
            "  EscThemeManager.style(applicationId) — same as UiStyle from theme",
            "  EscThemeManager.registerApplicationTheme(id, theme) — soft per-mod skin",
            "  EscThemeManager.registerApplicationPartial(id, partial) — overlay tokens",
            "  EscThemeManager.canPlayerCustomize() — false when pack locks players",
            "",
            "Budget",
            "  EscVisualBudget.dynamicBackground() / glow / crt / animatedBorders / motionScale",
            "  Respect these before drawing expensive custom FX.",
            "",
            "Hub layouts (used by ESH)",
            "  EscHubLayout / EscHubLayouts.create(id) / EscHubLayoutContext",
            "  EscAccordion — nested group/leaf lists",
            "",
            "GUI profiles (declarative)",
            "  EscGui.open(parent, profileId).bind(key, value).action(id, runnable).open()",
            "  EscGuiProfile / EscGuiProfileStore — JSON load/save/list (runtime in ESC)",
            "  EscAssetRegistry — registered ESC bricks (esc:header, esc:panel, …)",
            "  ESG (extraspecialgui) — optional authoring: GUI Builder, Asset Library, Profiles"
        ));

        register("esg", "ESG", "Visual GUI authoring", "esg", "ESG", 26, List.of(
            "What it is",
            "ESG — Extra Special GUI (modId: extraspecialgui).",
            "Optional visual authoring layer for ESC. How you build GUIs — not required to play mods that use ESC profiles.",
            "",
            "What it does",
            "In-game GUI Builder with live preview, Asset Library, and Profiles manager.",
            "Uses the same ESC renderer and profile format as runtime — edit what players actually see.",
            "",
            "How to use it",
            "Install extraspecialgui alongside extraspecialcore (+ extraspecialhub for F9 access).",
            "F9 → ESS → ESG → GUI Builder | Asset Library | Profiles.",
            "Without ESG installed, ESC runtime and EscGui.open() still work from JSON profiles.",
            "",
            "Dependency rule",
            "ESG → ESC (required). ESH discovers ESG when present. ESC never depends on ESG or ESH."
        ));

        register("quality_motion", "Quality & Motion", "Q / Motion / BG", "esc", "ESC", 21, List.of(
            "What it is",
            "Client visual budget knobs on the ESH footer. They change how much ESC draws — not your layout, colours, or which menus exist.",
            "",
            "What it does",
            "Q (quality) sets the effect ceiling. Motion softens or freezes animation for comfort. BG picks an animated backdrop style when the budget allows it.",
            "",
            "How to use it",
            "Q: OFF — no extras.",
            "Q: LOW — light chrome only.",
            "Q: MEDIUM — shadows, glow, CRT, border sweeps (cheaper than HIGH).",
            "Q: HIGH — full FX, including animated backdrop when Motion is ON.",
            "",
            "Motion ON — full motion + backdrop / border animation.",
            "Motion OFF — comfort mode; freezes backdrop and softens transitions.",
            "",
            "BG styles: Grid, Scan, Rain, Pulse, Sparks, Matrix, Ocean, Clouds, Aurora, Ember, Stars, Bolt.",
            "Animated backdrop only runs at Q HIGH + Motion ON.",
            "Pressing BG: cycles style and bumps Q to HIGH so the backdrop draws; Motion stays as you left it.",
            "",
            "ESH Theme Tuner can raise CRT / glow / shadow knobs; Q still decides whether they draw."
        ));

        register("esh", "ESH", "F9 hub layer", "esh", "ESH", 30, List.of(
            "What it is",
            "ESH — Extra Special Hub (modId: extraspecialhub).",
            "The hub layer of ESS: one hotkey that organises studio menus, docs, methods, instructions, and tool hubs — and lets other developers plug their own mod menus into the same place.",
            "It also showcases what ESC can do (layouts, themes, Quality & Motion, Showcase) and hosts ESH Theme Tuner for hub colours.",
            "",
            "What it does",
            "Owns F9 only. Hosts sectioned windows: ESS (studio wiki + ESC tools), Utility (ops / power-user tools — search / filter; grey badges recommend missing studio tools), Mods (gameplay and settings menus — search / filter).",
            "Feature mods still own their screens; ESH only lists them and opens the factory you register.",
            "",
            "What does NOT belong here",
            "Gameplay item GUIs — Dead Air walkies, Pip-Boy, RadioTowers airdrop / panel screens, scrapbooks, combat HUDs, first-join carousels. Those stay on their own items / keys.",
            "",
            "How to use it (players)",
            "F9 — open / close the hub.",
            "Arrows / WASD — move focus. Enter — open. Footer — Layout, Colours, Q, Motion, BG, Close.",
            "ESS — read these docs; open ESC Showcase / ESH Theme Tuner.",
            "Mods — find another mod’s menu (search / filter on that tab).",
            "Hermes and Aegis open under Utility when those mods are installed.",
            "Utility — ops tools (search / filter); studio + third-party utility mods.",
            "",
            "How to use it (developers)",
            "Depend on extraspecialhub (and usually extraspecialcore + extraspeciallib).",
            "On client setup, build an EshWindowSpec and call EshApi.registerModWindow(...).",
            "Gameplay / settings menus → EshSection.MODS (Mods).",
            "Ops / power-user tools → EshSection.UTILITY (same search / Author·Title·Group filter as Mods).",
            "Studio docs → ESS. Hermes / Aegis / CTL / WhatLIB → UTILITY.",
            "Register each mod’s own EshWindowSpec leaf under ESS, UTILITY, or MODS.",
            "ESH injects a ← Hub button on every screen opened from the hub (and their children). Prefer EshApi.openHub() over setScreen(null) when leaving an ESH-hosted menu.",
            "Already have your own back control? Implement EshOwnsHubNav on the screen and ESH skips its ← Hub button (make sure yours really returns to the hub).",
            "ESC-only screens can implement EscOwnsBackNav instead — same effect, no ESH dependency.",
            "Optional JSON windows: config/extraspecialhub/windows/*.json",
            "",
            "Methods",
            "EshApi",
            "  registerModWindow(EshWindowSpec spec) — one-line registration into the hub",
            "  openHub() — reopen Extra Special Hub from a hosted menu (client)",
            "",
            "EshWindowSpec.builder(modId, windowId, title)",
            "  .section(EshSection.MODS | ESS | UTILITY)",
            "  .author(\"Your Name\") — Mods / Utility grouping / search",
            "  .group(groupId, groupTitle) — accordion group under the section",
            "  .subtitle(\"short blurb\")",
            "  .sortOrder(int) — lower sorts earlier",
            "  .icon(ResourceLocation) — optional; else ESH tries your mod logo",
            "  .factory(parent -> new YourScreen(parent)) — required; returns the Screen to open",
            "  .build()",
            "",
            "EshSection — ESS, UTILITY, MODS",
            "",
            "Example (Mods)",
            "EshApi.registerModWindow(",
            "  EshWindowSpec.builder(\"yourmod\", \"settings\", \"Your Mod Settings\")",
            "    .section(EshSection.MODS)",
            "    .author(\"You\")",
            "    .group(\"yourmod\", \"Your Mod\")",
            "    .subtitle(\"Options & help\")",
            "    .factory(parent -> new YourSettingsScreen(parent))",
            "    .build());",
            "",
            "Example (Utility tool)",
            "EshApi.registerModWindow(",
            "  EshWindowSpec.builder(\"yourtool\", \"panel\", \"Your Tool\")",
            "    .section(EshSection.UTILITY)",
            "    .author(\"You\")",
            "    .group(\"yourtool\", \"Your Tool\")",
            "    .subtitle(\"Ops / diagnostics\")",
            "    .factory(parent -> new YourToolScreen(parent))",
            "    .build());"
        ));

        register("integrators", "For Mod Authors", "Plug into the stack", "ess", "ESS", 1, List.of(
            "What this page is",
            "Short integrator guide: how another mod uses ESL / ESC / ESH without fighting studio naming.",
            "",
            "Depend on what you need",
            "extraspeciallib — shared backend helpers (Esl*).",
            "extraspecialcore — build EscScreen UIs, themes, budget (Esc*).",
            "extraspecialhub — list a menu under F9 (Esh*).",
            "",
            "Naming rule",
            "Public types and methods use shorthand prefixes only: Esl*, Esc*, Esh*, Esg*.",
            "Do not invent ExtraSpecialCoreApi, ExtraSpecialHub.registerWindow, or similar long product-name APIs.",
            "JAR / Maven / modId strings stay long (extraspecialcore, extraspecialgui, …).",
            "",
            "Typical path",
            "1) Optional: EslMods.isLoaded / EslRegistry for soft links.",
            "2) Build your screen with EscScreen + EscUiStyle.active(\"yourmod\").",
            "   Starter pattern: ESC_SCREEN_COOKBOOK.md (Desktop ExtraSpecialCore/docs). Pitfalls: ESC_PITFALLS.md.",
            "3) Register it: EshApi.registerModWindow(EshWindowSpec.builder(...).section(EshSection.MODS).factory(...).build()).",
            "4) Respect EscVisualBudget before expensive FX.",
            "5) Own Back on ESH-hosted screens → EscOwnsBackNav / EshOwnsHubNav.",
            "",
            "Section etiquette",
            "Mods (EshSection.MODS) — third-party author menus (search / Author·Title·Group filter).",
            "Utility (EshSection.UTILITY) — ops / power-user tools; same search / filter chrome as Mods.",
            "ESS — Extra Special Studio curated docs/demos only; do not park random gameplay GUIs there.",
            "Recommended Utility stubs (CTL / WhatLIB) are owned by ESH when those mods are missing.",
            "",
            "JSON alternative",
            "Drop window descriptors in config/extraspecialhub/windows/*.json when you only need a simple open target.",
            "",
            "See the ESL, ESC, and ESH pages for full method lists."
        ));

        EshApi.registerModWindow(EshWindowSpec.builder("extraspecialhub", "esc_showcase", "ESC Showcase")
            .section(EshSection.ESS)
            .author("Extra Special Studio")
            .group("esc", "ESC")
            .subtitle("Frames · focus · skins · effects")
            .sortOrder(22)
            .factory(EshEscShowcaseScreen::new)
            .build());
        EsgHubBridge.registerIfPresent();
        EshApi.registerModWindow(EshWindowSpec.builder("extraspecialhub", "theme_tuner", "ESH Theme Tuner")
            .section(EshSection.ESS)
            .author("Extra Special Studio")
            .group("esh", "ESH")
            .subtitle("Live ESH look · powered by ESC")
            .sortOrder(31)
            .factory(EshThemeEditorScreen::new)
            .build());

        // Reserved slots only — one Roadmap leaf, not separate ESA/ESN installable pages.
        register("roadmap", "Roadmap", "ESA · ESN · reserved slots", "ess", "ESS", 2, List.of(
            "Status",
            "Shipped: ESL → ESC → { ESH, ESG }.",
            "Reserved (not shipping): ESA, ESN.",
            "",
            "ESA — Extra Special Anchor",
            "Planned model-attach tooling. Working id extraspecialanchor. Docs/design only — no Gradle project, no public API.",
            "Do not invent ESA features in other mods.",
            "",
            "ESN",
            "Name reserved; role undefined. Do not invent features, APIs, or gameplay.",
            "",
            "Rule",
            "These are documentation slots, not backlog items. Nothing here is installable yet."
        ));
    }

    private static void register(String id, String title, String subtitle, String groupId, String groupTitle, int sort, List<String> lines) {
        register(id, title, subtitle, groupId, groupTitle, sort, null, lines);
    }

    /**
     * @param icon explicit leaf badge, or null to auto-resolve from the owning modId.
     *             ESS docs are all owned by extraspecialhub, so stack pages pass their own logo.
     */
    private static void register(String id, String title, String subtitle, String groupId, String groupTitle,
                                int sort, ResourceLocation icon, List<String> lines) {
        EshWindowSpec.Builder b = EshWindowSpec.builder("extraspecialhub", id, title)
            .section(EshSection.ESS)
            .author("Extra Special Studio")
            .group(groupId, groupTitle)
            .subtitle(subtitle)
            .sortOrder(sort)
            .factory(parent -> openDoc(parent, title, lines));
        if (icon != null) {
            b.icon(icon);
        }
        EshApi.registerModWindow(b.build());
    }

    private static final class DocScreen extends EshEscPageScreen {
        private static final int BADGE_SIZE = 64;
        private static final int BADGE_GAP = 8;
        private final List<String> lines;
        private final ResourceLocation badge;
        private int scrollPx;
        private int cachedMaxW = -1;
        private int cachedContentH = -1;
        private List<List<String>> cachedWrapped = List.of();

        private DocScreen(Screen parent, String title, List<String> lines, ResourceLocation badge) {
            super(EscText.literal(title), parent, EscTransition.TERMINAL_BOOT);
            this.lines = lines;
            this.badge = badge;
        }

        private int badgeAreaH() {
            return badge == null ? 0 : BADGE_SIZE + BADGE_GAP;
        }

        @Override
        protected void renderPage(GuiGraphics g, EscRect panel, float alpha) {
            int pad = 12;
            int maxW = panel.width() - pad * 2;
            int hintH = font.lineHeight + 6;
            int clipTop = panel.y() + pad;
            int clipBottom = panel.bottom() - pad;
            boolean showHint = scrollPx > 0;
            if (showHint) {
                clipBottom -= hintH;
            }
            int viewH = Math.max(1, clipBottom - clipTop);
            ensureWrapCache(maxW);
            int contentH = cachedContentH;
            int maxScroll = Math.max(0, contentH - viewH);
            scrollPx = Math.max(0, Math.min(scrollPx, maxScroll));

            int x = panel.x() + pad;
            int y = clipTop - scrollPx;
            g.enableScissor(panel.x() + 2, clipTop, panel.right() - 2, clipBottom);
            try {
                if (badge != null) {
                    EscRect box = new EscRect(
                        panel.x() + (panel.width() - BADGE_SIZE) / 2, y, BADGE_SIZE, BADGE_SIZE);
                    EscImage.drawContained(g, badge, box, BADGE_SIZE, BADGE_SIZE, 0xFFFFFF, alpha);
                    y += badgeAreaH();
                }
                EscTypography.draw(g, font, getTitle().getString(),
                    x, y, style, EscTypeRole.SUBTITLE, maxW, alpha);
                y += font.lineHeight + 4;
                int rule = Math.min(maxW, 160);
                int accent = (Math.round(200 * alpha) << 24) | (style.accentColor() & 0xFFFFFF);
                g.fill(x, y, x + rule, y + 2, accent);
                y += 10;
                int lineH = font.lineHeight + 2;
                for (List<String> wrapped : cachedWrapped) {
                    if (wrapped.isEmpty()) {
                        y += lineH / 2;
                        continue;
                    }
                    for (String w : wrapped) {
                        EscTypography.draw(g, font, w, x, y, style, EscTypeRole.BODY, maxW, alpha);
                        y += lineH;
                    }
                }
            } finally {
                g.disableScissor();
            }
            if (showHint) {
                EscTypography.draw(g, font, "▲ scroll",
                    x, panel.bottom() - font.lineHeight - 4, style, EscTypeRole.META, maxW, alpha * 0.8f);
            }
        }

        private void ensureWrapCache(int maxW) {
            if (cachedMaxW == maxW && cachedContentH >= 0) {
                return;
            }
            int lineH = font.lineHeight + 2;
            int h = font.lineHeight + 4 + 10 + badgeAreaH();
            java.util.ArrayList<List<String>> wrapped = new java.util.ArrayList<>();
            for (String raw : lines) {
                if (raw == null || raw.isBlank()) {
                    wrapped.add(List.of());
                    h += lineH / 2;
                    continue;
                }
                List<String> parts = EscTypography.wrap(font, raw, maxW);
                wrapped.add(parts);
                h += parts.size() * lineH;
            }
            cachedWrapped = wrapped;
            cachedContentH = h;
            cachedMaxW = maxW;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (panelBounds.contains(mouseX, mouseY)) {
                scrollPx = Math.max(0, scrollPx - (int) Math.signum(scrollY) * (font.lineHeight + 2) * 2);
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
    }
}
