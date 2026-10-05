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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.DashboardSummary
import com.example.data.model.Product
import com.example.data.model.SaleWithItems
import com.example.ui.components.AppScreen
import com.example.ui.components.MetricCard
import com.example.ui.components.StockBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryContainerTeal
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TertiaryAmber
import com.example.ui.theme.TertiaryContainerAmber
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    profile: BusinessProfile,
    lowStockProducts: List<Product>,
    recentSales: List<SaleWithItems>,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cash Status Banner
        item {
            CashStatusBanner(
                isOpen = summary.isCashSessionOpen,
                cashAmount = summary.currentCashInRegister,
                currencySymbol = profile.currencySymbol,
                onCashClick = { onNavigate(AppScreen.CASH) }
            )
        }

        // Main Action: POS / New Sale
        item {
            Card(
                onClick = { onNavigate(AppScreen.POS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_pos_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Nueva Venta (POS)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Cobro rápido a clientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        // Today's Key Business Metrics (2x2 Grid)
        item {
            Text(
                text = "Resumen de Hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Ventas Hoy",
                    value = formatCurrency(summary.todaySalesTotal, profile.currencySymbol),
                    subtitle = "${summary.todaySalesCount} ventas",
                    icon = Icons.Default.AttachMoney,
                    iconBgColor = PrimaryContainerTeal,
                    iconTint = PrimaryTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.REPORTS) }
                )
                MetricCard(
                    title = "Ganancia Neta",
                    value = formatCurrency(summary.todayNetProfit, profile.currencySymbol),
                    subtitle = "Margen real",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconBgColor = ProfitGreen.copy(alpha = 0.15f),
                    iconTint = ProfitGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.REPORTS) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Gastos Hoy",
                    value = formatCurrency(summary.todayExpensesTotal, profile.currencySymbol),
                    subtitle = "Salidas",
                    icon = Icons.Default.ReceiptLong,
                    iconBgColor = LossRed.copy(alpha = 0.12f),
                    iconTint = LossRed,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.EXPENSES) }
                )
                MetricCard(
                    title = "Por Cobrar (Fiados)",
                    value = formatCurrency(summary.pendingCreditsTotal, profile.currencySymbol),
                    subtitle = "${summary.pendingCreditsCount} clientes",
                    icon = Icons.Default.People,
                    iconBgColor = TertiaryContainerAmber,
                    iconTint = TertiaryAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.CREDITS) }
                )
            }
        }

        // Quick Operations Row
        item {
            Text(
                text = "Operaciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.ShoppingBag,
                    label = "Comprar Stock",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.PURCHASES) }
                )
                QuickActionButton(
                    icon = Icons.Default.ReceiptLong,
                    label = "Registrar Gasto",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.EXPENSES) }
                )
                QuickActionButton(
                    icon = Icons.Default.Assessment,
                    label = "Ver Ganancias",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.REPORTS) }
                )
            }
        }

        // Stock Alerts (if any)
        if (summary.lowStockProductsCount > 0 || summary.outOfStockProductsCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Alertas de Inventario",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            OutlinedButton(
                                onClick = { onNavigate(AppScreen.INVENTORY) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Ver Todo", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        lowStockProducts.take(3).forEach { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(product.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StockBadge(stock = product.stock, minStock = product.minStockAlert, unit = product.unit)
                            }
                        }
                    }
                }
            }
        }

        // Recent Sales Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimas Ventas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recentSales.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { onNavigate(AppScreen.REPORTS) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("Historial", fontSize = 12.sp)
                    }
                }
            }
        }

        if (recentSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Aún no hay ventas registradas hoy. ¡Presiona 'Nueva Venta' para comenzar!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentSales.take(5)) { saleWithItems ->
                val sale = saleWithItems.sale
                val dateStr = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(sale.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sale.invoiceNumber,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${sale.customerName} • ${sale.paymentMethod} • $dateStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(sale.totalAmount, profile.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "+${formatCurrency(sale.profit, profile.currencySymbol)} ganancia",
                                fontSize = 11.sp,
                                color = ProfitGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashStatusBanner(
    isOpen: Boolean,
    cashAmount: Double,
    currencySymbol: String,
    onCashClick: () -> Unit
) {
    Card(
        onClick = onCashClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_cash_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpen)
                MaterialTheme.colorScheme.surfaceVariant
            else
                WarningAmber.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isOpen) ProfitGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOpen) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isOpen) ProfitGreen else WarningAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isOpen) "Caja del Día: ABIERTA" else "Caja del Día: CERRADA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOpen) MaterialTheme.colorScheme.onSurface else WarningAmber
                    )
                    Text(
                        text = if (isOpen)
                            "Efectivo en caja: ${formatCurrency(cashAmount, currencySymbol)}"
                        else
                            "Toca para abrir caja y fijar fondo inicial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
