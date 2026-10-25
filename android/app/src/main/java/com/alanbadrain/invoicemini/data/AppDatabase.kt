package com.alanbadrain.invoicemini.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [InvoiceEntity::class, LineItemEntity::class, BusinessProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun lineItemDao(): LineItemDao
    abstract fun businessProfileDao(): BusinessProfileDao

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE business_profile ADD COLUMN countryCode TEXT")
                db.execSQL("ALTER TABLE business_profile ADD COLUMN callingCode TEXT")
                db.execSQL("ALTER TABLE business_profile ADD COLUMN taxNumber TEXT")
                db.execSQL("ALTER TABLE invoices ADD COLUMN businessPhone TEXT")
                db.execSQL("ALTER TABLE invoices ADD COLUMN clientPhone TEXT")
                db.execSQL("ALTER TABLE invoices ADD COLUMN businessTaxNumber TEXT")
                db.execSQL("ALTER TABLE invoices ADD COLUMN currencyCode TEXT")
                db.execSQL("ALTER TABLE invoices ADD COLUMN stampPath TEXT")
                db.execSQL("ALTER TABLE invoices ADD COLUMN signaturePath TEXT")
            }
        }

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "invoice_mini.db"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
