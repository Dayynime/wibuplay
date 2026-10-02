package com.dayynime.wibuplay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.PillShape
import com.dayynime.wibuplay.ui.theme.SurfaceCard
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextSecondary
import com.dayynime.wibuplay.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroBanner(
    sliderItems: List<AnimeItem>,
    onItemClick: (AnimeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sliderItems.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { sliderItems.size })

    // Auto-slide: automatically advances every 5 seconds, pauses when user touches/drags
    var isUserInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            isUserInteracting = true
        } else if (isUserInteracting) {
            delay(4000)
            isUserInteracting = false
        }
    }

    LaunchedEffect(pagerState.currentPage, isUserInteracting, sliderItems.size) {
        if (sliderItems.size <= 1) return@LaunchedEffect
        while (!isUserInteracting) {
            delay(5000)
            if (!isUserInteracting && !pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % sliderItems.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // Play Button subtle pulsating pulse (1.0 to 1.06)
    val infiniteTransition = rememberInfiniteTransition(label = "heroPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playPulse"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(CardShape)
                .background(SurfaceCard)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val anime = sliderItems[page]

                // Parallax calculation
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val parallaxX = (pageOffset * 40f)

                // Ken Burns Zoom: smooth animation while page is active
                val isCurrent = pagerState.currentPage == page
                val kenBurnsScale by animateFloatAsState(
                    targetValue = if (isCurrent) 1.06f else 1.0f,
                    animationSpec = tween(durationMillis = 4500, easing = LinearEasing),
                    label = "kenBurns"
                )

                // Resolve image: Cover first, fallback to Poster
                val imageUrl = if (!anime.image_cover.isNullOrBlank()) {
                    anime.getCoverUrl()
                } else {
                    anime.getPosterUrl()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onItemClick(anime) }
                ) {
                    // Anime Hero Image with Parallax & Ken Burns zoom
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = anime.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = kenBurnsScale
                                scaleY = kenBurnsScale
                                translationX = parallaxX
                            }
                    )

                    // Cinematic multilayer gradient: dark on bottom and left, readable over any background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x111E1B2E),
                                        Color(0x551E1B2E),
                                        Color(0xDD1E1B2E),
                                        Color(0xFF1E1B2E)
                                    ),
                                    startY = 0f,
                                    endY = 620f
                                )
                            )
                    )

                    // Left-side subtle vignette gradient for extra contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xAA1E1B2E),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 400f
                                )
                            )
                    )

                    // Text Content Container: Staggered entry for title and chips
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.72f)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // Chips row: Type, Year, and Genres (max 3)
                        var showMeta by remember(page) { mutableStateOf(false) }
                        LaunchedEffect(page, isCurrent) {
                            if (isCurrent) {
                                delay(80)
                                showMeta = true
                            } else {
                                showMeta = false
                            }
                        }

                        AnimatedVisibility(
                            visible = showMeta,
                            enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 2 }
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Type Chip
                                if (!anime.type.isNullOrBlank() && !anime.type.equals("NONE", true)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AccentViolet.copy(alpha = 0.25f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = anime.type,
                                            color = AccentViolet,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Year
                                if (!anime.year.isNullOrBlank() && !anime.year.equals("NONE", true)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SurfaceDark.copy(alpha = 0.6f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = anime.year,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Genre chips (max 3)
                                val genres = anime.genre?.split(",")?.map { it.trim() }
                                    ?.filter { it.isNotBlank() && !it.equals("NONE", true) }
                                    ?.take(3) ?: emptyList()

                                genres.forEach { g ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SurfaceDark.copy(alpha = 0.7f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = g,
                                            color = TextWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Anime Real Title (no placeholder like "Anime" or "NONE")
                        val displayTitle = if (!anime.title.isNullOrBlank() &&
                            !anime.title.equals("NONE", true) &&
                            !anime.title.equals("Anime", true)
                        ) {
                            anime.title
                        } else {
                            ""
                        }

                        if (displayTitle.isNotBlank()) {
                            Text(
                                text = displayTitle,
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // Pulsating Violet Play Button in Bottom-Right
                    val playInteractionSource = remember { MutableInteractionSource() }
                    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
                    val buttonScale = if (isPlayPressed) 0.94f else pulseScale

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .scale(buttonScale)
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AccentViolet)
                            .clickable(
                                interactionSource = playInteractionSource,
                                indication = null,
                                onClick = { onItemClick(anime) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Tonton Sekarang",
                            tint = TextWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Animated Dots / Expanding Pill Indicators
        if (sliderItems.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                sliderItems.forEachIndexed { index, _ ->
                    val isSelected = index == pagerState.currentPage
                    val pillWidth by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 6.dp,
                        animationSpec = spring(dampingRatio = 0.75f),
                        label = "pillIndicatorWidth"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = pillWidth, height = 5.dp)
                            .clip(PillShape)
                            .background(
                                if (isSelected) AccentViolet else SurfaceDark
                            )
                    )
                }
            }
        }
    }
}
