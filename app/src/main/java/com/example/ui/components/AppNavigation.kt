package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryTeal

enum class AppScreen(val label: String, val icon: ImageVector) {
    DASHBOARD("Inicio", Icons.Default.Dashboard),
    POS("Vender", Icons.Default.PointOfSale),
    INVENTORY("Inventario", Icons.Default.Inventory2),
    CASH("Caja", Icons.Default.AccountBalanceWallet),
    EXPENSES("Gastos", Icons.Default.ReceiptLong),
    PURCHASES("Compras", Icons.Default.ShoppingBag),
    CREDITS("Fiados", Icons.Default.People),
    REPORTS("Reportes", Icons.Default.Assessment),
    SETTINGS("Ajustes", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    businessName: String,
    onNavigate: (AppScreen) -> Unit,
    lowStockCount: Int = 0,
    cartItemCount: Int = 0,
    onCartClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = when (currentScreen) {
                        AppScreen.DASHBOARD -> businessName
                        AppScreen.POS -> "Punto de Venta (POS)"
                        AppScreen.INVENTORY -> "Control de Inventario"
                        AppScreen.CASH -> "Control y Arqueo de Caja"
                        AppScreen.EXPENSES -> "Gastos del Negocio"
                        AppScreen.PURCHASES -> "Compras a Proveedores"
                        AppScreen.CREDITS -> "Cuentas por Cobrar (Fiados)"
                        AppScreen.REPORTS -> "Reportes y Ganancias"
                        AppScreen.SETTINGS -> "Ajustes y Configuración"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (currentScreen == AppScreen.DASHBOARD) {
                    Text(
                        text = "Sistema de Gestión Comercial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            if (currentScreen == AppScreen.POS && onCartClick != null) {
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.testTag("topbar_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("$cartItemCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Carrito de compras"
                        )
                    }
                }
            } else if (currentScreen != AppScreen.SETTINGS) {
                IconButton(
                    onClick = { onNavigate(AppScreen.SETTINGS) },
                    modifier = Modifier.testTag("topbar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    lowStockCount: Int = 0,
    cartItemCount: Int = 0
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    val mainScreens = listOf(
        AppScreen.DASHBOARD,
        AppScreen.POS,
        AppScreen.INVENTORY,
        AppScreen.CASH
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        mainScreens.forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    when (screen) {
                        AppScreen.POS -> {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("$cartItemCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        }
                        AppScreen.INVENTORY -> {
                            BadgedBox(
                                badge = {
                                    if (lowStockCount > 0) {
                                        Badge(containerColor = LossRed) {
                                            Text("$lowStockCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        }
                        else -> {
                            Icon(screen.icon, contentDescription = screen.label)
                        }
                    }
                },
                label = {
                    Text(
                        text = screen.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }

        val isMoreSelected = currentScreen !in mainScreens
        NavigationBarItem(
            selected = isMoreSelected,
            onClick = { showMoreMenu = true },
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Más opciones") },
            label = {
                Text(
                    text = "Más",
                    fontWeight = if (isMoreSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("nav_item_more")
        )
    }

    if (showMoreMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMoreMenu = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Módulos del Negocio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                val subModules = listOf(
                    Triple(AppScreen.REPORTS, "Reportes y Ganancias", "Balance P&L, ventas, costos y top de productos"),
                    Triple(AppScreen.EXPENSES, "Gastos y Salidas", "Control de alquiler, sueldos, servicios y más"),
                    Triple(AppScreen.PURCHASES, "Compras a Proveedores", "Recepción de mercancía y reposición"),
                    Triple(AppScreen.CREDITS, "Cuentas por Cobrar", "Control de fiados y cobros a clientes"),
                    Triple(AppScreen.SETTINGS, "Ajustes de Negocio", "Nombre de tienda, moneda, tickets y datos")
                )

                subModules.forEach { (screen, title, description) ->
                    Card(
                        onClick = {
                            showMoreMenu = false
                            onNavigate(screen)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("menu_sub_${screen.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentScreen == screen)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
