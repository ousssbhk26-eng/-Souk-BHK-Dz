package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

object UserRoles {
    const val PLAYER = "Player"
    const val ADMIN = "Admin"
}

object TournamentFormats {
    const val LEAGUE = "LEAGUE"
    const val KNOCKOUT = "KNOCKOUT"
}

object TournamentStatuses {
    const val OPEN = "OPEN"
    const val ONGOING = "ONGOING"
    const val COMPLETED = "COMPLETED"
}

object MatchStatuses {
    const val SCHEDULED = "SCHEDULED"
    const val LOBBY_OPEN = "LOBBY_OPEN"
    const val WAITING_OPPONENT = "WAITING_OPPONENT"
    const val DISPUTED = "DISPUTED"
    const val COMPLETED = "COMPLETED"
    const val FORFEIT = "FORFEIT"
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val password: String,
    val teamName: String,
    val konamiId: String,
    val role: String = UserRoles.PLAYER,
    val level: Int = 1,
    val xp: Int = 0,
    val played: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val points: Int = 0,
    val isBanned: Boolean = false,
    val clubTag: String = "DZ",
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
    val nextLevelXpTarget: Int get() = level * 500
    val xpProgressFraction: Float
        get() {
            val prevFloor = (level - 1) * 500
            val span = (nextLevelXpTarget - prevFloor).coerceAtLeast(1)
            return ((xp - prevFloor).toFloat() / span.toFloat()).coerceIn(0f, 1f)
        }
}

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val format: String, // LEAGUE or KNOCKOUT
    val maxPlayers: Int, // 8, 16, or 32
    val status: String, // OPEN, ONGOING, COMPLETED
    val prizePool: String,
    val startTimeMillis: Long,
    val participantIdsCsv: String,
    val createdAtMillis: Long,
) {
    val participantIds: List<String>
        get() = participantIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val tournamentId: String,
    val tournamentTitle: String,
    val roundName: String,
    val roundOrder: Int,
    val bracketSlot: Int,
    val playerAId: String,
    val playerATeam: String,
    val playerAName: String,
    val playerAKonamiId: String,
    val playerBId: String,
    val playerBTeam: String,
    val playerBName: String,
    val playerBKonamiId: String,
    val scheduledTimeMillis: Long,
    val roomId: String,
    val status: String,
    val scoreA: Int? = null,
    val scoreB: Int? = null,
    val submittedByAScoreA: Int? = null,
    val submittedByAScoreB: Int? = null,
    val proofUriA: String? = null,
    val playerAPresentAtMillis: Long? = null,
    val submittedByBScoreA: Int? = null,
    val submittedByBScoreB: Int? = null,
    val proofUriB: String? = null,
    val playerBPresentAtMillis: Long? = null,
    val winnerId: String? = null,
    val disputeNote: String? = null,
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val channelId: String, // "GLOBAL" or matchId
    val senderId: String,
    val senderName: String,
    val senderTeam: String,
    val senderRole: String,
    val message: String,
    val timestampMillis: Long,
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val category: String,
    val authorName: String,
    val publishedAtMillis: Long,
    val isPinned: Boolean = false,
)
