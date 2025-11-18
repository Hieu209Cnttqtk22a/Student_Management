package com.studentmanagement.app.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.ui.theme.Primary
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthCalendarView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    currentDate: LocalDate,
    classesMap: Map<Int, List<ClassEntity>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
    val daysInMonth = yearMonth.lengthOfMonth()
    
    // Calculate offset for first day (0 = Monday, 6 = Sunday)
    val offset = firstDayOfWeek - 1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7

            repeat(rows) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { day ->
                        val cellIndex = week * 7 + day
                        val dayNum = cellIndex - offset + 1
                        
                        if (cellIndex >= offset && dayNum <= daysInMonth) {
                            val date = LocalDate.of(yearMonth.year, yearMonth.month, dayNum)
                            MonthDayItem(
                                dayNum = dayNum,
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == currentDate,
                                hasClasses = classesMap[dayNum]?.isNotEmpty() == true,
                                onDateSelected = onDateSelected,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayItem(
    dayNum: Int,
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasClasses: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Primary
                    isToday -> Primary.copy(alpha = 0.2f)
                    hasClasses -> Primary.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(2.dp, Primary, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayNum.toString(),
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> Primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (hasClasses && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
        }
    }
}
