package com.alanbadrain.invoicemini.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY updatedAtMillis DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceWithItemsOnce(id: String): InvoiceWithItems?

    @Query("SELECT * FROM invoices ORDER BY updatedAtMillis DESC")
    suspend fun getAllInvoicesOnce(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
}

@Dao
interface LineItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LineItemEntity)

    @Delete
    suspend fun delete(item: LineItemEntity)

    @Query("DELETE FROM line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteAllForInvoice(invoiceId: String)

    @Query("SELECT * FROM line_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: String): List<LineItemEntity>
}

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<BusinessProfileEntity?>

    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): BusinessProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: BusinessProfileEntity)
}
