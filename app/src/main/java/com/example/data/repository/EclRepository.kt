package com.example.data.repository

import com.example.data.local.EclDatabase
import com.example.data.local.SessionPreferences
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MatchEntity
import com.example.data.model.MatchStatuses
import com.example.data.model.NewsEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.TournamentFormats
import com.example.data.model.TournamentStatuses
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import kotlin.math.abs

data class LevelUpEvent(
    val userId: String,
    val username: String,
    val newLevel: Int,
    val totalXp: Int,
)

sealed interface ActionResult {
    data class Success(val message: String, val levelUp: LevelUpEvent? = null) : ActionResult
    data class Error(val message: String) : ActionResult
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EclRepository(
    private val db: EclDatabase,
    private val sessionPrefs: SessionPreferences,
) {
    private val userDao = db.userDao()
    private val tournamentDao = db.tournamentDao()
    private val matchDao = db.matchDao()
    private val chatDao = db.chatDao()
    private val newsDao = db.newsDao()

    val currentUserId: Flow<String?> = sessionPrefs.currentUserId

    val currentUser: Flow<UserEntity?> = sessionPrefs.currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.observeUserById(id)
    }

    val standings: Flow<List<UserEntity>> = userDao.observeStandings()
    val allUsers: Flow<List<UserEntity>> = userDao.observeAllUsers()
    val tournaments: Flow<List<TournamentEntity>> = tournamentDao.observeAll()
    val matches: Flow<List<MatchEntity>> = matchDao.observeAll()
    val news: Flow<List<NewsEntity>> = newsDao.observeAll()

    fun observeChat(channelId: String): Flow<List<ChatMessageEntity>> =
        chatDao.observeChannel(channelId)

    fun observeMatch(matchId: String): Flow<MatchEntity?> =
        matchDao.observeById(matchId)

    suspend fun login(identifier: String, password: String): ActionResult {
        val cleanId = identifier.trim()
        val cleanPass = password.trim()
        if (cleanId.isEmpty() || cleanPass.isEmpty()) {
            return ActionResult.Error("يرجى إدخال اسم المستخدم/البريد الإلكتروني وكلمة المرور.")
        }
        val user = userDao.findByEmailOrUsername(cleanId)
            ?: return ActionResult.Error("الحساب غير موجود. تأكد من البيانات أو أنشئ حساباً جديداً.")
        if (user.password != cleanPass) {
            return ActionResult.Error("كلمة المرور غير صحيحة.")
        }
        if (user.isBanned) {
            return ActionResult.Error("تم تعليق هذا الحساب من طرف الإدارة لمخالفة القوانين.")
        }
        sessionPrefs.setCurrentUserId(user.id)
        return ActionResult.Success("مرحباً بعودتك، ${user.username}!")
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        teamName: String,
        konamiId: String,
    ): ActionResult {
        val u = username.trim()
        val e = email.trim()
        val p = password.trim()
        val t = teamName.trim()
        val k = konamiId.trim()

        if (u.isEmpty() || e.isEmpty() || p.length < 4 || t.isEmpty() || k.isEmpty()) {
            return ActionResult.Error("يرجى ملء جميع الحقول المطلوبة (كلمة المرور 4 أحرف على الأقل).")
        }
        if (userDao.findByEmailOrUsername(u) != null || userDao.findByEmailOrUsername(e) != null) {
            return ActionResult.Error("اسم المستخدم أو البريد الإلكتروني مسجل مسبقاً.")
        }
        val tag = t.split(" ").filter { it.isNotBlank() }.take(3).joinToString("") {
            it.first().uppercaseChar().toString()
        }.ifEmpty { "DZ" }

        val newUser = UserEntity(
            id = "u_${UUID.randomUUID().toString().take(8)}",
            username = u,
            email = e,
            password = p,
            teamName = t,
            konamiId = k,
            role = UserRoles.PLAYER,
            level = 1,
            xp = 0,
            played = 0,
            wins = 0,
            draws = 0,
            losses = 0,
            goalsFor = 0,
            goalsAgainst = 0,
            points = 0,
            isBanned = false,
            clubTag = tag,
        )
        userDao.insert(newUser)
        sessionPrefs.setCurrentUserId(newUser.id)
        return ActionResult.Success("تم إنشاء الحساب بنجاح! تم تعيينك في المستوى 1 كلاعب.")
    }

    suspend fun switchQuickAccount(userId: String): ActionResult {
        val user = userDao.getUserById(userId) ?: return ActionResult.Error("الحساب غير موجود.")
        if (user.isBanned) return ActionResult.Error("هذا الحساب محظور حالياً.")
        sessionPrefs.setCurrentUserId(user.id)
        return ActionResult.Success("تم التبديل إلى حساب ${user.username} (${if (user.role == UserRoles.ADMIN) "مشرف" else "لاعب"})")
    }

    suspend fun logout() {
        sessionPrefs.setCurrentUserId(null)
    }

    suspend fun joinTournament(tournamentId: String, userId: String): ActionResult {
        val user = userDao.getUserById(userId) ?: return ActionResult.Error("يرجى تسجيل الدخول أولاً.")
        if (user.isBanned) return ActionResult.Error("الحساب محظور من المشاركة.")
        val tournament = tournamentDao.getById(tournamentId)
            ?: return ActionResult.Error("البطولة غير موجودة.")
        if (tournament.status != TournamentStatuses.OPEN) {
            return ActionResult.Error("التسجيل مغلق لهذه البطولة.")
        }
        val currentIds = tournament.participantIds.toMutableList()
        if (currentIds.contains(userId)) {
            return ActionResult.Error("أنت مسجل بالفعل في هذه البطولة.")
        }
        if (currentIds.size >= tournament.maxPlayers) {
            return ActionResult.Error("البطولة مكتملة العدد.")
        }
        currentIds.add(userId)
        val isFull = currentIds.size >= tournament.maxPlayers
        val updated = tournament.copy(
            participantIdsCsv = currentIds.joinToString(","),
            status = if (isFull) TournamentStatuses.ONGOING else TournamentStatuses.OPEN,
        )
        tournamentDao.update(updated)

        if (isFull) {
            generateScheduleForTournament(updated)
            return ActionResult.Success("تم انضمامك واكتمل العدد (${updated.maxPlayers}/${updated.maxPlayers})! تم توليد جدول المباريات تلقائياً.")
        }
        return ActionResult.Success("تم تسجيل فريقك (${user.teamName}) في البطولة بنجاح!")
    }

    suspend fun createTournament(
        title: String,
        description: String,
        format: String,
        maxPlayers: Int,
        prizePool: String,
    ): ActionResult {
        if (title.isBlank()) return ActionResult.Error("يرجى إدخال اسم البطولة.")
        val safeMax = if (maxPlayers in listOf(8, 16, 32)) maxPlayers else 8
        val now = System.currentTimeMillis()
        val tournament = TournamentEntity(
            id = "t_${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            description = description.trim().ifEmpty { "بطولة رسمية معتمدة من منصة ECL الجزائر." },
            format = format,
            maxPlayers = safeMax,
            status = TournamentStatuses.OPEN,
            prizePool = prizePool.trim().ifEmpty { "30,000 دج + لقب البطولة" },
            startTimeMillis = now + 86_400_000L,
            participantIdsCsv = "",
            createdAtMillis = now,
        )
        tournamentDao.insert(tournament)
        return ActionResult.Success("تم إنشاء البطولة (${tournament.title}) بسعة $safeMax لاعباً.")
    }

    suspend fun autoFillAndStartTournament(tournamentId: String): ActionResult {
        val tournament = tournamentDao.getById(tournamentId) ?: return ActionResult.Error("البطولة غير موجودة.")
        if (tournament.status != TournamentStatuses.OPEN) {
            return ActionResult.Error("البطولة بدأت بالفعل.")
        }
        val activeUsers = userDao.getActiveUsers()
        val existing = tournament.participantIds.toMutableList()
        for (u in activeUsers) {
            if (existing.size >= tournament.maxPlayers) break
            if (!existing.contains(u.id)) existing.add(u.id)
        }
        // Create synthetic reserve clubs if maxPlayers > activeUsers.size (e.g., 16 or 32)
        var idx = 1
        while (existing.size < tournament.maxPlayers) {
            val botId = "u_club_${tournament.id.take(4)}_$idx"
            if (userDao.getUserById(botId) == null) {
                userDao.insert(
                    UserEntity(
                        id = botId,
                        username = "ClubDZ_$idx",
                        email = "club$idx@ecl.dz",
                        password = "123456",
                        teamName = "نادي النخبة $idx",
                        konamiId = "ECL-PRO-${100 + idx}",
                        role = UserRoles.PLAYER,
                        level = 1,
                        xp = 150,
                        clubTag = "DZ$idx",
                    )
                )
            }
            existing.add(botId)
            idx++
        }
        val started = tournament.copy(
            participantIdsCsv = existing.joinToString(","),
            status = TournamentStatuses.ONGOING,
        )
        tournamentDao.update(started)
        generateScheduleForTournament(started)
        return ActionResult.Success("تم إكمال المقاعد (${started.maxPlayers}/${started.maxPlayers}) وتوليد جدول المباريات تلقائياً!")
    }

    private suspend fun generateScheduleForTournament(tournament: TournamentEntity) {
        val usersMap = userDao.getUsersByIds(tournament.participantIds).associateBy { it.id }
        val participants = tournament.participantIds.mapNotNull { usersMap[it] }
        if (participants.size < 2) return

        val now = System.currentTimeMillis()
        val generated = mutableListOf<MatchEntity>()

        if (tournament.format == TournamentFormats.LEAGUE) {
            // Round-robin Berger algorithm
            val list = participants.toMutableList()
            if (list.size % 2 != 0) return
            val rounds = list.size - 1
            val half = list.size / 2
            var slotCounter = 1
            for (round in 1..rounds) {
                for (i in 0 until half) {
                    val home = list[i]
                    val away = list[list.size - 1 - i]
                    generated.add(
                        MatchEntity(
                            id = "m_${tournament.id.take(6)}_r${round}_$i",
                            tournamentId = tournament.id,
                            tournamentTitle = tournament.title,
                            roundName = "الجولة $round",
                            roundOrder = round,
                            bracketSlot = slotCounter++,
                            playerAId = home.id,
                            playerATeam = home.teamName,
                            playerAName = home.username,
                            playerAKonamiId = home.konamiId,
                            playerBId = away.id,
                            playerBTeam = away.teamName,
                            playerBName = away.username,
                            playerBKonamiId = away.konamiId,
                            scheduledTimeMillis = now + (round * 3_600_000L),
                            roomId = "ECL-${abs(tournament.id.hashCode() % 9000) + 1000}-$round$i",
                            status = if (round == 1) MatchStatuses.LOBBY_OPEN else MatchStatuses.SCHEDULED,
                        )
                    )
                }
                val last = list.removeAt(list.size - 1)
                list.add(1, last)
            }
        } else {
            // Knockout Bracket Round 1
            val roundName = when (participants.size) {
                32 -> "دور الـ 32"
                16 -> "ثمن النهائي"
                8 -> "ربع النهائي"
                4 -> "نصف النهائي"
                else -> "الجولة الأولى"
            }
            var slot = 1
            for (i in participants.indices step 2) {
                val pA = participants[i]
                val pB = participants[i + 1]
                generated.add(
                    MatchEntity(
                        id = "m_${tournament.id.take(6)}_ko1_$slot",
                        tournamentId = tournament.id,
                        tournamentTitle = tournament.title,
                        roundName = roundName,
                        roundOrder = 1,
                        bracketSlot = slot,
                        playerAId = pA.id,
                        playerATeam = pA.teamName,
                        playerAName = pA.username,
                        playerAKonamiId = pA.konamiId,
                        playerBId = pB.id,
                        playerBTeam = pB.teamName,
                        playerBName = pB.username,
                        playerBKonamiId = pB.konamiId,
                        scheduledTimeMillis = now + (10 * 60_000L) + (slot * 300_000L),
                        roomId = "ECL-KO-${1000 + slot * 37}",
                        status = MatchStatuses.LOBBY_OPEN,
                    )
                )
                slot++
            }
        }
        matchDao.insertAll(generated)
    }

    suspend fun submitMatchResult(
        matchId: String,
        submittingUserId: String,
        scoreA: Int,
        scoreB: Int,
        proofUri: String?,
    ): ActionResult {
        val match = matchDao.getById(matchId) ?: return ActionResult.Error("المباراة غير موجودة.")
        if (match.status == MatchStatuses.COMPLETED || match.status == MatchStatuses.FORFEIT) {
            return ActionResult.Error("تم اعتماد نتيجة هذه المباراة مسبقاً.")
        }
        if (proofUri.isNullOrBlank()) {
            return ActionResult.Error("يرجى إرفاق لقطة شاشة لإثبات النتيجة.")
        }
        val now = System.currentTimeMillis()
        val isPlayerA = submittingUserId == match.playerAId
        val isPlayerB = submittingUserId == match.playerBId
        if (!isPlayerA && !isPlayerB) {
            return ActionResult.Error("أنت لست طرفاً في هذه المباراة.")
        }

        val updated = if (isPlayerA) {
            match.copy(
                submittedByAScoreA = scoreA,
                submittedByAScoreB = scoreB,
                proofUriA = proofUri,
                playerAPresentAtMillis = match.playerAPresentAtMillis ?: now,
            )
        } else {
            match.copy(
                submittedByBScoreA = scoreA,
                submittedByBScoreB = scoreB,
                proofUriB = proofUri,
                playerBPresentAtMillis = match.playerBPresentAtMillis ?: now,
            )
        }

        val aScoreA = updated.submittedByAScoreA
        val aScoreB = updated.submittedByAScoreB
        val bScoreA = updated.submittedByBScoreA
        val bScoreB = updated.submittedByBScoreB

        if (aScoreA != null && aScoreB != null && bScoreA != null && bScoreB != null) {
            return if (aScoreA == bScoreA && aScoreB == bScoreB) {
                // Auto-approve!
                val levelUp = finalizeMatchAndScore(
                    match = updated,
                    finalScoreA = aScoreA,
                    finalScoreB = aScoreB,
                    finalStatus = MatchStatuses.COMPLETED,
                    activeViewerId = submittingUserId,
                    note = "تم التطابق التلقائي لنتيجة اللاعبين.",
                )
                ActionResult.Success(
                    message = "تطابقت النتيجة ($aScoreA - $aScoreB)! تم اعتماد الفوز واحتساب النقاط و XP تلقائياً.",
                    levelUp = levelUp,
                )
            } else {
                // Conflict -> Flag as Dispute!
                val disputed = updated.copy(
                    status = MatchStatuses.DISPUTED,
                    disputeNote = "تعارض نتائج: ${updated.playerAName} أرسل ($aScoreA-$aScoreB) بينما ${updated.playerBName} أرسل ($bScoreA-$bScoreB).",
                )
                matchDao.update(disputed)
                ActionResult.Success(
                    message = "تعارضت النتيجة مع إدخال الخصم! تم تحويل المباراة إلى حالة (نزاع) وإشعار الإدارة للمراجعة.",
                )
            }
        } else {
            val waiting = updated.copy(status = MatchStatuses.WAITING_OPPONENT)
            matchDao.update(waiting)
            return ActionResult.Success("تم إرسال نتيجتك وإثبات الصورة! بانتظار تأكيد الخصم للمطابقة التلقائية.")
        }
    }

    suspend fun claimForfeitWin(
        matchId: String,
        presentUserId: String,
        proofUri: String?,
    ): ActionResult {
        val match = matchDao.getById(matchId) ?: return ActionResult.Error("المباراة غير موجودة.")
        if (match.status == MatchStatuses.COMPLETED || match.status == MatchStatuses.FORFEIT) {
            return ActionResult.Error("هذه المباراة منتهية بالفعل.")
        }
        val now = System.currentTimeMillis()
        val overdueMillis = now - match.scheduledTimeMillis
        if (overdueMillis < 15 * 60_000L) {
            val minsLeft = ((15 * 60_000L - overdueMillis) / 60_000L) + 1
            return ActionResult.Error("يجب انتظار مرور 15 دقيقة على موعد الانطلاق للمطالبة بالفوز الاعتباري (متبقي $minsLeft د).")
        }
        if (proofUri.isNullOrBlank()) {
            return ActionResult.Error("يرجى إرفاق لقطة شاشة تثبت تواجدك في الغرفة.")
        }

        val isPlayerA = presentUserId == match.playerAId
        val isPlayerB = presentUserId == match.playerBId
        if (!isPlayerA && !isPlayerB) return ActionResult.Error("أنت لست طرفاً في هذه المباراة.")

        if (isPlayerA && match.submittedByBScoreA != null) {
            return ActionResult.Error("الخصم متواجد وأرسل نتيجته بالفعل.")
        }
        if (isPlayerB && match.submittedByAScoreA != null) {
            return ActionResult.Error("الخصم متواجد وأرسل نتيجته بالفعل.")
        }

        val finalScoreA = if (isPlayerA) 3 else 0
        val finalScoreB = if (isPlayerB) 3 else 0
        val withProof = if (isPlayerA) {
            match.copy(proofUriA = proofUri, playerAPresentAtMillis = now)
        } else {
            match.copy(proofUriB = proofUri, playerBPresentAtMillis = now)
        }

        val levelUp = finalizeMatchAndScore(
            match = withProof,
            finalScoreA = finalScoreA,
            finalScoreB = finalScoreB,
            finalStatus = MatchStatuses.FORFEIT,
            activeViewerId = presentUserId,
            note = "فوز اعتباري (3-0) بسبب غياب الخصم لأكثر من 15 دقيقة.",
        )
        return ActionResult.Success(
            message = "تم اعتماد الفوز الاعتباري (3 - 0) وإضافة النقاط و XP إلى رصيدك!",
            levelUp = levelUp,
        )
    }

    suspend fun adminResolveDispute(
        matchId: String,
        officialScoreA: Int,
        officialScoreB: Int,
        resolutionNote: String,
    ): ActionResult {
        val match = matchDao.getById(matchId) ?: return ActionResult.Error("المباراة غير موجودة.")
        val levelUp = finalizeMatchAndScore(
            match = match,
            finalScoreA = officialScoreA,
            finalScoreB = officialScoreB,
            finalStatus = MatchStatuses.COMPLETED,
            activeViewerId = null,
            note = resolutionNote.ifBlank { "تم الحسم بقرار إداري بعد مراجعة لقطات الشاشة." },
        )
        return ActionResult.Success("تم حسم النزاع واعتماد النتيجة ($officialScoreA - $officialScoreB).", levelUp)
    }

    private suspend fun finalizeMatchAndScore(
        match: MatchEntity,
        finalScoreA: Int,
        finalScoreB: Int,
        finalStatus: String,
        activeViewerId: String?,
        note: String,
    ): LevelUpEvent? {
        val winnerId = when {
            finalScoreA > finalScoreB -> match.playerAId
            finalScoreB > finalScoreA -> match.playerBId
            else -> null
        }

        val updatedMatch = match.copy(
            scoreA = finalScoreA,
            scoreB = finalScoreB,
            status = finalStatus,
            winnerId = winnerId,
            disputeNote = note,
        )
        matchDao.update(updatedMatch)

        val userA = userDao.getUserById(match.playerAId)
        val userB = userDao.getUserById(match.playerBId)
        var viewerLevelUp: LevelUpEvent? = null

        if (userA != null) {
            val (updatedA, lvlUpA) = applyStatsAndXp(userA, finalScoreA, finalScoreB)
            userDao.update(updatedA)
            if (activeViewerId == userA.id && lvlUpA != null) viewerLevelUp = lvlUpA
        }
        if (userB != null) {
            val (updatedB, lvlUpB) = applyStatsAndXp(userB, finalScoreB, finalScoreA)
            userDao.update(updatedB)
            if (activeViewerId == userB.id && lvlUpB != null) viewerLevelUp = lvlUpB
        }

        // Advance Knockout Winner if tournament is KNOCKOUT
        val tournament = tournamentDao.getById(match.tournamentId)
        if (tournament != null && tournament.format == TournamentFormats.KNOCKOUT && winnerId != null) {
            advanceKnockoutWinner(tournament, updatedMatch, winnerId)
        }

        return viewerLevelUp
    }

    private fun applyStatsAndXp(
        user: UserEntity,
        scored: Int,
        conceded: Int,
    ): Pair<UserEntity, LevelUpEvent?> {
        val isWin = scored > conceded
        val isDraw = scored == conceded
        val addedPoints = when {
            isWin -> 3
            isDraw -> 1
            else -> 0
        }
        val addedXp = when {
            isWin -> 100
            isDraw -> 50
            else -> 20
        }

        var newLevel = user.level
        val newXp = user.xp + addedXp
        while (newXp >= newLevel * 500) {
            newLevel += 1
        }
        val leveledUp = if (newLevel > user.level) {
            LevelUpEvent(user.id, user.username, newLevel, newXp)
        } else null

        val updated = user.copy(
            played = user.played + 1,
            wins = user.wins + if (isWin) 1 else 0,
            draws = user.draws + if (isDraw) 1 else 0,
            losses = user.losses + if (!isWin && !isDraw) 1 else 0,
            goalsFor = user.goalsFor + scored,
            goalsAgainst = user.goalsAgainst + conceded,
            points = user.points + addedPoints,
            xp = newXp,
            level = newLevel,
        )
        return updated to leveledUp
    }

    private suspend fun advanceKnockoutWinner(
        tournament: TournamentEntity,
        completedMatch: MatchEntity,
        winnerId: String,
    ) {
        val winner = userDao.getUserById(winnerId) ?: return
        val allMatches = matchDao.getByTournament(tournament.id)
        val roundMatches = allMatches.filter { it.roundOrder == completedMatch.roundOrder }
        val nextRoundOrder = completedMatch.roundOrder + 1
        val nextSlot = (completedMatch.bracketSlot + 1) / 2
        val partnerSlot = if (completedMatch.bracketSlot % 2 == 1) completedMatch.bracketSlot + 1 else completedMatch.bracketSlot - 1
        val partnerMatch = roundMatches.firstOrNull { it.bracketSlot == partnerSlot }

        // Check if this was already the Final (only 1 match in round and tournament maxPlayers reached final)
        val expectedMatchesInRound = tournament.maxPlayers / (1 shl completedMatch.roundOrder)
        if (expectedMatchesInRound <= 1 && completedMatch.roundOrder > 1) {
            tournamentDao.update(tournament.copy(status = TournamentStatuses.COMPLETED))
            return
        }

        val existingNext = allMatches.firstOrNull {
            it.roundOrder == nextRoundOrder && it.bracketSlot == nextSlot
        }
        if (existingNext != null) {
            // Fill vacant side if needed
            val isOddSlot = completedMatch.bracketSlot % 2 == 1
            val patched = if (isOddSlot) {
                existingNext.copy(
                    playerAId = winner.id,
                    playerATeam = winner.teamName,
                    playerAName = winner.username,
                    playerAKonamiId = winner.konamiId,
                )
            } else {
                existingNext.copy(
                    playerBId = winner.id,
                    playerBTeam = winner.teamName,
                    playerBName = winner.username,
                    playerBKonamiId = winner.konamiId,
                )
            }
            matchDao.update(patched)
            return
        }

        val partnerWinnerId = partnerMatch?.winnerId ?: return
        val partnerWinner = userDao.getUserById(partnerWinnerId) ?: return
        val firstPlayer = if (completedMatch.bracketSlot < partnerMatch.bracketSlot) winner else partnerWinner
        val secondPlayer = if (completedMatch.bracketSlot < partnerMatch.bracketSlot) partnerWinner else winner

        val remainingTeams = expectedMatchesInRound
        val nextRoundName = when (remainingTeams) {
            8 -> "ربع النهائي"
            4 -> "نصف النهائي"
            2 -> "النهائي"
            else -> "الدور $nextRoundOrder"
        }

        val newMatch = MatchEntity(
            id = "m_${tournament.id.take(6)}_r${nextRoundOrder}_$nextSlot",
            tournamentId = tournament.id,
            tournamentTitle = tournament.title,
            roundName = nextRoundName,
            roundOrder = nextRoundOrder,
            bracketSlot = nextSlot,
            playerAId = firstPlayer.id,
            playerATeam = firstPlayer.teamName,
            playerAName = firstPlayer.username,
            playerAKonamiId = firstPlayer.konamiId,
            playerBId = secondPlayer.id,
            playerBTeam = secondPlayer.teamName,
            playerBName = secondPlayer.username,
            playerBKonamiId = secondPlayer.konamiId,
            scheduledTimeMillis = System.currentTimeMillis() + 3_600_000L,
            roomId = "ECL-KO-${2000 + nextRoundOrder * 100 + nextSlot}",
            status = MatchStatuses.LOBBY_OPEN,
        )
        matchDao.insert(newMatch)
    }

    suspend fun sendChatMessage(channelId: String, user: UserEntity, text: String): ActionResult {
        val clean = text.trim()
        if (clean.isEmpty()) return ActionResult.Error("اكتب رسالة قبل الإرسال.")
        if (user.isBanned) return ActionResult.Error("لا يمكن للحسابات المحظورة الإرسال في الدردشة.")
        chatDao.insert(
            ChatMessageEntity(
                id = "c_${UUID.randomUUID().toString().take(8)}",
                channelId = channelId,
                senderId = user.id,
                senderName = user.username,
                senderTeam = user.teamName,
                senderRole = user.role,
                message = clean,
                timestampMillis = System.currentTimeMillis(),
            )
        )
        return ActionResult.Success("تم إرسال الرسالة.")
    }

    suspend fun publishNews(title: String, body: String, category: String, authorName: String): ActionResult {
        if (title.isBlank() || body.isBlank()) {
            return ActionResult.Error("يرجى إدخال عنوان الخبر والمحتوى.")
        }
        newsDao.insert(
            NewsEntity(
                id = "n_${UUID.randomUUID().toString().take(8)}",
                title = title.trim(),
                body = body.trim(),
                category = category,
                authorName = authorName,
                publishedAtMillis = System.currentTimeMillis(),
                isPinned = false,
            )
        )
        return ActionResult.Success("تم نشر الخبر بنجاح!")
    }

    suspend fun deleteNews(newsId: String) {
        newsDao.deleteById(newsId)
    }

    suspend fun toggleUserBan(userId: String): ActionResult {
        val user = userDao.getUserById(userId) ?: return ActionResult.Error("المستخدم غير موجود.")
        if (user.role == UserRoles.ADMIN) return ActionResult.Error("لا يمكن حظر حساب الإدارة.")
        val updated = user.copy(isBanned = !user.isBanned)
        userDao.update(updated)
        return ActionResult.Success(
            if (updated.isBanned) "تم حظر ${updated.username} بنجاح." else "تم فك الحظر عن ${updated.username}."
        )
    }

    suspend fun toggleUserRole(userId: String): ActionResult {
        val user = userDao.getUserById(userId) ?: return ActionResult.Error("المستخدم غير موجود.")
        val nextRole = if (user.role == UserRoles.ADMIN) UserRoles.PLAYER else UserRoles.ADMIN
        userDao.update(user.copy(role = nextRole))
        return ActionResult.Success("تم تغيير صلاحية ${user.username} إلى $nextRole.")
    }
}
