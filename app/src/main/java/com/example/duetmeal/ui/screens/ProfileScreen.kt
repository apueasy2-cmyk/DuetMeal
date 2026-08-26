package com.example.duetmeal.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duetmeal.data.ApiState
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.ui.components.Screen
import com.example.duetmeal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: DuetMealViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val apiState by viewModel.apiState.collectAsState()
    val isUpdating = apiState is ApiState.Loading

    var fullName by remember { mutableStateOf("Dr. Fazlul Hasan") }
    var email by remember { mutableStateOf("fazlul.hasan@duet.edu.bd") }
    var phone by remember { mutableStateOf("+880 1234 567890") }

    var isInsideResident by remember { mutableStateOf(true) } // true = Inside, false = Outside
    var isOfficer by remember { mutableStateOf(true) } // true = Officer, false = Teacher

    var mealReminder by remember { mutableStateOf(true) }
    var autoBooking by remember { mutableStateOf(false) }

    // Sync initial state whenever user profile arrives from API
    LaunchedEffect(user) {
        user?.let { u ->
            if (u.fullName.isNotEmpty()) fullName = u.fullName
            if (u.email.isNotEmpty()) email = u.email
            if (u.phone.isNotEmpty()) phone = u.phone
            isInsideResident = u.residentType.lowercase() == "inside"
            isOfficer = u.userType.lowercase() == "officer"
            mealReminder = u.preferences.mealReminder
            autoBooking = u.preferences.autoBooking
        }
    }

    val initials = user?.initials?.ifEmpty { "FH" } ?: "FH"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                    Text(text = "Edit Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }

            // Logout icon button
            IconButton(
                onClick = {
                    viewModel.logout {
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        onNavigate(Screen.Login.route)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = StatusRedText
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // Main Profile Form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Profile Info Card
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(BrandLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDark
                            )
                        }

                        Column {
                            Text(text = "Profile Photo", fontSize = 13.sp, color = Muted)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Opening photo picker...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(text = "Change Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = null, tint = BrandDark, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Full Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = BrandDark
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Email Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = BrandDark
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Phone Number", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = BrandDark
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resident Area & User Type Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Resident & Role", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(text = "Select your area and user category", fontSize = 11.sp, color = Muted)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resident Area Toggle
                    Text(text = "Resident Area", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Inside Resident" to true, "Outside Resident" to false).forEach { (label, value) ->
                            val selected = isInsideResident == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) BrandDark else Color.Transparent)
                                    .clickable { isInsideResident = value }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) Color.White else Muted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // User Type Toggle
                    Text(text = "User Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Officer" to true, "Teacher" to false).forEach { (label, value) ->
                            val selected = isOfficer == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) BrandDark else Color.Transparent)
                                    .clickable { isOfficer = value }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) Color.White else Muted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preferences Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(text = "Meal notifications and reminders", fontSize = 11.sp, color = Muted)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Meal Reminder", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
                        Switch(
                            checked = mealReminder,
                            onCheckedChange = { mealReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Auto Booking", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
                        Switch(
                            checked = autoBooking,
                            onCheckedChange = { autoBooking = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandDark
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Changes Button
            Button(
                onClick = {
                    viewModel.updateProfile(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        residentType = if (isInsideResident) "inside" else "outside",
                        userType = if (isOfficer) "officer" else "teacher",
                        mealReminder = mealReminder,
                        autoBooking = autoBooking,
                        onSuccess = {
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !isUpdating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(text = "Save Changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout Button
            OutlinedButton(
                onClick = {
                    viewModel.logout {
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        onNavigate(Screen.Login.route)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = StatusRedText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Log Out", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRedText)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
