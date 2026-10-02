package com.dayynime.wibuplay.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.dayynime.wibuplay.data.local.WatchHistoryEntity
import com.dayynime.wibuplay.ui.components.AnimePosterCard
import com.dayynime.wibuplay.ui.components.EmptyState
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.ErrorRed
import com.dayynime.wibuplay.ui.theme.PillShape
import com.dayynime.wibuplay.ui.theme.SurfaceCard
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.SurfaceElevated
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextSecondary
import com.dayynime.wibuplay.ui.theme.TextWhite

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onAnimeClick: (String) -> Unit,
    onWatchEpisode: (movieId: String, episodeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showClearDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val tabs = listOf("Favorit", "Riwayat", "Pengaturan")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // User Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(AccentViolet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Wibu Sejati",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VIP Member",
                            color = AccentViolet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${favorites.size} Favorit • ${watchHistory.size} Ditonton",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Custom Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundDark,
            contentColor = AccentViolet,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AccentViolet,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) TextWhite else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    // Favorit Tab
                    if (favorites.isEmpty()) {
                        EmptyState(
                            title = "Belum Ada Favorit",
                            subtitle = "Tekan tombol '+' pada anime yang kamu sukai untuk menyimpannya di sini",
                            icon = Icons.Outlined.FavoriteBorder,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 105.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 135.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(favorites, key = { it.id }) { fav ->
                                AnimePosterCard(
                                    anime = fav.toAnimeItem(),
                                    onClick = { onAnimeClick(fav.id) },
                                    width = 110.dp
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Riwayat Tab
                    if (watchHistory.isEmpty()) {
                        EmptyState(
                            title = "Riwayat Masih Kosong",
                            subtitle = "Anime yang kamu tonton akan otomatis tercatat dan tersimpan progresnya di sini",
                            icon = Icons.Outlined.History,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${watchHistory.size} Riwayat Tontonan",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Hapus Semua",
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { showClearDialog = true }
                                        .padding(4.dp)
                                )
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 135.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(watchHistory, key = { it.id }) { history ->
                                    WatchHistoryRowItem(
                                        history = history,
                                        onPlay = { onWatchEpisode(history.movieId, history.episodeId) },
                                        onDelete = { viewModel.deleteHistory(history.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Pengaturan Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        item {
                            Text(
                                text = "Penyimpanan & Cache",
                                color = AccentViolet,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            SettingsItem(
                                icon = Icons.Outlined.CleaningServices,
                                title = "Bersihkan Cache Memori",
                                subtitle = "Mengosongkan cache cover dan metadata",
                                onClick = {
                                    Toast.makeText(context, "Cache berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Tentang Aplikasi",
                                color = AccentViolet,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            SettingsItem(
                                icon = Icons.Outlined.Info,
                                title = "Wibuplay v1.0",
                                subtitle = "Aplikasi streaming anime modern & Cuplix",
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for clearing all history
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(text = "Hapus Semua Riwayat?", color = TextWhite)
            },
            text = {
                Text(
                    text = "Semua riwayat tontonan dan posisi pemutaran akan dihapus dari perangkat.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Hapus", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun WatchHistoryRowItem(
    history: WatchHistoryEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceCard)
            .clickable { onPlay() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with progress
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceDark)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(history.moviePoster)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            LinearProgressIndicator(
                progress = { history.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = AccentViolet,
                trackColor = SurfaceElevated
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = history.movieTitle,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val epLabel = if (history.episodeTitle.contains("Episode", ignoreCase = true) ||
                history.episodeTitle.contains("Ep", ignoreCase = true)
            ) {
                history.episodeTitle
            } else {
                "Episode ${history.episodeIndex}"
            }
            val percent = (history.progressFraction * 100).toInt()
            val progressLabel = if (percent > 0) "$epLabel • $percent%" else epLabel

            Text(
                text = progressLabel,
                color = AccentViolet,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Hapus",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceCard)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentViolet,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
