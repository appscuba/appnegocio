package com.example.data.model

data class DashboardSummary(
    val todaySalesTotal: Double = 0.0,
    val todaySalesCount: Int = 0,
    val todayGrossProfit: Double = 0.0,
    val todayExpensesTotal: Double = 0.0,
    val todayNetProfit: Double = 0.0,
    val currentCashInRegister: Double = 0.0,
    val isCashSessionOpen: Boolean = false,
    val openSessionId: Long? = null,
    val totalProductsCount: Int = 0,
    val lowStockProductsCount: Int = 0,
    val outOfStockProductsCount: Int = 0,
    val pendingCreditsTotal: Double = 0.0,
    val pendingCreditsCount: Int = 0,
    val inventoryTotalValuation: Double = 0.0 // Costo total del inventario
)

data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val customPrice: Double? = null,
    val discountPercent: Double = 0.0
) {
    val unitPrice: Double get() = customPrice ?: product.sellingPrice
    val subtotal: Double get() = (unitPrice * quantity) * (1.0 - (discountPercent / 100.0))
    val unitCost: Double get() = product.costPrice
    val totalCost: Double get() = unitCost * quantity
    val profit: Double get() = subtotal - totalCost
}

data class TopProductReport(
    val productId: Long,
    val productName: String,
    val totalQuantitySold: Double,
    val totalRevenue: Double,
    val totalProfit: Double
)

data class CategoryPerformance(
    val categoryName: String,
    val totalRevenue: Double,
    val totalItemsSold: Double,
    val percentageOfTotal: Double
)

data class PaymentMethodSummary(
    val paymentMethod: String,
    val totalAmount: Double,
    val transactionCount: Int
)

data class FinancialReport(
    val totalSales: Double,
    val totalSalesCount: Int,
    val totalCostOfGoods: Double,
    val grossProfit: Double,
    val totalOperatingExpenses: Double,
    val netProfit: Double,
    val profitMarginPercent: Double,
    val topSellingProducts: List<TopProductReport>,
    val salesByPaymentMethod: List<PaymentMethodSummary>,
    val salesByCategory: List<CategoryPerformance>
)
