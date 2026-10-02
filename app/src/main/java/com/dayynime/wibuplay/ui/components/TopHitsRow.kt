package com.dayynime.wibuplay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.SurfaceCard
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextWhite
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun TopHitsRow(
    items: List<AnimeItem>,
    onItemClick: (AnimeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    // Mulai dari poster tengah (mis. #5 dari 10) supaya kiri & kanan sama-sama terisi.
    val startIndex = (items.size - 1) / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()

    // Determine the index closest to the viewport center
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) 0
            else {
                visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2f
                    abs(viewportCenter - itemCenter)
                }?.index ?: 0
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val screenWidth = maxWidth
        val itemBaseWidth = 148.dp
        // Content padding so that the first and last items can be placed exactly in the center
        val horizontalPadding = ((screenWidth - itemBaseWidth) / 2).coerceAtLeast(16.dp)

        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(295.dp), // Ample height so scaled-up posters won't be clipped
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(items) { index, anime ->
                val isFocused = index == centerIndex

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.65f),
                    label = "pressScale"
                )

                // Animated title color & underline width
                val titleColor by animateColorAsState(
                    targetValue = if (isFocused) TextWhite else TextMuted.copy(alpha = 0.55f),
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "titleColor"
                )

                val underlineWidth by animateDpAsState(
                    targetValue = if (isFocused) 36.dp else 0.dp,
                    animationSpec = spring(dampingRatio = 0.7f),
                    label = "underlineWidth"
                )

                val badgeColor by animateColorAsState(
                    targetValue = if (isFocused) AccentViolet else Color(0xAA1E1B2E),
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "badgeColor"
                )

                Column(
                    modifier = Modifier
                        .width(itemBaseWidth)
                        .scale(pressScale)
                        .graphicsLayer {
                            // Compute dynamic scale, alpha, and vertical elevation relative to center
                            val layoutInfo = listState.layoutInfo
                            val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                            if (itemInfo != null) {
                                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                val distanceFromCenter = abs(viewportCenter - itemCenter)
                                val maxDistance = itemInfo.size * 1.5f
                                val fraction = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)

                                // Scale from 1.0 down to 0.82
                                val dynScale = 1.0f - (fraction * 0.18f)
                                scaleX = dynScale
                                scaleY = dynScale

                                // Alpha from 1.0 down to 0.65
                                alpha = 1.0f - (fraction * 0.35f)

                                // Lift the center card slightly upwards
                                translationY = fraction * 14f - 6f
                            }
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                if (isFocused) {
                                    onItemClick(anime)
                                } else {
                                    scope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Poster Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(CardShape)
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

                        // Subtle bottom gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xCC1E1B2E))
                                    )
                                )
                        )

                        // Ranking Badge (#1, #2, ...)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .size(if (isFocused) 28.dp else 24.dp)
                                .clip(CircleShape)
                                .background(badgeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${index + 1}",
                                color = TextWhite,
                                fontSize = if (isFocused) 12.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = anime.title ?: "",
                        color = titleColor,
                        fontSize = if (isFocused) 13.sp else 12.sp,
                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Animated Purple Underline
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(underlineWidth)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentViolet)
                    )
                }
            }
        }
    }
}
