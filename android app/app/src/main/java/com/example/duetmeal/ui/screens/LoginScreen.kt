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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duetmeal.R
import com.example.duetmeal.data.ApiState
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: DuetMealViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("fazlul.hasan@duet.edu.bd") }
    var password by remember { mutableStateOf("password123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val apiState by viewModel.apiState.collectAsState()
    val isLoading = apiState is ApiState.Loading

    fun performLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter your DUET email and password"
            return
        }
        focusManager.clearFocus()
        errorMessage = null

        viewModel.login(
            email = email,
            pass = password,
            onSuccess = {
                onLoginSuccess()
            },
            onError = { error ->
                errorMessage = error
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Top Brand Header ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.duet_logo),
                    contentDescription = "DUET Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "DUET MEAL",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                letterSpacing = 1.sp
            )

            Text(
                text = "Meal Management System",
                fontSize = 12.sp,
                color = Muted,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Login Card ───────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = "Access your digital dormitory wallet",
                        fontSize = 12.sp,
                        color = Muted,
                        modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                    )

                    // Error Message Banner
                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            color = StatusRedBg,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Error",
                                    tint = StatusRedText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = errorMessage ?: "",
                                    color = StatusRedText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Email Field
                    Text(
                        text = "DUET Email",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("example@duet.edu.bd", fontSize = 13.sp, color = Muted.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = BrandDark, modifier = Modifier.size(20.dp))
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = BrandDark
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", fontSize = 13.sp, color = Muted.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = BrandDark, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = Muted
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = BrandDark
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { performLogin() })
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign In Button
                    Button(
                        onClick = { performLogin() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Quick-Fill Preset Credentials ────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        email = "fazlul.hasan@duet.edu.bd"
                        password = "password123"
                        errorMessage = null
                    },
                color = BrandLight.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = BrandDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Demo Account (Tap to Fill)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDark
                            )
                            Text(
                                text = "fazlul.hasan@duet.edu.bd • password123",
                                fontSize = 10.sp,
                                color = Muted
                            )
                        }
                    }

                    Text(
                        text = "Fill",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer info
            Text(
                text = "Dhaka University of Engineering & Technology",
                fontSize = 11.sp,
                color = Muted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
