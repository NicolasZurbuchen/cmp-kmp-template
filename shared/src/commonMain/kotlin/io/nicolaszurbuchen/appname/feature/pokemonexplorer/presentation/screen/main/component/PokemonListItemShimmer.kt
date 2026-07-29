package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

private val HERO_IMAGE_SIZE = 120.dp
private val ROW_IMAGE_SIZE = 56.dp

@Composable
fun PokemonListItemShimmer(
    modifier: Modifier = Modifier,
    isHero: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "pokemonShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
        label = "pokemonShimmerAlpha",
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(12.dp),
    ) {
        Spacer(
            modifier =
                Modifier
                    .size(if (isHero) HERO_IMAGE_SIZE else ROW_IMAGE_SIZE)
                    .clip(CircleShape)
                    .background(shimmerColor),
        )

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Spacer(
                modifier =
                    Modifier
                        .height(12.dp)
                        .width(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(
                modifier =
                    Modifier
                        .height(if (isHero) 24.dp else 16.dp)
                        .width(if (isHero) 140.dp else 100.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerColor),
            )
        }
    }
}
