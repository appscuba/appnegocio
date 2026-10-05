package com.example.data.repository

import com.example.data.model.BusinessProfile
import com.example.data.model.Category
import com.example.data.model.Product

object SampleDataProvider {
    fun getDefaultProfile(): BusinessProfile {
        return BusinessProfile(
            id = 1,
            businessName = "Minimarket El Buen Vecino",
            ownerName = "Carlos Mendoza",
            taxId = "RFC: MEN900101-ABC",
            phone = "+52 55 5432 1098",
            address = "Av. Los Sauces 450, Centro",
            currencySymbol = "$",
            receiptHeader = "¡Gracias por su compra en El Buen Vecino!",
            receiptFooter = "¡Vuelva pronto! Atendido con gusto.",
            lowStockThresholdDefault = 5.0
        )
    }

    fun getDefaultCategories(): List<Category> {
        return listOf(
            Category(name = "Bebidas", iconName = "local_drink", colorHex = "#0284C7"),
            Category(name = "Abarrotes", iconName = "shopping_basket", colorHex = "#059669"),
            Category(name = "Lácteos y Huevos", iconName = "egg", colorHex = "#D97706"),
            Category(name = "Snacks y Dulces", iconName = "cookie", colorHex = "#E11D48"),
            Category(name = "Limpieza y Hogar", iconName = "cleaning_services", colorHex = "#7C3AED"),
            Category(name = "Panadería", iconName = "bakery_dining", colorHex = "#B45309"),
            Category(name = "Frutas y Verduras", iconName = "nutrition", colorHex = "#16A34A"),
            Category(name = "Cuidado Personal", iconName = "spa", colorHex = "#DB2777")
        )
    }

    fun getDefaultProducts(): List<Product> {
        return listOf(
            Product(
                barcode = "7501055310884",
                name = "Coca-Cola 600ml No Retornable",
                category = "Bebidas",
                costPrice = 12.50,
                sellingPrice = 18.00,
                stock = 24.0,
                minStockAlert = 6.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501000112233",
                name = "Agua Mineral Peñafiel 600ml",
                category = "Bebidas",
                costPrice = 10.00,
                sellingPrice = 15.00,
                stock = 18.0,
                minStockAlert = 5.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501020512345",
                name = "Jugo del Valle Naranja 1L",
                category = "Bebidas",
                costPrice = 18.00,
                sellingPrice = 26.00,
                stock = 12.0,
                minStockAlert = 4.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501005112001",
                name = "Leche Entera Lala 1 Litro",
                category = "Lácteos y Huevos",
                costPrice = 21.00,
                sellingPrice = 28.00,
                stock = 3.0, // Stock bajo
                minStockAlert = 6.0,
                unit = "lt"
            ),
            Product(
                barcode = "7501005112018",
                name = "Queso Panela Fresco 400g",
                category = "Lácteos y Huevos",
                costPrice = 38.00,
                sellingPrice = 52.00,
                stock = 8.0,
                minStockAlert = 3.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501005112025",
                name = "Huevo Blanco Paquete 12 pzas",
                category = "Lácteos y Huevos",
                costPrice = 32.00,
                sellingPrice = 42.00,
                stock = 0.0, // Agotado
                minStockAlert = 4.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501000612345",
                name = "Arroz Super Extra Schettino 1kg",
                category = "Abarrotes",
                costPrice = 22.00,
                sellingPrice = 30.00,
                stock = 15.0,
                minStockAlert = 5.0,
                unit = "kg"
            ),
            Product(
                barcode = "7501000654321",
                name = "Frijol Negro Verde Valle 1kg",
                category = "Abarrotes",
                costPrice = 34.00,
                sellingPrice = 46.00,
                stock = 10.0,
                minStockAlert = 4.0,
                unit = "kg"
            ),
            Product(
                barcode = "7501000678901",
                name = "Aceite Vegetal 123 1 Litro",
                category = "Abarrotes",
                costPrice = 36.00,
                sellingPrice = 48.00,
                stock = 14.0,
                minStockAlert = 4.0,
                unit = "lt"
            ),
            Product(
                barcode = "7501000699887",
                name = "Atún Dolores en Agua 140g",
                category = "Abarrotes",
                costPrice = 16.50,
                sellingPrice = 23.00,
                stock = 2.0, // Stock bajo
                minStockAlert = 5.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501011122334",
                name = "Papas Sabritas Original 45g",
                category = "Snacks y Dulces",
                costPrice = 13.00,
                sellingPrice = 19.00,
                stock = 20.0,
                minStockAlert = 5.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501011155667",
                name = "Galletas Marías Gamesa 170g",
                category = "Snacks y Dulces",
                costPrice = 12.00,
                sellingPrice = 17.00,
                stock = 16.0,
                minStockAlert = 5.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501011199001",
                name = "Chocolate Snickers 52g",
                category = "Snacks y Dulces",
                costPrice = 15.00,
                sellingPrice = 22.00,
                stock = 25.0,
                minStockAlert = 6.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501025511223",
                name = "Detergente Ariel en Polvo 1kg",
                category = "Limpieza y Hogar",
                costPrice = 32.00,
                sellingPrice = 44.00,
                stock = 9.0,
                minStockAlert = 3.0,
                unit = "kg"
            ),
            Product(
                barcode = "7501025533445",
                name = "Cloro Cloralex Tradicional 950ml",
                category = "Limpieza y Hogar",
                costPrice = 14.00,
                sellingPrice = 20.00,
                stock = 12.0,
                minStockAlert = 4.0,
                unit = "lt"
            ),
            Product(
                barcode = "7501025577889",
                name = "Papel Higiénico Regio 4 Rollos",
                category = "Limpieza y Hogar",
                costPrice = 24.00,
                sellingPrice = 35.00,
                stock = 8.0,
                minStockAlert = 4.0,
                unit = "paquete"
            ),
            Product(
                barcode = "7501030011223",
                name = "Pan de Caja Blanco Bimbo Grande",
                category = "Panadería",
                costPrice = 42.00,
                sellingPrice = 54.00,
                stock = 6.0,
                minStockAlert = 3.0,
                unit = "pza"
            ),
            Product(
                barcode = "7501040022334",
                name = "Manzana Roja Americana",
                category = "Frutas y Verduras",
                costPrice = 30.00,
                sellingPrice = 45.00,
                stock = 15.0,
                minStockAlert = 5.0,
                unit = "kg"
            ),
            Product(
                barcode = "7501040033445",
                name = "Plátano Tabasco",
                category = "Frutas y Verduras",
                costPrice = 15.00,
                sellingPrice = 24.00,
                stock = 20.0,
                minStockAlert = 5.0,
                unit = "kg"
            ),
            Product(
                barcode = "7501040044556",
                name = "Jitomate Saladet",
                category = "Frutas y Verduras",
                costPrice = 18.00,
                sellingPrice = 28.00,
                stock = 12.0,
                minStockAlert = 4.0,
                unit = "kg"
            )
        )
    }
}
