package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.CashMovement
import com.example.data.model.CashSession
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryContainerTeal
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TertiaryAmber
import com.example.ui.theme.TertiaryContainerAmber
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashRegisterScreen(
    currentSession: CashSession?,
    allSessions: List<CashSession>,
    profile: BusinessProfile,
    getMovementsForSession: (Long) -> Flow<List<CashMovement>>,
    onOpenCash: (initialCash: Double, notes: String) -> Unit,
    onAddMovement: (sessionId: Long, type: String, amount: Double, category: String, description: String) -> Unit,
    onCloseCash: (sessionId: Long, actualCash: Double, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Caja Actual, 1: Historial de Cierres
    var showOpenDialog by remember { mutableStateOf(false) }
    var showMovementDialog by remember { mutableStateOf<String?>(null) } // "INGRESO" or "RETIRO"
    var showCloseDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Turno de Caja", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Historial de Arqueos (${allSessions.size})", fontWeight = FontWeight.SemiBold) }
            )
        }

        if (selectedTabIndex == 0) {
            if (currentSession == null) {
                // Cash is Closed
                EmptyStateView(
                    icon = Icons.Default.Lock,
                    title = "La Caja está Cerrada",
                    description = "Para registrar ventas en efectivo y controlar las entradas/salidas de dinero, inicia el turno abriendo la caja.",
                    modifier = Modifier.weight(1f),
                    actionButton = {
                        Button(
                            onClick = { showOpenDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("open_cash_main_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir Turno de Caja", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            } else {
                // Cash is Open
                val movementsFlow = remember(currentSession.id) { getMovementsForSession(currentSession.id) }
                val movements by movementsFlow.collectAsState(initial = emptyList())

                val openDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(currentSession.openTimestamp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(ProfitGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.LockOpen,
                                                contentDescription = null,
                                                tint = ProfitGreen,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Caja Abierta",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Iniciada: $openDateStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showCloseDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("close_cash_button")
                                    ) {
                                        Text("Cerrar Caja", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                // Balance calculation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Efectivo Esperado en Caja",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatCurrency(currentSession.expectedCash, profile.currencySymbol),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Fondo Inicial:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatCurrency(currentSession.initialCash, profile.currencySymbol),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cash In / Cash Out Action Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showMovementDialog = "INGRESO" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cash_in_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Entrada de Efectivo", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { showMovementDialog = "RETIRO" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cash_out_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = LossRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retiro de Caja", fontSize = 13.sp)
                            }
                        }
                    }

                    // Movements Audit Feed
                    item {
                        Text(
                            text = "Movimientos del Turno (${movements.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (movements.isEmpty()) {
                        item {
                            Text(
                                text = "Sin movimientos adicionales todavía.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(movements, key = { it.id }) { movement ->
                            val isIncome = movement.type == "INGRESO" || movement.type == "VENTA_EFECTIVO"
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(movement.timestamp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isIncome) ProfitGreen.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isIncome) Icons.Default.Add else Icons.Default.Remove,
                                                contentDescription = null,
                                                tint = if (isIncome) ProfitGreen else LossRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = movement.description,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${movement.category} • $timeStr",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${if (isIncome) "+" else "-"}${formatCurrency(movement.amount, profile.currencySymbol)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isIncome) ProfitGreen else LossRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Historial de Cierres de Caja
            if (allSessions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "Sin historial de arqueos",
                    description = "Cuando cierres un turno de caja aparecerá aquí el registro comparativo.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allSessions, key = { it.id }) { session ->
                        val openDateStr = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(session.openTimestamp))
                        val closeDateStr = session.closeTimestamp?.let {
                            SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(it))
                        } ?: "En curso"

                        val diff = session.difference ?: 0.0

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
                                    Text(
                                        text = "Turno #${session.id} (${if (session.status == "OPEN") "Abierta" else "Cerrada"})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (session.status == "CLOSED") {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when {
                                                diff == 0.0 -> ProfitGreen.copy(alpha = 0.15f)
                                                diff > 0.0 -> WarningAmber.copy(alpha = 0.15f)
                                                else -> LossRed.copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    diff == 0.0 -> "Cuadrada exacta"
                                                    diff > 0.0 -> "Sobrante +${formatCurrency(diff, profile.currencySymbol)}"
                                                    else -> "Faltante ${formatCurrency(diff, profile.currencySymbol)}"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    diff == 0.0 -> ProfitGreen
                                                    diff > 0.0 -> WarningAmber
                                                    else -> LossRed
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Apertura: $openDateStr • Cierre: $closeDateStr",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Fondo Inicial: ${formatCurrency(session.initialCash, profile.currencySymbol)}", fontSize = 12.sp)
                                    Text("Esperado: ${formatCurrency(session.expectedCash, profile.currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    if (session.actualCash != null) {
                                        Text("Contado: ${formatCurrency(session.actualCash, profile.currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Open Cash
    if (showOpenDialog) {
        OpenCashDialog(
            currencySymbol = profile.currencySymbol,
            onDismiss = { showOpenDialog = false },
            onConfirm = { initialCash, notes ->
                onOpenCash(initialCash, notes)
                showOpenDialog = false
            }
        )
    }

    // Dialog: Cash In / Out
    if (showMovementDialog != null && currentSession != null) {
        CashMovementDialog(
            type = showMovementDialog!!,
            currencySymbol = profile.currencySymbol,
            onDismiss = { showMovementDialog = null },
            onConfirm = { amount, category, description ->
                onAddMovement(currentSession.id, showMovementDialog!!, amount, category, description)
                showMovementDialog = null
            }
        )
    }

    // Dialog: Close Cash (Arqueo)
    if (showCloseDialog && currentSession != null) {
        CloseCashDialog(
            expectedCash = currentSession.expectedCash,
            currencySymbol = profile.currencySymbol,
            onDismiss = { showCloseDialog = false },
            onConfirm = { actualCash, notes ->
                onCloseCash(currentSession.id, actualCash, notes)
                showCloseDialog = false
            }
        )
    }
}

@Composable
fun OpenCashDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (initialCash: Double, notes: String) -> Unit
) {
    var initialCashText by remember { mutableStateOf("500") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abrir Turno de Caja", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Ingresa el monto de fondo inicial o cambio para empezar a operar:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = initialCashText,
                    onValueChange = { initialCashText = it },
                    label = { Text("Fondo Inicial de Caja *") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de Apertura (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cash = initialCashText.toDoubleOrNull() ?: 0.0
                    onConfirm(cash, notes)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Abrir Caja", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CashMovementDialog(
    type: String, // INGRESO or RETIRO
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, description: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (type == "INGRESO") "Aporte de Efectivo" else "Retiro de Utilidad") }

    val isIncome = type == "INGRESO"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isIncome) "Entrada de Efectivo a Caja" else "Retiro de Dinero de Caja", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    if (isIncome) "Ingresa dinero extra para cambio o inyección de capital:"
                    else "Registra el retiro de efectivo de la caja (ej. cambio o utilidades):",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto *") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Concepto o Motivo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            val isValid = (amountText.toDoubleOrNull() ?: 0.0) > 0 && description.isNotBlank()
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onConfirm(amount, category, description)
                },
                enabled = isValid,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isIncome) "Registrar Ingreso" else "Registrar Retiro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CloseCashDialog(
    expectedCash: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (actualCash: Double, notes: String) -> Unit
) {
    var actualCashText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val actualCash = actualCashText.toDoubleOrNull() ?: 0.0
    val diff = if (actualCashText.isNotBlank()) actualCash - expectedCash else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cierre de Caja y Arqueo", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Efectivo Calculado por Sistema:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatCurrency(expectedCash, currencySymbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Cuenta el dinero físico en el cajón e ingresa el monto total contado:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = actualCashText,
                    onValueChange = { actualCashText = it },
                    label = { Text("Efectivo Real Contado *") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (actualCashText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            diff == 0.0 -> ProfitGreen.copy(alpha = 0.12f)
                            diff > 0.0 -> WarningAmber.copy(alpha = 0.12f)
                            else -> LossRed.copy(alpha = 0.12f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Diferencia de Arqueo:", fontSize = 12.sp)
                            Text(
                                text = when {
                                    diff == 0.0 -> "Exacta ($0.00)"
                                    diff > 0.0 -> "Sobrante +${formatCurrency(diff, currencySymbol)}"
                                    else -> "Faltante ${formatCurrency(diff, currencySymbol)}"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when {
                                    diff == 0.0 -> ProfitGreen
                                    diff > 0.0 -> WarningAmber
                                    else -> LossRed
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observaciones del Cierre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            val isValid = actualCashText.isNotBlank()
            Button(
                onClick = {
                    onConfirm(actualCash, notes)
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Finalizar y Cerrar Turno", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
