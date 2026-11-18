package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MatchEntity
import com.example.data.model.MatchStatuses
import com.example.data.model.NewsEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.EclCard
import com.example.ui.components.EclGoldButton
import com.example.ui.components.FormatUtils
import com.example.ui.components.MatchStatusBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldPitch
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SkyDraw
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    currentUser: UserEntity?,
    standings: List<UserEntity>,
    tournaments: List<TournamentEntity>,
    matches: List<MatchEntity>,
    news: List<NewsEntity>,
    onOpenAuth: () -> Unit,
    onNavigateChampionsCup: () -> Unit,
    onNavigateNews: () -> Unit,
    onOpenMatchLobby: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val featuredTournament = remember(tournaments) {
        tournaments.firstOrNull { it.id == "t_cup_global" } ?: tournaments.firstOrNull()
    }
    val matchOfTheDay = remember(matches, currentUser) {
        if (currentUser == null) {
            matches.firstOrNull { it.status != MatchStatuses.COMPLETED }
        } else {
            matches.firstOrNull {
                (it.playerAId == currentUser.id || it.playerBId == currentUser.id) &&
                    it.status != MatchStatuses.COMPLETED &&
                    it.status != MatchStatuses.FORFEIT
            } ?: matches.firstOrNull { it.playerAId == currentUser.id || it.playerBId == currentUser.id }
                ?: matches.firstOrNull()
        }
    }
    val topNews = remember(news) { news.take(3) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HeroBannerCard(
                    tournament = featuredTournament,
                    onDetailsClick = onNavigateChampionsCup,
                )
            }

            if (isWide) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            ProfileCard(
                                user = currentUser,
                                onOpenAuth = onOpenAuth,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            MatchOfTheDayCard(
                                match = matchOfTheDay,
                                onOpenLobby = onOpenMatchLobby,
                            )
                        }
                    }
                }
            } else {
                item {
                    ProfileCard(
                        user = currentUser,
                        onOpenAuth = onOpenAuth,
                    )
                }
                item {
                    MatchOfTheDayCard(
                        match = matchOfTheDay,
                        onOpenLobby = onOpenMatchLobby,
                    )
                }
            }

            item {
                StandingsTableCard(
                    standings = standings.filter { it.role != UserRoles.ADMIN || it.played > 0 },
                    currentUserId = currentUser?.id,
                )
            }

            item {
                LatestNewsCard(
                    news = topNews,
                    onViewAll = onNavigateNews,
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun HeroBannerCard(
    tournament: TournamentEntity?,
    onDetailsClick: () -> Unit,
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val targetMillis = tournament?.startTimeMillis ?: (nowMillis + 195_000_000L)
    val remaining = (targetMillis - nowMillis).coerceAtLeast(0L)
    val days = remaining / 86_400_000L
    val hours = (remaining % 86_400_000L) / 3_600_000L
    val minutes = (remaining % 3_600_000L) / 60_000L
    val seconds = (remaining % 60_000L) / 1_000L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
        color = SlateCard,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_champions_cup),
                contentDescription = "كأس الأبطال - بطولة القمة العالمية",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.42f,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ObsidianBlack.copy(alpha = 0.94f),
                                ObsidianBlack.copy(alpha = 0.72f),
                                GoldContainer.copy(alpha = 0.55f),
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Surface(
                    color = GoldContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الحدث الرئيسي • ${tournament?.prizePool ?: "80,000 دج"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberGold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "كأس الأبطال - بطولة القمة العالمية",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "المنافسة الرسمية لأفضل أبطال eFootball في الجزائر بنظام التصفيات الاحترافية.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "العد التنازلي للبطولة القادمة",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalGold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CountdownBox(value = days, label = "أيام", modifier = Modifier.weight(1f))
                    CountdownBox(value = hours, label = "ساعات", modifier = Modifier.weight(1f))
                    CountdownBox(value = minutes, label = "دقائق", modifier = Modifier.weight(1f))
                    CountdownBox(value = seconds, label = "ثواني", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                EclGoldButton(
                    text = "تفاصيل البطولة",
                    onClick = onDetailsClick,
                    modifier = Modifier.testTag("hero_tournament_details_button"),
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                )
            }
        }
    }
}

@Composable
private fun CountdownBox(value: Long, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = ObsidianBlack.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.titleLarge,
                color = RoyalGold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun ProfileCard(
    user: UserEntity?,
    onOpenAuth: () -> Unit,
) {
    EclCard(
        borderColor = RoyalGold.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "حسابي",
                style = MaterialTheme.typography.titleMedium,
                color = RoyalGold,
            )
            OutlinedButton(
                onClick = onOpenAuth,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("switch_account_button"),
                border = BorderStroke(1.dp, SlateBorder),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = RoyalGold,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (user == null) "تسجيل الدخول" else "تبديل / دخول",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (user == null) {
            Text(
                text = "سجل الدخول للانضمام إلى البطولات وتتبع نقاط الخبرة والترتيب.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        } else {
            val animatedProgress by animateFloatAsState(
                targetValue = user.xpProgressFraction,
                animationSpec = tween(600),
                label = "xp_progress",
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = painterResource(id = R.drawable.img_player_avatar),
                        contentDescription = "صورة اللاعب ${user.username}",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, RoyalGold, CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Surface(
                        color = RoyalGold,
                        shape = CircleShape,
                    ) {
                        Text(
                            text = "L${user.level}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ObsidianBlack,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (user.role == UserRoles.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Filled.Verified,
                            contentDescription = user.role,
                            tint = if (user.role == UserRoles.ADMIN) RoyalGold else EmeraldPitch,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = user.teamName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Konami ID: ${user.konamiId} • ${if (user.role == UserRoles.ADMIN) "مشرف" else "لاعب"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "المستوى ${user.level}",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalGold,
                )
                Text(
                    text = "${user.xp} / ${user.nextLevelXpTarget} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = RoyalGold,
                trackColor = SlateElevated,
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatChip(label = "المباريات", value = "${user.played}", color = TextPrimary, modifier = Modifier.weight(1f))
                StatChip(label = "الفوز", value = "${user.wins}", color = EmeraldPitch, modifier = Modifier.weight(1f))
                StatChip(label = "التعادل", value = "${user.draws}", color = SkyDraw, modifier = Modifier.weight(1f))
                StatChip(label = "النقاط", value = "${user.points}", color = RoyalGold, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SlateElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SlateBorder),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun MatchOfTheDayCard(
    match: MatchEntity?,
    onOpenLobby: (String) -> Unit,
) {
    EclCard(
        borderColor = EmeraldPitch.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.SportsSoccer,
                    contentDescription = null,
                    tint = EmeraldPitch,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مباراة اليوم",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
            }
            if (match != null) {
                MatchStatusBadge(status = match.status)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (match == null) {
            Text(
                text = "لا توجد مباراة مجدولة لك اليوم. انضم إلى إحدى البطولات لبدء المنافسة!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        } else {
            Text(
                text = "${match.tournamentTitle} • ${match.roundName}",
                style = MaterialTheme.typography.bodySmall,
                color = AmberGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ClubCrestBadge(teamName = match.playerATeam, size = 50.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.playerATeam,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = match.playerAName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Text(
                        text = if (match.scoreA != null && match.scoreB != null) {
                            "${match.scoreA} - ${match.scoreB}"
                        } else {
                            "VS"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = RoyalGold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = SlateElevated,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = FormatUtils.formatTime(match.scheduledTimeMillis),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ClubCrestBadge(teamName = match.playerBTeam, size = 50.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.playerBTeam,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = match.playerBName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            EclGoldButton(
                text = "مشاهدة المباراة / دخول الغرفة",
                onClick = { onOpenLobby(match.id) },
                modifier = Modifier.fillMaxWidth().testTag("match_of_day_lobby_button"),
                icon = Icons.Filled.PlayArrow,
            )
        }
    }
}

@Composable
fun StandingsTableCard(
    standings: List<UserEntity>,
    currentUserId: String?,
    modifier: Modifier = Modifier,
) {
    EclCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "جدول الترتيب",
                style = MaterialTheme.typography.titleMedium,
                color = RoyalGold,
            )
            Text(
                text = "الفرز: النقاط ▸ فارق الأهداف ▸ المسجلة",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SlateElevated)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("#", style = MaterialTheme.typography.labelMedium, color = RoyalGold, modifier = Modifier.width(24.dp))
            Text("الفريق", style = MaterialTheme.typography.labelMedium, color = RoyalGold, modifier = Modifier.weight(1f))
            Text("لعب", style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(30.dp))
            Text("ف", style = MaterialTheme.typography.labelSmall, color = EmeraldPitch, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
            Text("ت", style = MaterialTheme.typography.labelSmall, color = SkyDraw, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
            Text("خ", style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
            Text("نقاط", style = MaterialTheme.typography.labelMedium, color = RoyalGold, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        standings.forEachIndexed { idx, player ->
            val rank = idx + 1
            val isMe = player.id == currentUserId
            val rowBg = if (isMe) GoldContainer.copy(alpha = 0.6f) else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(rowBg)
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelLarge,
                    color = when (rank) {
                        1 -> RoyalGold
                        2 -> TextPrimary
                        3 -> AmberGold
                        else -> TextSecondary
                    },
                    modifier = Modifier.width(24.dp),
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ClubCrestBadge(teamName = player.teamName, size = 26.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = player.teamName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isMe) RoyalGold else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${player.username} • فارق ${if (player.goalDifference >= 0) "+${player.goalDifference}" else "${player.goalDifference}"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1,
                        )
                    }
                }
                Text("${player.played}", style = MaterialTheme.typography.bodySmall, color = TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.width(30.dp))
                Text("${player.wins}", style = MaterialTheme.typography.bodySmall, color = EmeraldPitch, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                Text("${player.draws}", style = MaterialTheme.typography.bodySmall, color = SkyDraw, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                Text("${player.losses}", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.width(26.dp))
                Text(
                    text = "${player.points}",
                    style = MaterialTheme.typography.titleSmall,
                    color = RoyalGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp),
                )
            }
            if (idx < standings.lastIndex) {
                HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun LatestNewsCard(
    news: List<NewsEntity>,
    onViewAll: () -> Unit,
) {
    EclCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Newspaper,
                    contentDescription = null,
                    tint = RoyalGold,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "آخر الأخبار",
                    style = MaterialTheme.typography.titleMedium,
                    color = RoyalGold,
                )
            }
            TextButton(
                onClick = onViewAll,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("dashboard_view_all_news"),
            ) {
                Text("عرض الكل", color = AmberGold, style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (news.isEmpty()) {
            Text(
                text = "لا توجد أخبار منشورة حالياً.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        } else {
            news.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onViewAll() }
                        .padding(vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            color = GoldContainer,
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = FormatUtils.formatDate(item.publishedAtMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (index < news.lastIndex) {
                    HorizontalDivider(color = SlateBorder.copy(alpha = 0.6f))
                }
            }
        }
    }
}
