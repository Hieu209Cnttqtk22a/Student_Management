package com.studentmanagement.app.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * ResetFilterButton component that displays a button to reset/clear active filters.
 * The button is only visible when a filter is active (not showing all students).
 * Optimized for high refresh rate displays (120Hz/144Hz) with fast spring animations.
 * 
 * @param isVisible Whether the button should be visible (true when filter is active)
 * @param onReset Callback invoked when the reset button is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun ResetFilterButton(
    isVisible: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use spring animation with high stiffness for smooth 120Hz/144Hz displays
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = animationSpec) + scaleIn(animationSpec = animationSpec),
        exit = fadeOut(animationSpec = animationSpec) + scaleOut(animationSpec = animationSpec),
        modifier = modifier
    ) {
        IconButton(onClick = onReset) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Xóa bộ lọc",
                tint = Color.White
            )
        }
    }
}
