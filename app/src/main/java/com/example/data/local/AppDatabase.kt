package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.CashMovement
import com.example.data.model.CashSession
import com.example.data.model.Category
import com.example.data.model.CustomerCredit
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.PurchaseItem
import com.example.data.model.Sale
import com.example.data.model.SaleItem

@Database(
    entities = [
        Product::class,
        Category::class,
        Sale::class,
        SaleItem::class,
        Purchase::class,
        PurchaseItem::class,
        CashSession::class,
        CashMovement::class,
        Expense::class,
        CustomerCredit::class,
        BusinessProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun cashDao(): CashDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun customerCreditDao(): CustomerCreditDao
    abstract fun businessProfileDao(): BusinessProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minegocio_pos.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
