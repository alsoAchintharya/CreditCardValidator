package com.example.cardvalidator

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val cardNumber: String,
    val holderName: String,
    val expiry: String,
    val cvv: String,
    val brandName: String?
)

@Dao
interface CardDao {

    @Query("SELECT * FROM cards ORDER BY id DESC")
    fun getAllCards(): Flow<List<CreditCard>>

    @Insert
    suspend fun insert(card: CreditCard)

    @Delete
    suspend fun delete(card: CreditCard)
}

@Database(
    entities = [CreditCard::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wallet_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}