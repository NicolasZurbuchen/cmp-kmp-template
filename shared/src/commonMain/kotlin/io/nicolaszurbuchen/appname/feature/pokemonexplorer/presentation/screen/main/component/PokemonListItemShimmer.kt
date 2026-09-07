package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nicolaszurbuchen.appname.design.theme.shimmerBlock

@Composable
fun PokemonListItemShimmer(
    modifier: Modifier = Modifier,
    isHero: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(ROW_PADDING),
    ) {
        Spacer(
            modifier =
                Modifier
                    .size(if (isHero) HERO_IMAGE_SIZE else ROW_IMAGE_SIZE)
                    .shimmerBlock(CircleShape),
        )

        Column(modifier = Modifier.padding(start = ROW_PADDING)) {
            Spacer(modifier = Modifier.height(NUMBER_HEIGHT).width(NUMBER_WIDTH).shimmerBlock())

            Spacer(modifier = Modifier.height(LINE_GAP))

            Spacer(
                modifier =
                    Modifier
                        .height(if (isHero) HERO_NAME_HEIGHT else ROW_NAME_HEIGHT)
                        .width(if (isHero) HERO_NAME_WIDTH else ROW_NAME_WIDTH)
                        .shimmerBlock(),
            )
        }
    }
}

private val ROW_PADDING = 12.dp
private val LINE_GAP = 8.dp

private val HERO_IMAGE_SIZE = 120.dp
private val ROW_IMAGE_SIZE = 56.dp

private val NUMBER_HEIGHT = 12.dp
private val NUMBER_WIDTH = 48.dp

private val HERO_NAME_HEIGHT = 24.dp
private val HERO_NAME_WIDTH = 140.dp
private val ROW_NAME_HEIGHT = 16.dp
private val ROW_NAME_WIDTH = 100.dp
