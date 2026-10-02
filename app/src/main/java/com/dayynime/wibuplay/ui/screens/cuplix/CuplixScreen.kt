@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.dayynime.wibuplay.ui.screens.cuplix

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dayynime.wibuplay.data.model.CuplixItem
import com.dayynime.wibuplay.ui.components.EmptyState
import com.dayynime.wibuplay.ui.components.ErrorState
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.ErrorRed
import com.dayynime.wibuplay.ui.theme.PillShape
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextSecondary
import com.dayynime.wibuplay.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun CuplixScreen(
    viewModel: CuplixViewModel,
    onWatchAnime: (movieId: String, episodeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentViolet)
                }
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadFeed(isInitial = true) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.items.isEmpty() -> {
                EmptyState(
                    title = "Cuplix Belum Tersedia",
                    subtitle = "Tarik ke bawah atau coba kembali beberapa saat lagi",
                    icon = Icons.Outlined.VideoLibrary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                val pagerState = rememberPagerState(pageCount = { uiState.items.size })

                LaunchedEffect(pagerState.currentPage) {
                    viewModel.onPageChanged(pagerState.currentPage)
                }

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val item = uiState.items[page]
                    val isCurrentPage = pagerState.currentPage == page

                    CuplixVideoPlayerItem(
                        item = item,
                        isActive = isCurrentPage,
                        isLiked = uiState.likedIds.contains(item.id),
                        onToggleLike = { viewModel.toggleLike(item.id) },
                        onWatchFull = {
                            val movieId = item.id_movie ?: ""
                            val episodeId = item.id_episode ?: ""
                            if (movieId.isNotBlank() && episodeId.isNotBlank()) {
                                onWatchAnime(movieId, episodeId)
                            }
                        },
                        resolveStreamUrl = { epId -> viewModel.resolveStreamUrl(epId) },
                        onStreamFailed = { epId -> viewModel.invalidateStreamCache(epId) }
                    )
                }
            }
        }
    }
}

@Composable
fun CuplixVideoPlayerItem(
    item: CuplixItem,
    isActive: Boolean,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onWatchFull: () -> Unit,
    resolveStreamUrl: suspend (String) -> String?,
    onStreamFailed: (String) -> Unit
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var showPauseIcon by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Manage ExoPlayer lifecycle strictly tied to isActive
    DisposableEffect(isActive, item.id) {
        if (!isActive) {
            return@DisposableEffect onDispose {}
        }

        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)

        val player = ExoPlayer.Builder(context, renderersFactory).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
        }
        exoPlayer = player

        scope.launch {
            val epId = item.id_episode
            if (epId.isNullOrBlank()) {
                isBuffering = false
                return@launch
            }
            val streamUrl = resolveStreamUrl(epId)
            if (streamUrl != null) {
                val mediaItem = MediaItem.fromUri(streamUrl)
                player.setMediaItem(mediaItem)

                val startMs = item.getTimeStartMs()
                if (startMs > 0) {
                    player.seekTo(startMs)
                }
                player.prepare()
            } else {
                isBuffering = false
            }
        }

        // Listener for playback boundaries
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_ENDED) {
                    val startMs = item.getTimeStartMs()
                    player.seekTo(startMs)
                    player.play()
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                item.id_episode?.let { onStreamFailed(it) }
                isBuffering = false
            }
        }
        player.addListener(listener)

        // Coroutine to monitor time_end
        val timeEndMs = item.getTimeEndMs()
        val timeStartMs = item.getTimeStartMs()
        val job = if (timeEndMs > timeStartMs) {
            scope.launch {
                while (isActive && isActive) {
                    delay(150)
                    if (player.currentPosition >= timeEndMs) {
                        player.seekTo(timeStartMs)
                        player.play()
                    }
                }
            }
        } else null

        onDispose {
            job?.cancel()
            player.removeListener(listener)
            player.stop()
            player.clearMediaItems()
            player.release()
            exoPlayer = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        showPauseIcon = true
                    } else {
                        player.play()
                        showPauseIcon = false
                    }
                }
            }
    ) {
        // Thumbnail preview placeholder while loading
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.getThumbnailUrl())
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Video View
        if (isActive && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = exoPlayer
                        useController = false
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient overlays for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x991E1B2E), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xDD1E1B2E), Color(0xFF1E1B2E))
                    )
                )
        )

        // Buffering indicator
        if (isBuffering && isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AccentViolet,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
            }
        }

        // Pause Icon Overlay
        AnimatedVisibility(
            visible = showPauseIcon && !isPlaying,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Right Action Bar (Like, Comment, Share, Watch Full)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 95.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.85f))
                        .clickable { onToggleLike() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Suka",
                        tint = if (isLiked) ErrorRed else TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.count_likes ?: "0",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Komentar",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.count_comments ?: "0",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark.copy(alpha = 0.85f))
                        .clickable {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, item.anime ?: "Wibuplay")
                                putExtra(Intent.EXTRA_TEXT, "Tonton klip anime ${item.anime ?: ""} di Wibuplay!")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Cuplix"))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Bagikan",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bagi",
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Watch Full Anime Button
            if (!item.id_movie.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(AccentViolet)
                        .clickable { onWatchFull() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Movie,
                        contentDescription = "Tonton Anime Penuh",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Bottom Left Info: Username, Anime title, Caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, end = 16.dp, bottom = 95.dp)
        ) {
            val user = item.username
            if (!user.isNullOrBlank()) {
                Text(
                    text = "@$user",
                    color = AccentViolet,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
            }

            Text(
                text = item.anime ?: "Anime",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!item.episode.isNullOrBlank()) {
                Text(
                    text = "Episode ${item.episode}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            val caption = item.caption
            if (!caption.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = caption,
                    color = TextWhite.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
