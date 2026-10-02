package com.dayynime.wibuplay.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.SurfaceElevated

@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    showShimmer: Boolean = true
): Brush {
    if (!showShimmer) {
        return Brush.linearGradient(
            colors = listOf(SurfaceDark, SurfaceDark)
        )
    }
    val shimmerColors = listOf(
        SurfaceDark.copy(alpha = 0.9f),
        SurfaceElevated.copy(alpha = 0.6f),
        SurfaceDark.copy(alpha = 0.9f),
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun AnimeCardSkeleton(
    modifier: Modifier = Modifier,
    width: Dp = 140.dp
) {
    Column(
        modifier = modifier.width(width)
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
            shape = CardShape
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(14.dp),
            shape = RoundedCornerShape(4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun HeroBannerSkeleton(
    modifier: Modifier = Modifier
) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = CardShape
    )
}

@Composable
fun AnimeRowSkeleton(
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            AnimeCardSkeleton()
        }
    }
}
