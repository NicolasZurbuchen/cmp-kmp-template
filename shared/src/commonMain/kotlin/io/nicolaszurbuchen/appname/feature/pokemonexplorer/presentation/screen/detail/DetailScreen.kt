package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import appname.shared.generated.resources.Res
import appname.shared.generated.resources.pokemon_detail_back
import appname.shared.generated.resources.pokemon_detail_not_found
import coil3.compose.AsyncImage
import io.nicolaszurbuchen.appname.infra.ui.asString
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.pokemon_detail_back))
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> {
                    DetailSkeleton()
                }

                state.spriteUrl == null -> {
                    Text(stringResource(Res.string.pokemon_detail_not_found))
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AsyncImage(
                            model = state.spriteUrl,
                            contentDescription = state.name,
                            modifier = Modifier.size(200.dp),
                        )
                        Text(text = state.numberText.orEmpty(), style = MaterialTheme.typography.labelLarge)
                        Text(text = state.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
                        Text(text = state.heightText?.asString().orEmpty(), style = MaterialTheme.typography.bodyLarge)
                        Text(text = state.weightText?.asString().orEmpty(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "detailSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
        label = "detailSkeletonAlpha",
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.size(200.dp).clip(CircleShape).background(shimmerColor))
        Spacer(modifier = Modifier.height(16.dp).width(64.dp).clip(RoundedCornerShape(4.dp)).background(shimmerColor))
        Spacer(modifier = Modifier.height(28.dp).width(160.dp).clip(RoundedCornerShape(4.dp)).background(shimmerColor))
        Spacer(modifier = Modifier.height(20.dp).width(120.dp).clip(RoundedCornerShape(4.dp)).background(shimmerColor))
        Spacer(modifier = Modifier.height(20.dp).width(120.dp).clip(RoundedCornerShape(4.dp)).background(shimmerColor))
    }
}
