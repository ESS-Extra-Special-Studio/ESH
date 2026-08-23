package uk.co.extraspecialstudio.esh.api;

import uk.co.extraspecialstudio.extraspecial.esc.ui.EscOwnsBackNav;

/**
 * Hosted-screen navigation contract for ES Hub.
 * <p>
 * <b>Policy</b>
 * <ul>
 *   <li>{@code INJECT_HUB} (default) — ESH injects a {@code ← Hub} button top-left on every
 *       screen opened from the hub (and children tracked by {@code EshHubSession}).</li>
 *   <li>{@code OWNED} — screen implements {@link EshOwnsHubNav} (or ESC
 *       {@link EscOwnsBackNav}); ESH skips injection. The screen <em>must</em> return to the
 *       hub (prefer {@link EshApi#openHub()}).</li>
 * </ul>
 * Placement of the injected button is best-effort; if chrome overlaps, implement OWNED.
 */
public interface EshOwnsHubNav extends EscOwnsBackNav {
}
