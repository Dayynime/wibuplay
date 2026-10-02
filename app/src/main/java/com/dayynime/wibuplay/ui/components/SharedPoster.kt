package com.dayynime.wibuplay.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Menyimpan poster yang barusan diklik di Beranda, supaya halaman Detail tahu
 * key shared element mana yang harus dipasangkan (dan URL poster-nya, buat
 * ditampilkan selagi data Detail masih loading).
 */
class PosterTransitionHolder {
    var key: String? = null
    var url: String? = null
}

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalPosterTransitionHolder = compositionLocalOf { PosterTransitionHolder() }

/**
 * Pasang shared element dengan [key]. Kalau key null atau scope belum disediakan
 * (mis. layar lain), modifier ini tidak melakukan apa-apa.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.posterSharedElement(key: String?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalNavAnimatedScope.current
    if (key == null || sharedScope == null || animScope == null) return this
    return with(sharedScope) {
        this@posterSharedElement.sharedElement(
            state = rememberSharedContentState(key = key),
            animatedVisibilityScope = animScope,
            boundsTransform = { _, _ -> spring(dampingRatio = 0.85f, stiffness = 380f) }
        )
    }
}
