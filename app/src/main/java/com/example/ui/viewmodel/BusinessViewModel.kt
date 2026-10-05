package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.CartItem
import com.example.data.model.CashMovement
import com.example.data.model.CashSession
import com.example.data.model.Category
import com.example.data.model.CustomerCredit
import com.example.data.model.DashboardSummary
import com.example.data.model.Expense
import com.example.data.model.FinancialReport
import com.example.data.model.Product
import com.example.data.model.PurchaseWithItems
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleWithItems
import com.example.data.repository.BusinessRepository
import com.example.data.repository.SampleDataProvider
import com.example.ui.components.AppScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ReportPeriod(val label: String) {
    TODAY("Hoy"),
    THIS_WEEK("Esta Semana"),
    THIS_MONTH("Este Mes"),
    THIS_YEAR("Este Año"),
    ALL_TIME("Histórico")
}

class BusinessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BusinessRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BusinessRepository(db)
        viewModelScope.launch {
            repository.initializeIfEmpty()
            loadFinancialReport(ReportPeriod.THIS_MONTH)
        }
    }

    // Navigation Stack
    private val screenStack = mutableListOf(AppScreen.DASHBOARD)
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            true
        } else {
            false
        }
    }

    // Feedback message channel
    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _messageEvent.emit(msg)
        }
    }

    // Core Data Streams
    val dashboardSummary: StateFlow<DashboardSummary> = repository.getDashboardSummaryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    val businessProfile: StateFlow<BusinessProfile> = repository.businessProfile
        .map { it ?: SampleDataProvider.getDefaultProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDataProvider.getDefaultProfile())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleWithItems>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases: StateFlow<List<PurchaseWithItems>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentCashSession: StateFlow<CashSession?> = repository.currentOpenCashSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCashSessions: StateFlow<List<CashSession>> = repository.allCashSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCredits: StateFlow<List<CustomerCredit>> = repository.allCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCredits: StateFlow<List<CustomerCredit>> = repository.pendingCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -----------------------------------------------------------------
    // POS State
    // -----------------------------------------------------------------
    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    private val _posSelectedCategory = MutableStateFlow("Todos")
    val posSelectedCategory: StateFlow<String> = _posSelectedCategory.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _lastSaleResult = MutableStateFlow<Pair<Sale, List<SaleItem>>?>(null)
    val lastSaleResult: StateFlow<Pair<Sale, List<SaleItem>>?> = _lastSaleResult.asStateFlow()

    fun setPosSearch(query: String) { _posSearchQuery.value = query }
    fun setPosCategory(category: String) { _posSelectedCategory.value = category }

    fun addToCart(product: Product, quantity: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity))
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = newQuantity)
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun dismissReceipt() {
        _lastSaleResult.value = null
    }

    fun completeCheckout(
        paymentMethod: String,
        amountReceived: Double,
        customerName: String,
        customerPhone: String,
        notes: String
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val result = repository.completeSale(
                cartItems = items,
                paymentMethod = paymentMethod,
                amountReceived = amountReceived,
                customerName = customerName,
                customerPhone = customerPhone,
                notes = notes
            )
            _cartItems.value = emptyList()
            _lastSaleResult.value = result
            showMessage("¡Venta completada con éxito! Ticket: ${result.first.invoiceNumber}")
            // Refresh financial report
            loadFinancialReport(_selectedReportPeriod.value)
        }
    }

    // -----------------------------------------------------------------
    // Products & Inventory Management
    // -----------------------------------------------------------------
    fun saveProduct(product: Product, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveProduct(product)
            showMessage(if (product.id == 0L) "Producto '${product.name}' registrado" else "Producto actualizado")
            onSuccess()
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            showMessage("Producto eliminado")
        }
    }

    fun adjustStock(productId: Long, newStock: Double, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(productId, newStock, reason)
            showMessage("Inventario actualizado ($newStock)")
        }
    }

    // -----------------------------------------------------------------
    // Purchases & Stock Intake
    // -----------------------------------------------------------------
    fun registerPurchase(
        supplierName: String,
        invoiceNumber: String,
        paymentMethod: String,
        notes: String,
        items: List<Pair<Product, Pair<Double, Double>>>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.registerPurchase(supplierName, invoiceNumber, paymentMethod, notes, items)
            showMessage("Compra registrada. Stock actualizado correctamente.")
            onSuccess()
        }
    }

    // -----------------------------------------------------------------
    // Cash Register Management
    // -----------------------------------------------------------------
    fun openCashRegister(initialCash: Double, notes: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.openCashRegister(initialCash, notes)
            showMessage("Caja abierta con fondo inicial de $initialCash")
            onSuccess()
        }
    }

    fun addCashMovement(sessionId: Long, type: String, amount: Double, category: String, description: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addCashMovement(sessionId, type, amount, category, description)
            showMessage("Movimiento de caja registrado: $type $amount")
            onSuccess()
        }
    }

    fun closeCashRegister(sessionId: Long, actualCash: Double, notes: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val closed = repository.closeCashRegister(sessionId, actualCash, notes)
            if (closed != null) {
                val diff = closed.difference ?: 0.0
                val diffMsg = when {
                    diff > 0.0 -> " (Sobrante: +$diff)"
                    diff < 0.0 -> " (Faltante: $diff)"
                    else -> " (Caja cuadrada exacta)"
                }
                showMessage("Caja cerrada correctamente$diffMsg")
            }
            onSuccess()
        }
    }

    fun getCashMovementsForSession(sessionId: Long) = repository.getMovementsForSession(sessionId)

    // -----------------------------------------------------------------
    // Expenses
    // -----------------------------------------------------------------
    fun addExpense(
        category: String,
        amount: Double,
        description: String,
        paymentMethod: String,
        receiptNumber: String,
        deductFromCashRegister: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addExpense(category, amount, description, paymentMethod, receiptNumber, deductFromCashRegister)
            showMessage("Gasto de $amount registrado ($category)")
            loadFinancialReport(_selectedReportPeriod.value)
            onSuccess()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            showMessage("Gasto eliminado")
            loadFinancialReport(_selectedReportPeriod.value)
        }
    }

    // -----------------------------------------------------------------
    // Customer Credits (Fiados)
    // -----------------------------------------------------------------
    fun registerCreditPayment(creditId: Long, amount: Double, paymentMethod: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.registerCreditPayment(creditId, amount, paymentMethod)
            showMessage("Abono de $amount registrado con éxito")
            onSuccess()
        }
    }

    // -----------------------------------------------------------------
    // Reports & Analytics
    // -----------------------------------------------------------------
    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    private val _financialReport = MutableStateFlow<FinancialReport?>(null)
    val financialReport: StateFlow<FinancialReport?> = _financialReport.asStateFlow()

    fun setReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
        loadFinancialReport(period)
    }

    fun loadFinancialReport(period: ReportPeriod) {
        viewModelScope.launch {
            val (start, end) = getTimestampsForPeriod(period)
            _financialReport.value = repository.generateFinancialReport(start, end)
        }
    }

    private fun getTimestampsForPeriod(period: ReportPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val startTimestamp = when (period) {
            ReportPeriod.TODAY -> cal.timeInMillis
            ReportPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.timeInMillis
            }
            ReportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.timeInMillis
            }
            ReportPeriod.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            ReportPeriod.ALL_TIME -> 0L
        }

        return Pair(startTimestamp, now + (24 * 60 * 60 * 1000L))
    }

    // -----------------------------------------------------------------
    // Settings & Database
    // -----------------------------------------------------------------
    fun updateBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.updateBusinessProfile(profile)
            showMessage("Información del negocio guardada")
        }
    }

    fun reloadSampleData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.seedSampleData()
            showMessage("Datos de prueba cargados correctamente")
            loadFinancialReport(_selectedReportPeriod.value)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            clearCart()
            showMessage("Todos los datos han sido eliminados")
            loadFinancialReport(_selectedReportPeriod.value)
        }
    }
}
