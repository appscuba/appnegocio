package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Product
import com.example.data.model.PurchaseWithItems
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatQuantity
import com.example.ui.theme.PrimaryTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    purchases: List<PurchaseWithItems>,
    products: List<Product>,
    profile: BusinessProfile,
    onRegisterPurchase: (supplierName: String, invoice: String, paymentMethod: String, notes: String, items: List<Pair<Product, Pair<Double, Double>>>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (purchases.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ShoppingBag,
                    title = "Sin compras a proveedores",
                    description = "Registra compras para surtir tu inventario, actualizar costos de adquisición y llevar el control de pagos a proveedores.",
                    modifier = Modifier.weight(1f),
                    actionButton = {
                        Button(
                            onClick = { showAddSheet = true },
                            modifier = Modifier.testTag("purchases_empty_add_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Registrar Compra")
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchases, key = { it.purchase.id }) { purchaseWithItems ->
                        val p = purchaseWithItems.purchase
                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(p.timestamp))

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
                                    Column {
                                        Text(
                                            text = p.supplierName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${p.invoiceNumber} • ${p.paymentMethod} • $dateStr",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(p.totalAmount, profile.currencySymbol),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (purchaseWithItems.items.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(6.dp))

                                    purchaseWithItems.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• ${item.productName} (${formatQuantity(item.quantity)})",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatCurrency(item.subtotal, profile.currencySymbol),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("purchases_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva Compra")
        }
    }

    if (showAddSheet) {
        AddPurchaseSheet(
            products = products,
            currencySymbol = profile.currencySymbol,
            onDismiss = { showAddSheet = false },
            onConfirm = { supplier, invoice, method, notes, items ->
                onRegisterPurchase(supplier, invoice, method, notes, items)
                showAddSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseSheet(
    products: List<Product>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (supplier: String, invoice: String, method: String, notes: String, items: List<Pair<Product, Pair<Double, Double>>>) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Efectivo") }
    var notes by remember { mutableStateOf("") }

    // Selected items in purchase: Product, Quantity, UnitCost
    val purchaseItems = remember { mutableStateListOf<Triple<Product, Double, Double>>() }

    // Add item picker
    var selectedProductToAdd by remember { mutableStateOf<Product?>(products.firstOrNull()) }
    var quantityToAddText by remember { mutableStateOf("10") }
    var unitCostToAddText by remember { mutableStateOf(selectedProductToAdd?.costPrice?.toString() ?: "0") }

    val totalPurchaseAmount = remember(purchaseItems.toList()) {
        purchaseItems.sumOf { it.second * it.third }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Registrar Compra a Proveedor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = supplierName,
                onValueChange = { supplierName = it },
                label = { Text("Nombre del Proveedor (ej. Distribuidora Central) *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchase_supplier_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Nº Factura / Remisión") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Método Pago") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("Agregar Productos a la Compra:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (products.isEmpty()) {
                Text("Primero crea productos en Inventario para poder comprarlos.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Selecciona el producto:", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Category/Product dropdown or horizontal chips
                        LazyColumn(modifier = Modifier.height(110.dp)) {
                            items(products) { prod ->
                                val isSelected = selectedProductToAdd?.id == prod.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedProductToAdd = prod
                                            unitCostToAddText = prod.costPrice.toString()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        Text("${prod.name} (${prod.category}) - Costo: $currencySymbol${prod.costPrice}", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quantityToAddText,
                                onValueChange = { quantityToAddText = it },
                                label = { Text("Cantidad (${selectedProductToAdd?.unit ?: "pza"})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = unitCostToAddText,
                                onValueChange = { unitCostToAddText = it },
                                label = { Text("Costo Unitario") },
                                prefix = { Text("$currencySymbol ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    val prod = selectedProductToAdd ?: return@Button
                                    val qty = quantityToAddText.toDoubleOrNull() ?: 1.0
                                    val cost = unitCostToAddText.toDoubleOrNull() ?: prod.costPrice
                                    purchaseItems.add(Triple(prod, qty, cost))
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Añadir")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items added list
            if (purchaseItems.isNotEmpty()) {
                Text("Productos en esta compra (${purchaseItems.size}):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                purchaseItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.first.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text(
                                "${formatQuantity(item.second, item.first.unit)} x ${formatCurrency(item.third, currencySymbol)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                formatCurrency(item.second * item.third, currencySymbol),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            IconButton(onClick = { purchaseItems.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL DE LA COMPRA:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        formatCurrency(totalPurchaseAmount, currencySymbol),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isValid = supplierName.isNotBlank() && purchaseItems.isNotEmpty()
            Button(
                onClick = {
                    val formattedItems = purchaseItems.map { Pair(it.first, Pair(it.second, it.third)) }
                    onConfirm(supplierName.trim(), invoiceNumber, paymentMethod, notes, formattedItems)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_purchase_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Finalizar Compra y Aumentar Stock", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
