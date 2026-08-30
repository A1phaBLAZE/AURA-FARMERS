package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CropEntity::class,
        LotEntity::class,
        OfferEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KisanDatabase : RoomDatabase() {
    abstract fun kisanDao(): KisanDao

    companion object {
        @Volatile
        private var INSTANCE: KisanDatabase? = null

        fun getDatabase(context: Context): KisanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KisanDatabase::class.java,
                    "kisan_vani_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
