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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.data.model.Notice
import com.example.duetmeal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoticesScreen(
    viewModel: DuetMealViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All Notice") }
    val categories = listOf("All Notice", "Dining", "Academic", "Maintenance")

    // ── Observe API state ────────────────────────────────────────────────────
    val allNotices by viewModel.notices.collectAsState()

    // Reload when category changes
    LaunchedEffect(selectedCategory) {
        val apiCategory = when (selectedCategory) {
            "Dining"      -> "dining"
            "Academic"    -> "academic"
            "Maintenance" -> "maintenance"
            else          -> "All"
        }
        viewModel.loadNotices(apiCategory)
    }

    // Client-side filter
    val filteredNotices = remember(selectedCategory, allNotices) {
        if (selectedCategory == "All Notice") allNotices
        else allNotices.filter { it.category.lowercase() == selectedCategory.lowercase() }
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
                    Text(text = "Notices", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) BrandDark else Color(0xFFF1F5F9))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Muted
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // Notice Cards
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (allNotices.isEmpty()) {
                // ── Placeholders while API loads ──────────────────────────────
                if (selectedCategory == "All Notice" || selectedCategory == "Dining") {
                    FeaturedNoticeCard(
                        tag = "NEW UPDATE",
                        title = "August Meal Rate Finalized",
                        content = "The meal rate for August 2026 has been calculated at ৳88.10. Balance adjustments will be reflected in your next bill.",
                        timestamp = "2 hours ago",
                        onAction = { Toast.makeText(context, "Opening August Meal Rate details...", Toast.LENGTH_SHORT).show() }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                if (selectedCategory == "All Notice" || selectedCategory == "Maintenance") {
                    NoticeCard(
                        icon = Icons.Outlined.Info, iconBg = Color(0xFFF1F5F9), iconTint = Muted,
                        tag = "SYSTEM INFO", tagColor = Muted,
                        title = "App Maintenance Notice",
                        content = "The Meal Management System will be offline for routine maintenance from 12:00 AM to 02:00 AM tonight.",
                        timestamp = "Yesterday, 4:30 PM", actionText = "Dismiss",
                        onAction = { Toast.makeText(context, "Notice dismissed", Toast.LENGTH_SHORT).show() }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                if (selectedCategory == "All Notice" || selectedCategory == "Dining") {
                    NoticeCard(
                        icon = Icons.Outlined.ErrorOutline, iconBg = StatusAmberBg, iconTint = StatusAmberText,
                        tag = "IMPORTANT", tagColor = StatusAmberText,
                        title = "Guest Booking Policy Update",
                        content = "Starting from next week, guest bookings must be confirmed at least 24 hours in advance for dinner services.",
                        timestamp = "3 days ago", actionText = "Read more",
                        onAction = { Toast.makeText(context, "Guest Booking Policy details opened", Toast.LENGTH_SHORT).show() }
                    )
                }
            } else {
                // ── API data ──────────────────────────────────────────────────
                filteredNotices.forEachIndexed { index, notice ->
                    NoticeCardFromApi(
                        notice = notice,
                        onAction = {
                            viewModel.markNoticeRead(notice.id)
                            Toast.makeText(context, "Notice: ${notice.title}", Toast.LENGTH_SHORT).show()
                        }
                    )
                    if (index < filteredNotices.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                }

                if (filteredNotices.isEmpty()) {
                    Text(
                        text = "No notices in this category.",
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

// ── Dynamic card from API Notice ─────────────────────────────────────────────

@Composable
private fun NoticeCardFromApi(notice: Notice, onAction: () -> Unit) {
    when (notice.tag) {
        "NEW UPDATE" -> FeaturedNoticeCard(
            tag = notice.tag,
            title = notice.title,
            content = notice.content,
            timestamp = formatNoticeTimestamp(notice.createdAt),
            isUnread = notice.isUnread,
            onAction = onAction
        )
        "IMPORTANT" -> NoticeCard(
            icon = Icons.Outlined.ErrorOutline,
            iconBg = StatusAmberBg,
            iconTint = StatusAmberText,
            tag = notice.tag,
            tagColor = StatusAmberText,
            title = notice.title,
            content = notice.content,
            timestamp = formatNoticeTimestamp(notice.createdAt),
            actionText = "Read more",
            onAction = onAction
        )
        else -> NoticeCard(
            icon = Icons.Outlined.Info,
            iconBg = Color(0xFFF1F5F9),
            iconTint = Muted,
            tag = notice.tag,
            tagColor = Muted,
            title = notice.title,
            content = notice.content,
            timestamp = formatNoticeTimestamp(notice.createdAt),
            actionText = "Dismiss",
            onAction = onAction
        )
    }
}

private fun formatNoticeTimestamp(iso: String): String {
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val outFmt = SimpleDateFormat("MMM dd, h:mm a", Locale.US)
        val parsed = inFmt.parse(iso)
        if (parsed != null) outFmt.format(parsed) else iso
    } catch (e: Exception) {
        iso
    }
}

// ── Featured "New Update" card (big branded card) ─────────────────────────────

@Composable
private fun FeaturedNoticeCard(
    tag: String,
    title: String,
    content: String,
    timestamp: String,
    isUnread: Boolean = false,
    onAction: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE6F0E9))
            .border(1.dp, BrandPrimary.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        if (isUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .background(StatusRed, CircleShape)
            )
        }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = tag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = content, fontSize = 13.sp, color = Ink.copy(alpha = 0.75f), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = timestamp, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Muted)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAction() }
                ) {
                    Text(text = "View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ── Standard notice card ──────────────────────────────────────────────────────

@Composable
private fun NoticeCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    tag: String,
    tagColor: Color,
    title: String,
    content: String,
    timestamp: String,
    actionText: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Text(text = tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tagColor, letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = content, fontSize = 13.sp, color = Ink.copy(alpha = 0.75f), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = timestamp, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Muted)
                Text(text = actionText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, modifier = Modifier.clickable { onAction() })
            }
        }
    }
}
