package io.nicolaszurbuchen.appname.infra.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Light and dark, from one preview function — the multipreview pattern.
 *
 * **This is what replaces the two-functions-per-file shape.** The usual habit is to write every
 * preview twice, `FooScreenPreview` and `FooScreenDarkPreview`, identical but for the `darkTheme`
 * argument: a change to the fixtures then has to be made in both, and a preview that has drifted
 * apart from its own dark twin still looks fine in review. One annotated function renders both, and
 * a third rendering — a large font scale, a small screen — becomes one line here rather than one
 * new function per screen.
 *
 * The theme still comes from the system rather than from a parameter: the app theme reads
 * `isSystemInDarkTheme()`, and the tooling sets exactly that from `uiMode`. So the dark rendering is
 * the real dark theme rather than a preview-only override, which is the point of not passing
 * `darkTheme` by hand.
 *
 * **It is `infra/` rather than `design/` because it does not know this app exists.** It sets a
 * system flag; that the theme happens to read the flag is the theme's business, not this file's.
 * Nothing here names a colour, a font or a screen, and the whole annotation would work unchanged in
 * any Compose app — which is the placement rule's definition of plumbing.
 */
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = PreviewUiMode.NIGHT_YES)
annotation class PreviewThemes
