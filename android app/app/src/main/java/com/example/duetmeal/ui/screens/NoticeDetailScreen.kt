package com.example.duetmeal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.*
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

/**
 * Full Detailed Notice View Screen
 */
@Composable
fun NoticeDetailScreen(
    noticeId: String,
    viewModel: DuetMealViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notices by viewModel.notices.collectAsState()

    // Find notice by ID or fallback
    val notice: Notice? = remember(noticeId, notices) {
        notices.find { it.id == noticeId } ?: notices.firstOrNull()
    }

    LaunchedEffect(noticeId) {
        if (noticeId.isNotEmpty()) {
            viewModel.markNoticeRead(noticeId)
        }
    }

    val tagColor = when (notice?.tag?.uppercase()) {
        "IMPORTANT" -> StatusRed
        "NEW UPDATE" -> BrandPrimary
        else -> StatusAmberText
    }

    val tagBg = when (notice?.tag?.uppercase()) {
        "IMPORTANT" -> StatusRedBg
        "NEW UPDATE" -> Color(0xFFE6F0E9)
        else -> StatusAmberBg
    }

    val iconVector: ImageVector = when (notice?.tag?.uppercase()) {
        "IMPORTANT" -> Icons.Outlined.ErrorOutline
        "NEW UPDATE" -> Icons.Outlined.Campaign
        else -> Icons.Outlined.Info
    }

    val formattedDate = remember(notice?.createdAt) {
        if (notice?.createdAt.isNullOrEmpty()) "Recently"
        else {
            try {
                val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val inFmt2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val outFmt = SimpleDateFormat("EEEE, MMMM dd, yyyy • hh:mm a", Locale.US)
                val parsed = try { inFmt.parse(notice!!.createdAt) } catch (_: Exception) { inFmt2.parse(notice!!.createdAt) }
                if (parsed != null) outFmt.format(parsed) else notice!!.createdAt
            } catch (_: Exception) {
                notice?.createdAt ?: "Recently"
            }
        }
    }

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                Text(text = "Notice Board", fontSize = 11.sp, color = Muted)
                Text(text = "Notice Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // --- Main Notice Content ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    // Tag & Category Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tagBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = tagColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = notice?.tag ?: "NOTICE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tagColor
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = (notice?.category ?: "Dining").replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Muted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Title
                    Text(
                        text = notice?.title ?: "Notice Title",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date & Publisher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            color = Muted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Published by: ${notice?.authorId?.replaceFirstChar { it.uppercase() } ?: "Canteen Administration"}",
                            fontSize = 12.sp,
                            color = Muted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Content Body
                    Text(
                        text = notice?.content ?: "Notice description content goes here.",
                        fontSize = 15.sp,
                        color = Ink.copy(alpha = 0.88f),
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Action / Acknowledgment Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BrandLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = BrandDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Official DUET Notice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                                Text(
                                    text = "This is an official communication from DUET Canteen Authority.",
                                    fontSize = 11.sp,
                                    color = Muted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Toast removed
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Back to All Notices",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
