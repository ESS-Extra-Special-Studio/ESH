# Changelog

## 1.0.0

Update by: Extra_Special_K

Added:

- ES Hub (ESH): F9 opens Extra Special Hub on ESC layout profiles (Carousel default; Control Centre, Dashboard, Orbital, Classic).
- Nested window groups via `EshWindowSpec.group(...)` (Pantheon family nests Hub / Aegis / Hermes under MODS).
- `EshApi.registerModWindow` for third-party window registration.
- Built-in ESS docs stubs; `ESHHermesBridge.registerHermesDefault` for Hermes profile defaults.
- Client config `hub.layoutId` + in-hub Layout cycle button.
- Depends on ES Library + Extra Special Core.
- Utility tab shares Other Mods search / Author·Title·Group filter chrome (third-party ops tools plug in with `EshSection.UTILITY`).
- `EshOwnsHubNav` — hosted screens that already have their own way back can opt out of ESH's injected `← Hub` button; ESC's `EscOwnsBackNav` is honoured too, so ESC screens (e.g. the colour picker) opt out without depending on ESH.
- ESS section and stack doc rows carry their own art: ESS badge for ESS / For Mod Authors / Roadmap, ESL and ESC logos on their pages.
- ESA / ESN collapsed into one ESS **Roadmap** leaf (docs slots only).
- "Extra Special Studio" author rows (Utility / Other Mods) show the ESS badge rather than the first mod's logo.
- Hub footer uses `EscButtonBar.fitDense` (two rows when narrow) so labels stay readable.
- `EshWindowSpec.groupIcon` + IconPolicy / NavPolicy documented in studio docs.
- Hub theme knobs clarified as **this-PC client prefs** (`Apply to: ESH` / `Apply to: ALL`); pack/server override can lock colours (`Theme: Pack locked`).
- Fresh install uses ESC **Growth** (ESH green chrome, forest backdrop, Quality HIGH). Tuner presets still cover Vanilla through Ember.
