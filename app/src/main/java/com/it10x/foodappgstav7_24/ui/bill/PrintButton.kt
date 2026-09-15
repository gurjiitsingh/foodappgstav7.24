package com.it10x.foodappgstav7_24.ui.bill
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun PrintButton(
    onPrint: () -> Unit
) {
    Button(
        onClick = onPrint,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2E7D32), // POS green
            contentColor = Color.White
        )
    ) {
        Text(
            text = "🖨️ Print Bill",
            fontSize = 16.sp
        )
    }
}