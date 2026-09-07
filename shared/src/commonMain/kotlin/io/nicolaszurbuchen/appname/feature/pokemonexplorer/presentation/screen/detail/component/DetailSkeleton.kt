package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.appname.design.theme.shimmerBlock

/**
 * The detail page's silhouette: the sprite, the number, the name, and the two facts under it — the
 * same five things in the same order and at the same sizes as the loaded screen.
 *
 * It is a file of its own rather than a private function at the bottom of `DetailScreen`, because a
 * skeleton is a second rendering of the same layout and the two drift apart the moment one of them
 * is easier to reach than the other.
 */
@Composable
fun DetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BLOCK_GAP),
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.size(SPRITE_SIZE).shimmerBlock(CircleShape))
        Spacer(modifier = Modifier.height(NUMBER_HEIGHT).width(NUMBER_WIDTH).shimmerBlock())
        Spacer(modifier = Modifier.height(NAME_HEIGHT).width(NAME_WIDTH).shimmerBlock())
        Spacer(modifier = Modifier.height(FACT_HEIGHT).width(FACT_WIDTH).shimmerBlock())
        Spacer(modifier = Modifier.height(FACT_HEIGHT).width(FACT_WIDTH).shimmerBlock())
    }
}

private val BLOCK_GAP = 8.dp

private val SPRITE_SIZE = 200.dp

private val NUMBER_HEIGHT = 16.dp
private val NUMBER_WIDTH = 64.dp

private val NAME_HEIGHT = 28.dp
private val NAME_WIDTH = 160.dp

private val FACT_HEIGHT = 20.dp
private val FACT_WIDTH = 120.dp
