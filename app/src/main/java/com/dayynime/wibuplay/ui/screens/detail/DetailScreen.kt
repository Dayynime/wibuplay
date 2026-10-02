package com.dayynime.wibuplay.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dayynime.wibuplay.data.model.EpisodeItem
import com.dayynime.wibuplay.ui.components.EmptyState
import com.dayynime.wibuplay.ui.components.EpisodeListItem
import com.dayynime.wibuplay.ui.components.ErrorState
import com.dayynime.wibuplay.ui.components.GenreChip
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.PillShape
import com.dayynime.wibuplay.ui.theme.SurfaceCard
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.SurfaceElevated
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextSecondary
import com.dayynime.wibuplay.ui.theme.TextWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onWatchEpisode: (movieId: String, episodeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    var isSynopsisExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val tabs = listOf("Ringkasan", "Daftar Episode", "Media & Cuplix")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet)
            }
            return
        }

        if (uiState.error != null && uiState.anime == null) {
            ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.loadDetail() },
                modifier = Modifier.align(Alignment.Center)
            )
            return
        }

        val anime = uiState.anime
        // Poster (portrait) dipakai sebagai latar layar penuh; kalau kosong pakai cover.
        val backdropUrl = anime?.getPosterUrl().orEmpty().ifBlank { anime?.getCoverUrl().orEmpty() }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Tinggi area gambar yang dibiarkan terlihat sebelum konten mulai
            val heroHeight = maxHeight * 0.42f
            val heroHeightPx = with(LocalDensity.current) { heroHeight.toPx() }
            val extraScrim by remember(heroHeightPx) {
                derivedStateOf {
                    if (listState.firstVisibleItemIndex > 0) 1f
                    else (listState.firstVisibleItemScrollOffset / heroHeightPx).coerceIn(0f, 1f)
                }
            }

            // Latar gambar layar penuh
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backdropUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = anime?.title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient: gambar jelas di atas, makin gelap ke bawah supaya teks terbaca
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color(0x331E1B2E),
                                0.40f to Color(0x661E1B2E),
                                0.68f to Color(0xE61E1B2E),
                                1f to BackgroundDark
                            )
                        )
                    )
            )

            // Makin gelap saat konten di-scroll ke atas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = extraScrim * 0.92f }
                    .background(BackgroundDark)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                // Ruang kosong supaya gambar terlihat di atas
                item { Spacer(modifier = Modifier.height(heroHeight)) }

                // Tab (teks kecil, garis bawah di tab aktif)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val selected = uiState.selectedTab == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { viewModel.setTab(index) }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    color = if (selected) TextWhite else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(3.dp)
                                        .width(if (selected) 28.dp else 0.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(AccentViolet)
                                )
                            }
                        }
                    }
                }

                // Judul + chip genre + info singkat
                item {
                    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp)) {
                        Text(
                            text = anime?.title ?: "Detail Anime",
                            color = TextWhite,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        val genres = anime?.genre?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                        if (genres.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                genres.forEach { g -> OutlineChip(text = g) }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!anime?.status.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentViolet)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = anime!!.status!!,
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (!anime?.type.isNullOrBlank()) {
                                Text(
                                    text = anime!!.type!!,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (!anime?.year.isNullOrBlank()) {
                                Text(text = "• ${anime!!.year}", color = TextSecondary, fontSize = 12.sp)
                            }
                            if (!anime?.views.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = anime!!.views!!, color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Tab Content
                when (uiState.selectedTab) {
                    0 -> {
                        // Overview / About Tab
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text(
                                    text = "Tentang",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val synopsisText = anime?.synopsis ?: "Belum ada sinopsis untuk anime ini."
                                Column(modifier = Modifier.animateContentSize()) {
                                    Text(
                                        text = synopsisText,
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (synopsisText.length > 180) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isSynopsisExpanded) "Sembunyikan" else "Baca selengkapnya",
                                            color = AccentViolet,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                                .padding(vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Additional Info Grid
                                Text(
                                    text = "Informasi Tambahan",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                InfoRow(label = "Studio", value = anime?.studio ?: "-")
                                InfoRow(label = "Hari Rilis", value = anime?.day ?: "-")
                                InfoRow(label = "Mulai Tayang", value = anime?.aired_start ?: "-")
                                InfoRow(label = "Selesai Tayang", value = anime?.aired_end ?: "-")
                            }
                        }
                    }
                    1 -> {
                        // List of Episodes Tab
                        item {
                            // Episode Search Bar
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                TextField(
                                    value = uiState.episodeSearch,
                                    onValueChange = { viewModel.onEpisodeSearchChange(it) },
                                    placeholder = {
                                        Text(text = "Cari episode...", color = TextMuted, fontSize = 12.sp)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    singleLine = true,
                                    shape = PillShape,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceDark,
                                        unfocusedContainerColor = SurfaceDark,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                )
                            }
                        }

                        if (uiState.isLoadingEpisodes) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AccentViolet, modifier = Modifier.size(28.dp))
                                }
                            }
                        } else if (uiState.episodes.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "Belum Ada Episode",
                                    subtitle = "Daftar episode belum tersedia atau sedang diperbarui",
                                    icon = Icons.Default.PlayArrow,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        } else {
                            items(uiState.episodes) { ep ->
                                EpisodeListItem(
                                    episode = ep,
                                    onClick = {
                                        ep.id?.let { epId ->
                                            onWatchEpisode(viewModel.movieId, epId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    2 -> {
                        // Media & Cuplix Tab
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                if (uiState.covers.isNotEmpty()) {
                                    Text(
                                        text = "Cover & Poster",
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        uiState.covers.take(3).forEach { media ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(16f / 9f)
                                                    .clip(CardShape)
                                                    .background(SurfaceDark)
                                            ) {
                                                AsyncImage(
                                                    model = media.getFullImageUrl(),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                if (uiState.cuplix.isNotEmpty()) {
                                    Text(
                                        text = "Cuplix Terkait",
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    uiState.cuplix.forEach { clip ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(CardShape)
                                                .background(SurfaceCard)
                                                .clickable {
                                                    clip.id_episode?.let { epId ->
                                                        onWatchEpisode(viewModel.movieId, epId)
                                                    }
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SurfaceDark)
                                            ) {
                                                AsyncImage(
                                                    model = clip.getThumbnailUrl(),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = clip.caption ?: "Klip Anime",
                                                    color = TextWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${clip.count_views ?: "0"} views • ${clip.count_likes ?: "0"} suka",
                                                    color = TextMuted,
                                                    fontSize = 11.sp
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

        // Tombol Back (tetap di atas gambar)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x881E1B2E))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Kembali",
                tint = TextWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        // Bar bawah: tombol (+) bulat outline di kiri, pill putih "Tonton Sekarang" di kanan
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xE61E1B2E), BackgroundDark)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .then(
                        if (isFavorite) Modifier.background(AccentViolet)
                        else Modifier.border(1.5.dp, Color(0xCCFFFFFF), CircleShape)
                    )
                    .clickable { viewModel.toggleFavorite() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = "Favorit",
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Button(
                onClick = {
                    val firstEp = uiState.episodes.firstOrNull()?.id ?: ""
                    onWatchEpisode(viewModel.movieId, firstEp)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextWhite,
                    contentColor = BackgroundDark
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tonton Sekarang",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BackgroundDark
                )
            }
        }
    }
}

@Composable
private fun OutlineChip(text: String) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .border(1.dp, Color(0x99FFFFFF), PillShape)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

