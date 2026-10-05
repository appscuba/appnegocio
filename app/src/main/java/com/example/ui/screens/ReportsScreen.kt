package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.FinancialReport
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatQuantity
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryContainerTeal
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TertiaryAmber
import com.example.ui.theme.TertiaryContainerAmber
import com.example.ui.viewmodel.ReportPeriod
import java.util.Locale

@Composable
fun ReportsScreen(
    report: FinancialReport?,
    selectedPeriod: ReportPeriod,
    profile: BusinessProfile,
    onSelectPeriod: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        // Period Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text("Periodo del Reporte:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ReportPeriod.values()) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onSelectPeriod(period) },
                        label = { Text(period.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("report_chip_${period.name.lowercase()}")
                    )
                }
            }
        }

        if (report == null || (report.totalSales == 0.0 && report.totalOperatingExpenses == 0.0)) {
            EmptyStateView(
                icon = Icons.Default.Assessment,
                title = "Sin actividad en este periodo",
                description = "Selecciona otro periodo (ej. Este Mes o Histórico) para ver las ganancias y estados financieros.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Executive P&L (Estado de Resultados)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (report.netProfit >= 0)
                                MaterialTheme.colorScheme.surface
                            else
                                LossRed.copy(alpha = 0.08f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado de Resultados (${selectedPeriod.label})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                OutlinedButton(
                                    onClick = { shareReportText(context, report, selectedPeriod, profile) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("share_report_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Compartir", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Breakdown lines
                            ReportLineItem(
                                label = "(+) Ventas Totales (${report.totalSalesCount} transacciones)",
                                amount = formatCurrency(report.totalSales, profile.currencySymbol),
                                isPositive = true
                            )
                            ReportLineItem(
                                label = "(-) Costo de Productos Vendidos",
                                amount = "-${formatCurrency(report.totalCostOfGoods, profile.currencySymbol)}",
                                isNegative = true
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            ReportLineItem(
                                label = "(=) Ganancia Bruta en Ventas",
                                amount = formatCurrency(report.grossProfit, profile.currencySymbol),
                                isBold = true
                            )
                            ReportLineItem(
                                label = "(-) Gastos Operativos del Negocio",
                                amount = "-${formatCurrency(report.totalOperatingExpenses, profile.currencySymbol)}",
                                isNegative = true
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Net Profit Highlight
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "GANANCIA NETA REAL:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Margen Neto: ${String.format(Locale.US, "%.1f%%", report.profitMarginPercent)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatCurrency(report.netProfit, profile.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = if (report.netProfit >= 0) ProfitGreen else LossRed
                                )
                            }
                        }
                    }
                }

                // Top 10 Best Selling Products
                if (report.topSellingProducts.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = TertiaryAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Top Productos Más Vendidos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                report.topSellingProducts.forEachIndexed { index, topProd ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}.",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 13.sp,
                                                modifier = Modifier.width(22.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = topProd.productName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${formatQuantity(topProd.totalQuantitySold)} vendidas",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = formatCurrency(topProd.totalRevenue, profile.currencySymbol),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "+${formatCurrency(topProd.totalProfit, profile.currencySymbol)} ganancia",
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

                // Category Breakdown
                if (report.salesByCategory.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ventas por Categoría",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                report.salesByCategory.forEach { cat ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = cat.categoryName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${formatCurrency(cat.totalRevenue, profile.currencySymbol)} (${String.format(Locale.US, "%.1f%%", cat.percentageOfTotal)})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (cat.percentageOfTotal / 100.0).toFloat().coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sales by Payment Method
                if (report.salesByPaymentMethod.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ventas por Método de Pago",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                report.salesByPaymentMethod.forEach { pm ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${pm.paymentMethod} (${pm.transactionCount} ventas)",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatCurrency(pm.totalAmount, profile.currencySymbol),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
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
}

@Composable
fun ReportLineItem(
    label: String,
    amount: String,
    isPositive: Boolean = false,
    isNegative: Boolean = false,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = amount,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isPositive -> ProfitGreen
                isNegative -> LossRed
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun shareReportText(
    context: Context,
    report: FinancialReport,
    period: ReportPeriod,
    profile: BusinessProfile
) {
    val sb = StringBuilder()
    sb.appendLine("📊 *REPORTE FINANCIERO: ${profile.businessName}*")
    sb.appendLine("Periodo: ${period.label}")
    sb.appendLine("--------------------------------")
    sb.appendLine("(+) Ventas Totales: ${formatCurrency(report.totalSales, profile.currencySymbol)} (${report.totalSalesCount} transacciones)")
    sb.appendLine("(-) Costo de Mercancía: ${formatCurrency(report.totalCostOfGoods, profile.currencySymbol)}")
    sb.appendLine("(=) Ganancia Bruta: ${formatCurrency(report.grossProfit, profile.currencySymbol)}")
    sb.appendLine("(-) Gastos Operativos: ${formatCurrency(report.totalOperatingExpenses, profile.currencySymbol)}")
    sb.appendLine("--------------------------------")
    sb.appendLine("*GANANCIA NETA REAL: ${formatCurrency(report.netProfit, profile.currencySymbol)}*")
    sb.appendLine("Margen Neto: ${String.format(Locale.US, "%.1f%%", report.profitMarginPercent)}")
    sb.appendLine("--------------------------------")
    if (report.topSellingProducts.isNotEmpty()) {
        sb.appendLine("🏆 *TOP 5 PRODUCTOS:*")
        report.topSellingProducts.take(5).forEachIndexed { i, p ->
            sb.appendLine("${i + 1}. ${p.productName} (${formatQuantity(p.totalQuantitySold)}) -> ${formatCurrency(p.totalRevenue, profile.currencySymbol)}")
        }
    }

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "Compartir Reporte Financiero"))
}
