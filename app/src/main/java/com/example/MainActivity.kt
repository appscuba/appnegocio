package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppScreen
import com.example.ui.components.AppTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.CashRegisterScreen
import com.example.ui.screens.CustomerCreditsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MiNegocioTheme
import com.example.ui.viewmodel.BusinessViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: BusinessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MiNegocioTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: BusinessViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val recentSales by viewModel.allSales.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val posSearch by viewModel.posSearchQuery.collectAsStateWithLifecycle()
    val posCategory by viewModel.posSelectedCategory.collectAsStateWithLifecycle()
    val lastSaleResult by viewModel.lastSaleResult.collectAsStateWithLifecycle()
    val currentCashSession by viewModel.currentCashSession.collectAsStateWithLifecycle()
    val allCashSessions by viewModel.allCashSessions.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val purchases by viewModel.allPurchases.collectAsStateWithLifecycle()
    val credits by viewModel.allCredits.collectAsStateWithLifecycle()
    val financialReport by viewModel.financialReport.collectAsStateWithLifecycle()
    val selectedReportPeriod by viewModel.selectedReportPeriod.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateBack()
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.messageEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                businessName = profile.businessName,
                onNavigate = { viewModel.navigateTo(it) },
                lowStockCount = dashboardSummary.lowStockProductsCount,
                cartItemCount = cartItems.sumOf { it.quantity }.toInt()
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) },
                lowStockCount = dashboardSummary.lowStockProductsCount,
                cartItemCount = cartItems.sumOf { it.quantity }.toInt()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(
                    summary = dashboardSummary,
                    profile = profile,
                    lowStockProducts = lowStockProducts,
                    recentSales = recentSales,
                    onNavigate = { viewModel.navigateTo(it) },
                    modifier = screenModifier
                )
            }
            AppScreen.POS -> {
                PosScreen(
                    products = products,
                    categories = categories,
                    cartItems = cartItems,
                    profile = profile,
                    searchQuery = posSearch,
                    selectedCategory = posCategory,
                    onSearchChange = { viewModel.setPosSearch(it) },
                    onCategoryChange = { viewModel.setPosCategory(it) },
                    onAddToCart = { viewModel.addToCart(it) },
                    onUpdateCartQty = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                    onRemoveFromCart = { viewModel.removeFromCart(it) },
                    onClearCart = { viewModel.clearCart() },
                    onCheckout = { method, received, name, phone, notes ->
                        viewModel.completeCheckout(method, received, name, phone, notes)
                    },
                    modifier = screenModifier
                )
            }
            AppScreen.INVENTORY -> {
                InventoryScreen(
                    products = products,
                    categories = categories,
                    profile = profile,
                    onSaveProduct = { viewModel.saveProduct(it) },
                    onDeleteProduct = { viewModel.deleteProduct(it) },
                    onAdjustStock = { id, qty, reason -> viewModel.adjustStock(id, qty, reason) },
                    modifier = screenModifier
                )
            }
            AppScreen.CASH -> {
                CashRegisterScreen(
                    currentSession = currentCashSession,
                    allSessions = allCashSessions,
                    profile = profile,
                    getMovementsForSession = { viewModel.getCashMovementsForSession(it) },
                    onOpenCash = { cash, notes -> viewModel.openCashRegister(cash, notes) },
                    onAddMovement = { sid, type, amt, cat, desc -> viewModel.addCashMovement(sid, type, amt, cat, desc) },
                    onCloseCash = { sid, cash, notes -> viewModel.closeCashRegister(sid, cash, notes) },
                    modifier = screenModifier
                )
            }
            AppScreen.EXPENSES -> {
                ExpensesScreen(
                    expenses = expenses,
                    profile = profile,
                    isCashSessionOpen = currentCashSession != null,
                    onAddExpense = { cat, amt, desc, method, receipt, deduct ->
                        viewModel.addExpense(cat, amt, desc, method, receipt, deduct)
                    },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    modifier = screenModifier
                )
            }
            AppScreen.PURCHASES -> {
                PurchasesScreen(
                    purchases = purchases,
                    products = products,
                    profile = profile,
                    onRegisterPurchase = { supplier, invoice, method, notes, items ->
                        viewModel.registerPurchase(supplier, invoice, method, notes, items)
                    },
                    modifier = screenModifier
                )
            }
            AppScreen.CREDITS -> {
                CustomerCreditsScreen(
                    credits = credits,
                    profile = profile,
                    onRegisterPayment = { id, amt, method -> viewModel.registerCreditPayment(id, amt, method) },
                    modifier = screenModifier
                )
            }
            AppScreen.REPORTS -> {
                ReportsScreen(
                    report = financialReport,
                    selectedPeriod = selectedReportPeriod,
                    profile = profile,
                    onSelectPeriod = { viewModel.setReportPeriod(it) },
                    modifier = screenModifier
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    profile = profile,
                    onSaveProfile = { viewModel.updateBusinessProfile(it) },
                    onReloadSampleData = { viewModel.reloadSampleData() },
                    onClearAllData = { viewModel.clearAllData() },
                    modifier = screenModifier
                )
            }
        }

        // Digital Receipt Dialog if a sale just completed
        if (lastSaleResult != null) {
            val (sale, items) = lastSaleResult!!
            ReceiptDialog(
                sale = sale,
                items = items,
                profile = profile,
                onDismiss = { viewModel.dismissReceipt() }
            )
        }
    }
}
