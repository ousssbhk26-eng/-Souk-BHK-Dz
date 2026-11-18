package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MatchEntity
import com.example.data.model.NewsEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY points DESC, (goalsFor - goalsAgainst) DESC, goalsFor DESC")
    fun observeStandings(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY level DESC, xp DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    suspend fun findByEmailOrUsername(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<UserEntity>

    @Query("SELECT * FROM users WHERE isBanned = 0")
    suspend fun getActiveUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tournaments: List<TournamentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tournament: TournamentEntity)

    @Update
    suspend fun update(tournament: TournamentEntity)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY scheduledTimeMillis ASC, roundOrder ASC, bracketSlot ASC")
    fun observeAll(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundOrder ASC, bracketSlot ASC")
    fun observeByTournament(tournamentId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundOrder ASC, bracketSlot ASC")
    suspend fun getByTournament(tournamentId: String): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: MatchEntity)

    @Update
    suspend fun update(match: MatchEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestampMillis ASC")
    fun observeChannel(channelId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY isPinned DESC, publishedAtMillis DESC")
    fun observeAll(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NewsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NewsEntity)

    @Query("DELETE FROM news WHERE id = :id")
    suspend fun deleteById(id: String)
}
