package com.example.duetmeal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.data.model.BookingRequest
import com.example.duetmeal.ui.theme.*

@Composable
fun BookingScreen(
    viewModel: DuetMealViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val wallet by viewModel.wallet.collectAsState()
    var selectedMonth by remember { mutableStateOf("August 2026") }

    var startDay by remember { mutableIntStateOf(10) }
    var endDay by remember { mutableIntStateOf(14) }
    var isSelectingEnd by remember { mutableStateOf(false) }

    var isLunchSelected by remember { mutableStateOf(true) }
    var isDinnerSelected by remember { mutableStateOf(true) }
    var guestCount by remember { mutableIntStateOf(1) }

    val daysCount = if (startDay > 0 && endDay >= startDay) (endDay - startDay + 1) else 1
    val mealsPerDay = (if (isLunchSelected) 1 else 0) + (if (isDinnerSelected) 1 else 0)
    val totalPeople = 1 + guestCount
    val pricePerMeal = 90.0
    val totalRequired = daysCount * mealsPerDay * totalPeople * pricePerMeal
    val availableBalance = wallet?.availableBalance ?: 1090.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(1.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFF8FAFC))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Back",
                        tint = Ink
                    )
                }
                Column {
                    Text(text = "Back", fontSize = 11.sp, color = Muted)
                    Text(text = "Book Meals", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = selectedMonth,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Calendar Grid Days Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val monthDays = (27..31).map { Pair(it, false) } + (1..31).map { Pair(it, true) }
            val rows = monthDays.chunked(7)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { (dayNum, isCurrentMonth) ->
                            val isSelectedStart = isCurrentMonth && dayNum == startDay
                            val isSelectedEnd = isCurrentMonth && dayNum == endDay
                            val isInRange = isCurrentMonth && dayNum in (startDay..endDay)

                            val cellBg = when {
                                isSelectedStart || isSelectedEnd -> BrandDark
                                isInRange -> Color(0xFFE7F3EA)
                                else -> Color.Transparent
                            }

                            val textColor = when {
                                isSelectedStart || isSelectedEnd -> Color.White
                                isInRange -> BrandDark
                                !isCurrentMonth -> Color.LightGray
                                else -> Ink
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(
                                        when {
                                            isSelectedStart && isSelectedEnd -> CircleShape
                                            isSelectedStart -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                                            isSelectedEnd -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                                            isInRange -> RoundedCornerShape(0.dp)
                                            else -> CircleShape
                                        }
                                    )
                                    .background(cellBg)
                                    .clickable(enabled = isCurrentMonth) {
                                        if (!isSelectingEnd) {
                                            // Start new selection
                                            startDay = dayNum
                                            endDay = dayNum
                                            isSelectingEnd = true
                                        } else {
                                            // Choose end date
                                            if (dayNum >= startDay) {
                                                endDay = dayNum
                                                isSelectingEnd = false
                                            } else {
                                                // If tapped earlier date, reset startDay to that date
                                                startDay = dayNum
                                                endDay = dayNum
                                                isSelectingEnd = true
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelectedStart || isSelectedEnd || isInRange) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                        if (row.size < 7) {
                            repeat(7 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Slideable / Interactive Date Range Controls ──────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(18.dp)),
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SELECTED DATE RANGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Muted,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (startDay == endDay) "August $startDay, 2026" else "Aug $startDay – Aug $endDay, 2026",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BrandLight)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$daysCount ${if (daysCount == 1) "Day" else "Days"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start Date Slider Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start: Aug $startDay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.width(85.dp)
                        )
                        Slider(
                            value = startDay.toFloat(),
                            onValueChange = {
                                startDay = it.toInt()
                                if (endDay < startDay) {
                                    endDay = startDay
                                }
                            },
                            valueRange = 1f..31f,
                            steps = 29,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = BrandDark,
                                activeTrackColor = BrandDark,
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // End Date Slider Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "End: Aug $endDay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.width(85.dp)
                        )
                        Slider(
                            value = endDay.toFloat(),
                            onValueChange = {
                                endDay = it.toInt()
                                if (startDay > endDay) {
                                    startDay = endDay
                                }
                            },
                            valueRange = 1f..31f,
                            steps = 29,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = BrandPrimary,
                                activeTrackColor = BrandPrimary,
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Quick Duration Presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Today Only" to 1,
                            "3 Days" to 3,
                            "5 Days" to 5,
                            "7 Days" to 7,
                            "Rest of Month" to (31 - startDay + 1)
                        ).forEach { (label, duration) ->
                            val isSelected = daysCount == duration
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) BrandDark else Color(0xFFF1F5F9))
                                    .clickable {
                                        endDay = (startDay + duration - 1).coerceAtMost(31)
                                        isSelectingEnd = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Muted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Booking Details Bottom Sheet Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                color = Color.White,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFE2E8F0), CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lunch Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .clickable { isLunchSelected = !isLunchSelected }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StatusAmberBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WbSunny,
                                    contentDescription = "Lunch",
                                    tint = StatusAmberText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(text = "Book Lunch", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                                Text(text = "৳90.00 / meal", fontSize = 11.sp, color = Muted)
                            }
                        }
                        Checkbox(
                            checked = isLunchSelected,
                            onCheckedChange = { isLunchSelected = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandDark)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dinner Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .clickable { isDinnerSelected = !isDinnerSelected }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrandLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NightsStay,
                                    contentDescription = "Dinner",
                                    tint = BrandDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(text = "Book Dinner", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                                Text(text = "৳90.00 / meal", fontSize = 11.sp, color = Muted)
                            }
                        }
                        Checkbox(
                            checked = isDinnerSelected,
                            onCheckedChange = { isDinnerSelected = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandDark)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guest Meals Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Guest Meals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text(text = "Same menu as yours", fontSize = 11.sp, color = Muted)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .shadow(1.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { if (guestCount > 0) guestCount-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp), tint = Ink)
                            }

                            Text(
                                text = guestCount.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .shadow(1.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { if (guestCount < 10) guestCount++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp), tint = Ink)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Price Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Required", fontSize = 13.sp, color = Muted)
                        Text(
                            text = "৳ ${String.format("%.2f", totalRequired)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Available Balance", fontSize = 13.sp, color = Muted)
                        Text(
                            text = "৳ ${String.format("%.2f", availableBalance)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreenText
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.createBooking(
                                request = BookingRequest(
                                    userId = viewModel.userId,
                                    startDate = "2026-08-${String.format("%02d", startDay)}",
                                    endDate = "2026-08-${String.format("%02d", endDay)}",
                                    includeLunch = isLunchSelected,
                                    includeDinner = isDinnerSelected,
                                    guestCount = guestCount
                                ),
                                onSuccess = {
                                    Toast.makeText(context, "Meals successfully booked for August $startDay - $endDay!", Toast.LENGTH_LONG).show()
                                    onBack()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Confirm & Deduct Balance",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
