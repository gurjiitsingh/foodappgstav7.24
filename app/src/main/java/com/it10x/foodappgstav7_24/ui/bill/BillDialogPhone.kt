package com.it10x.foodappgstav7_24.ui.bill

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_24.ui.payment.PaymentInput
import com.it10x.foodappgstav7_24.ui.pos.PosSessionViewModel

import android.widget.Toast

import androidx.compose.ui.text.style.TextAlign

import com.it10x.foodappgstav7_24.ui.components.NumPad

import com.it10x.foodappgstav7_24.ui.theme.PosTheme

@Composable
fun BillDialogPhone(
    showBill: Boolean,
    onDismiss: () -> Unit,
    sessionId: String?,
    tableId: String?,
    orderType: String,
    localeTag: String,
    currencyCode: String,
    selectedTableName: String,
    posSessionViewModel: PosSessionViewModel,
) {

    if (!showBill || sessionId == null) return

    val context = LocalContext.current

    // =========================================================
    // STATE
    // =========================================================

    var activeInput by remember { mutableStateOf<String?>(null) }

    val discountFlat = remember { mutableStateOf("") }
    val discountPercent = remember { mutableStateOf("") }
    val creditAmount = remember { mutableStateOf("") }
    val deliveryFee = remember { mutableStateOf("") }

    var showDiscount by remember { mutableStateOf(false) }
    var showDelivery by remember { mutableStateOf(false) }

    var showRemainingOptions by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    var isPrinted by remember { mutableStateOf(false) }
    var isCreditSelected by remember { mutableStateOf(false) }

    // =========================================================
    // BILL VIEW MODEL
    // =========================================================

    val billViewModel: BillViewModel = viewModel(
        key = "BillVM_PHONE_${sessionId}_${orderType}",
        factory = BillViewModelFactory(
            application =
                (context.applicationContext as? Application)
                    ?: throw IllegalStateException("Application not found"),

            tableId = tableId ?: orderType,

            tableName = selectedTableName,

            orderType = orderType,

            posSessionViewModel = posSessionViewModel
        )
    )

    // =========================================================
    // TOAST EVENTS
    // =========================================================

    LaunchedEffect(Unit) {

        billViewModel.toastEvent.collect { message ->

            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // UI STATE
    // =========================================================

    val uiState by billViewModel.uiState.collectAsState()

    val suggestions by billViewModel.customerSuggestions.collectAsState()

    val remainingPaise by billViewModel.remainingPaise.collectAsState()

    // =========================================================
    // CLOSE WHEN BILL BECOMES EMPTY
    // =========================================================

    var hasLoadedItems by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(uiState.items) {

        if (uiState.items.isNotEmpty()) {
            hasLoadedItems = true
        }

        if (
            hasLoadedItems &&
            uiState.items.isEmpty()
        ) {
            onDismiss()
        }
    }

    // =========================================================
    // LOAD EXISTING DISCOUNT / DELIVERY
    // =========================================================

    LaunchedEffect(showBill) {

        if (showBill) {

            if (uiState.discountFlat > 0) {

                discountFlat.value =
                    uiState.discountFlat.toString()

                discountPercent.value = ""

                showDiscount = true

            } else if (uiState.discountPercent > 0) {

                discountPercent.value =
                    uiState.discountPercent.toString()

                discountFlat.value = ""

                showDiscount = true

            } else {

                discountFlat.value = ""
                discountPercent.value = ""
                showDiscount = false
            }

            if (uiState.deliveryFee > 0) {

                deliveryFee.value =
                    uiState.deliveryFee.toString()

                showDelivery = true

            } else {

                deliveryFee.value = ""
                showDelivery = false
            }
        }
    }

    // =========================================================
    // MAIN PHONE DIALOG
    // =========================================================

    Dialog(
        onDismissRequest = {

            if (!isPrinted) {
                onDismiss()
            }
        },

        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),

            shape = RoundedCornerShape(12.dp),

            color = PosTheme.bill.billBg,

            contentColor = PosTheme.bill.billText,

            tonalElevation = 0.dp,

            shadowElevation = 8.dp
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {

                // =================================================
                // HEADER
                // =================================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                if (selectedTableName.isNotBlank())
                                    "Final Bill - $selectedTableName"
                                else
                                    "Final Bill",

                            fontSize = 18.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                PosTheme.bill.billText,

                            maxLines = 1
                        )

                        Text(
                            text = when (orderType) {
                                "DINE_IN" -> "Dine In"
                                "TAKEAWAY" -> "Takeaway"
                                "DELIVERY" -> "Delivery"
                                else -> orderType
                            },

                            fontSize = 12.sp,

                            color =
                                PosTheme.bill.billText
                                    .copy(alpha = 0.65f)
                        )
                    }

                    Button(
                        onClick = {

                            if (!isPrinted) {
                                onDismiss()
                            }
                        },

                        enabled = !isPrinted,

                        modifier = Modifier
                            .height(32.dp)
                            .width(72.dp),

                        contentPadding =
                            PaddingValues(0.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    PosTheme.bill.danger,

                                contentColor =
                                    Color.White,

                                disabledContainerColor =
                                    PosTheme.bill.danger
                                        .copy(alpha = 0.35f),

                                disabledContentColor =
                                    Color.White.copy(alpha = 0.6f)
                            )
                    ) {

                        Text(
                            text = "Close",
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(
                    color =
                        PosTheme.bill.billText
                            .copy(alpha = 0.25f)
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                // =================================================
                // BILL ITEMS
                // =================================================

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    color = PosTheme.bill.billTab,

                    shape = RoundedCornerShape(10.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        Text(
                            text = "Bill Items",

                            fontSize = 15.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                PosTheme.bill.billText
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        BillScreen(
                            viewModel = billViewModel,

                            onPayClick = { paymentType ->

                                val totalAmount =
                                    billViewModel.totalPaise

                                billViewModel.payBill(

                                    payments = listOf(
                                        PaymentInput(
                                            mode =
                                                paymentType.name,

                                            amount =
                                                totalAmount
                                        )
                                    ),

                                    name = "Customer",

                                    phone =
                                        uiState.customerPhone
                                )
                            },

                            currencyCode =
                                currencyCode,

                            localeTag =
                                localeTag
                        )
                    }
                }

                // =================================================
// SCROLLABLE CONTROLS AREA
// =================================================

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // =================================================
                    // CUSTOMER PHONE
                    // =================================================



                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activeInput = "PHONE"
                        }
                )
                {

                    OutlinedTextField(

                        value =
                            uiState.customerPhone,

                        onValueChange = {},

                        label = {
                            Text("Customer Phone")
                        },

                        enabled = false,

                        readOnly = true,

                        singleLine = true,

                        colors =
                            OutlinedTextFieldDefaults.colors(

                                disabledContainerColor =
                                    if (activeInput == "PHONE")
                                        PosTheme.bill.inputActiveBg
                                    else
                                        PosTheme.bill.inputBg,

                                disabledBorderColor =
                                    if (activeInput == "PHONE")
                                        PosTheme.bill.inputActiveBorder
                                    else
                                        PosTheme.bill.inputBorder,

                                disabledTextColor =
                                    PosTheme.bill.billText,

                                disabledLabelColor =
                                    PosTheme.bill.billText
                                        .copy(alpha = 0.7f)
                            ),

                        textStyle =
                            LocalTextStyle.current.copy(
                                fontSize = 15.sp
                            ),

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }

                // =================================================
                // CUSTOMER SUGGESTIONS
                // =================================================

                if (
                    suggestions.isNotEmpty() &&
                    activeInput == "PHONE"
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    PosTheme.bill.inputBg
                            )
                    ) {

                        Column {

                            suggestions.forEach { customer ->

                                Row(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {

                                            billViewModel
                                                .setCustomerPhone(
                                                    customer.phone
                                                )

                                            billViewModel
                                                .clearCustomerSuggestions()

                                            activeInput = null
                                        }
                                        .padding(8.dp)
                                ) {

                                    Text(
                                        text =
                                            "${customer.phone}  (${customer.name})",

                                        color =
                                            PosTheme.bill.billText,

                                        fontSize = 13.sp,

                                        maxLines = 1
                                    )
                                }

                                HorizontalDivider(
                                    color =
                                        PosTheme.bill.billText
                                            .copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                // =================================================
                // DISCOUNT TOGGLE
                // =================================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Enable Discount",

                        fontSize = 13.sp,

                        fontWeight =
                            FontWeight.Medium,

                        color =
                            PosTheme.bill.billText
                                .copy(alpha = 0.75f)
                    )

                    Switch(
                        checked = showDiscount,

                        onCheckedChange = {
                            showDiscount = it
                        },

                        colors =
                            SwitchDefaults.colors(

                                checkedThumbColor =
                                    PosTheme.bill.warning,

                                checkedTrackColor =
                                    PosTheme.bill.warning
                                        .copy(alpha = 0.35f),

                                uncheckedThumbColor =
                                    PosTheme.bill.billText
                                        .copy(alpha = 0.6f),

                                uncheckedTrackColor =
                                    PosTheme.bill.inputBorder
                                        .copy(alpha = 0.35f),

                                uncheckedBorderColor =
                                    PosTheme.bill.inputBorder
                            )
                    )
                }

                // =================================================
                // DISCOUNT INPUT
                // =================================================

                if (showDiscount) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {

                                    activeInput = "FLAT"

                                    discountPercent.value = ""

                                    billViewModel
                                        .setPercentDiscount(0.0)
                                }
                        ) {

                            OutlinedTextField(

                                value =
                                    discountFlat.value,

                                onValueChange = {},

                                label = {
                                    Text(
                                        "Flat",
                                        fontSize = 12.sp
                                    )
                                },

                                enabled = false,

                                readOnly = true,

                                singleLine = true,

                                colors =
                                    OutlinedTextFieldDefaults.colors(

                                        disabledContainerColor =
                                            if (activeInput == "FLAT")
                                                PosTheme.bill.inputActiveBg
                                            else
                                                PosTheme.bill.inputBg,

                                        disabledBorderColor =
                                            if (activeInput == "FLAT")
                                                PosTheme.bill.inputActiveBorder
                                            else
                                                PosTheme.bill.inputBorder,

                                        disabledTextColor =
                                            PosTheme.bill.billText,

                                        disabledLabelColor =
                                            PosTheme.bill.billText
                                                .copy(alpha = 0.7f)
                                    ),

                                modifier =
                                    Modifier.fillMaxWidth()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {

                                    activeInput = "PERCENT"

                                    discountFlat.value = ""

                                    billViewModel
                                        .setFlatDiscount(0.0)
                                }
                        ) {

                            OutlinedTextField(

                                value =
                                    discountPercent.value,

                                onValueChange = {},

                                label = {
                                    Text(
                                        "%",
                                        fontSize = 12.sp
                                    )
                                },

                                enabled = false,

                                readOnly = true,

                                singleLine = true,

                                colors =
                                    OutlinedTextFieldDefaults.colors(

                                        disabledContainerColor =
                                            if (activeInput == "PERCENT")
                                                PosTheme.bill.inputActiveBg
                                            else
                                                PosTheme.bill.inputBg,

                                        disabledBorderColor =
                                            if (activeInput == "PERCENT")
                                                PosTheme.bill.inputActiveBorder
                                            else
                                                PosTheme.bill.inputBorder,

                                        disabledTextColor =
                                            PosTheme.bill.billText,

                                        disabledLabelColor =
                                            PosTheme.bill.billText
                                                .copy(alpha = 0.7f)
                                    ),

                                modifier =
                                    Modifier.fillMaxWidth()
                            )
                        }

                        IconButton(
                            onClick = {

                                if (!isPrinted) {

                                    discountFlat.value = ""

                                    discountPercent.value = ""

                                    billViewModel
                                        .setFlatDiscount(0.0)

                                    billViewModel
                                        .setPercentDiscount(0.0)

                                    activeInput = null
                                }
                            },

                            enabled = !isPrinted,

                            modifier = Modifier.size(32.dp)
                        ) {

                            Text(
                                text = "✕",
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                // =================================================
                // DELIVERY TOGGLE
                // =================================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            "Enable Delivery Charges",

                        fontSize = 13.sp,

                        fontWeight =
                            FontWeight.Medium,

                        color =
                            PosTheme.bill.billText
                                .copy(alpha = 0.75f)
                    )

                    Switch(
                        checked = showDelivery,

                        onCheckedChange = {
                            showDelivery = it
                        }
                    )
                }

                // =================================================
                // DELIVERY INPUT
                // =================================================

                if (showDelivery) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeInput = "DELIVERY"
                                }
                        ) {

                            OutlinedTextField(

                                value =
                                    deliveryFee.value,

                                onValueChange = {},

                                label = {
                                    Text("Delivery")
                                },

                                enabled = false,

                                readOnly = true,

                                singleLine = true,

                                colors =
                                    OutlinedTextFieldDefaults.colors(

                                        disabledContainerColor =
                                            if (activeInput == "DELIVERY")
                                                PosTheme.bill.inputActiveBg
                                            else
                                                PosTheme.bill.inputBg,

                                        disabledBorderColor =
                                            if (activeInput == "DELIVERY")
                                                PosTheme.bill.inputActiveBorder
                                            else
                                                PosTheme.bill.inputBorder,

                                        disabledTextColor =
                                            PosTheme.bill.billText,

                                        disabledLabelColor =
                                            PosTheme.bill.billText
                                                .copy(alpha = 0.7f)
                                    ),

                                modifier =
                                    Modifier.fillMaxWidth()
                            )
                        }

                        IconButton(
                            onClick = {

                                if (!isPrinted) {

                                    deliveryFee.value = ""

                                    activeInput = null

                                    billViewModel
                                        .setDeliveryFee(0.0)
                                }
                            },

                            enabled = !isPrinted,

                            modifier = Modifier.size(32.dp)
                        ) {

                            Text(
                                text = "✕",
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                // =================================================
                // PRINT / PAYMENT
                // =================================================

                if (!isPrinted) {

                    PrintButton(
                        onPrint = {

                            billViewModel
                                .printCurrentBill()

                            isPrinted = true
                        }
                    )

                } else {

                    // =================================================
                    // PAYMENT BUTTONS
                    // =================================================

                    PaymentButtonsSection(

                        remainingPaise =
                            remainingPaise,

                        onPay = { mode, amount ->

                            billViewModel.payBill(

                                payments = listOf(
                                    PaymentInput(
                                        mode = mode,
                                        amount = amount
                                    )
                                ),

                                name = "Customer",

                                phone =
                                    uiState.customerPhone
                            )
                        },

                        onMoreClick = {
                            showMoreOptions = true
                        },

                        currencyCode =
                            currencyCode,

                        localeTag =
                            localeTag,

                        isCreditSelected =
                            isCreditSelected,

                        showRemainingOptions =
                            showRemainingOptions,

                        onCreditClick = {

                            billViewModel.clearCredit()

                            val paise =
                                remainingPaise

                            val rupees =
                                paise / 100

                            val paisaPart =
                                paise % 100

                            creditAmount.value =
                                if (paisaPart == 0L) {

                                    rupees.toString()

                                } else {

                                    "$rupees.${
                                        paisaPart
                                            .toString()
                                            .padStart(2, '0')
                                    }"
                                }

                            activeInput = "CREDIT"

                            isCreditSelected = true

                            showRemainingOptions = false
                        },

                        onPayLaterClick = {

                            val phone =
                                uiState.customerPhone.trim()

                            if (phone.length != 10) {

                                Toast.makeText(
                                    context,
                                    "Enter valid 10 digit phone number",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@PaymentButtonsSection
                            }

                            billViewModel.payBill(

                                payments = listOf(
                                    PaymentInput(
                                        mode =
                                            "DELIVERY_PENDING",

                                        amount =
                                            remainingPaise
                                    )
                                ),

                                name = "Customer",

                                phone = phone
                            )
                        },

                        creditContent = {

                            Column(
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    OutlinedTextField(

                                        value =
                                            creditAmount.value,

                                        onValueChange = {},

                                        label = {
                                            Text("Credit")
                                        },

                                        readOnly = true,

                                        modifier =
                                            Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {

                                            creditAmount.value = ""

                                            activeInput = null

                                            isCreditSelected = false
                                        }
                                    ) {

                                        Text("✕")
                                    }

                                    IconButton(onClick = {

                                        val input = creditAmount.value.trim()
                                        val parts = input.split(".")

                                        val rupees = parts.getOrNull(0)?.toLongOrNull() ?: 0L
                                        val paise = parts.getOrNull(1)?.padEnd(2, '0')?.take(2)?.toLongOrNull() ?: 0L
                                        val enteredPaise = rupees * 100 + paise

                                        if (enteredPaise <= 0 || enteredPaise > remainingPaise) return@IconButton

                                        billViewModel.setCreditAmountRaw(input)

                                        val totalPaise = billViewModel.totalPaise

                                        if (enteredPaise == totalPaise) {
//                                            billViewModel.payBill(
//                                                payments = emptyList(),
//                                                name = "Customer",
//                                                phone = uiState.value.customerPhone
//                                            )
                                        } else {
                                            showRemainingOptions = true
                                        }

                                        creditAmount.value = ""
                                        activeInput = null
                                        isCreditSelected = false

                                    }) {
                                        Text("✔")
                                    }
                            }
                        }
                }
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // =================================================
            // UNLOCK AFTER PRINT
            // =================================================

            if (isPrinted) {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.Center
                ) {

                    Button(
                        onClick = {
                            isPrinted = false
                        },

                        modifier = Modifier
                            .height(32.dp)
                            .width(90.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    PosTheme.bill.warning,

                                contentColor =
                                    Color.White
                            ),

                        contentPadding =
                            PaddingValues(0.dp)
                    ) {

                        Text(
                            "Unlock",
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text =
                        "Complete payment or press Unlock.",

                    fontSize = 11.sp,

                    color =
                        PosTheme.bill.warning,

                    textAlign =
                        TextAlign.Center,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 3.dp
                        )
                )
            }

            // =================================================
            // NUMPAD
            // =================================================

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            HorizontalDivider(
                color =
                    PosTheme.bill.inputBorder
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            NumPad { label ->

                handleInput(

                    label = label,

                    activeInput =
                        activeInput,

                    uiState =
                        uiState,

                    discountFlat =
                        discountFlat,

                    discountPercent =
                        discountPercent,

                    creditAmount =
                        creditAmount,

                    deliveryFee =
                        deliveryFee,

                    billViewModel =
                        billViewModel,

                    isPrinted =
                        isPrinted
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}

// =========================================================
// MORE OPTIONS / COMPLIMENTARY ORDER
// =========================================================

if (showMoreOptions) {

    Dialog(
        onDismissRequest = {
            showMoreOptions = false
        }
    ) {

        Surface(

            modifier =
                Modifier.fillMaxWidth(0.9f),

            shape =
                RoundedCornerShape(20.dp),

            tonalElevation = 12.dp,

            color =
                PosTheme.bill.billText
        ) {

            Column(

                modifier =
                    Modifier.padding(20.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "🎁",
                    fontSize = 38.sp
                )

                Text(
                    text =
                        "Complimentary Order",

                    style =
                        MaterialTheme.typography.titleLarge,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color.DarkGray
                )

                Text(
                    text =
                        "Select reason for free order",

                    fontSize = 13.sp,

                    color = Color.Gray,

                    textAlign =
                        TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                // DRIVER
                Button(
                    onClick = {

                        billViewModel
                            .complimentaryOrder(

                                payments = listOf(
                                    PaymentInput(
                                        mode = "FREE",
                                        amount = 0
                                    )
                                ),

                                name = "Driver",

                                phone =
                                    uiState.customerPhone,

                                reason = "DRIVER"
                            )

                        showMoreOptions = false

                        onDismiss()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF1976D2)
                        )
                ) {

                    Text(
                        "🚚  Driver",
                        fontSize = 15.sp
                    )
                }

                // FAMILY
                Button(
                    onClick = {

                        billViewModel
                            .complimentaryOrder(

                                payments = listOf(
                                    PaymentInput(
                                        mode = "FREE",
                                        amount = 0
                                    )
                                ),

                                name = "Family",

                                phone =
                                    uiState.customerPhone,

                                reason = "FAMILY"
                            )

                        showMoreOptions = false

                        onDismiss()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF9C27B0)
                        )
                ) {

                    Text(
                        "👨‍👩‍👧  Family",
                        fontSize = 15.sp
                    )
                }

                // FRIEND
                Button(
                    onClick = {

                        billViewModel
                            .complimentaryOrder(

                                payments = listOf(
                                    PaymentInput(
                                        mode = "FREE",
                                        amount = 0
                                    )
                                ),

                                name = "Friend",

                                phone =
                                    uiState.customerPhone,

                                reason = "FRIEND"
                            )

                        showMoreOptions = false

                        onDismiss()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF4CAF50)
                        )
                ) {

                    Text(
                        "🤝  Friend",
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        showMoreOptions = false
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),

                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Text(
                        "Cancel",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}}
}