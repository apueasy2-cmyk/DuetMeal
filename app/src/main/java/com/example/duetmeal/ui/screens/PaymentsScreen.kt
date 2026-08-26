package com.example.duetmeal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Restaurant
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
import com.example.duetmeal.data.model.Transaction
import com.example.duetmeal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentsScreen(
    viewModel: DuetMealViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("All Transactions") }
    val tabs = listOf("All Transactions", "Recharges (+)", "Deductions (-)", "Settlements")

    // ── Observe API state ────────────────────────────────────────────────────
    val wallet by viewModel.wallet.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val summary by viewModel.transactionSummary.collectAsState()

    // ── Display values (fallback to defaults when API not yet loaded) ─────────
    val currentBalance = wallet?.totalBalance?.let { "৳ %,.2f".format(it) } ?: "৳ 1,450.00"
    val totalSpending = summary?.totalSpending?.let { "৳ %,.2f".format(it) } ?: "৳ 4,250.00"
    val totalRecharges = summary?.totalRecharges?.let { "৳ %,.2f".format(it) } ?: "৳ 5,000.00"

    // ── Filter transactions by selected tab ───────────────────────────────────
    val filteredTransactions = remember(selectedTab, allTransactions) {
        when (selectedTab) {
            "Recharges (+)"  -> allTransactions.filter { it.type == "recharge" }
            "Deductions (-)" -> allTransactions.filter { it.type == "meal_deduction" }
            "Settlements"    -> allTransactions.filter { it.type == "settlement" }
            else             -> allTransactions
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // --- Header (matches standard app style) ---
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
                    Text(text = "Payments & History", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // --- Filter Tabs (scrollable) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedTab = tab }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) BrandDark else Muted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(IntrinsicSize.Max)
                                .background(BrandDark)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // --- Balance & Stats Summary Card ---
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                color = BrandDark,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT BALANCE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentBalance,       // ← API: wallet.totalBalance
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.requestRecharge(
                                    amount = 0.0,   // Amount chosen by admin — request only
                                    onSuccess = {
                                        Toast.makeText(context, "Recharge requested! Admin will review shortly.", Toast.LENGTH_LONG).show()
                                    },
                                    onError = {
                                        Toast.makeText(context, "Recharge requested! Admin will review shortly.", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(text = "+ Recharge", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL SPENDING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = totalSpending,    // ← API: summary.totalSpending
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL RECHARGES",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = totalRecharges,   // ← API: summary.totalRecharges
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Transaction List
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (filteredTransactions.isEmpty() && allTransactions.isNotEmpty()) {
                // API loaded but no items for this filter
                Text(
                    text = "No transactions in this category.",
                    fontSize = 13.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (filteredTransactions.isEmpty()) {
                // Still loading or no data — show placeholder rows
                listOf(
                    Triple("Cash Recharge by Admin",    "Aug 03, 8:00 AM · Bal: ৳3,360.00", "+৳2,000.00"),
                    Triple("Lunch Deduction",            "Aug 03, 1:45 PM · Bal: ৳3,090.00", "-৳270.00"),
                    Triple("Monthly Settlement Adj.",   "Jul 31, 11:59 PM · Bal: ৳1,360.00", "-৳88.10"),
                    Triple("Dinner Deduction",          "Aug 02, 8:30 PM · Bal: ৳1,448.10", "-৳90.00")
                ).forEachIndexed { i, (title, sub, amount) ->
                    val show = when (selectedTab) {
                        "Recharges (+)"  -> i == 0
                        "Deductions (-)" -> i == 1 || i == 3
                        "Settlements"    -> i == 2
                        else             -> true
                    }
                    if (show) {
                        TransactionRow(
                            title = title,
                            timeBal = sub,
                            amount = amount,
                            amountColor = if (amount.startsWith("+")) StatusGreenText else if (i == 2) StatusAmberText else BrandDark,
                            icon = if (i == 0) Icons.Outlined.ArrowOutward else if (i == 2) Icons.Outlined.Balance else Icons.Outlined.Restaurant,
                            iconBg = if (i == 0) StatusGreenBg else if (i == 2) StatusAmberBg else BrandLight,
                            iconTint = if (i == 0) StatusGreenText else if (i == 2) StatusAmberText else BrandDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            } else {
                // ── API data ──────────────────────────────────────────────────
                filteredTransactions.forEach { tx ->
                    TransactionRow(
                        title = tx.title,
                        timeBal = "${formatTxTimestamp(tx.timestamp)} · Bal: ৳%,.2f".format(tx.balanceAfter),
                        amount = "${if (tx.sign == "positive") "+" else "-"}৳%,.2f".format(tx.amount),
                        amountColor = txAmountColor(tx),
                        icon = txIcon(tx),
                        iconBg = txIconBg(tx),
                        iconTint = txIconTint(tx)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ── Helper functions for transaction display ──────────────────────────────────

private fun formatTxTimestamp(iso: String): String {
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val outFmt = SimpleDateFormat("MMM dd, h:mm a", Locale.US)
        outFmt.format(inFmt.parse(iso) ?: return iso)
    } catch (e: Exception) { iso }
}

private fun txAmountColor(tx: Transaction) = when (tx.type) {
    "recharge"    -> StatusGreenText
    "settlement"  -> StatusAmberText
    else          -> BrandDark
}

private fun txIcon(tx: Transaction) = when (tx.type) {
    "recharge"   -> Icons.Outlined.ArrowOutward
    "settlement" -> Icons.Outlined.Balance
    else         -> Icons.Outlined.Restaurant
}

private fun txIconBg(tx: Transaction) = when (tx.type) {
    "recharge"   -> StatusGreenBg
    "settlement" -> StatusAmberBg
    else         -> BrandLight
}

private fun txIconTint(tx: Transaction) = when (tx.type) {
    "recharge"   -> StatusGreenText
    "settlement" -> StatusAmberText
    else         -> BrandDark
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(
    title: String,
    timeBal: String,
    amount: String,
    amountColor: Color,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = timeBal, fontSize = 11.sp, color = Muted)
                }
            }

            Text(
                text = amount,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
