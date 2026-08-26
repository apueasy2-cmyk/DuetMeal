package com.example.duetmeal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duetmeal.R
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.ui.components.Screen
import com.example.duetmeal.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: DuetMealViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var isLunchBooked by remember { mutableStateOf(true) }
    var isDinnerBooked by remember { mutableStateOf(false) }

    // ── Observe API state ────────────────────────────────────────────────────
    val user by viewModel.user.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val todayMeal by viewModel.todayMeal.collectAsState()

    // Auto-refresh profile and data when HomeScreen loads
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadWallet()
        viewModel.loadTodayMeal()
    }

    // ── Derived display values (fetched from database via API) ───────────────
    val displayName = if (!user?.fullName.isNullOrBlank()) {
        "Hello, ${user?.fullName}"
    } else {
        "Hello, Dr. Fazlul Hasan"
    }
    val initials = user?.initials?.ifEmpty { null }
        ?: user?.fullName?.split(" ")?.filter { it.isNotEmpty() }?.map { it.first() }?.take(2)?.joinToString("")?.ifEmpty { "FH" }
        ?: "FH"
    val totalBalance = wallet?.totalBalance?.let { "৳ %,.2f".format(it) } ?: "৳ 1,450.00"
    val availableBalance = wallet?.availableBalance?.let { "৳ %,.2f".format(it) } ?: "৳ 1,090.00"

    val lunchMenu = todayMeal?.lunch?.items?.joinToString(", ") ?: "Chicken Biryani, Salad, Borhani"
    val lunchTime = todayMeal?.lunch?.let { "Main Canteen · ${it.startTime} – ${it.endTime}" } ?: "Main Canteen · 12:30 – 02:00 PM"
    val lunchCutoff = todayMeal?.lunch?.cutoffTime?.let { "Cutoff: $it" } ?: "Cutoff: 12:00 PM"

    val dinnerMenu = todayMeal?.dinner?.items?.joinToString(", ") ?: "Steamed Rice, Fish Curry, Dal"
    val dinnerTime = todayMeal?.dinner?.let { "Main Canteen · ${it.startTime} – ${it.endTime}" } ?: "Main Canteen · 07:30 – 09:00 PM"
    val dinnerGuestInfo = todayMeal?.dinner?.maxGuests?.let { "Up to $it guests" } ?: "Up to 3 guests"
    // ─────────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // --- Header ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.duet_logo),
                            contentDescription = "DUET Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(
                            text = "DUET MEAL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Meal Management System",
                            fontSize = 10.sp,
                            color = Muted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onNavigate(Screen.Notices.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notices",
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(8.dp)
                                .background(BrandDark, CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }

                    // Logout Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                viewModel.logout {
                                    onNavigate(Screen.Login.route)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Log Out",
                            tint = StatusRedText,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning",
                        fontSize = 12.sp,
                        color = Muted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = displayName,                  // ← API: user.fullName
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BrandLight)
                        .clickable { onNavigate(Screen.Profile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,                     // ← API: user.initials
                        fontWeight = FontWeight.Bold,
                        color = BrandDark,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // --- Balance Card ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(BrandPrimary, BrandDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Canteen Wallet",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = totalBalance,             // ← API: wallet.totalBalance
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Available: $availableBalance", // ← API: wallet.availableBalance
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Button(
                        onClick = { onNavigate(Screen.Payments.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Recharge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Services & Menu Section ---
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(16.dp)
                            .background(BrandDark, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Services & Menu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
                Text(
                    text = "Quick Access",
                    fontSize = 12.sp,
                    color = Muted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "Meal Booking",
                    subtitle = "Advance & Guest",
                    badge = "Book",
                    badgeBg = BrandLight,
                    badgeTextColor = BrandDark,
                    icon = Icons.Outlined.CalendarMonth,
                    iconBg = BrandLight,
                    iconTint = BrandDark,
                    onClick = { onNavigate(Screen.Booking.route) }
                )

                QuickServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "Meal History",
                    subtitle = "Past & Monthly",
                    badge = "History",
                    badgeBg = StatusGreenBg,
                    badgeTextColor = StatusGreenText,
                    icon = Icons.Outlined.Receipt,
                    iconBg = StatusGreenBg,
                    iconTint = StatusGreenText,
                    onClick = { onNavigate(Screen.History.route) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "Payments",
                    subtitle = "Recharge & History",
                    badge = "Wallet",
                    badgeBg = StatusAmberBg,
                    badgeTextColor = StatusAmberText,
                    icon = Icons.Outlined.CreditCard,
                    iconBg = StatusAmberBg,
                    iconTint = StatusAmberText,
                    onClick = { onNavigate(Screen.Payments.route) }
                )

                QuickServiceCard(
                    modifier = Modifier.weight(1f),
                    title = "Notices",
                    subtitle = "Canteen Bulletins",
                    badge = "3 New",
                    badgeBg = StatusBlueBg,
                    badgeTextColor = StatusBlueText,
                    icon = Icons.Outlined.Campaign,
                    iconBg = StatusBlueBg,
                    iconTint = StatusBlueText,
                    onClick = { onNavigate(Screen.Notices.route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Today's Meals Section ---
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(16.dp)
                            .background(BrandDark, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Meals",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
                Text(
                    text = "Friday, 24 May",
                    fontSize = 12.sp,
                    color = Muted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lunch Card
            MealCard(
                statusBadge = if (isLunchBooked) "BOOKED (Self + 2 Guests)" else "NOT BOOKED",
                isBooked = isLunchBooked,
                mealName = "Lunch",
                time = lunchTime,                            // ← API: todayMeal.lunch
                menuItems = lunchMenu,                       // ← API: todayMeal.lunch.items
                icon = Icons.Outlined.WbSunny,
                footerLeftText = lunchCutoff,               // ← API: todayMeal.lunch.cutoffTime
                buttonText = if (isLunchBooked) "Cancel Lunch" else "Book Lunch",
                onButtonClick = {
                    isLunchBooked = !isLunchBooked
                    Toast.makeText(context, if (isLunchBooked) "Lunch Booked" else "Lunch Cancelled", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dinner Card
            MealCard(
                statusBadge = if (isDinnerBooked) "BOOKED (Self)" else "NOT BOOKED",
                isBooked = isDinnerBooked,
                mealName = "Dinner",
                time = dinnerTime,                           // ← API: todayMeal.dinner
                menuItems = dinnerMenu,                      // ← API: todayMeal.dinner.items
                icon = Icons.Outlined.NightsStay,
                footerLeftText = dinnerGuestInfo,            // ← API: todayMeal.dinner.maxGuests
                buttonText = if (isDinnerBooked) "Cancel Dinner" else "Book Dinner",
                onButtonClick = {
                    if (!isDinnerBooked) {
                        onNavigate(Screen.Booking.route)
                    } else {
                        isDinnerBooked = false
                        Toast.makeText(context, "Dinner Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickServiceCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badge: String,
    badgeBg: Color,
    badgeTextColor: Color,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Muted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MealCard(
    statusBadge: String,
    isBooked: Boolean,
    mealName: String,
    time: String,
    menuItems: String,
    icon: ImageVector,
    footerLeftText: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        color = Color.White,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isBooked) StatusGreenBg else Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isBooked) StatusGreen else Color(0xFF94A3B8),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBooked) StatusGreenText else Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = mealName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = Muted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mealName,
                        tint = if (isBooked) BrandDark else Muted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = menuItems,
                fontSize = 13.sp,
                color = Ink.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = footerLeftText,
                    fontSize = 11.sp,
                    color = Muted,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBooked) Color.Transparent else BrandDark,
                        contentColor = if (isBooked) BrandDark else Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
