package com.studentmanagement.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentmanagement.app.ui.theme.TagAbsentBorder
import com.studentmanagement.app.ui.theme.TagAbsentColor
import com.studentmanagement.app.ui.theme.TagFullHomeworkBorder
import com.studentmanagement.app.ui.theme.TagFullHomeworkColor
import com.studentmanagement.app.ui.theme.TagLowScoreBorder
import com.studentmanagement.app.ui.theme.TagLowScoreColor
import com.studentmanagement.app.ui.theme.TagMakeupBorder
import com.studentmanagement.app.ui.theme.TagMakeupColor
import com.studentmanagement.app.ui.theme.TagStudiedBorder
import com.studentmanagement.app.ui.theme.TagStudiedColor

@Composable
fun TagChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    tagType: TagType = TagType.STUDIED
) {
    val (backgroundColor, borderColor, textColor) = when {
        selected -> {
            when (tagType) {
                TagType.ABSENT -> Triple(TagAbsentColor, TagAbsentBorder, TagAbsentBorder)
                TagType.LOW_SCORE -> Triple(TagLowScoreColor, TagLowScoreBorder, TagLowScoreBorder)
                TagType.FULL_HOMEWORK -> Triple(TagFullHomeworkColor, TagFullHomeworkBorder, TagFullHomeworkBorder)
                TagType.MAKEUP -> Triple(TagMakeupColor, TagMakeupBorder, TagMakeupBorder)
                TagType.STUDIED -> Triple(TagStudiedColor, TagStudiedBorder, TagStudiedBorder)
            }
        }
        else -> {
            Triple(Color.Transparent, Color.LightGray, MaterialTheme.colorScheme.onSurface)
        }
    }

    Box(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

enum class TagType {
    ABSENT,
    LOW_SCORE,
    FULL_HOMEWORK,
    MAKEUP,
    STUDIED
}
