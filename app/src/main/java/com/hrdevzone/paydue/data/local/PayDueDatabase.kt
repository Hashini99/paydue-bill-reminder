package com.hrdevzone.paydue.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Due::class, InvoiceRecord::class, PaymentEvent::class],
    version = 2,
    exportSchema = true
)
abstract class PayDueDatabase : RoomDatabase() {

    abstract fun dueDao(): DueDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentEventDao(): PaymentEventDao

    companion object {
        @Volatile
        private var INSTANCE: PayDueDatabase? = null

        fun getInstance(context: Context): PayDueDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PayDueDatabase::class.java,
                    "paydue.db"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
