package com.example.duetmeal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.duetmeal.ui.theme.*

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Home")
    object History : Screen("history", "History")
    object Profile : Screen("profile", "Profile")
    object Payments : Screen("payments", "Payments")
    object Notices : Screen("notices", "Notice")
    object Booking : Screen("booking", "Book Meals")
}

@Composable
fun DUETBottomNavigationBar(
    currentRoute: String,
    initials: String = "FH",
    onNavigate: (String) -> Unit
) {
    // Wrap in a Box with unbounded height measurement + high zIndex so the
    // protruding profile circle is never clipped by the Scaffold body layer.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
            .zIndex(10f)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .shadow(16.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = Color.White.copy(alpha = 0.98f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // 1. Home
                NavItem(
                    title = "Home",
                    icon = Icons.Outlined.Home,
                    selectedIcon = Icons.Filled.Home,
                    isSelected = currentRoute == Screen.Home.route,
                    onClick = { onNavigate(Screen.Home.route) }
                )

                // 2. History
                NavItem(
                    title = "History",
                    icon = Icons.Outlined.Receipt,
                    selectedIcon = Icons.Filled.Receipt,
                    isSelected = currentRoute == Screen.History.route,
                    onClick = { onNavigate(Screen.History.route) }
                )

                // 3. Center Profile — floats above the bar
                Box(
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .zIndex(20f)
                        .clickable { onNavigate(Screen.Profile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(10.dp, CircleShape)
                            .background(
                                if (currentRoute == Screen.Profile.route) BrandDark else SurfaceBg,
                                CircleShape
                            )
                            .border(
                                width = 3.dp,
                                color = if (currentRoute == Screen.Profile.route) BrandDark else Color.White,
                                shape = CircleShape
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(BrandLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials.ifEmpty { "FH" },
                            fontWeight = FontWeight.Bold,
                            color = BrandDark,
                            fontSize = 18.sp
                        )
                    }
                }

                // 4. Payments
                NavItem(
                    title = "Payments",
                    icon = Icons.Outlined.CreditCard,
                    selectedIcon = Icons.Filled.CreditCard,
                    isSelected = currentRoute == Screen.Payments.route,
                    onClick = { onNavigate(Screen.Payments.route) }
                )

                // 5. Notices
                NavItem(
                    title = "Notice",
                    icon = Icons.Outlined.Campaign,
                    selectedIcon = Icons.Filled.Campaign,
                    isSelected = currentRoute == Screen.Notices.route,
                    badgeCount = 3,
                    onClick = { onNavigate(Screen.Notices.route) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = title,
                tint = if (isSelected) BrandDark else Muted,
                modifier = Modifier.size(24.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(BrandDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandDark else Muted
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .background(BrandDark, CircleShape)
            )
        }
    }
}
