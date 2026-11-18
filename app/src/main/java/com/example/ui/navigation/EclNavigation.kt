package com.example.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.MatchStatuses
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import com.example.ui.components.AuthModalDialog
import com.example.ui.components.LevelUpCelebrationDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ChampionsCupScreen
import com.example.ui.screens.CommunityChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LeaguesScreen
import com.example.ui.screens.MatchLobbyScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonDispute
import com.example.ui.theme.EmeraldPitch
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EclUiState
import com.example.ui.viewmodel.EclViewModel
import kotlinx.coroutines.launch

object EclRoutes {
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_LEAGUES = "leagues"
    const val ROUTE_CHAMPIONS_CUP = "champions_cup"
    const val ROUTE_MATCHES = "matches"
    const val ROUTE_NEWS = "news"
    const val ROUTE_CHAT = "chat"
    const val ROUTE_ADMIN = "admin"
}

data class EclNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val adminOnly: Boolean = false,
)

private val allNavItems = listOf(
    EclNavItem(EclRoutes.ROUTE_DASHBOARD, "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    EclNavItem(EclRoutes.ROUTE_LEAGUES, "الدوريات", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    EclNavItem(EclRoutes.ROUTE_CHAMPIONS_CUP, "كأس الأبطال", Icons.Filled.MilitaryTech, Icons.Outlined.MilitaryTech),
    EclNavItem(EclRoutes.ROUTE_MATCHES, "المباريات", Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer),
    EclNavItem(EclRoutes.ROUTE_NEWS, "الأخبار", Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
    EclNavItem(EclRoutes.ROUTE_CHAT, "الدردشة", Icons.Filled.Chat, Icons.Outlined.Chat),
    EclNavItem(EclRoutes.ROUTE_ADMIN, "الإدارة", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, adminOnly = true),
)

private val bottomBarRoutes = setOf(
    EclRoutes.ROUTE_DASHBOARD,
    EclRoutes.ROUTE_LEAGUES,
    EclRoutes.ROUTE_CHAMPIONS_CUP,
    EclRoutes.ROUTE_MATCHES,
    EclRoutes.ROUTE_CHAT,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EclAppRoot(viewModel: EclViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: EclRoutes.ROUTE_DASHBOARD
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isAdmin = uiState.currentUser?.role == UserRoles.ADMIN
    val visibleNavItems = remember(isAdmin) {
        allNavItems.filter { !it.adminOnly || isAdmin }
    }
    val disputedCount = remember(uiState.matches) {
        uiState.matches.count { it.status == MatchStatuses.DISPUTED }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Redirect off Admin route if user switches back to Player
    LaunchedEffect(isAdmin, currentRoute) {
        if (!isAdmin && currentRoute == EclRoutes.ROUTE_ADMIN) {
            navController.navigate(EclRoutes.ROUTE_DASHBOARD) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useSidebar = maxWidth >= 720.dp

            if (useSidebar) {
                Row(modifier = Modifier.fillMaxSize()) {
                    SidebarDrawerContent(
                        items = visibleNavItems,
                        currentRoute = currentRoute,
                        currentUser = uiState.currentUser,
                        disputedCount = disputedCount,
                        onSelectRoute = { navigateTo(it) },
                        onOpenAuth = { viewModel.openAuthDialog() },
                        modifier = Modifier
                            .width(252.dp)
                            .fillMaxHeight()
                            .background(SlateCard)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .windowInsetsPadding(WindowInsets.navigationBars),
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(SlateBorder),
                    )
                    Scaffold(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        containerColor = ObsidianBlack,
                        snackbarHost = {
                            EclSnackbarHost(snackbarHostState, uiState.isErrorSnackbar)
                        },
                        contentWindowInsets = WindowInsets.statusBars,
                    ) { innerPadding ->
                        EclNavHost(
                            navController = navController,
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigate = ::navigateTo,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = SlateCard,
                            drawerContentColor = TextPrimary,
                            modifier = Modifier.width(280.dp),
                        ) {
                            SidebarDrawerContent(
                                items = visibleNavItems,
                                currentRoute = currentRoute,
                                currentUser = uiState.currentUser,
                                disputedCount = disputedCount,
                                onSelectRoute = { route ->
                                    scope.launch { drawerState.close() }
                                    navigateTo(route)
                                },
                                onOpenAuth = {
                                    scope.launch { drawerState.close() }
                                    viewModel.openAuthDialog()
                                },
                            )
                        }
                    },
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = ObsidianBlack,
                        snackbarHost = {
                            EclSnackbarHost(snackbarHostState, uiState.isErrorSnackbar)
                        },
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "ECL DZ",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = RoyalGold,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = visibleNavItems.firstOrNull { it.route == currentRoute }?.label ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("open_drawer_button"),
                                    ) {
                                        Icon(Icons.Filled.Menu, contentDescription = "القائمة", tint = RoyalGold)
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { navigateTo(EclRoutes.ROUTE_NEWS) },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("topbar_news_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Newspaper,
                                            contentDescription = "الأخبار",
                                            tint = if (currentRoute == EclRoutes.ROUTE_NEWS) RoyalGold else TextSecondary,
                                        )
                                    }
                                    if (isAdmin) {
                                        IconButton(
                                            onClick = { navigateTo(EclRoutes.ROUTE_ADMIN) },
                                            modifier = Modifier.minimumInteractiveComponentSize().testTag("topbar_admin_button"),
                                        ) {
                                            BadgedBox(
                                                badge = {
                                                    if (disputedCount > 0) {
                                                        Badge(containerColor = CrimsonDispute) {
                                                            Text("$disputedCount")
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AdminPanelSettings,
                                                    contentDescription = "الإدارة",
                                                    tint = if (currentRoute == EclRoutes.ROUTE_ADMIN) RoyalGold else AmberGold,
                                                )
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.openAuthDialog() },
                                        modifier = Modifier.minimumInteractiveComponentSize().testTag("topbar_account_button"),
                                    ) {
                                        Icon(Icons.Filled.Person, contentDescription = "الحساب", tint = EmeraldPitch)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = SlateCard,
                                    titleContentColor = TextPrimary,
                                ),
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = SlateCard,
                                contentColor = RoyalGold,
                                windowInsets = WindowInsets.navigationBars,
                            ) {
                                visibleNavItems
                                    .filter { it.route in bottomBarRoutes }
                                    .forEach { item ->
                                        val selected = currentRoute == item.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTo(item.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label,
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = RoyalGold,
                                                selectedTextColor = RoyalGold,
                                                indicatorColor = GoldContainer,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary,
                                            ),
                                            modifier = Modifier.testTag("bottom_nav_${item.route}"),
                                        )
                                    }
                            }
                        },
                    ) { innerPadding ->
                        EclNavHost(
                            navController = navController,
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigate = ::navigateTo,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }

        // Full-Screen Match Lobby overlay when user enters a match room
        val lobbyMatchId = uiState.activeLobbyMatchId
        if (lobbyMatchId != null) {
            val lobbyMatch by viewModel.observeMatch(lobbyMatchId).collectAsStateWithLifecycle(initialValue = null)
            val lobbyChat by viewModel.observeMatchChat(lobbyMatchId).collectAsStateWithLifecycle(initialValue = emptyList())
            val resolvedMatch = lobbyMatch ?: uiState.matches.firstOrNull { it.id == lobbyMatchId }
            if (resolvedMatch != null) {
                Surface(
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars),
                    color = ObsidianBlack,
                ) {
                    MatchLobbyScreen(
                        match = resolvedMatch,
                        currentUser = uiState.currentUser,
                        chatMessages = lobbyChat,
                        onBack = { viewModel.closeMatchLobby() },
                        onSendChat = { msg -> viewModel.sendLobbyChat(resolvedMatch.id, msg) },
                        onSubmitResult = { sA, sB, proof ->
                            viewModel.submitMatchResult(resolvedMatch.id, sA, sB, proof)
                        },
                        onClaimForfeit = { proof ->
                            viewModel.claimForfeitWin(resolvedMatch.id, proof)
                        },
                        onShowToast = { viewModel.showSnackbar(it) },
                    )
                }
            }
        }

        if (uiState.showAuthDialog) {
            AuthModalDialog(
                canDismiss = uiState.currentUser != null,
                onDismiss = { viewModel.closeAuthDialog() },
                onLogin = { id, pass -> viewModel.login(id, pass) },
                onRegister = { u, e, p, t, k -> viewModel.register(u, e, p, t, k) },
                onQuickSwitch = { userId -> viewModel.switchQuickAccount(userId) },
            )
        }

        uiState.levelUpEvent?.let { lvlEvent ->
            LevelUpCelebrationDialog(
                event = lvlEvent,
                onDismiss = { viewModel.dismissLevelUp() },
            )
        }
    }
}

@Composable
private fun EclSnackbarHost(
    hostState: SnackbarHostState,
    isError: Boolean,
) {
    SnackbarHost(hostState = hostState) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = if (isError) CrimsonContainer else SlateElevated,
            contentColor = if (isError) CrimsonDispute else RoyalGold,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun SidebarDrawerContent(
    items: List<EclNavItem>,
    currentRoute: String,
    currentUser: UserEntity?,
    disputedCount: Int,
    onSelectRoute: (String) -> Unit,
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_ecl_launcher_fg),
                contentDescription = "شعار ECL DZ",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, RoyalGold, CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("ECL DZ", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                Text("دوري أبطال إي فوتبول", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = SlateBorder)
        Spacer(modifier = Modifier.height(12.dp))

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationDrawerItem(
                selected = selected,
                onClick = { onSelectRoute(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(item.label, style = MaterialTheme.typography.labelLarge)
                        if (item.route == EclRoutes.ROUTE_ADMIN && disputedCount > 0) {
                            Badge(containerColor = CrimsonDispute) {
                                Text("$disputedCount")
                            }
                        }
                    }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = GoldContainer,
                    selectedIconColor = RoyalGold,
                    selectedTextColor = RoyalGold,
                    unselectedContainerColor = SlateCard,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextPrimary,
                ),
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .testTag("drawer_nav_${item.route}"),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = SlateBorder)
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            onClick = onOpenAuth,
            color = SlateElevated,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().testTag("drawer_account_switcher"),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_player_avatar),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.username ?: "تسجيل الدخول",
                        style = MaterialTheme.typography.titleSmall,
                        color = RoyalGold,
                        maxLines = 1,
                    )
                    Text(
                        text = if (currentUser?.role == UserRoles.ADMIN) "مشرف • تبديل الحساب" else "لاعب • المستوى ${currentUser?.level ?: 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EclNavHost(
    navController: androidx.navigation.NavHostController,
    uiState: EclUiState,
    viewModel: EclViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = EclRoutes.ROUTE_DASHBOARD,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(EclRoutes.ROUTE_DASHBOARD) {
            DashboardScreen(
                currentUser = uiState.currentUser,
                standings = uiState.standings,
                tournaments = uiState.tournaments,
                matches = uiState.matches,
                news = uiState.news,
                onOpenAuth = { viewModel.openAuthDialog() },
                onNavigateChampionsCup = { onNavigate(EclRoutes.ROUTE_CHAMPIONS_CUP) },
                onNavigateNews = { onNavigate(EclRoutes.ROUTE_NEWS) },
                onOpenMatchLobby = { viewModel.openMatchLobby(it) },
            )
        }
        composable(EclRoutes.ROUTE_LEAGUES) {
            LeaguesScreen(
                currentUser = uiState.currentUser,
                tournaments = uiState.tournaments,
                standings = uiState.standings,
                matches = uiState.matches,
                onJoinTournament = { viewModel.joinTournament(it) },
                onOpenLobby = { viewModel.openMatchLobby(it) },
            )
        }
        composable(EclRoutes.ROUTE_CHAMPIONS_CUP) {
            ChampionsCupScreen(
                currentUser = uiState.currentUser,
                tournaments = uiState.tournaments,
                matches = uiState.matches,
                onJoinTournament = { viewModel.joinTournament(it) },
                onOpenLobby = { viewModel.openMatchLobby(it) },
            )
        }
        composable(EclRoutes.ROUTE_MATCHES) {
            MatchesScreen(
                currentUser = uiState.currentUser,
                matches = uiState.matches,
                onOpenLobby = { viewModel.openMatchLobby(it) },
            )
        }
        composable(EclRoutes.ROUTE_NEWS) {
            NewsScreen(news = uiState.news)
        }
        composable(EclRoutes.ROUTE_CHAT) {
            CommunityChatScreen(
                currentUser = uiState.currentUser,
                messages = uiState.globalChat,
                onSendMessage = { viewModel.sendGlobalChat(it) },
            )
        }
        composable(EclRoutes.ROUTE_ADMIN) {
            AdminScreen(
                matches = uiState.matches,
                tournaments = uiState.tournaments,
                news = uiState.news,
                users = uiState.allUsers,
                onResolveDispute = { id, sA, sB, note -> viewModel.adminResolveDispute(id, sA, sB, note) },
                onCreateTournament = { t, d, f, max, p -> viewModel.createTournament(t, d, f, max, p) },
                onAutoFillTournament = { viewModel.autoFillAndStartTournament(it) },
                onPublishNews = { t, b, c -> viewModel.publishNews(t, b, c) },
                onDeleteNews = { viewModel.deleteNews(it) },
                onToggleBan = { viewModel.toggleUserBan(it) },
                onToggleRole = { viewModel.toggleUserRole(it) },
            )
        }
    }
}
