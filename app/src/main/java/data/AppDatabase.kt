package data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val username: String,
    val passwordHash: String,
    val profileImagePath: String? = null
)

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userOwnerId"])]
)
data class CreditCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardNumber: String,
    val holderName: String,
    val expiry: String,
    val cvv: String,
    val brandName: String?,
    val userOwnerId: Long
)

@Dao
interface CardDao {

    @Query("SELECT * FROM cards WHERE userOwnerId = :userId ORDER BY id DESC")
    fun getCardsForUser(userId: Long): Flow<List<CreditCard>>

    @Insert
    suspend fun insert(card: CreditCard)

    @Delete
    suspend fun delete(card: CreditCard)
}

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("UPDATE users SET profileImagePath = :path WHERE username = :username")
    suspend fun updateProfileImage(username: String, path: String)
}

@Database(
    entities = [CreditCard::class, User::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao
    abstract fun userDao(): UserDao

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
                    //.fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}