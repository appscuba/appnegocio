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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchBarField
import com.example.ui.components.StockBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatQuantity
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.WarningAmber
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    products: List<Product>,
    categories: List<Category>,
    profile: BusinessProfile,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onAdjustStock: (Long, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Todos, 1: Stock Bajo, 2: Agotados
    var selectedCategory by remember { mutableStateOf("Todos") }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddEditSheet by remember { mutableStateOf(false) }

    var productToAdjust by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = remember(products, searchQuery, selectedTabIndex, selectedCategory) {
        products.filter { p ->
            val matchesTab = when (selectedTabIndex) {
                1 -> p.isLowStock
                2 -> p.isOutOfStock
                else -> true
            }
            val matchesCat = (selectedCategory == "Todos") || p.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery, ignoreCase = true)

            matchesTab && matchesCat && matchesSearch
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Search & Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SearchBarField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Buscar en inventario..."
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tabs: Todos, Stock Bajo, Agotados
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Todos (${products.size})", fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            val lowCount = products.count { it.isLowStock }
                            Text("Stock Bajo ($lowCount)", fontSize = 13.sp, color = if (lowCount > 0) WarningAmber else MaterialTheme.colorScheme.onSurface)
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            val outCount = products.count { it.isOutOfStock }
                            Text("Agotados ($outCount)", fontSize = 13.sp, color = if (outCount > 0) LossRed else MaterialTheme.colorScheme.onSurface)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "Todos",
                            onClick = { selectedCategory = "Todos" },
                            label = { Text("Todas Categorías", fontSize = 12.sp) }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.name, fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Inventory2,
                    title = "Sin productos en esta vista",
                    description = "Agrega nuevos productos o cambia los filtros de búsqueda",
                    modifier = Modifier.weight(1f),
                    actionButton = {
                        Button(
                            onClick = {
                                productToEdit = null
                                showAddEditSheet = true
                            },
                            modifier = Modifier.testTag("inventory_empty_add_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agregar Producto")
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        InventoryProductCard(
                            product = product,
                            currencySymbol = profile.currencySymbol,
                            onEdit = {
                                productToEdit = product
                                showAddEditSheet = true
                            },
                            onAdjustStock = { productToAdjust = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }

        // Floating Action Button: Add Product
        FloatingActionButton(
            onClick = {
                productToEdit = null
                showAddEditSheet = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("inventory_fab_add_product")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
        }
    }

    // Add / Edit Product Sheet
    if (showAddEditSheet) {
        AddEditProductSheet(
            initialProduct = productToEdit,
            categories = categories,
            currencySymbol = profile.currencySymbol,
            onDismiss = { showAddEditSheet = false },
            onSave = { product ->
                onSaveProduct(product)
                showAddEditSheet = false
            }
        )
    }

    // Stock Adjustment Dialog
    if (productToAdjust != null) {
        StockAdjustmentDialog(
            product = productToAdjust!!,
            onDismiss = { productToAdjust = null },
            onConfirm = { newStock, reason ->
                onAdjustStock(productToAdjust!!.id, newStock, reason)
                productToAdjust = null
            }
        )
    }

    // Delete Product Confirmation Dialog
    if (productToDelete != null) {
        ConfirmDialog(
            title = "Eliminar Producto",
            text = "¿Deseas eliminar '${productToDelete!!.name}' del catálogo?",
            confirmText = "Eliminar",
            isDestructive = true,
            onConfirm = {
                onDeleteProduct(productToDelete!!.id)
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }
}

@Composable
fun InventoryProductCard(
    product: Product,
    currencySymbol: String,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_card_${product.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${product.category} • ${if (product.barcode.isNotBlank()) "SKU: ${product.barcode}" else "Sin código"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar Producto") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ajustar Stock") },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAdjustStock()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = LossRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = LossRed) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Stock Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Precio Venta", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        formatCurrency(product.sellingPrice, currencySymbol),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text("Costo Compra", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatCurrency(product.costPrice, currencySymbol), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Ganancia (Margen)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "+${formatCurrency(product.profitPerUnit, currencySymbol)} (${String.format(java.util.Locale.US, "%.0f%%", product.profitMarginPercent)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfitGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockBadge(stock = product.stock, minStock = product.minStockAlert, unit = product.unit)

                TextButton(
                    onClick = onAdjustStock,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajustar Stock", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductSheet(
    initialProduct: Product?,
    categories: List<Category>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: (categories.firstOrNull()?.name ?: "General")) }
    var costPriceText by remember { mutableStateOf(initialProduct?.costPrice?.toString() ?: "") }
    var sellingPriceText by remember { mutableStateOf(initialProduct?.sellingPrice?.toString() ?: "") }
    var stockText by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "10") }
    var minStockText by remember { mutableStateOf(initialProduct?.minStockAlert?.toString() ?: "5") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "pza") }

    val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
    val sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0
    val profit = sellingPrice - costPrice
    val margin = if (costPrice > 0) (profit / costPrice) * 100.0 else 0.0

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
            Text(
                text = if (initialProduct == null) "Nuevo Producto" else "Editar Producto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Producto *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Barcode + Generate SKU button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Código de Barras / SKU") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_barcode_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        val randomSku = "750" + (100000000..999999999).random()
                        barcode = randomSku
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SKU", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            Text("Categoría:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat.name,
                        onClick = { category = cat.name },
                        label = { Text(cat.name, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it },
                    label = { Text("Costo Compra *") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_cost_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = sellingPriceText,
                    onValueChange = { sellingPriceText = it },
                    label = { Text("Precio Venta *") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_price_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Profit indicator banner
            if (sellingPrice > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (profit >= 0) ProfitGreen.copy(alpha = 0.12f) else LossRed.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ganancia estimada por unidad:", fontSize = 12.sp)
                        Text(
                            text = "${formatCurrency(profit, currencySymbol)} (${String.format(java.util.Locale.US, "%.0f%%", margin)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (profit >= 0) ProfitGreen else LossRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock & Min Alert & Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("Stock Actual *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_stock_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = minStockText,
                    onValueChange = { minStockText = it },
                    label = { Text("Alerta Mínima") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Unit chips
            Text("Unidad de Medida:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            val units = listOf("pza", "kg", "lt", "paquete", "caja", "g")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                units.forEach { u ->
                    FilterChip(
                        selected = unit == u,
                        onClick = { unit = u },
                        label = { Text(u, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val isValid = name.isNotBlank() && sellingPrice > 0
            Button(
                onClick = {
                    val newProduct = Product(
                        id = initialProduct?.id ?: 0L,
                        barcode = barcode,
                        name = name.trim(),
                        category = category,
                        costPrice = costPrice,
                        sellingPrice = sellingPrice,
                        stock = stockText.toDoubleOrNull() ?: 0.0,
                        minStockAlert = minStockText.toDoubleOrNull() ?: 5.0,
                        unit = unit,
                        isActive = true
                    )
                    onSave(newProduct)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_product_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Producto", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StockAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (newStock: Double, reason: String) -> Unit
) {
    var newStockText by remember { mutableStateOf("${product.stock.toInt()}") }
    var selectedReason by remember { mutableStateOf("Conteo Físico / Inventario") }

    val reasons = listOf(
        "Conteo Físico / Inventario",
        "Merma / Producto Dañado",
        "Devolución de Cliente",
        "Entrada Extraordinaria",
        "Uso Interno"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Stock de '${product.name}'", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Stock actual registrado: ${formatQuantity(product.stock, product.unit)}", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newStockText,
                    onValueChange = { newStockText = it },
                    label = { Text("Nuevo Stock Real") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Motivo del Ajuste:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(reason, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ns = newStockText.toDoubleOrNull() ?: product.stock
                    onConfirm(ns, selectedReason)
                }
            ) {
                Text("Aplicar Ajuste")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
