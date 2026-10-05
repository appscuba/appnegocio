package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.CustomerCredit
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TertiaryAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerCreditsScreen(
    credits: List<CustomerCredit>,
    profile: BusinessProfile,
    onRegisterPayment: (creditId: Long, amount: Double, paymentMethod: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pendientes, 1: Liquidadas / Todas
    var creditToPay by remember { mutableStateOf<CustomerCredit?>(null) }
    val context = LocalContext.current

    val filteredCredits = remember(credits, selectedTab) {
        if (selectedTab == 0) credits.filter { !it.isPaid }
        else credits
    }

    val totalPendingAmount = remember(credits) {
        credits.filter { !it.isPaid }.sumOf { it.remainingBalance }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Summary Header Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TertiaryAmber.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Pendiente por Cobrar (Fiados)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatCurrency(totalPendingAmount, profile.currencySymbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TertiaryAmber
                        )
                    }
                    Text("${credits.count { !it.isPaid }} cuentas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pendientes (${credits.count { !it.isPaid }})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Todas (${credits.size})", fontSize = 13.sp) }
                )
            }
        }

        if (filteredCredits.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.People,
                title = if (selectedTab == 0) "¡Excelente! Sin deudas pendientes" else "Sin historial de créditos",
                description = "Cuando realices una venta con medio de pago 'Fiado', aparecerá aquí para darle seguimiento y cobrar abonos.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCredits, key = { it.id }) { credit ->
                    val createdDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(credit.createdAt))
                    val isFullyPaid = credit.isPaid

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isFullyPaid) ProfitGreen.copy(alpha = 0.15f) else TertiaryAmber.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isFullyPaid) Icons.Default.CheckCircle else Icons.Default.People,
                                            contentDescription = null,
                                            tint = if (isFullyPaid) ProfitGreen else TertiaryAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(credit.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Fecha: $createdDateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                if (isFullyPaid) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = ProfitGreen.copy(alpha = 0.15f)) {
                                        Text("Liquidado", color = ProfitGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Resta pagar:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            formatCurrency(credit.remainingBalance, profile.currencySymbol),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = LossRed
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Original: ${formatCurrency(credit.totalDebt, profile.currencySymbol)}", fontSize = 12.sp)
                                Text("Abonado: ${formatCurrency(credit.paidAmount, profile.currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ProfitGreen)
                            }

                            if (!isFullyPaid) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (credit.customerPhone.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${credit.customerPhone}"))
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Llamar", fontSize = 12.sp)
                                        }
                                    }

                                    Button(
                                        onClick = { creditToPay = credit },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("credit_pay_button_${credit.id}"),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Registrar Abono", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creditToPay != null) {
        CreditPaymentDialog(
            credit = creditToPay!!,
            currencySymbol = profile.currencySymbol,
            onDismiss = { creditToPay = null },
            onConfirm = { amount, method ->
                onRegisterPayment(creditToPay!!.id, amount, method)
                creditToPay = null
            }
        )
    }
}

@Composable
fun CreditPaymentDialog(
    credit: CustomerCredit,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, paymentMethod: String) -> Unit
) {
    var amountText by remember { mutableStateOf("${credit.remainingBalance.toInt()}") }
    var selectedMethod by remember { mutableStateOf("Efectivo") }

    val methods = listOf("Efectivo", "Tarjeta", "Transferencia")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Abono de '${credit.customerName}'", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Saldo pendiente actual: ${formatCurrency(credit.remainingBalance, currencySymbol)}", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto del Abono *") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { amountText = "${credit.remainingBalance}" },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Liquidar Total (${formatCurrency(credit.remainingBalance, currencySymbol)})", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Medio de Cobro:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.forEach { m ->
                        FilterChip(
                            selected = selectedMethod == m,
                            onClick = { selectedMethod = m },
                            label = { Text(m, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = { onConfirm(amount, selectedMethod) },
                enabled = amount > 0
            ) {
                Text("Confirmar Abono", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
