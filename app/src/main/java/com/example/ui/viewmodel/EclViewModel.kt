package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EclDatabase
import com.example.data.local.SessionPreferences
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MatchEntity
import com.example.data.model.NewsEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.UserEntity
import com.example.data.repository.ActionResult
import com.example.data.repository.EclRepository
import com.example.data.repository.LevelUpEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EclUiState(
    val isLoading: Boolean = true,
    val currentUser: UserEntity? = null,
    val allUsers: List<UserEntity> = emptyList(),
    val standings: List<UserEntity> = emptyList(),
    val tournaments: List<TournamentEntity> = emptyList(),
    val matches: List<MatchEntity> = emptyList(),
    val news: List<NewsEntity> = emptyList(),
    val globalChat: List<ChatMessageEntity> = emptyList(),
    val activeLobbyMatchId: String? = null,
    val showAuthDialog: Boolean = false,
    val levelUpEvent: LevelUpEvent? = null,
    val snackbarMessage: String? = null,
    val isErrorSnackbar: Boolean = false,
)

class EclViewModel(application: Application) : AndroidViewModel(application) {
    private val db = EclDatabase.getInstance(application)
    private val prefs = SessionPreferences(application)
    private val repository = EclRepository(db, prefs)

    private val activeLobbyMatchId = MutableStateFlow<String?>(null)
    private val showAuthDialog = MutableStateFlow(false)
    private val levelUpEvent = MutableStateFlow<LevelUpEvent?>(null)
    private val snackbarState = MutableStateFlow<Pair<String, Boolean>?>(null)

    private val coreDataFlow = combine(
        repository.currentUser,
        repository.allUsers,
        repository.standings,
        repository.tournaments,
        repository.matches,
    ) { currentUser, allUsers, standings, tournaments, matches ->
        CoreDataBundle(currentUser, allUsers, standings, tournaments, matches)
    }

    private val secondaryDataFlow = combine(
        repository.news,
        repository.observeChat("GLOBAL"),
        activeLobbyMatchId,
        showAuthDialog,
        levelUpEvent,
    ) { news, chat, lobbyId, showAuth, lvlUp ->
        SecondaryDataBundle(news, chat, lobbyId, showAuth, lvlUp)
    }

    val uiState: StateFlow<EclUiState> = combine(
        coreDataFlow,
        secondaryDataFlow,
        snackbarState,
    ) { core, sec, snack ->
        EclUiState(
            isLoading = false,
            currentUser = core.currentUser,
            allUsers = core.allUsers,
            standings = core.standings,
            tournaments = core.tournaments,
            matches = core.matches,
            news = sec.news,
            globalChat = sec.chat,
            activeLobbyMatchId = sec.lobbyId,
            showAuthDialog = sec.showAuth || core.currentUser == null,
            levelUpEvent = sec.levelUp,
            snackbarMessage = snack?.first,
            isErrorSnackbar = snack?.second ?: false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EclUiState(isLoading = true),
    )

    fun observeMatch(matchId: String): Flow<MatchEntity?> = repository.observeMatch(matchId)

    fun observeMatchChat(matchId: String): Flow<List<ChatMessageEntity>> = repository.observeChat(matchId)

    fun openMatchLobby(matchId: String) {
        activeLobbyMatchId.value = matchId
    }

    fun closeMatchLobby() {
        activeLobbyMatchId.value = null
    }

    fun openAuthDialog() {
        showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        if (uiState.value.currentUser != null) {
            showAuthDialog.value = false
        }
    }

    fun clearSnackbar() {
        snackbarState.value = null
    }

    fun showSnackbar(message: String) {
        snackbarState.value = message to false
    }

    fun dismissLevelUp() {
        levelUpEvent.value = null
    }

    private fun handleActionResult(result: ActionResult) {
        when (result) {
            is ActionResult.Success -> {
                snackbarState.value = result.message to false
                if (result.levelUp != null) {
                    levelUpEvent.value = result.levelUp
                }
            }
            is ActionResult.Error -> {
                snackbarState.value = result.message to true
            }
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            val res = repository.login(identifier, password)
            handleActionResult(res)
            if (res is ActionResult.Success) showAuthDialog.value = false
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        teamName: String,
        konamiId: String,
    ) {
        viewModelScope.launch {
            val res = repository.register(username, email, password, teamName, konamiId)
            handleActionResult(res)
            if (res is ActionResult.Success) showAuthDialog.value = false
        }
    }

    fun switchQuickAccount(userId: String) {
        viewModelScope.launch {
            val res = repository.switchQuickAccount(userId)
            handleActionResult(res)
            if (res is ActionResult.Success) showAuthDialog.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            activeLobbyMatchId.value = null
            showAuthDialog.value = true
            snackbarState.value = "تم تسجيل الخروج." to false
        }
    }

    fun joinTournament(tournamentId: String) {
        val user = uiState.value.currentUser ?: return
        viewModelScope.launch {
            handleActionResult(repository.joinTournament(tournamentId, user.id))
        }
    }

    fun createTournament(
        title: String,
        description: String,
        format: String,
        maxPlayers: Int,
        prizePool: String,
    ) {
        viewModelScope.launch {
            handleActionResult(repository.createTournament(title, description, format, maxPlayers, prizePool))
        }
    }

    fun autoFillAndStartTournament(tournamentId: String) {
        viewModelScope.launch {
            handleActionResult(repository.autoFillAndStartTournament(tournamentId))
        }
    }

    fun submitMatchResult(
        matchId: String,
        scoreA: Int,
        scoreB: Int,
        proofUri: String?,
        overrideSubmittingUserId: String? = null,
    ) {
        val userId = overrideSubmittingUserId ?: uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            handleActionResult(repository.submitMatchResult(matchId, userId, scoreA, scoreB, proofUri))
        }
    }

    fun claimForfeitWin(matchId: String, proofUri: String?) {
        val userId = uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            handleActionResult(repository.claimForfeitWin(matchId, userId, proofUri))
        }
    }

    fun adminResolveDispute(
        matchId: String,
        officialScoreA: Int,
        officialScoreB: Int,
        note: String,
    ) {
        viewModelScope.launch {
            handleActionResult(repository.adminResolveDispute(matchId, officialScoreA, officialScoreB, note))
        }
    }

    fun sendGlobalChat(message: String) {
        val user = uiState.value.currentUser ?: return
        viewModelScope.launch {
            val res = repository.sendChatMessage("GLOBAL", user, message)
            if (res is ActionResult.Error) handleActionResult(res)
        }
    }

    fun sendLobbyChat(matchId: String, message: String) {
        val user = uiState.value.currentUser ?: return
        viewModelScope.launch {
            val res = repository.sendChatMessage(matchId, user, message)
            if (res is ActionResult.Error) handleActionResult(res)
        }
    }

    fun publishNews(title: String, body: String, category: String) {
        val author = uiState.value.currentUser?.username ?: "Admin_ECL"
        viewModelScope.launch {
            handleActionResult(repository.publishNews(title, body, category, author))
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            repository.deleteNews(newsId)
            snackbarState.value = "تم حذف الخبر." to false
        }
    }

    fun toggleUserBan(userId: String) {
        viewModelScope.launch {
            handleActionResult(repository.toggleUserBan(userId))
        }
    }

    fun toggleUserRole(userId: String) {
        viewModelScope.launch {
            handleActionResult(repository.toggleUserRole(userId))
        }
    }

    private data class CoreDataBundle(
        val currentUser: UserEntity?,
        val allUsers: List<UserEntity>,
        val standings: List<UserEntity>,
        val tournaments: List<TournamentEntity>,
        val matches: List<MatchEntity>,
    )

    private data class SecondaryDataBundle(
        val news: List<NewsEntity>,
        val chat: List<ChatMessageEntity>,
        val lobbyId: String?,
        val showAuth: Boolean,
        val levelUp: LevelUpEvent?,
    )
}
