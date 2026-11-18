package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MatchEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.TournamentFormats
import com.example.data.model.TournamentStatuses
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.EclCard
import com.example.ui.components.EclGoldButton
import com.example.ui.components.FormatUtils
import com.example.ui.components.MatchStatusBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPitch
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SkyContainer
import com.example.ui.theme.SkyDraw
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeaguesScreen(
    currentUser: UserEntity?,
    tournaments: List<TournamentEntity>,
    standings: List<UserEntity>,
    matches: List<MatchEntity>,
    onJoinTournament: (String) -> Unit,
    onOpenLobby: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val leagueTournaments = remember(tournaments) {
        tournaments.filter { it.format == TournamentFormats.LEAGUE }
    }
    val leagueMatches = remember(matches, leagueTournaments) {
        val ids = leagueTournaments.map { it.id }.toSet()
        matches.filter { it.tournamentId in ids }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "الدوريات الرسمية ECL",
                subtitle = "انضم إلى دوريات الذهاب والإياب — يتولد الجدول تلقائياً فور اكتمال المقاعد.",
                icon = Icons.Filled.EmojiEvents,
            )
        }

        items(leagueTournaments, key = { it.id }) { tournament ->
            TournamentCard(
                tournament = tournament,
                currentUserId = currentUser?.id,
                onJoin = { onJoinTournament(tournament.id) },
            )
        }

        item {
            StandingsTableCard(
                standings = standings.filter { it.role != UserRoles.ADMIN || it.played > 0 },
                currentUserId = currentUser?.id,
            )
        }

        if (leagueMatches.isNotEmpty()) {
            item {
                Text(
                    text = "مباريات الدوري المجدولة",
                    style = MaterialTheme.typography.titleMedium,
                    color = RoyalGold,
                )
            }
            items(leagueMatches, key = { it.id }) { match ->
                CompactMatchRowCard(match = match, onClick = { onOpenLobby(match.id) })
            }
        }
    }
}

@Composable
fun ChampionsCupScreen(
    currentUser: UserEntity?,
    tournaments: List<TournamentEntity>,
    matches: List<MatchEntity>,
    onJoinTournament: (String) -> Unit,
    onOpenLobby: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cupTournaments = remember(tournaments) {
        tournaments.filter { it.format == TournamentFormats.KNOCKOUT }
    }
    val featuredCup = remember(cupTournaments) {
        cupTournaments.firstOrNull { it.status == TournamentStatuses.ONGOING } ?: cupTournaments.firstOrNull()
    }
    val cupMatches = remember(matches, featuredCup) {
        if (featuredCup == null) emptyList() else matches.filter { it.tournamentId == featuredCup.id }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "كأس الأبطال - خروج المغلوب",
                subtitle = "شجرة التصفيات الإقصائية — الفائز يتأهل تلقائياً للدور التالي نحو اللقب.",
                icon = Icons.Filled.MilitaryTech,
            )
        }

        items(cupTournaments, key = { it.id }) { cup ->
            TournamentCard(
                tournament = cup,
                currentUserId = currentUser?.id,
                onJoin = { onJoinTournament(cup.id) },
            )
        }

        if (featuredCup != null) {
            item {
                KnockoutBracketCard(
                    tournamentTitle = featuredCup.title,
                    matches = cupMatches,
                    onOpenLobby = onOpenLobby,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = GoldContainer,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.padding(10.dp).size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
fun TournamentCard(
    tournament: TournamentEntity,
    currentUserId: String?,
    onJoin: () -> Unit,
) {
    val participants = tournament.participantIds
    val joined = currentUserId != null && participants.contains(currentUserId)
    val isOpen = tournament.status == TournamentStatuses.OPEN
    val progress = (participants.size.toFloat() / tournament.maxPlayers.toFloat()).coerceIn(0f, 1f)

    val (statusLabel, statusBg, statusFg) = when (tournament.status) {
        TournamentStatuses.OPEN -> Triple("التسجيل مفتوح", EmeraldContainer, EmeraldPitch)
        TournamentStatuses.ONGOING -> Triple("جارية الآن", GoldContainer, RoyalGold)
        else -> Triple("مكتملة", SkyContainer, SkyDraw)
    }

    EclCard(
        borderColor = if (isOpen) RoyalGold.copy(alpha = 0.5f) else SlateBorder,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = SlateElevated,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = if (tournament.format == TournamentFormats.LEAGUE) "دوري (نقاط)" else "كأس (خروج المغلوب)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberGold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Surface(
                color = statusBg,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, statusFg.copy(alpha = 0.45f)),
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusFg,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = tournament.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = tournament.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Groups, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "المشاركون: ${participants.size} / ${tournament.maxPlayers}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                )
            }
            Text(
                text = "الجائزة: ${tournament.prizePool}",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldPitch,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
            color = if (isOpen) EmeraldPitch else RoyalGold,
            trackColor = SlateElevated,
            strokeCap = StrokeCap.Round,
        )

        Spacer(modifier = Modifier.height(14.dp))
        when {
            joined && isOpen -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldPitch, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تم تسجيلك • بانتظار اكتمال المقاعد", style = MaterialTheme.typography.labelLarge, color = EmeraldPitch)
                }
            }
            isOpen -> {
                EclGoldButton(
                    text = "انضمام للبطولة",
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth().testTag("join_tournament_${tournament.id}"),
                    icon = Icons.Filled.GroupAdd,
                )
            }
            joined -> {
                Text(
                    text = "أنت مشارك في هذه البطولة — تابع مبارياتك من تبويب المباريات.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberGold,
                )
            }
        }
    }
}

@Composable
private fun KnockoutBracketCard(
    tournamentTitle: String,
    matches: List<MatchEntity>,
    onOpenLobby: (String) -> Unit,
) {
    val rounds = remember(matches) {
        matches.groupBy { it.roundOrder }.toSortedMap()
    }

    EclCard(borderColor = RoyalGold.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "شجرة البطولة - $tournamentTitle",
            style = MaterialTheme.typography.titleMedium,
            color = RoyalGold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "اضغط على أي مواجهة لفتح غرفة المباراة والدردشة المباشرة",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (rounds.isEmpty()) {
            Text(
                text = "سيتم عرض شجرة المواجهات بمجرد اكتمال عدد اللاعبين.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                rounds.forEach { (_, roundMatches) ->
                    val roundTitle = roundMatches.firstOrNull()?.roundName ?: "جولة"
                    Column(
                        modifier = Modifier.width(240.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            color = GoldContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = roundTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = RoyalGold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }

                        roundMatches.forEach { m ->
                            BracketNodeCard(match = m, onClick = { onOpenLobby(m.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BracketNodeCard(match: MatchEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = SlateElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SlateBorder),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    ClubCrestBadge(teamName = match.playerATeam, size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.playerATeam,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (match.winnerId == match.playerAId) RoyalGold else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = match.scoreA?.toString() ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    color = RoyalGold,
                )
            }
            HorizontalDivider(color = SlateBorder, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    ClubCrestBadge(teamName = match.playerBTeam, size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.playerBTeam,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (match.winnerId == match.playerBId) RoyalGold else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = match.scoreB?.toString() ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    color = RoyalGold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            MatchStatusBadge(status = match.status)
        }
    }
}

@Composable
fun CompactMatchRowCard(
    match: MatchEntity,
    onClick: () -> Unit,
) {
    EclCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${match.roundName} • ${FormatUtils.formatDateTime(match.scheduledTimeMillis)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            MatchStatusBadge(status = match.status)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                ClubCrestBadge(teamName = match.playerATeam, size = 32.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = match.playerATeam,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianBlack)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = if (match.scoreA != null && match.scoreB != null) "${match.scoreA} : ${match.scoreB}" else "VS",
                    style = MaterialTheme.typography.titleSmall,
                    color = RoyalGold,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = match.playerBTeam,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                ClubCrestBadge(teamName = match.playerBTeam, size = 32.dp)
            }
        }
    }
}
