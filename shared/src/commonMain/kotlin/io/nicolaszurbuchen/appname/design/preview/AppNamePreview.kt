package io.nicolaszurbuchen.appname.design.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nicolaszurbuchen.appname.design.theme.AppNameTheme
import io.nicolaszurbuchen.appname.design.theme.appColors

/**
 * The theme and the ground under it, which every preview needs and none of them should be writing
 * out.
 *
 * **The ground is the part that is easy to forget and expensive to miss.** Compose's preview pane
 * paints its own white behind whatever it renders, so a screen that does not fill its background
 * looks correct in light mode and shows dark-theme text on a white sheet in dark mode — legible
 * enough in the pane to pass a glance, and nothing like what the device does. Painting
 * `appColors.background` here means the dark rendering is dark before a single component draws.
 *
 * Paired with `PreviewThemes` rather than taking a `darkTheme` flag: the annotation sets the ui
 * mode, the theme reads it, and this only has to provide the surface. The annotation lives in
 * `infra/preview/` because it knows nothing about this app; this file cannot follow it there,
 * because it imports the theme and the palette and would invert the layering.
 *
 * **A package of its own rather than `design/component/`.** A component is something a screen
 * draws, and this is never drawn in a shipped screen — filing it beside the real components would
 * offer it to anyone browsing for parts to build a screen from. `PresentationLayerTest` says the
 * same thing mechanically: a component package may not hold a file with a screen suffix, and this
 * one ends in `Preview`.
 */
@Composable
fun AppNamePreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AppNameTheme {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.background)) {
            content()
        }
    }
}
