package com.studentmanagement.app.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentmanagement.app.ui.theme.Primary
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun YearCalendarView(
    year: Int,
    currentYearMonth: YearMonth,
    classCountByMonth: Map<Int, Int>,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = (1..12).map { monthValue ->
        YearMonth.of(year, monthValue)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(months) { yearMonth ->
            MonthCard(
                yearMonth = yearMonth,
                isCurrentMonth = yearMonth == currentYearMonth,
                classCount = classCountByMonth[yearMonth.monthValue] ?: 0,
                onMonthSelected = onMonthSelected
            )
        }
    }
}

@Composable
private fun MonthCard(
    yearMonth: YearMonth,
    isCurrentMonth: Boolean,
    classCount: Int,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale("vi"))
        .replaceFirstChar { it.uppercase() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onMonthSelected(yearMonth) },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentMonth) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentMonth) {
                Primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCurrentMonth) {
                        Modifier.border(2.dp, Primary, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monthName,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCurrentMonth) Primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (classCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Primary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$classCount lớp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                } else {
                    Text(
                        text = "Không có lớp",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
