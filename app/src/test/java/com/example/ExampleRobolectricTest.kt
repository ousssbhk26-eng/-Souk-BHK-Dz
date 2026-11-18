package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.EclDatabase
import com.example.data.local.SeedData
import com.example.data.local.SessionPreferences
import com.example.data.model.MatchStatuses
import com.example.data.model.TournamentStatuses
import com.example.data.model.UserRoles
import com.example.data.repository.ActionResult
import com.example.data.repository.EclRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: EclDatabase
    private lateinit var repo: EclRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EclDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runBlocking { SeedData.populate(db) }
        repo = EclRepository(db, SessionPreferences(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ECL DZ", appName)
    }

    @Test
    fun `register initializes new user with level 1 xp 0 and role Player`() = runBlocking {
        val res = repo.register(
            username = "Nabil_DZ",
            email = "nabil@ecl.dz",
            password = "secret",
            teamName = "نصر حسين داي",
            konamiId = "NAHD-991-DZ",
        )
        assertTrue(res is ActionResult.Success)
        val user = db.userDao().findByEmailOrUsername("nabil@ecl.dz")
        assertNotNull(user)
        assertEquals(1, user!!.level)
        assertEquals(0, user.xp)
        assertEquals(UserRoles.PLAYER, user.role)
    }

    @Test
    fun `joining 7 of 8 tournament automatically generates round robin schedule`() = runBlocking {
        val res = repo.joinTournament("t_open_elite", "u_yacine")
        assertTrue(res is ActionResult.Success)

        val tournament = db.tournamentDao().getById("t_open_elite")!!
        assertEquals(TournamentStatuses.ONGOING, tournament.status)
        assertEquals(8, tournament.participantIds.size)

        val generatedMatches = db.matchDao().getByTournament("t_open_elite")
        // 8-player single round-robin generates 8 * 7 / 2 = 28 matches
        assertEquals(28, generatedMatches.size)
    }

    @Test
    fun `matching score submission auto approves awards points and triggers level up`() = runBlocking {
        // In m_today_1, Riyad_Pro (B) already submitted 2 - 1.
        // Raise Yacine's XP to 1450 (Level 3, target 1500) so +100 XP triggers Level 4!
        val yacine = db.userDao().getUserById("u_yacine")!!
        db.userDao().update(yacine.copy(xp = 1450))

        val res = repo.submitMatchResult(
            matchId = "m_today_1",
            submittingUserId = "u_yacine",
            scoreA = 2,
            scoreB = 1,
            proofUri = "res:img_proof_sample_1",
        )
        assertTrue(res is ActionResult.Success)
        val success = res as ActionResult.Success
        assertNotNull(success.levelUp)
        assertEquals(4, success.levelUp!!.newLevel)

        val updatedMatch = db.matchDao().getById("m_today_1")!!
        assertEquals(MatchStatuses.COMPLETED, updatedMatch.status)
        assertEquals(2, updatedMatch.scoreA)
        assertEquals(1, updatedMatch.scoreB)
    }

    @Test
    fun `conflicting score submission flags match as disputed`() = runBlocking {
        // Riyad_Pro submitted 2 - 1; Yacine submits 3 - 0 -> Conflict!
        val res = repo.submitMatchResult(
            matchId = "m_today_1",
            submittingUserId = "u_yacine",
            scoreA = 3,
            scoreB = 0,
            proofUri = "res:img_proof_sample_2",
        )
        assertTrue(res is ActionResult.Success)
        val updatedMatch = db.matchDao().getById("m_today_1")!!
        assertEquals(MatchStatuses.DISPUTED, updatedMatch.status)
    }

    @Test
    fun `overdue absent opponent allows 3-0 forfeit win claim`() = runBlocking {
        val beforeStandings = repo.standings.first()
        assertTrue(beforeStandings.isNotEmpty())

        val res = repo.claimForfeitWin("m_forfeit_test", "u_yacine", "res:img_proof_sample_1")
        assertTrue(res is ActionResult.Success)
        val match = db.matchDao().getById("m_forfeit_test")!!
        assertEquals(MatchStatuses.FORFEIT, match.status)
        assertEquals(3, match.scoreA)
        assertEquals(0, match.scoreB)
    }
}
