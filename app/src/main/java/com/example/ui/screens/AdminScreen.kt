package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MatchEntity
import com.example.data.model.MatchStatuses
import com.example.data.model.NewsEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.TournamentFormats
import com.example.data.model.TournamentStatuses
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import com.example.ui.components.EclCard
import com.example.ui.components.EclGoldButton
import com.example.ui.components.FormatUtils
import com.example.ui.components.ProofImageViewer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonDispute
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPitch
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminScreen(
    matches: List<MatchEntity>,
    tournaments: List<TournamentEntity>,
    news: List<NewsEntity>,
    users: List<UserEntity>,
    onResolveDispute: (String, Int, Int, String) -> Unit,
    onCreateTournament: (String, String, String, Int, String) -> Unit,
    onAutoFillTournament: (String) -> Unit,
    onPublishNews: (String, String, String) -> Unit,
    onDeleteNews: (String) -> Unit,
    onToggleBan: (String) -> Unit,
    onToggleRole: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val disputedMatches = remember(matches) { matches.filter { it.status == MatchStatuses.DISPUTED } }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(color = SlateCard, border = BorderStroke(1.dp, SlateBorder)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("لوحة تحكم الإدارة", style = MaterialTheme.typography.headlineSmall, color = RoyalGold)
                        Text("مراجعة النزاعات، إدارة البطولات، نشر الأخبار ومراقبة اللاعبين", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateElevated,
            contentColor = RoyalGold,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RoyalGold,
                )
            },
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("النزاعات (${disputedMatches.size})", style = MaterialTheme.typography.labelLarge) },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("البطولات", style = MaterialTheme.typography.labelLarge) },
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("الأخبار", style = MaterialTheme.typography.labelLarge) },
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("المستخدمون (${users.size})", style = MaterialTheme.typography.labelLarge) },
            )
        }

        when (selectedTab) {
            0 -> AdminDisputesTab(disputedMatches = disputedMatches, onResolveDispute = onResolveDispute)
            1 -> AdminTournamentsTab(
                tournaments = tournaments,
                onCreateTournament = onCreateTournament,
                onAutoFillTournament = onAutoFillTournament,
            )
            2 -> AdminNewsTab(news = news, onPublishNews = onPublishNews, onDeleteNews = onDeleteNews)
            3 -> AdminUsersTab(users = users, onToggleBan = onToggleBan, onToggleRole = onToggleRole)
        }
    }
}

@Composable
private fun AdminDisputesTab(
    disputedMatches: List<MatchEntity>,
    onResolveDispute: (String, Int, Int, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (disputedMatches.isEmpty()) {
            item {
                EclCard(borderColor = EmeraldPitch.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldPitch)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("لا توجد نزاعات مفتوحة حالياً! جميع النتائج سليمة.", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        } else {
            items(disputedMatches, key = { it.id }) { match ->
                DisputeResolutionCard(match = match, onResolve = onResolveDispute)
            }
        }
    }
}

@Composable
private fun DisputeResolutionCard(
    match: MatchEntity,
    onResolve: (String, Int, Int, String) -> Unit,
) {
    var officialA by remember(match.id) { mutableIntStateOf(match.submittedByAScoreA ?: 0) }
    var officialB by remember(match.id) { mutableIntStateOf(match.submittedByAScoreB ?: 0) }
    var note by remember(match.id) { mutableStateOf("") }

    EclCard(borderColor = CrimsonDispute.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Gavel, contentDescription = null, tint = CrimsonDispute, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("${match.playerATeam} ضد ${match.playerBTeam}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("${match.tournamentTitle} • ${match.roundName}", style = MaterialTheme.typography.bodySmall, color = AmberGold)
        if (!match.disputeNote.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(match.disputeNote, style = MaterialTheme.typography.bodySmall, color = CrimsonDispute)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("إثبات ${match.playerAName}: (${match.submittedByAScoreA ?: "-"}-${match.submittedByAScoreB ?: "-"})", style = MaterialTheme.typography.labelSmall, color = RoyalGold)
                Spacer(modifier = Modifier.height(6.dp))
                ProofImageViewer(
                    proofUri = match.proofUriA,
                    contentDescription = "لقطة شاشة ${match.playerAName}",
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("إثبات ${match.playerBName}: (${match.submittedByBScoreA ?: "-"}-${match.submittedByBScoreB ?: "-"})", style = MaterialTheme.typography.labelSmall, color = RoyalGold)
                Spacer(modifier = Modifier.height(6.dp))
                ProofImageViewer(
                    proofUri = match.proofUriB,
                    contentDescription = "لقطة شاشة ${match.playerBName}",
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    officialA = match.submittedByAScoreA ?: 0
                    officialB = match.submittedByAScoreB ?: 0
                },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                border = BorderStroke(1.dp, EmeraldPitch.copy(alpha = 0.6f)),
            ) {
                Text("اختيار نتيجة أ", color = EmeraldPitch, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = {
                    officialA = match.submittedByBScoreA ?: 0
                    officialB = match.submittedByBScoreB ?: 0
                },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
            ) {
                Text("اختيار نتيجة ب", color = AmberGold, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (officialA > 0) officialA-- }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Remove, contentDescription = "إنقاص أ", tint = TextPrimary)
                }
                Text("$officialA", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                IconButton(onClick = { officialA++ }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "زيادة أ", tint = RoyalGold)
                }
            }
            Text(":", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (officialB > 0) officialB-- }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Remove, contentDescription = "إنقاص ب", tint = TextPrimary)
                }
                Text("$officialB", style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                IconButton(onClick = { officialB++ }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "زيادة ب", tint = RoyalGold)
                }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("قرار الإدارة (اختياري)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalGold,
                unfocusedBorderColor = SlateBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
        )
        Spacer(modifier = Modifier.height(12.dp))
        EclGoldButton(
            text = "حسم النزاع واعتماد النتيجة ($officialA - $officialB)",
            onClick = { onResolve(match.id, officialA, officialB, note) },
            modifier = Modifier.fillMaxWidth().testTag("resolve_dispute_${match.id}"),
            icon = Icons.Filled.CheckCircle,
        )
    }
}

@Composable
private fun AdminTournamentsTab(
    tournaments: List<TournamentEntity>,
    onCreateTournament: (String, String, String, Int, String) -> Unit,
    onAutoFillTournament: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prizePool by remember { mutableStateOf("50,000 دج") }
    var format by remember { mutableStateOf(TournamentFormats.LEAGUE) }
    var maxPlayers by remember { mutableIntStateOf(8) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            EclCard(borderColor = RoyalGold.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Text("إنشاء بطولة جديدة", style = MaterialTheme.typography.titleMedium, color = RoyalGold)
                Spacer(modifier = Modifier.height(10.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalGold,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم البطولة") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("admin_tournament_title_input"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف البطولة") },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prizePool,
                    onValueChange = { prizePool = it },
                    label = { Text("الجائزة") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("نظام البطولة:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = format == TournamentFormats.LEAGUE,
                        onClick = { format = TournamentFormats.LEAGUE },
                        label = { Text("دوري (Round-Robin)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldContainer, selectedLabelColor = RoyalGold),
                    )
                    FilterChip(
                        selected = format == TournamentFormats.KNOCKOUT,
                        onClick = { format = TournamentFormats.KNOCKOUT },
                        label = { Text("كأس (خروج المغلوب)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldContainer, selectedLabelColor = RoyalGold),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("عدد اللاعبين الأقصى:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(8, 16, 32).forEach { count ->
                        FilterChip(
                            selected = maxPlayers == count,
                            onClick = { maxPlayers = count },
                            label = { Text("$count لاعباً") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldContainer, selectedLabelColor = RoyalGold),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                EclGoldButton(
                    text = "إنشاء البطولة",
                    onClick = {
                        onCreateTournament(title, description, format, maxPlayers, prizePool)
                        title = ""
                        description = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_create_tournament_button"),
                    icon = Icons.Filled.Add,
                )
            }
        }

        item {
            Text("البطولات الحالية", style = MaterialTheme.typography.titleMedium, color = RoyalGold)
        }

        items(tournaments, key = { it.id }) { t ->
            EclCard(modifier = Modifier.fillMaxWidth()) {
                Text(t.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (t.format == TournamentFormats.LEAGUE) "دوري" else "كأس"} • ${t.participantIds.size}/${t.maxPlayers} • ${t.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                if (t.status == TournamentStatuses.OPEN) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { onAutoFillTournament(t.id) },
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize(),
                        border = BorderStroke(1.dp, EmeraldPitch.copy(alpha = 0.6f)),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = EmeraldPitch, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إكمال المقاعد وتوليد الجدول تلقائياً", color = EmeraldPitch, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminNewsTab(
    news: List<NewsEntity>,
    onPublishNews: (String, String, String) -> Unit,
    onDeleteNews: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("بطولة") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            EclCard(borderColor = RoyalGold.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Text("نشر إعلان أو خبر جديد", style = MaterialTheme.typography.titleMedium, color = RoyalGold)
                Spacer(modifier = Modifier.height(10.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalGold,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الخبر") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("admin_news_title_input"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("نص الإعلان") },
                    minLines = 3,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("admin_news_body_input"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("بطولة", "قوانين", "تحديث").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldContainer, selectedLabelColor = RoyalGold),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                EclGoldButton(
                    text = "نشر الخبر الآن",
                    onClick = {
                        onPublishNews(title, body, category)
                        title = ""
                        body = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_publish_news_button"),
                    icon = Icons.Filled.Publish,
                )
            }
        }

        items(news, key = { it.id }) { item ->
            EclCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${item.category} • ${FormatUtils.formatDate(item.publishedAtMillis)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    IconButton(onClick = { onDeleteNews(item.id) }, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = CrimsonDispute)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<UserEntity>,
    onToggleBan: (String) -> Unit,
    onToggleRole: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(users, key = { it.id }) { u ->
            EclCard(
                borderColor = if (u.isBanned) CrimsonDispute.copy(alpha = 0.6f) else SlateBorder,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${u.username} (${u.teamName})",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (u.isBanned) CrimsonDispute else TextPrimary,
                        )
                        Text(
                            text = "Konami: ${u.konamiId} • المستوى ${u.level} (${u.xp} XP)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Surface(
                        color = if (u.isBanned) CrimsonContainer else if (u.role == UserRoles.ADMIN) GoldContainer else EmeraldContainer,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = if (u.isBanned) "محظور" else if (u.role == UserRoles.ADMIN) "مشرف" else "لاعب",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (u.isBanned) CrimsonDispute else if (u.role == UserRoles.ADMIN) RoyalGold else EmeraldPitch,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SlateBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onToggleRole(u.id) },
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = if (u.role == UserRoles.ADMIN) "تحويل إلى لاعب" else "ترقية إلى مشرف",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoyalGold,
                        )
                    }
                    if (u.role != UserRoles.ADMIN) {
                        OutlinedButton(
                            onClick = { onToggleBan(u.id) },
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("ban_user_${u.id}"),
                            border = BorderStroke(1.dp, CrimsonDispute.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDispute),
                        ) {
                            Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (u.isBanned) "فك الحظر" else "حظر المستخدم", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
