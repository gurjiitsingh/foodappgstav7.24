package com.it10x.foodappgstav7_24.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_24.ui.bill.PaymentButton
import com.it10x.foodappgstav7_24.utils.MoneyUtils
import com.it10x.foodappgstav7_24.utils.formatter.MoneyFormatter

@Composable
fun PaymentButtonsSection(
    remainingPaise: Long,
    onPay: (String, Long) -> Unit,
    onMoreClick: () -> Unit,

    currencyCode: String,
    localeTag: String,

    isCreditSelected: Boolean,
    showRemainingOptions: Boolean,
    onCreditClick: () -> Unit,
    onPayLaterClick: () -> Unit,

    creditContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Spacer(Modifier.height(6.dp))

        Text(
            "Select Options",
            style = MaterialTheme.typography.titleSmall
        )

        // 🔘 CREDIT + PAY LATER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Button(
                onClick = onCreditClick,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text("💳 Credit", fontSize = 13.sp)
            }

            Button(
                onClick = onPayLaterClick,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E9E9E),
                    contentColor = Color.White
                )
            ) {
                Text("🕒 Pay Later", fontSize = 13.sp)
            }
        }

        // ✅ CREDIT INPUT
        if (isCreditSelected) {
            creditContent?.invoke()
        }

        // ✅ REMAINING TEXT
        if (showRemainingOptions && remainingPaise > 0) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Pay Remaining: ${
                    MoneyFormatter.format(
                        amount = MoneyUtils.fromPaise(remainingPaise),
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )
                }",
                style = MaterialTheme.typography.titleSmall
            )
        }

        // 💵 CASH + CARD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PaymentButton(
                modifier = Modifier.weight(1f),
                text = "💵 Cash",
                color = Color(0xFF4CAF50)
            ) { onPay("CASH", remainingPaise) }

            PaymentButton(
                modifier = Modifier.weight(1f),
                text = "💳 Card",
                color = Color(0xFF1976D2)
            ) { onPay("CARD", remainingPaise) }
        }

        // 📱 UPI + MORE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PaymentButton(
                modifier = Modifier.weight(1f),
                text = "📱 UPI",
                color = Color(0xFFFF9800)
            ) { onPay("UPI", remainingPaise) }

            PaymentButton(
                modifier = Modifier.weight(1f),
                text = "More",
                color = Color(0xFF9C27B0)
            ) { onMoreClick() }
        }
    }
}