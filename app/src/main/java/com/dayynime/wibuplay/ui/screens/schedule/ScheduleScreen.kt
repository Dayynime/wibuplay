package com.dayynime.wibuplay.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.ui.components.EmptyState
import com.dayynime.wibuplay.ui.components.ErrorState
import com.dayynime.wibuplay.ui.components.GenreChip
import com.dayynime.wibuplay.ui.components.ShimmerBox
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.SurfaceCard
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextSecondary
import com.dayynime.wibuplay.ui.theme.TextWhite

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onAnimeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Title Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Jadwal Rilis",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Jadwal penayangan episode baru setiap harinya",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // Days Selection Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.days) { day ->
                val isSelected = uiState.selectedDay == day
                GenreChip(
                    text = day,
                    isSelected = isSelected,
                    onClick = { viewModel.onDaySelected(day) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Schedule items list
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    ScheduleLoadingSkeleton()
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadSchedule(uiState.selectedDay) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.items.isEmpty() -> {
                    EmptyState(
                        title = "Tidak Ada Jadwal",
                        subtitle = "Belum ada anime rilis terjadwal pada hari ${uiState.selectedDay}",
                        icon = Icons.Outlined.CalendarToday,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 135.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.items, key = { it.id ?: it.title.hashCode().toString() }) { anime ->
                            ScheduleAnimeCard(
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

@Composable
fun ScheduleAnimeCard(
    anime: AnimeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceCard)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster image
        Box(
            modifier = Modifier
                .width(80.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(anime.getPosterUrl())
                    .crossfade(true)
                    .build(),
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            // Status chip & type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentViolet.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = anime.status ?: "Airing",
                        color = AccentViolet,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!anime.type.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${anime.type}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = anime.title ?: "Anime",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )

            val genre = anime.genre
            if (!genre.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = genre,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!anime.views.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${anime.views} tayangan",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleLoadingSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(SurfaceCard)
                    .padding(10.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(115.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(18.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
    }
}
