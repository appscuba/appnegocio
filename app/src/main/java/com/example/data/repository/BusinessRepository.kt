package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.CartItem
import com.example.data.model.CashMovement
import com.example.data.model.CashSession
import com.example.data.model.Category
import com.example.data.model.CategoryPerformance
import com.example.data.model.CustomerCredit
import com.example.data.model.DashboardSummary
import com.example.data.model.Expense
import com.example.data.model.FinancialReport
import com.example.data.model.PaymentMethodSummary
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseWithItems
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleWithItems
import com.example.data.model.TopProductReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BusinessRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val saleDao = database.saleDao()
    private val purchaseDao = database.purchaseDao()
    private val cashDao = database.cashDao()
    private val expenseDao = database.expenseDao()
    private val creditDao = database.customerCreditDao()
    private val profileDao = database.businessProfileDao()

    // -------------------------------------------------------------
    // INITIALIZATION & SEEDING
    // -------------------------------------------------------------
    suspend fun initializeIfEmpty() {
        val count = productDao.getActiveProductsCount().first()
        if (count == 0) {
            seedSampleData()
        }
        val profile = profileDao.getProfile()
        if (profile == null) {
            profileDao.insertOrUpdateProfile(SampleDataProvider.getDefaultProfile())
        }
    }

    suspend fun seedSampleData() {
        categoryDao.insertCategories(SampleDataProvider.getDefaultCategories())
        productDao.insertProducts(SampleDataProvider.getDefaultProducts())
        if (profileDao.getProfile() == null) {
            profileDao.insertOrUpdateProfile(SampleDataProvider.getDefaultProfile())
        }
    }

    suspend fun clearAllData() {
        productDao.deleteAllProducts()
        categoryDao.deleteAllCategories()
        saleDao.deleteAllSales()
        saleDao.deleteAllSaleItems()
        purchaseDao.deleteAllPurchases()
        purchaseDao.deleteAllPurchaseItems()
        cashDao.deleteAllSessions()
        cashDao.deleteAllMovements()
        expenseDao.deleteAllExpenses()
        creditDao.deleteAllCredits()
    }

    // -------------------------------------------------------------
    // PRODUCTS & CATEGORIES
    // -------------------------------------------------------------
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val outOfStockProducts: Flow<List<Product>> = productDao.getOutOfStockProducts()

    fun searchProducts(query: String): Flow<List<Product>> =
        if (query.isBlank()) productDao.getAllProducts() else productDao.searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<Product>> =
        if (category == "Todos" || category.isBlank()) productDao.getAllProducts()
        else productDao.getProductsByCategory(category)

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)

    suspend fun saveProduct(product: Product): Long {
        return if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun deleteProduct(productId: Long) = productDao.softDeleteProduct(productId)

    suspend fun adjustStock(productId: Long, newStock: Double, reason: String) {
        val currentProduct = productDao.getProductById(productId) ?: return
        val difference = newStock - currentProduct.stock
        productDao.setStock(productId, newStock)
    }

    suspend fun addCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    // -------------------------------------------------------------
    // PROFILE & SETTINGS
    // -------------------------------------------------------------
    val businessProfile: Flow<BusinessProfile?> = profileDao.getProfileFlow()

    suspend fun updateBusinessProfile(profile: BusinessProfile) {
        profileDao.insertOrUpdateProfile(profile)
    }

    // -------------------------------------------------------------
    // POS & SALES
    // -------------------------------------------------------------
    val allSales: Flow<List<SaleWithItems>> = saleDao.getAllSalesWithItems()

    suspend fun completeSale(
        cartItems: List<CartItem>,
        paymentMethod: String,
        amountReceived: Double,
        customerName: String,
        customerPhone: String,
        notes: String
    ): Pair<Sale, List<SaleItem>> {
        val totalAmount = cartItems.sumOf { it.subtotal }
        val totalCost = cartItems.sumOf { it.totalCost }
        val profit = totalAmount - totalCost
        val changeGiven = (amountReceived - totalAmount).coerceAtLeast(0.0)
        val isCredit = paymentMethod.equals("Fiado", ignoreCase = true) || paymentMethod.equals("Crédito", ignoreCase = true)

        val timestamp = System.currentTimeMillis()
        val invoiceNumber = "TK-" + SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date(timestamp))

        val sale = Sale(
            invoiceNumber = invoiceNumber,
            timestamp = timestamp,
            totalAmount = totalAmount,
            totalCost = totalCost,
            profit = profit,
            paymentMethod = paymentMethod,
            amountReceived = if (isCredit) 0.0 else amountReceived,
            changeGiven = if (isCredit) 0.0 else changeGiven,
            customerName = customerName.ifBlank { "Cliente Mostrador" },
            customerPhone = customerPhone,
            notes = notes,
            isCreditSale = isCredit,
            status = "COMPLETED"
        )

        val saleId = saleDao.insertSale(sale)

        val saleItems = cartItems.map { item ->
            SaleItem(
                saleId = saleId,
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                unitCost = item.unitCost,
                unitPrice = item.unitPrice,
                subtotal = item.subtotal,
                profit = item.profit
            )
        }
        saleDao.insertSaleItems(saleItems)

        // Reduce inventory
        for (item in cartItems) {
            productDao.decreaseStock(item.product.id, item.quantity)
        }

        // Cash flow tracking
        if (paymentMethod.equals("Efectivo", ignoreCase = true)) {
            val openSession = cashDao.getCurrentOpenSession()
            if (openSession != null) {
                cashDao.insertMovement(
                    CashMovement(
                        sessionId = openSession.id,
                        type = "VENTA_EFECTIVO",
                        amount = totalAmount,
                        category = "Ventas",
                        description = "Venta $invoiceNumber (${cartItems.size} productos)",
                        timestamp = timestamp
                    )
                )
                val updatedExpected = openSession.expectedCash + totalAmount
                cashDao.updateSession(openSession.copy(expectedCash = updatedExpected))
            }
        }

        // Customer credit tracking
        if (isCredit) {
            creditDao.insertCredit(
                CustomerCredit(
                    customerName = customerName.ifBlank { "Cliente por Cobrar" },
                    customerPhone = customerPhone,
                    saleId = saleId,
                    totalDebt = totalAmount,
                    paidAmount = 0.0,
                    isPaid = false,
                    createdAt = timestamp,
                    dueDate = timestamp + (15L * 24 * 60 * 60 * 1000), // +15 days default
                    notes = notes
                )
            )
        }

        return Pair(sale.copy(id = saleId), saleItems)
    }

    // -------------------------------------------------------------
    // PURCHASES & SUPPLIERS
    // -------------------------------------------------------------
    val allPurchases: Flow<List<PurchaseWithItems>> = purchaseDao.getAllPurchasesWithItems()

    suspend fun registerPurchase(
        supplierName: String,
        invoiceNumber: String,
        paymentMethod: String,
        notes: String,
        items: List<Pair<Product, Pair<Double, Double>>> // Product, Quantity, UnitCost
    ): Long {
        val totalAmount = items.sumOf { it.second.first * it.second.second }
        val timestamp = System.currentTimeMillis()
        val invoice = invoiceNumber.ifBlank { "COMP-" + SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault()).format(Date(timestamp)) }

        val purchase = Purchase(
            invoiceNumber = invoice,
            supplierName = supplierName.ifBlank { "Proveedor General" },
            timestamp = timestamp,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            notes = notes
        )

        val purchaseId = purchaseDao.insertPurchase(purchase)

        val purchaseItems = items.map { (product, qtyAndCost) ->
            val quantity = qtyAndCost.first
            val unitCost = qtyAndCost.second

            // Update product stock and cost
            productDao.increaseStock(product.id, quantity)
            if (unitCost > 0 && unitCost != product.costPrice) {
                productDao.updateProduct(product.copy(costPrice = unitCost))
            }

            PurchaseItem(
                purchaseId = purchaseId,
                productId = product.id,
                productName = product.name,
                quantity = quantity,
                unitCost = unitCost,
                subtotal = quantity * unitCost
            )
        }
        purchaseDao.insertPurchaseItems(purchaseItems)

        // If paid in cash from register
        if (paymentMethod.equals("Efectivo", ignoreCase = true)) {
            val openSession = cashDao.getCurrentOpenSession()
            if (openSession != null) {
                cashDao.insertMovement(
                    CashMovement(
                        sessionId = openSession.id,
                        type = "RETIRO",
                        amount = totalAmount,
                        category = "Compras",
                        description = "Pago a proveedor $supplierName ($invoice)",
                        timestamp = timestamp
                    )
                )
                val updatedExpected = openSession.expectedCash - totalAmount
                cashDao.updateSession(openSession.copy(expectedCash = updatedExpected))
            }
        }

        return purchaseId
    }

    // -------------------------------------------------------------
    // CASH REGISTER & FLOW
    // -------------------------------------------------------------
    val currentOpenCashSession: Flow<CashSession?> = cashDao.getCurrentOpenSessionFlow()
    val allCashSessions: Flow<List<CashSession>> = cashDao.getAllSessions()

    fun getMovementsForSession(sessionId: Long): Flow<List<CashMovement>> =
        cashDao.getMovementsForSession(sessionId)

    suspend fun openCashRegister(initialCash: Double, notes: String): Long {
        val session = CashSession(
            openTimestamp = System.currentTimeMillis(),
            initialCash = initialCash,
            expectedCash = initialCash,
            status = "OPEN",
            notes = notes
        )
        val id = cashDao.insertSession(session)
        cashDao.insertMovement(
            CashMovement(
                sessionId = id,
                type = "INGRESO",
                amount = initialCash,
                category = "Apertura",
                description = "Fondo inicial de caja",
                timestamp = session.openTimestamp
            )
        )
        return id
    }

    suspend fun addCashMovement(sessionId: Long, type: String, amount: Double, category: String, description: String) {
        val session = cashDao.getSessionById(sessionId) ?: return
        val timestamp = System.currentTimeMillis()

        cashDao.insertMovement(
            CashMovement(
                sessionId = sessionId,
                type = type,
                amount = amount,
                category = category,
                description = description,
                timestamp = timestamp
            )
        )

        val newExpected = if (type == "INGRESO" || type == "VENTA_EFECTIVO") {
            session.expectedCash + amount
        } else {
            session.expectedCash - amount
        }

        cashDao.updateSession(session.copy(expectedCash = newExpected))
    }

    suspend fun closeCashRegister(sessionId: Long, actualCash: Double, notes: String): CashSession? {
        val session = cashDao.getSessionById(sessionId) ?: return null
        val diff = actualCash - session.expectedCash
        val closedSession = session.copy(
            closeTimestamp = System.currentTimeMillis(),
            actualCash = actualCash,
            difference = diff,
            status = "CLOSED",
            notes = notes
        )
        cashDao.updateSession(closedSession)
        return closedSession
    }

    // -------------------------------------------------------------
    // EXPENSES
    // -------------------------------------------------------------
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun addExpense(
        category: String,
        amount: Double,
        description: String,
        paymentMethod: String,
        receiptNumber: String,
        deductFromCashRegister: Boolean
    ): Long {
        val expense = Expense(
            category = category,
            amount = amount,
            description = description,
            paymentMethod = paymentMethod,
            receiptNumber = receiptNumber
        )
        val id = expenseDao.insertExpense(expense)

        if (deductFromCashRegister && paymentMethod.equals("Efectivo", ignoreCase = true)) {
            val session = cashDao.getCurrentOpenSession()
            if (session != null) {
                addCashMovement(
                    sessionId = session.id,
                    type = "GASTO",
                    amount = amount,
                    category = category,
                    description = "Gasto: $description"
                )
            }
        }
        return id
    }

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    // -------------------------------------------------------------
    // CUSTOMER CREDITS (FIADOS)
    // -------------------------------------------------------------
    val allCredits: Flow<List<CustomerCredit>> = creditDao.getAllCredits()
    val pendingCredits: Flow<List<CustomerCredit>> = creditDao.getPendingCredits()
    val totalPendingCreditAmount: Flow<Double> = creditDao.getTotalPendingCreditsAmount()

    suspend fun registerCreditPayment(creditId: Long, paymentAmount: Double, paymentMethod: String) {
        val credit = creditDao.getCreditById(creditId) ?: return
        val newPaid = credit.paidAmount + paymentAmount
        val isNowPaid = newPaid >= credit.totalDebt
        val updatedCredit = credit.copy(
            paidAmount = newPaid,
            isPaid = isNowPaid,
            lastPaymentDate = System.currentTimeMillis()
        )
        creditDao.updateCredit(updatedCredit)

        // If paid in cash and register is open
        if (paymentMethod.equals("Efectivo", ignoreCase = true)) {
            val session = cashDao.getCurrentOpenSession()
            if (session != null) {
                addCashMovement(
                    sessionId = session.id,
                    type = "INGRESO",
                    amount = paymentAmount,
                    category = "Cobro Fiado",
                    description = "Abono cliente ${credit.customerName}"
                )
            }
        }
    }

    // -------------------------------------------------------------
    // DASHBOARD & FINANCIAL REPORTING
    // -------------------------------------------------------------
    fun getDashboardSummaryFlow(): Flow<DashboardSummary> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfDay = cal.timeInMillis

        return combine(
            saleDao.getTotalSalesAmountBetweenDates(startOfDay, endOfDay),
            saleDao.getSalesCountBetweenDates(startOfDay, endOfDay),
            saleDao.getTotalProfitBetweenDates(startOfDay, endOfDay),
            expenseDao.getTotalExpensesBetweenDates(startOfDay, endOfDay),
            cashDao.getCurrentOpenSessionFlow(),
            productDao.getActiveProductsCount(),
            productDao.getLowStockProductsCount(),
            productDao.getOutOfStockProductsCount(),
            creditDao.getTotalPendingCreditsAmount(),
            creditDao.getPendingCreditsCount(),
            productDao.getTotalInventoryCostValuation()
        ) { values ->
            val salesTotal = values[0] as Double
            val salesCount = values[1] as Int
            val grossProfit = values[2] as Double
            val expensesTotal = values[3] as Double
            val openSession = values[4] as CashSession?
            val totalProducts = values[5] as Int
            val lowStockCount = values[6] as Int
            val outOfStockCount = values[7] as Int
            val pendingCreditsTotal = values[8] as Double
            val pendingCreditsCount = values[9] as Int
            val inventoryValuation = values[10] as Double

            val netProfit = grossProfit - expensesTotal

            DashboardSummary(
                todaySalesTotal = salesTotal,
                todaySalesCount = salesCount,
                todayGrossProfit = grossProfit,
                todayExpensesTotal = expensesTotal,
                todayNetProfit = netProfit,
                currentCashInRegister = openSession?.expectedCash ?: 0.0,
                isCashSessionOpen = openSession != null,
                openSessionId = openSession?.id,
                totalProductsCount = totalProducts,
                lowStockProductsCount = lowStockCount,
                outOfStockProductsCount = outOfStockCount,
                pendingCreditsTotal = pendingCreditsTotal,
                pendingCreditsCount = pendingCreditsCount,
                inventoryTotalValuation = inventoryValuation
            )
        }
    }

    suspend fun generateFinancialReport(startTimestamp: Long, endTimestamp: Long): FinancialReport {
        val saleItems = saleDao.getSaleItemsBetweenDates(startTimestamp, endTimestamp)
        val totalSales = saleItems.sumOf { it.subtotal }
        val totalCost = saleItems.sumOf { it.unitCost * it.quantity }
        val grossProfit = totalSales - totalCost

        val allSalesInPeriod = saleDao.getAllSalesWithItems().first().filter {
            it.sale.timestamp in startTimestamp..endTimestamp && it.sale.status == "COMPLETED"
        }
        val totalSalesCount = allSalesInPeriod.size

        val expensesInPeriod = expenseDao.getExpensesBetweenDates(startTimestamp, endTimestamp).first()
        val totalExpenses = expensesInPeriod.sumOf { it.amount }
        val netProfit = grossProfit - totalExpenses
        val profitMargin = if (totalSales > 0) (netProfit / totalSales) * 100.0 else 0.0

        // Top selling products
        val topProducts = saleItems
            .groupBy { it.productId }
            .map { (productId, items) ->
                TopProductReport(
                    productId = productId,
                    productName = items.firstOrNull()?.productName ?: "Producto #$productId",
                    totalQuantitySold = items.sumOf { it.quantity },
                    totalRevenue = items.sumOf { it.subtotal },
                    totalProfit = items.sumOf { it.profit }
                )
            }
            .sortedByDescending { it.totalRevenue }
            .take(10)

        // Payment method breakdown
        val paymentMethodMap = allSalesInPeriod
            .groupBy { it.sale.paymentMethod }
            .map { (method, salesList) ->
                PaymentMethodSummary(
                    paymentMethod = method,
                    totalAmount = salesList.sumOf { it.sale.totalAmount },
                    transactionCount = salesList.size
                )
            }

        // Category breakdown
        val productsList = productDao.getAllProducts().first().associateBy { it.id }
        val categoryMap = mutableMapOf<String, Pair<Double, Double>>() // category -> (revenue, qty)
        for (item in saleItems) {
            val cat = productsList[item.productId]?.category ?: "General"
            val current = categoryMap[cat] ?: Pair(0.0, 0.0)
            categoryMap[cat] = Pair(current.first + item.subtotal, current.second + item.quantity)
        }

        val categoryPerformance = categoryMap.map { (catName, pair) ->
            CategoryPerformance(
                categoryName = catName,
                totalRevenue = pair.first,
                totalItemsSold = pair.second,
                percentageOfTotal = if (totalSales > 0) (pair.first / totalSales) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalRevenue }

        return FinancialReport(
            totalSales = totalSales,
            totalSalesCount = totalSalesCount,
            totalCostOfGoods = totalCost,
            grossProfit = grossProfit,
            totalOperatingExpenses = totalExpenses,
            netProfit = netProfit,
            profitMarginPercent = profitMargin,
            topSellingProducts = topProducts,
            salesByPaymentMethod = paymentMethodMap,
            salesByCategory = categoryPerformance
        )
    }
}
