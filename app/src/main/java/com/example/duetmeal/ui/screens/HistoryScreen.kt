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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.data.model.HistoryItem
import com.example.duetmeal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: DuetMealViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Consumed", "Booked", "Auto-Cancelled")

    // ── Observe API state ────────────────────────────────────────────────────
    val allItems by viewModel.historyItems.collectAsState()

    // Reload when filter chip changes
    LaunchedEffect(selectedFilter) {
        viewModel.loadHistory(selectedFilter)
    }

    // Client-side filter (also covers the case the API returns all and we filter locally)
    val filteredItems = remember(selectedFilter, allItems) {
        when (selectedFilter) {
            "Consumed"       -> allItems.filter { it.status == "consumed" }
            "Booked"         -> allItems.filter { it.status == "booked" }
            "Auto-Cancelled" -> allItems.filter { it.status == "auto_cancelled" }
            else             -> allItems
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

        // History Content List
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (allItems.isEmpty()) {
                // ── Placeholders while API loads ──────────────────────────────
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
                // ── API data ──────────────────────────────────────────────────
                if (thisWeekItems.isNotEmpty()) {
                    HistorySectionHeader("THIS WEEK")
                    Spacer(modifier = Modifier.height(10.dp))
                    thisWeekItems.forEach { item ->
                        HistoryItemCard(
                            date = formatHistoryDate(item.date),
                            mealDetails = "${item.mealType.replaceFirstChar { it.uppercase() }} (${item.participants})",
                            status = item.status.replace("_", "-").replaceFirstChar { it.uppercase() },
                            isConsumed = item.status == "consumed",
                            price = "৳%,.2f".format(item.price),
                            icon = if (item.mealType == "lunch") Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                            iconTint = if (item.mealType == "lunch") StatusAmber else BrandDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                if (lastWeekItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HistorySectionHeader("LAST WEEK")
                    Spacer(modifier = Modifier.height(10.dp))
                    lastWeekItems.forEach { item ->
                        HistoryItemCard(
                            date = formatHistoryDate(item.date),
                            mealDetails = "${item.mealType.replaceFirstChar { it.uppercase() }} (${item.participants})",
                            status = item.status.replace("_", "-").replaceFirstChar { it.uppercase() },
                            isConsumed = item.status == "consumed",
                            price = "৳%,.2f".format(item.price),
                            icon = if (item.mealType == "lunch") Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                            iconTint = if (item.mealType == "lunch") StatusAmber else BrandDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                if (olderItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HistorySectionHeader("EARLIER")
                    Spacer(modifier = Modifier.height(10.dp))
                    olderItems.forEach { item ->
                        HistoryItemCard(
                            date = formatHistoryDate(item.date),
                            mealDetails = "${item.mealType.replaceFirstChar { it.uppercase() }} (${item.participants})",
                            status = item.status.replace("_", "-").replaceFirstChar { it.uppercase() },
                            isConsumed = item.status == "consumed",
                            price = "৳%,.2f".format(item.price),
                            icon = if (item.mealType == "lunch") Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                            iconTint = if (item.mealType == "lunch") StatusAmber else BrandDark
                        )
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
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

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
    iconTint: Color
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

            Text(
                text = price,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }
    }
}
