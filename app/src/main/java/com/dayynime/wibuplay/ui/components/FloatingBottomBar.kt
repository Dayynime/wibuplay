package com.dayynime.wibuplay.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayynime.wibuplay.ui.theme.AccentViolet
import com.dayynime.wibuplay.ui.theme.PillShape
import com.dayynime.wibuplay.ui.theme.SurfaceDark
import com.dayynime.wibuplay.ui.theme.SurfaceElevated
import com.dayynime.wibuplay.ui.theme.TextMuted

sealed class BottomTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomTab("home", "Beranda", Icons.Outlined.Home)
    object Explore : BottomTab("explore", "Jelajah", Icons.Outlined.Explore)
    object Schedule : BottomTab("schedule", "Jadwal", Icons.Outlined.CalendarMonth)
    object Cuplix : BottomTab("cuplix", "Cuplix", Icons.Outlined.VideoLibrary)
    object Profile : BottomTab("profile", "Profil", Icons.Outlined.Person)

    companion object {
        val allTabs = listOf(Home, Explore, Schedule, Cuplix, Profile)
    }
}

@Composable
fun FloatingBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    val barOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 110.dp,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "navSlide"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .graphicsLayer {
                translationY = barOffset.toPx()
            }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 16.dp, shape = PillShape, spotColor = Color(0x88000000))
                .clip(PillShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceDark.copy(alpha = 0.96f),
                            Color(0xF0181524)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = SurfaceElevated.copy(alpha = 0.6f),
                    shape = PillShape
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.allTabs.forEach { tab ->
                val isSelected = currentRoute == tab.route

                val tint by animateColorAsState(
                    targetValue = if (isSelected) AccentViolet else TextMuted,
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "tabTint"
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.7f),
                    label = "iconScale"
                )

                val tabBackground by animateColorAsState(
                    targetValue = if (isSelected) AccentViolet.copy(alpha = 0.12f) else Color.Transparent,
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "tabBg"
                )

                Column(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(tabBackground)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab.route) }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = tint,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(iconScale)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        color = tint,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
