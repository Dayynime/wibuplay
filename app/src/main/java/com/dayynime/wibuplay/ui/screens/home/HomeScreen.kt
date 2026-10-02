package com.dayynime.wibuplay.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.ui.components.AnimeCardSkeleton
import com.dayynime.wibuplay.ui.components.AnimePosterCard
import com.dayynime.wibuplay.ui.components.AnimeRowSkeleton
import com.dayynime.wibuplay.ui.components.ContinueWatchingCard
import com.dayynime.wibuplay.ui.components.ErrorState
import com.dayynime.wibuplay.ui.components.HeroBanner
import com.dayynime.wibuplay.ui.components.HeroBannerSkeleton
import com.dayynime.wibuplay.ui.components.SectionHeader
import com.dayynime.wibuplay.ui.components.TopHitsRow
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAnimeClick: (String) -> Unit,
    onWatchEpisode: (movieId: String, episodeId: String) -> Unit,
    onSearchClick: () -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()

    // Smooth header collapse/transparency on scroll
    val isScrolled by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 40
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Crossfade(
            targetState = uiState,
            animationSpec = tween(durationMillis = 350),
            label = "homeContentCrossfade"
        ) { state ->
            when (state) {
                is HomeUiState.Loading -> {
                    HomeLoadingSkeleton()
                }
                is HomeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadHomeData(forceRefresh = true) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                is HomeUiState.Success -> {
                    val sections = state.sections

                    // Trigger staggered entrance animation
                    var animateSections by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(50)
                        animateSections = true
                    }

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 135.dp) // Leave enough padding for floating nav
                    ) {
                        // Header Bar: App Name & Search button
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                                    .graphicsLayer {
                                        alpha = if (isScrolled) 0.94f else 1.0f
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Wibu",
                                            color = TextWhite,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Text(
                                            text = "play",
                                            color = AccentViolet,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        )
                                    }
                                    Text(
                                        text = "Streaming Anime Tanpa Batas",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = onSearchClick,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CardShape)
                                        .background(SurfaceDark)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Cari Anime",
                                        tint = TextWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // 1. Hero Carousel (5-6 real anime from hot/popular, no promotional banners)
                        if (sections.slider.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 60) {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                        HeroBanner(
                                            sliderItems = sections.slider,
                                            onItemClick = { item -> item.id?.let { onAnimeClick(it) } }
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Continue Watching (Riwayat Tonton)
                        if (watchHistory.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 120) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SectionHeader(title = "Lanjutkan Menonton")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            items(watchHistory) { history ->
                                                // Clean label without double "Ep 14 • Episode 14"
                                                val epLabel = if (history.episodeTitle.contains("Episode", ignoreCase = true) ||
                                                    history.episodeTitle.contains("Ep", ignoreCase = true)
                                                ) {
                                                    history.episodeTitle
                                                } else {
                                                    "Episode ${history.episodeIndex}"
                                                }
                                                val percent = (history.progressFraction * 100).toInt()
                                                val progressLabel = if (percent > 0) "$epLabel • $percent%" else epLabel

                                                ContinueWatchingCard(
                                                    title = history.movieTitle,
                                                    episodeText = progressLabel,
                                                    posterUrl = history.moviePoster,
                                                    progress = history.progressFraction,
                                                    onClick = { onWatchEpisode(history.movieId, history.episodeId) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Top Hits (Continuous Center-Scaling Carousel)
                        val topHitsList = if (sections.hot.isNotEmpty()) sections.hot else sections.popular
                        if (topHitsList.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 180) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SectionHeader(
                                            title = "Top Hits",
                                            actionText = "Lihat semua",
                                            onActionClick = { onSeeAllClick("hot") }
                                        )
                                        TopHitsRow(
                                            items = topHitsList.take(10),
                                            onItemClick = { anime -> anime.id?.let { onAnimeClick(it) } }
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Favorites (Room data)
                        if (favorites.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 240) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SectionHeader(title = "Favorit Saya")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(favorites) { fav ->
                                                AnimePosterCard(
                                                    anime = fav.toAnimeItem(),
                                                    onClick = { onAnimeClick(fav.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Episode Baru (Update / New)
                        val newEpisodes = if (sections.update.isNotEmpty()) sections.update else sections.newRelease
                        if (newEpisodes.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 300) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SectionHeader(
                                            title = "Episode Baru",
                                            actionText = "Lihat semua",
                                            onActionClick = { onSeeAllClick("new") }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(newEpisodes) { anime ->
                                                AnimePosterCard(
                                                    anime = anime,
                                                    onClick = { anime.id?.let { onAnimeClick(it) } }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Jadwal Hari Ini
                        if (sections.today.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 340) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SectionHeader(title = "Jadwal Hari Ini")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(sections.today) { anime ->
                                                AnimePosterCard(
                                                    anime = anime,
                                                    onClick = { anime.id?.let { onAnimeClick(it) } }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 7. Populer
                        if (sections.popular.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 380) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SectionHeader(
                                            title = "Anime Populer",
                                            actionText = "Lihat semua",
                                            onActionClick = { onSeeAllClick("popular") }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(sections.popular) { anime ->
                                                AnimePosterCard(
                                                    anime = anime,
                                                    onClick = { anime.id?.let { onAnimeClick(it) } }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 8. You May Also Like (Rekomendasi dari random)
                        if (sections.random.isNotEmpty()) {
                            item {
                                StaggeredSection(visible = animateSections, delayMs = 420) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SectionHeader(
                                            title = "Mungkin Kamu Suka",
                                            actionText = "Lihat semua",
                                            onActionClick = { onSeeAllClick("random") }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(sections.random) { anime ->
                                                AnimePosterCard(
                                                    anime = anime,
                                                    onClick = { anime.id?.let { onAnimeClick(it) } }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaggeredSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = delayMs)) +
            slideInVertically(animationSpec = tween(durationMillis = 300, delayMillis = delayMs)) { 35 }
    ) {
        content()
    }
}

@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            HeroBannerSkeleton()
        }
        Spacer(modifier = Modifier.height(24.dp))
        AnimeRowSkeleton()
        Spacer(modifier = Modifier.height(24.dp))
        AnimeRowSkeleton()
    }
}
