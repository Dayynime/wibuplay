@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.dayynime.wibuplay.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dayynime.wibuplay.data.model.EpisodeItem
import com.dayynime.wibuplay.data.model.StreamServer
import com.dayynime.wibuplay.ui.components.AnimePosterCard
import com.dayynime.wibuplay.ui.components.EpisodeListItem
import com.dayynime.wibuplay.ui.components.GenreChip
import com.dayynime.wibuplay.ui.components.SectionHeader
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.BottomSheetShape
import com.dayynime.wibuplay.ui.theme.CardShape
import com.dayynime.wibuplay.ui.theme.PillShape
import com.dayynime.wibuplay.ui.theme.SurfaceCard
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.SurfaceElevated
import com.dayynime.wibuplay.ui.theme.TextMuted
import com.dayynime.wibuplay.ui.theme.TextSecondary
import com.dayynime.wibuplay.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    onAnimeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var controlsTimeoutKey by remember { mutableStateOf(0) }
    var showServerSheet by remember { mutableStateOf(false) }
    var isSynopsisExpanded by remember { mutableStateOf(false) }
    var episodeSearchQuery by remember { mutableStateOf("") }
    var gestureOverlayText by remember { mutableStateOf<String?>(null) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    // Fullscreen back handling
    BackHandler {
        if (uiState.isFullscreen) {
            viewModel.setFullscreen(false)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            onBackClick()
        }
    }

    // Toggle Screen Orientation and System Bars for Fullscreen
    LaunchedEffect(uiState.isFullscreen) {
        val window = activity?.window ?: return@LaunchedEffect
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (uiState.isFullscreen) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Keep screen on while playing
    DisposableEffect(isPlaying) {
        val window = activity?.window
        if (isPlaying) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Initialize ExoPlayer
    DisposableEffect(context) {
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)

        val player = ExoPlayer.Builder(context, renderersFactory).build().apply {
            playWhenReady = true
        }
        exoPlayer = player

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                duration = player.duration.coerceAtLeast(0L)
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.startAutoNextCountdown {
                        // next episode triggered
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isBuffering = false
            }
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.stop()
            player.clearMediaItems()
            player.release()
            exoPlayer = null
        }
    }

    // Load stream link into ExoPlayer when selectedServer changes
    LaunchedEffect(uiState.selectedServer?.link) {
        val link = uiState.selectedServer?.link
        val player = exoPlayer ?: return@LaunchedEffect
        if (!link.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(link)
            player.setMediaItem(mediaItem)
            val resumeMs = uiState.resumePositionMs
            if (resumeMs > 0) {
                player.seekTo(resumeMs)
            }
            player.prepare()
            player.play()
        }
    }

    // Periodic time update and watch progress save to Room every 4-5 seconds
    LaunchedEffect(exoPlayer) {
        var counter = 0
        while (isActive) {
            delay(1000)
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    currentPosition = player.currentPosition
                    duration = player.duration.coerceAtLeast(0L)
                    counter++
                    if (counter % 5 == 0 && duration > 0) {
                        viewModel.saveProgress(currentPosition, duration)
                    }
                }
            }
        }
    }

    // Auto-hide controls overlay after 3.5 seconds
    LaunchedEffect(showControls, controlsTimeoutKey, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    val episodesListState = rememberLazyListState()

    // Auto-scroll episodes to current playing episode
    LaunchedEffect(uiState.currentEpisodeId, uiState.episodes) {
        val idx = uiState.episodes.indexOfFirst { it.id == uiState.currentEpisodeId }
        if (idx >= 0) {
            episodesListState.animateScrollToItem(idx)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isFullscreen) {
            // FULLSCREEN VIDEO LAYOUT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    // Gestures: Left vertical drag = brightness, Right vertical drag = volume, Horizontal = seek
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            val isLeft = change.position.x < (size.width / 2)
                            if (isLeft) {
                                // Adjust brightness
                                activity?.let { act ->
                                    val lp = act.window.attributes
                                    val currentBrightness = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                                    val newBrightness = (currentBrightness - (dragAmount / 400f)).coerceIn(0.05f, 1f)
                                    lp.screenBrightness = newBrightness
                                    act.window.attributes = lp
                                    gestureOverlayText = "Kecerahan: ${(newBrightness * 100).toInt()}%"
                                }
                            } else {
                                // Adjust volume
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val delta = if (dragAmount < 0) 1 else -1
                                val newVol = (curVol + delta).coerceIn(0, maxVol)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                gestureOverlayText = "Volume: ${(newVol * 100 / maxVol)}%"
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { gestureOverlayText = null },
                            onDragCancel = { gestureOverlayText = null }
                        ) { _, dragAmount ->
                            exoPlayer?.let { player ->
                                val deltaMs = (dragAmount * 250).toLong()
                                val target = (player.currentPosition + deltaMs).coerceIn(0L, duration)
                                player.seekTo(target)
                                currentPosition = target
                                gestureOverlayText = "${formatTime(target)} / ${formatTime(duration)}"
                            }
                        }
                    }
            ) {
                // Video View
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

                // Gesture overlay indicator (Brightness / Volume / Seek feedback)
                gestureOverlayText?.let { text ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(PillShape)
                            .background(Color(0x991E1B2E))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(text = text, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Video Controls Overlay
                PlayerControlsOverlay(
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    currentPosition = currentPosition,
                    duration = duration,
                    showControls = showControls,
                    isFullscreen = true,
                    title = "${uiState.anime?.title ?: "Anime"} - Ep ${uiState.streamData?.episode?.index ?: ""}",
                    selectedServer = uiState.selectedServer,
                    onTogglePlay = {
                        exoPlayer?.let { if (it.isPlaying) it.pause() else it.play() }
                        controlsTimeoutKey++
                    },
                    onSeek = { pos ->
                        exoPlayer?.seekTo(pos)
                        currentPosition = pos
                        controlsTimeoutKey++
                    },
                    onToggleFullscreen = {
                        viewModel.setFullscreen(false)
                    },
                    onOpenServers = { showServerSheet = true },
                    onDoubleTapLeft = {
                        exoPlayer?.let {
                            val target = (it.currentPosition - 10000).coerceAtLeast(0L)
                            it.seekTo(target)
                            currentPosition = target
                        }
                    },
                    onDoubleTapRight = {
                        exoPlayer?.let {
                            val target = (it.currentPosition + 10000).coerceAtMost(duration)
                            it.seekTo(target)
                            currentPosition = target
                        }
                    },
                    onToggleOverlay = {
                        showControls = !showControls
                        controlsTimeoutKey++
                    },
                    onBack = {
                        viewModel.setFullscreen(false)
                    }
                )
            }
        } else {
            // PORTRAIT YOUTUBE-STYLE LAYOUT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top: 16:9 Video Player Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
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

                    // Video Controls Overlay
                    PlayerControlsOverlay(
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        currentPosition = currentPosition,
                        duration = duration,
                        showControls = showControls,
                        isFullscreen = false,
                        title = "${uiState.anime?.title ?: "Anime"} - Ep ${uiState.streamData?.episode?.index ?: ""}",
                        selectedServer = uiState.selectedServer,
                        onTogglePlay = {
                            exoPlayer?.let { if (it.isPlaying) it.pause() else it.play() }
                            controlsTimeoutKey++
                        },
                        onSeek = { pos ->
                            exoPlayer?.seekTo(pos)
                            currentPosition = pos
                            controlsTimeoutKey++
                        },
                        onToggleFullscreen = {
                            viewModel.setFullscreen(true)
                        },
                        onOpenServers = { showServerSheet = true },
                        onDoubleTapLeft = {
                            exoPlayer?.let {
                                val target = (it.currentPosition - 10000).coerceAtLeast(0L)
                                it.seekTo(target)
                                currentPosition = target
                            }
                        },
                        onDoubleTapRight = {
                            exoPlayer?.let {
                                val target = (it.currentPosition + 10000).coerceAtMost(duration)
                                it.seekTo(target)
                                currentPosition = target
                            }
                        },
                        onToggleOverlay = {
                            showControls = !showControls
                            controlsTimeoutKey++
                        },
                        onBack = onBackClick
                    )

                    // Error state with "Coba server lain"
                    if (uiState.streamError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xDD1E1B2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Gagal memutar video di server ini",
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showServerSheet = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                                    shape = PillShape
                                ) {
                                    Text("Coba Server Lain", color = TextWhite, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Auto-Next countdown overlay banner
                    val countdown = uiState.autoNextCountdown
                    if (countdown != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xBB1E1B2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(SurfaceDark)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Episode berikutnya dalam ${countdown}d...",
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Batal",
                                    color = AccentViolet,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.cancelAutoNext() }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                // Scrollable Content Below Video Player
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    val currentEp = uiState.streamData?.episode ?: uiState.episodes.find { it.id == uiState.currentEpisodeId }

                    // Title & Badges
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = uiState.anime?.title ?: "Anime",
                                color = TextWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Episode ${currentEp?.index ?: ""} • ${currentEp?.title ?: ""}",
                                color = AccentViolet,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badges & Views
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!uiState.anime?.status.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SurfaceDark)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = uiState.anime!!.status!!,
                                            color = AccentViolet,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (!uiState.anime?.year.isNullOrBlank()) {
                                    Text(
                                        text = "${uiState.anime!!.year}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                if (!uiState.anime?.views.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = uiState.anime!!.views!!,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Actions Row: Favorite, Share, Server Selector, Next Episode (if available)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Favorite
                                ActionIconButton(
                                    icon = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    label = "Favorit",
                                    tint = if (isFavorite) AccentViolet else TextWhite,
                                    onClick = { viewModel.toggleFavorite() }
                                )

                                // Share
                                ActionIconButton(
                                    icon = Icons.Outlined.Share,
                                    label = "Bagikan",
                                    tint = TextWhite,
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, uiState.anime?.title ?: "Wibuplay")
                                            putExtra(Intent.EXTRA_TEXT, "Nonton ${uiState.anime?.title ?: "Anime"} di Wibuplay!")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan"))
                                    }
                                )

                                // Server & Quality Selector
                                ActionIconButton(
                                    icon = Icons.Outlined.Tune,
                                    label = uiState.selectedServer?.quality ?: "Server",
                                    tint = TextWhite,
                                    onClick = { showServerSheet = true }
                                )
                            }

                            // Next Episode Button (only if next exists)
                            val nextEp = uiState.streamData?.episode_next
                            if (nextEp != null || uiState.streamData?.hasNextEpisode == true) {
                                Button(
                                    onClick = {
                                        nextEp?.id?.let { viewModel.loadEpisodeStream(it) }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentViolet,
                                        contentColor = TextWhite
                                    ),
                                    shape = PillShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Synopsis Box
                    item {
                        val syn = uiState.anime?.synopsis
                        if (!syn.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .clip(CardShape)
                                    .background(SurfaceDark)
                                    .padding(14.dp)
                                    .animateContentSize()
                            ) {
                                Text(
                                    text = syn,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (syn.length > 100) {
                                    Text(
                                        text = if (isSynopsisExpanded) "Tutup" else "Baca selengkapnya",
                                        color = AccentViolet,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                            .padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Episode List Section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Episode (${uiState.episodes.size})",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Horizontal Episodes Scrollable Row (Quick switcher)
                    item {
                        LazyRow(
                            state = episodesListState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.episodes) { ep ->
                                val isCurrent = ep.id == uiState.currentEpisodeId
                                Box(
                                    modifier = Modifier
                                        .clip(CardShape)
                                        .background(if (isCurrent) AccentViolet else SurfaceCard)
                                        .clickable {
                                            ep.id?.let { viewModel.loadEpisodeStream(it) }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Ep ${ep.index ?: ""}",
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recommendations Section ("Mungkin Kamu Suka")
                    if (uiState.recommended.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionHeader(title = "Mungkin Kamu Suka")
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.recommended) { rec ->
                                    AnimePosterCard(
                                        anime = rec,
                                        onClick = { rec.id?.let { onAnimeClick(it) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Server & Quality Bottom Sheet
        if (showServerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showServerSheet = false },
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
                    Text(
                        text = "Pilih Server & Kualitas Video",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val servers = uiState.streamData?.server ?: emptyList()
                    if (servers.isEmpty()) {
                        Text(
                            text = "Tidak ada server video alternatif.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        servers.forEach { server ->
                            val isSelected = server == uiState.selectedServer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(CardShape)
                                    .background(if (isSelected) AccentViolet.copy(alpha = 0.2f) else SurfaceElevated)
                                    .clickable {
                                        viewModel.selectServer(server)
                                        showServerSheet = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = server.name ?: "Server Utama",
                                        color = if (isSelected) AccentViolet else TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Tipe: ${server.type ?: "direct"}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(if (isSelected) AccentViolet else SurfaceDark)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = server.quality ?: "Auto",
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun PlayerControlsOverlay(
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPosition: Long,
    duration: Long,
    showControls: Boolean,
    isFullscreen: Boolean,
    title: String,
    selectedServer: StreamServer?,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFullscreen: () -> Unit,
    onOpenServers: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit,
    onToggleOverlay: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggleOverlay() },
                    onDoubleTap = { offset ->
                        if (offset.x < size.width / 2) {
                            onDoubleTapLeft()
                        } else {
                            onDoubleTapRight()
                        }
                    }
                )
            }
    ) {
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
            ) {
                // Top Bar: Back button, Anime Title, Server/Quality button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Kembali",
                                tint = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Server/Quality indicator chip
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Color(0x66FFFFFF))
                            .clickable { onOpenServers() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedServer?.quality ?: "Kualitas",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Center: Play / Pause / Buffering Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = AccentViolet,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = TextWhite,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Bottom Controls: Time, Seek bar, Fullscreen button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() else 0f,
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentViolet,
                            activeTrackColor = AccentViolet,
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
