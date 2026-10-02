package com.dayynime.wibuplay.ui.screens.explore

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dayynime.wibuplay.ui.components.AnimeCardSkeleton
import com.dayynime.wibuplay.ui.components.AnimePosterCard
import com.dayynime.wibuplay.ui.components.EmptyState
import com.dayynime.wibuplay.ui.components.ErrorState
import com.dayynime.wibuplay.ui.components.GenreChip
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.BottomSheetShape
import com.dayynime.wibuplay.ui.theme.PillShape
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.SurfaceElevated
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextSecondary
import com.dayynime.wibuplay.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onAnimeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    var showFilterSheet by remember { mutableStateOf(false) }

    // Trigger pagination when reaching end of list
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Search Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = {
                    Text(
                        text = "Cari judul anime, genre, dll...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Hapus",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = PillShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    disabledContainerColor = SurfaceDark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = AccentViolet
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Filter button
            val hasActiveFilter = uiState.selectedGenreId != null || uiState.selectedType != null || uiState.selectedYear != null
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(PillShape)
                    .background(if (hasActiveFilter) AccentViolet else SurfaceDark)
                    .clickable { showFilterSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Filter",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Horizontal Quick Filter Chips (Genres & Sort)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sort Views
            item {
                GenreChip(
                    text = "Terpopuler",
                    isSelected = uiState.selectedSort == "views",
                    onClick = { viewModel.onSortSelected("views") }
                )
            }
            // Sort Alphabet
            item {
                GenreChip(
                    text = "A - Z",
                    isSelected = uiState.selectedSort == "alphabet",
                    onClick = { viewModel.onSortSelected("alphabet") }
                )
            }

            // Top genres
            items(uiState.genres.take(15)) { genre ->
                GenreChip(
                    text = genre.name ?: "",
                    isSelected = uiState.selectedGenreId == genre.id,
                    onClick = { viewModel.onGenreSelected(genre.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    ExploreLoadingGrid()
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadNextPage() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.items.isEmpty() -> {
                    EmptyState(
                        title = "Tidak Ada Hasil",
                        subtitle = "Coba kata kunci lain atau ubah filter pencarianmu",
                        icon = Icons.Outlined.SearchOff,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 105.dp),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 135.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.items, key = { it.id ?: it.title.hashCode().toString() }) { anime ->
                            AnimePosterCard(
                                anime = anime,
                                onClick = { anime.id?.let { onAnimeClick(it) } },
                                width = 110.dp
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = AccentViolet,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = SurfaceDark,
            shape = BottomSheetShape
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Pencarian",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Reset",
                        color = AccentViolet,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                viewModel.resetFilters()
                                showFilterSheet = false
                            }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tipe Anime
                Text(
                    text = "Tipe",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                val types = listOf("TV", "Movie", "OVA", "ONA", "Special")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.forEach { type ->
                        GenreChip(
                            text = type,
                            isSelected = uiState.selectedType == type,
                            onClick = {
                                viewModel.onTypeSelected(type)
                                showFilterSheet = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tahun
                Text(
                    text = "Tahun Rilis",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                val years = listOf("2026", "2025", "2024", "2023", "2022", "2021", "2020")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(years) { year ->
                        GenreChip(
                            text = year,
                            isSelected = uiState.selectedYear == year,
                            onClick = {
                                viewModel.onYearSelected(year)
                                showFilterSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreLoadingGrid() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 105.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(9) {
            AnimeCardSkeleton(width = 110.dp)
        }
    }
}
