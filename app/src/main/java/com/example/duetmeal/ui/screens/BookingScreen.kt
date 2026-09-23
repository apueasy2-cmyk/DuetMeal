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
    val bookings by viewModel.bookings.collectAsState()

    // Dynamic Month & Year state
    var currentCalendar by remember { mutableStateOf(java.util.Calendar.getInstance()) }
    val currentYear = currentCalendar.get(java.util.Calendar.YEAR)
    val currentMonthIndex = currentCalendar.get(java.util.Calendar.MONTH) // 0-based

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH).format(currentCalendar.time)
    val monthShort = java.text.SimpleDateFormat("MMM", java.util.Locale.ENGLISH).format(currentCalendar.time)

    // Calculate days in current month & first day of week
    val maxDaysInMonth = remember(currentYear, currentMonthIndex) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, currentYear)
            set(java.util.Calendar.MONTH, currentMonthIndex)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    }

    // 1 (Sunday) to 7 (Saturday) -> convert to Mon=0, Sun=6
    val firstDayOfWeekOffset = remember(currentYear, currentMonthIndex) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, currentYear)
            set(java.util.Calendar.MONTH, currentMonthIndex)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        val dow = cal.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        if (dow == java.util.Calendar.SUNDAY) 6 else dow - 2
    }

    val prevMonthDaysCount = remember(currentYear, currentMonthIndex) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, currentYear)
            set(java.util.Calendar.MONTH, currentMonthIndex - 1)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    }

    val todayCalendar = remember { java.util.Calendar.getInstance() }
    val isViewingCurrentMonth = currentYear == todayCalendar.get(java.util.Calendar.YEAR) &&
            currentMonthIndex == todayCalendar.get(java.util.Calendar.MONTH)
    val todayDay = if (isViewingCurrentMonth) todayCalendar.get(java.util.Calendar.DAY_OF_MONTH) else 1

    var startDay by remember(currentYear, currentMonthIndex) {
        mutableIntStateOf(todayDay.coerceAtMost(maxDaysInMonth))
    }
    var endDay by remember(currentYear, currentMonthIndex) {
        mutableIntStateOf((todayDay + 3).coerceAtMost(maxDaysInMonth))
    }

    var isLunchSelected by remember { mutableStateOf(true) }
    var isDinnerSelected by remember { mutableStateOf(true) }
    var lunchGuestCount by remember { mutableIntStateOf(0) }
    var dinnerGuestCount by remember { mutableIntStateOf(0) }
    var hasUserSelected by remember(currentYear, currentMonthIndex) { mutableStateOf(false) }

    val daysCount = if (startDay > 0 && endDay >= startDay) (endDay - startDay + 1) else 1
    val pricePerMeal = 90.0

    // Reactive booking computation — recalculates whenever bookings, startDay, endDay or meal selections change
    data class RangeBookingInfo(
        val alreadyBookedLunchDays: Int,
        val alreadyBookedDinnerDays: Int,
        val lunchDaysToCharge: Int,
        val dinnerDaysToCharge: Int,
        val totalRequired: Double
    )

    val rangeInfo by remember(bookings, startDay, endDay, isLunchSelected, isDinnerSelected, lunchGuestCount, dinnerGuestCount, currentYear, currentMonthIndex) {
        derivedStateOf {
            var alreadyLunch = 0
            var alreadyDinner = 0
            var chargeLunch = 0
            var chargeDinner = 0

            if (startDay > 0 && endDay >= startDay) {
                for (day in startDay..endDay) {
                    val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonthIndex + 1, day)
                    val dayBookings = bookings.filter { b ->
                        b.status == "active" &&
                        dateStr >= b.startDate.take(10) &&
                        dateStr <= b.endDate.take(10)
                    }
                    val hasLunch = dayBookings.any { it.includeLunch }
                    val hasDinner = dayBookings.any { it.includeDinner }

                    if (hasLunch) alreadyLunch++
                    if (hasDinner) alreadyDinner++

                    if (isLunchSelected && !hasLunch) chargeLunch += (1 + lunchGuestCount)
                    if (isDinnerSelected && !hasDinner) chargeDinner += (1 + dinnerGuestCount)
                }
            }

            RangeBookingInfo(
                alreadyBookedLunchDays = alreadyLunch,
                alreadyBookedDinnerDays = alreadyDinner,
                lunchDaysToCharge = chargeLunch,
                dinnerDaysToCharge = chargeDinner,
                totalRequired = (chargeLunch + chargeDinner) * pricePerMeal
            )
        }
    }

    val availableBalance = wallet?.totalBalance ?: 1090.0

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

            // Interactive Month Selector with Prev/Next buttons
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val newCal = currentCalendar.clone() as java.util.Calendar
                            newCal.add(java.util.Calendar.MONTH, -1)
                            currentCalendar = newCal
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = Ink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = monthName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = {
                            val newCal = currentCalendar.clone() as java.util.Calendar
                            newCal.add(java.util.Calendar.MONTH, 1)
                            currentCalendar = newCal
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Next Month",
                            tint = Ink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
            // Calendar Legend + Days Header
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusAmberBg))
                    Text("Partial", fontSize = 9.sp, color = StatusAmberText, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusRedBg))
                    Text("Fully Booked", fontSize = 9.sp, color = StatusRedText, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandDark))
                    Text("Selected", fontSize = 9.sp, color = BrandDark, fontWeight = FontWeight.Bold)
                }
                if (bookings.isEmpty()) {
                    Text("(Loading...)", fontSize = 9.sp, color = Muted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days of week header
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

            // Generate full calendar grid for the selected month
            val prevMonthLeadingDays = ((prevMonthDaysCount - firstDayOfWeekOffset + 1)..prevMonthDaysCount).map { Pair(it, false) }
            val currentMonthGridDays = (1..maxDaysInMonth).map { Pair(it, true) }
            val totalCellsSoFar = prevMonthLeadingDays.size + currentMonthGridDays.size
            val trailingDaysCount = if (totalCellsSoFar % 7 != 0) 7 - (totalCellsSoFar % 7) else 0
            val nextMonthTrailingDays = (1..trailingDaysCount).map { Pair(it, false) }

            val allMonthDays = prevMonthLeadingDays + currentMonthGridDays + nextMonthTrailingDays
            val rows = allMonthDays.chunked(7)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { (dayNum, isCurrentMonth) ->
                            val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonthIndex + 1, dayNum)
                            
                            val dayBookings = if (isCurrentMonth) bookings.filter { booking ->
                                booking.status == "active" && 
                                dateStr >= booking.startDate.take(10) && 
                                dateStr <= booking.endDate.take(10)
                            } else emptyList()
                            
                            val hasLunch = dayBookings.any { it.includeLunch }
                            val hasDinner = dayBookings.any { it.includeDinner }
                            
                            val isFullyBooked = isCurrentMonth && hasLunch && hasDinner
                            val isPartiallyBooked = isCurrentMonth && (hasLunch || hasDinner) && !isFullyBooked
                            
                            val isPastMonth = currentYear < todayCalendar.get(java.util.Calendar.YEAR) || (currentYear == todayCalendar.get(java.util.Calendar.YEAR) && currentMonthIndex < todayCalendar.get(java.util.Calendar.MONTH))
                            val isPastDay = isPastMonth || (isViewingCurrentMonth && dayNum < todayCalendar.get(java.util.Calendar.DAY_OF_MONTH))
                            val isSelectable = isCurrentMonth && !isPastDay && !isFullyBooked

                            val isSelectedStart = isCurrentMonth && dayNum == startDay && !isFullyBooked && !isPastDay
                            val isSelectedEnd = isCurrentMonth && dayNum == endDay && !isFullyBooked && !isPastDay
                            val isInRange = isCurrentMonth && dayNum in (startDay..endDay) && !isFullyBooked && !isPastDay

                            val cellBg = when {
                                isPastDay || !isCurrentMonth -> Color.Transparent
                                isFullyBooked -> StatusRedBg
                                isSelectedStart || isSelectedEnd -> BrandDark
                                isPartiallyBooked -> StatusAmberBg
                                isInRange -> Color(0xFFE7F3EA)
                                else -> Color.Transparent
                            }

                            val textColor = when {
                                !isCurrentMonth || isPastDay -> Color.LightGray
                                isFullyBooked -> StatusRedText
                                isSelectedStart || isSelectedEnd -> Color.White
                                isPartiallyBooked -> StatusAmberText
                                isInRange -> BrandDark
                                else -> Ink
                            }

                            val isToday = isViewingCurrentMonth && dayNum == todayDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(
                                        when {
                                            isFullyBooked -> CircleShape
                                            isSelectedStart && isSelectedEnd -> CircleShape
                                            isSelectedStart -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                                            isSelectedEnd -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                                            isInRange -> RoundedCornerShape(0.dp)
                                            isPartiallyBooked -> CircleShape
                                            isToday -> CircleShape
                                            else -> CircleShape
                                        }
                                    )
                                    .background(cellBg)
                                    .then(
                                        if (isToday) Modifier.border(2.dp, Color(0xFF10B981), CircleShape)
                                        else Modifier
                                    )
                                    .clickable(enabled = isSelectable) {
                                        startDay = dayNum
                                        endDay = dayNum
                                        hasUserSelected = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = if (isFullyBooked || isPartiallyBooked || isSelectedStart || isSelectedEnd || isInRange) FontWeight.Bold else FontWeight.Medium,
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
                                text = if (startDay == endDay) "$monthShort $startDay, $currentYear" else "$monthShort $startDay – $monthShort $endDay, $currentYear",
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

                    val minStartDay = if (isViewingCurrentMonth) todayDay else 1

                    // Start Date Slider Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start: $monthShort $startDay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.width(95.dp)
                        )
                        Slider(
                            value = startDay.toFloat().coerceIn(minStartDay.toFloat(), maxDaysInMonth.toFloat()),
                            onValueChange = {
                                startDay = it.toInt().coerceIn(minStartDay, maxDaysInMonth)
                                if (endDay < startDay) {
                                    endDay = startDay
                                }
                                hasUserSelected = true
                            },
                            valueRange = minStartDay.toFloat()..maxDaysInMonth.toFloat(),
                            steps = if (maxDaysInMonth - minStartDay > 0) maxDaysInMonth - minStartDay - 1 else 0,
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
                            text = "End: $monthShort $endDay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.width(95.dp)
                        )
                        Slider(
                            value = endDay.toFloat().coerceIn(minStartDay.toFloat(), maxDaysInMonth.toFloat()),
                            onValueChange = {
                                endDay = it.toInt().coerceIn(startDay, maxDaysInMonth)
                                hasUserSelected = true
                            },
                            valueRange = minStartDay.toFloat()..maxDaysInMonth.toFloat(),
                            steps = if (maxDaysInMonth - minStartDay > 0) maxDaysInMonth - minStartDay - 1 else 0,
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
                            "Rest of Month" to (maxDaysInMonth - startDay + 1)
                        ).forEach { (label, duration) ->
                            val isSelected = daysCount == duration
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) BrandDark else Color(0xFFF1F5F9))
                                    .clickable {
                                        endDay = (startDay + duration - 1).coerceAtMost(maxDaysInMonth)
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

                    if (isLunchSelected) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Lunch Guest Meals", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                                Text(text = "Extra lunch for guests", fontSize = 11.sp, color = Muted)
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
                                        .clickable { if (lunchGuestCount > 0) lunchGuestCount-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "Decrease Lunch Guests", modifier = Modifier.size(14.dp), tint = Ink)
                                }

                                Text(
                                    text = lunchGuestCount.toString(),
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
                                        .clickable { if (lunchGuestCount < 10) lunchGuestCount++ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Increase Lunch Guests", modifier = Modifier.size(14.dp), tint = Ink)
                                }
                            }
                        }
                    }

                    if (isDinnerSelected) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Dinner Guest Meals", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                                Text(text = "Extra dinner for guests", fontSize = 11.sp, color = Muted)
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
                                        .clickable { if (dinnerGuestCount > 0) dinnerGuestCount-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "Decrease Dinner Guests", modifier = Modifier.size(14.dp), tint = Ink)
                                }

                                Text(
                                    text = dinnerGuestCount.toString(),
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
                                        .clickable { if (dinnerGuestCount < 10) dinnerGuestCount++ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Increase Dinner Guests", modifier = Modifier.size(14.dp), tint = Ink)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Already Booked info banner (only shown when there are conflicts in range)
                    val hasConflicts = hasUserSelected && (rangeInfo.alreadyBookedLunchDays > 0 || rangeInfo.alreadyBookedDinnerDays > 0)
                    if (hasConflicts) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusAmberBg)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = StatusAmberText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Already booked in this range:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusAmberText
                                    )
                                }
                                if (rangeInfo.alreadyBookedLunchDays > 0) {
                                    Text(
                                        text = "• ${rangeInfo.alreadyBookedLunchDays} day(s) with Lunch already booked — skipped",
                                        fontSize = 11.sp,
                                        color = StatusAmberText
                                    )
                                }
                                if (rangeInfo.alreadyBookedDinnerDays > 0) {
                                    Text(
                                        text = "• ${rangeInfo.alreadyBookedDinnerDays} day(s) with Dinner already booked — skipped",
                                        fontSize = 11.sp,
                                        color = StatusAmberText
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Price Breakdown
                    if (isLunchSelected && rangeInfo.lunchDaysToCharge > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Lunch (${rangeInfo.lunchDaysToCharge} meals)", fontSize = 12.sp, color = Muted)
                            Text(
                                text = "৳ ${String.format("%.2f", rangeInfo.lunchDaysToCharge * pricePerMeal)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (isDinnerSelected && rangeInfo.dinnerDaysToCharge > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Dinner (${rangeInfo.dinnerDaysToCharge} meals)", fontSize = 12.sp, color = Muted)
                            Text(
                                text = "৳ ${String.format("%.2f", rangeInfo.dinnerDaysToCharge * pricePerMeal)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Required", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(
                            text = "৳ ${String.format("%.2f", rangeInfo.totalRequired)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Balance", fontSize = 13.sp, color = Muted)
                        Text(
                            text = "৳ ${String.format("%.2f", availableBalance)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (availableBalance >= rangeInfo.totalRequired) StatusGreenText else StatusRedText
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val bookingYearMonthStr = String.format("%04d-%02d", currentYear, currentMonthIndex + 1)
                            val formattedStartDate = "$bookingYearMonthStr-${String.format("%02d", startDay)}"
                            val formattedEndDate = "$bookingYearMonthStr-${String.format("%02d", endDay)}"

                            viewModel.createBooking(
                                request = BookingRequest(
                                    userId = viewModel.userId,
                                    startDate = formattedStartDate,
                                    endDate = formattedEndDate,
                                    includeLunch = isLunchSelected,
                                    includeDinner = isDinnerSelected,
                                    guestCount = lunchGuestCount + dinnerGuestCount,
                                    lunchGuestCount = lunchGuestCount,
                                    dinnerGuestCount = dinnerGuestCount
                                ),
                                onSuccess = {
                                    val message = if (startDay == endDay) {
                                        "Meal booked for $formattedStartDate"
                                    } else {
                                        "Meals booked from $formattedStartDate to $formattedEndDate"
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    onBack()
                                },
                                onError = { error ->
                                    Toast.makeText(context, "Booking Error - $error", Toast.LENGTH_LONG).show()
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
