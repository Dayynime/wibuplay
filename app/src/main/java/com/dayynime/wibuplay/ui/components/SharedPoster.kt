@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.dayynime.wibuplay.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.memory.MemoryCache

/**
 * Menyimpan poster yang barusan diklik di Beranda, supaya halaman Detail tahu
 * key shared element mana yang dipasangkan, URL poster-nya, dan key memory cache
 * Coil (dipakai sebagai placeholder biar tidak ada kedip pas gambar full dimuat).
 */
class PosterTransitionHolder {
    var key: String? = null
    var url: String? = null
    var cacheKey: MemoryCache.Key? = null
}

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalPosterTransitionHolder = compositionLocalOf { PosterTransitionHolder() }

// Easing "emphasized decelerate" ala Material: cepat di awal, mendarat halus.
private val PosterEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/**
 * Pasang shared element dengan [key]. Kalau key null atau scope belum disediakan
 * (mis. layar lain), modifier ini tidak melakukan apa-apa.
 *
 * Sudut membulat (18dp, sama dengan CardShape) dianimasikan pelan-pelan jadi 0
 * selama gambar membesar, jadi tidak ada "pop" sudut di awal/akhir transisi.
 */
@Composable
fun Modifier.posterSharedElement(key: String?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalNavAnimatedScope.current
    if (key == null || sharedScope == null || animScope == null) return this

    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val cornerClip = remember(screenWidthPx) {
        object : SharedTransitionScope.OverlayClip {
            override fun getClipPath(
                sharedContentState: SharedTransitionScope.SharedContentState,
                bounds: Rect,
                layoutDirection: LayoutDirection,
                density: Density
            ): Path {
                // frac ~0.4 (ukuran kartu) -> 1.0 (layar penuh)
                val frac = (bounds.width / screenWidthPx).coerceIn(0f, 1f)
                val t = ((frac - 0.4f) / 0.6f).coerceIn(0f, 1f)
                val radius = with(density) { 18.dp.toPx() } * (1f - t)
                return Path().apply {
                    addRoundRect(RoundRect(bounds, CornerRadius(radius, radius)))
                }
            }
        }
    }

    return with(sharedScope) {
        this@posterSharedElement.sharedElement(
            state = rememberSharedContentState(key = key),
            animatedVisibilityScope = animScope,
            boundsTransform = { _, _ -> tween(durationMillis = 450, easing = PosterEasing) },
            clipInOverlayDuringTransition = cornerClip
        )
    }
}
