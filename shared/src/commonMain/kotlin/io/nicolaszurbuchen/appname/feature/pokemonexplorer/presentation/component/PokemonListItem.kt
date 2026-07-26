package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

private val HERO_IMAGE_SIZE = 120.dp
private val ROW_IMAGE_SIZE = 56.dp

@Composable
fun PokemonListItem(
    numberText: String,
    name: String,
    spriteUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHero: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .padding(12.dp),
    ) {
        AsyncImage(
            model = spriteUrl,
            contentDescription = name,
            modifier = Modifier.size(if (isHero) HERO_IMAGE_SIZE else ROW_IMAGE_SIZE),
        )

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = numberText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = name,
                style = if (isHero) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
