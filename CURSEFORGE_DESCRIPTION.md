# CurseForge copy — ExtraSpecialHub (ESH)

## Short summary

Extra Special Studio presents ExtraSpecialHub — one key for studio menus, a uniform ESC layout, a showcase of what ESC (and soon ESG) can do, and a hook for other mods. Requires ExtraSpecialCore and ExtraSpecialLIB.

## Main page

Extra Special Studio presents ExtraSpecialHub
One key for studio menus, a uniform ESC layout, a showcase of what ESC (and soon ESG) can do, and a hook for other mods.

Mostly built to put all of Extra Special Studio’s menus and studio stuff under one key (F9) and give them a uniform layout, instead of a pile of mismatched screens and hotkeys. ExtraSpecialHub (ESH) is the studio start menu, not a content pack—it doesn’t add items, biomes, or a gameplay loop. Gameplay item UIs stay on their items.

What it does:
Press F9. Studio docs, ESC demos, Theme Tuner, and other Extra Special Studio tools live in one hub. Layout profiles and colours come from ESC, so everything in the hub looks like it belongs together. Hosted menus get a way back to the hub so you are not dumped into the world. It also shows off what’s possible with ExtraSpecialCore (ESC) — themes, layouts, motion, chrome — and ExtraSpecialGUI (ESG), which is coming soon for authoring those screens.

How it works:
ESH lists windows other mods register and opens the screen they supply. Layout and theme chrome come from ESC; ESH just hosts them under F9. Third-party mods hook in with `EshApi.registerModWindow` on client setup. Use `EshSection.MODS` (Other Mods) for third-party menus, or `EshSection.UTILITY` for ops tools.

How to use it:
Players: Install ExtraSpecialHub with matching ExtraSpecialCore (ESC) and ExtraSpecialLIB (ESL) for your loader (Forge / NeoForge are separate). Press F9 in-game. If a mod’s page lists ESH, you want the hub for that mod’s menu as well as studio docs.
Authors: Depend on extraspecialhub (plus extraspecialcore and extraspeciallib). On client setup, register a window — ESH lists it and opens the screen you supply.

```
EshApi.registerModWindow(EshWindowSpec.builder(
        "yourmodid",
        "your_window",
        "Your Menu Title"
    )
    .section(EshSection.MODS)
    .author("Your name")
    .factory(parent -> new YourScreen(parent))
    .build());
```

Optional: `.group("family", "Family name")` to nest related windows, `.subtitle("…")`, `.icon(...)`. Prefer `EshApi.openHub()` when leaving a hosted screen. If you already have your own Back control, implement `EshOwnsHubNav` (or ESC’s `EscOwnsBackNav`) so ESH does not inject ← Hub.

Important:
ESH is a hub, not a standalone content pack. Match Minecraft + loader with ESC and ESL (Forge and NeoForge variants are separate if published that way). If F9 does nothing, confirm ESH is on the client and ESC/ESL versions match the pack. License is All Rights Reserved.
