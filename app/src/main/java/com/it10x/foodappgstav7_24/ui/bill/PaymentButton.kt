package com.it10x.foodappgstav7_24.ui.bill


import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp




@Composable
fun PaymentButton(
    modifier: Modifier = Modifier,   // 🔥 ADD THIS
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,         // 🔥 USE IT HERE
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 13.sp
        )
    }
}