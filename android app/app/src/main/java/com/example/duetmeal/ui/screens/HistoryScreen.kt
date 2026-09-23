package com.example.duetmeal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.data.model.HistoryItem
import com.example.duetmeal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun HistoryScreen(
    viewModel: DuetMealViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Consumed", "Booked", "Auto-Cancelled")
    
    // View mode: "List" or "Calendar"
    var viewMode by remember { mutableStateOf("List") }
    
    val context = LocalContext.current
    val allItems by viewModel.historyItems.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }
    
    val handleCancel: (HistoryItem) -> Unit = { item ->
        val itemDate = item.date.take(10)
        val bookingId = bookings.firstOrNull { b ->
            b.status == "active" &&
            itemDate >= b.startDate.take(10) &&
            itemDate <= b.endDate.take(10) &&
            ((item.mealType == "lunch" && b.includeLunch) || (item.mealType == "dinner" && b.includeDinner))
        }?.id

        if (bookingId != null) {
            viewModel.cancelBooking(
                bookingId = bookingId,
                onSuccess = { Toast.makeText(context, "Meal Cancelled. Amount refunded.", Toast.LENGTH_SHORT).show() },
                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
            )
        } else {
            Toast.makeText(context, "Cannot find associated booking to cancel.", Toast.LENGTH_SHORT).show()
        }
    }

    // Reload when filter chip changes
    LaunchedEffect(selectedFilter) {
        viewModel.loadHistory(selectedFilter)
    }

    // Client-side filter (also covers the case the API returns all and we filter locally)
    val deduplicatedAllItems = remember(allItems) {
        allItems.distinctBy { "${it.date}_${it.mealType}_${it.participants}_${it.status}" }
    }
    
    val filteredItems = remember(selectedFilter, deduplicatedAllItems) {
        when (selectedFilter) {
            "Consumed"       -> deduplicatedAllItems.filter { it.status == "consumed" }
            "Booked"         -> deduplicatedAllItems.filter { it.status == "booked" }
            "Auto-Cancelled" -> deduplicatedAllItems.filter { it.status == "auto_cancelled" }
            else             -> deduplicatedAllItems
        }
    }

    // ── Group by week ─────────────────────────────────────────────────────────
    val (thisWeekItems, lastWeekItems, olderItems) = remember(filteredItems) {
        groupByWeek(filteredItems)
    }
    // ─────────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // --- Sticky Header ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
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
                        Text(text = "Meal History", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                }
                
                // View Mode Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (viewMode == "List") BrandDark else Color.Transparent)
                            .clickable { viewMode = "List" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "List",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "List") Color.White else Muted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (viewMode == "Calendar") BrandDark else Color.Transparent)
                            .clickable { viewMode = "Calendar" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Calendar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "Calendar") Color.White else Muted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) BrandDark else Color.White)
                            .border(
                                1.dp,
                                if (isSelected) BrandDark else Color(0xFFE2E8F0),
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Muted
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        if (viewMode == "List") {
            // History Content List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // (Existing logic for placeholders and thisWeekItems/lastWeekItems/olderItems)
                if (allItems.isEmpty()) {
                    HistorySectionHeader("THIS WEEK")
                    Spacer(modifier = Modifier.height(10.dp))
                    if (selectedFilter == "All" || selectedFilter == "Consumed") {
                        HistoryItemCard("Aug 10, 2026", "Lunch (Self + 1 Guest)", "Consumed", true, "৳180.00", Icons.Outlined.WbSunny, StatusAmber)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    if (selectedFilter == "All" || selectedFilter == "Auto-Cancelled") {
                        HistoryItemCard("Aug 10, 2026", "Dinner (Self)", "Auto-Cancelled", false, "৳90.00", Icons.Outlined.NightsStay, BrandDark)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HistorySectionHeader("LAST WEEK")
                    Spacer(modifier = Modifier.height(10.dp))
                    if (selectedFilter == "All" || selectedFilter == "Consumed") {
                        HistoryItemCard("Aug 03, 2026", "Lunch (Self + 2 Guests)", "Consumed", true, "৳270.00", Icons.Outlined.WbSunny, StatusAmber)
                    }
                } else {
                    if (thisWeekItems.isNotEmpty()) {
                        HistorySectionHeader("THIS WEEK")
                        Spacer(modifier = Modifier.height(10.dp))
                        thisWeekItems.forEach { item ->
                            HistoryItemCardFromApi(item, onCancel = handleCancel)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (lastWeekItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HistorySectionHeader("LAST WEEK")
                        Spacer(modifier = Modifier.height(10.dp))
                        lastWeekItems.forEach { item ->
                            HistoryItemCardFromApi(item, onCancel = handleCancel)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (olderItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HistorySectionHeader("EARLIER")
                        Spacer(modifier = Modifier.height(10.dp))
                        olderItems.forEach { item ->
                            HistoryItemCardFromApi(item, onCancel = handleCancel)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        Text(
                            text = "No records for this filter.",
                            fontSize = 13.sp,
                            color = Muted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        } else {
            // Calendar View
            HistoryCalendarView(
                filteredItems = filteredItems,
                onCancel = handleCancel
            )
        }
    }
}

@Composable
fun HistoryCalendarView(filteredItems: List<HistoryItem>, onCancel: (HistoryItem) -> Unit) {
    var currentCalendar by remember { mutableStateOf(java.util.Calendar.getInstance()) }
    val currentYear = currentCalendar.get(java.util.Calendar.YEAR)
    val currentMonthIndex = currentCalendar.get(java.util.Calendar.MONTH) // 0-based

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(currentCalendar.time)

    val maxDaysInMonth = remember(currentYear, currentMonthIndex) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeekOffset = remember(currentYear, currentMonthIndex) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.SUNDAY) 6 else dow - 2
    }

    val prevMonthDaysCount = remember(currentYear, currentMonthIndex) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonthIndex - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    var selectedDateStr by remember { mutableStateOf("") }
    
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)
    }

    val itemsForSelectedDate = remember(selectedDateStr, filteredItems) {
        filteredItems.filter { it.date == selectedDateStr }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Month Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = monthName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
            Row {
                IconButton(
                    onClick = {
                        val newCal = currentCalendar.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        currentCalendar = newCal
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev", tint = Ink)
                }
                IconButton(
                    onClick = {
                        val newCal = currentCalendar.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        currentCalendar = newCal
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = Ink)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Days Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

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
                        val dateStr = if (isCurrentMonth) String.format("%04d-%02d-%02d", currentYear, currentMonthIndex + 1, dayNum) else ""
                        
                        val dayItems = if (isCurrentMonth) filteredItems.filter { it.date == dateStr } else emptyList()
                        
                        // Option 2: Color whole background
                        val bgColor = when {
                            dayItems.isEmpty() -> Color.Transparent
                            dayItems.any { it.status == "consumed" } -> StatusGreenBg
                            dayItems.any { it.status == "booked" } -> Color(0xFFFFF7ED) // Light amber
                            dayItems.any { it.status == "auto_cancelled" } -> StatusRedBg
                            else -> Color.Transparent
                        }

                        val textColor = when {
                            !isCurrentMonth -> Color.LightGray
                            dayItems.any { it.status == "consumed" } -> StatusGreenText
                            dayItems.any { it.status == "booked" } -> StatusAmberText
                            dayItems.any { it.status == "auto_cancelled" } -> StatusRedText
                            else -> Ink
                        }

                        val isSelected = isCurrentMonth && dateStr == selectedDateStr
                        val isToday = isCurrentMonth && dateStr == todayDateStr

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(
                                    width = if (isSelected || isToday) 2.dp else 0.dp,
                                    color = when {
                                        isSelected -> BrandDark
                                        isToday -> Color(0xFF10B981) // Green
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = isCurrentMonth) {
                                    selectedDateStr = dateStr
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 13.sp,
                                fontWeight = if (dayItems.isNotEmpty() || isSelected) FontWeight.Bold else FontWeight.Medium,
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

        Spacer(modifier = Modifier.height(24.dp))
        
        // Show details for selected date
        if (selectedDateStr.isNotEmpty()) {
            HistorySectionHeader("MEALS ON ${formatHistoryDate(selectedDateStr).uppercase()}")
            Spacer(modifier = Modifier.height(10.dp))
            if (itemsForSelectedDate.isEmpty()) {
                Text("No meals recorded for this date.", fontSize = 13.sp, color = Muted)
            } else {
                itemsForSelectedDate.forEach { item ->
                    HistoryItemCardFromApi(item, onCancel = onCancel)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        } else if (filteredItems.isEmpty()) {
            Text("No records match your filters in this month.", fontSize = 13.sp, color = Muted)
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
fun HistoryItemCardFromApi(item: HistoryItem, onCancel: ((HistoryItem) -> Unit)? = null) {
    HistoryItemCard(
        date = formatHistoryDate(item.date),
        mealDetails = "${item.mealType.replaceFirstChar { it.uppercase() }} (${item.participants})",
        status = item.status.replace("_", "-").replaceFirstChar { it.uppercase() },
        isConsumed = item.status == "consumed",
        price = "৳%,.2f".format(item.price),
        icon = if (item.mealType == "lunch") Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
        iconTint = if (item.mealType == "lunch") StatusAmber else BrandDark,
        isCancelable = item.status == "booked",
        onCancel = { onCancel?.invoke(item) }
    )
}

private fun formatHistoryDate(dateStr: String): String {
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outFmt = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val parsed = inFmt.parse(dateStr)
        if (parsed != null) outFmt.format(parsed) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

private fun groupByWeek(items: List<HistoryItem>): Triple<List<HistoryItem>, List<HistoryItem>, List<HistoryItem>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val nowMs = System.currentTimeMillis()
    val weekMs = 7L * 24 * 60 * 60 * 1000
    val thisWeek = mutableListOf<HistoryItem>()
    val lastWeek = mutableListOf<HistoryItem>()
    val older = mutableListOf<HistoryItem>()
    items.forEach { item ->
        val diffMs = try { nowMs - (sdf.parse(item.date)?.time ?: 0) } catch (e: Exception) { Long.MAX_VALUE }
        when {
            diffMs in 0..weekMs         -> thisWeek.add(item)
            diffMs in weekMs..(2*weekMs) -> lastWeek.add(item)
            else                         -> older.add(item)
        }
    }
    return Triple(thisWeek, lastWeek, older)
}

@Composable
private fun HistorySectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Muted,
        letterSpacing = 1.sp
    )
}

@Composable
private fun HistoryItemCard(
    date: String,
    mealDetails: String,
    status: String,
    isConsumed: Boolean,
    price: String,
    icon: ImageVector,
    iconTint: Color,
    isCancelable: Boolean = false,
    onCancel: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(18.dp)),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(18.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(1.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = date.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mealDetails,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isConsumed) StatusGreen else StatusRed,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isConsumed) StatusGreenText else StatusRedText
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = price,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                if (isCancelable && onCancel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusRedBg)
                            .clickable { onCancel() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRedText)
                    }
                }
            }
        }
    }
}
