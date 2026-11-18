package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MatchEntity
import com.example.data.model.MatchStatuses
import com.example.data.model.UserEntity
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.EclCard
import com.example.ui.components.EclGoldButton
import com.example.ui.components.FormatUtils
import com.example.ui.components.MatchStatusBadge
import com.example.ui.components.ProofImageViewer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonDispute
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPitch
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun MatchesScreen(
    currentUser: UserEntity?,
    matches: List<MatchEntity>,
    onOpenLobby: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filteredMatches = remember(matches, selectedFilter, currentUser) {
        when (selectedFilter) {
            "MINE" -> matches.filter {
                currentUser != null && (it.playerAId == currentUser.id || it.playerBId == currentUser.id)
            }
            "OPEN" -> matches.filter {
                it.status == MatchStatuses.LOBBY_OPEN || it.status == MatchStatuses.WAITING_OPPONENT
            }
            "DISPUTED" -> matches.filter { it.status == MatchStatuses.DISPUTED }
            "COMPLETED" -> matches.filter {
                it.status == MatchStatuses.COMPLETED || it.status == MatchStatuses.FORFEIT
            }
            else -> matches
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("المباريات وغرف اللعب", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                    Text("تُفتح الغرفة قبل 15 دقيقة من الانطلاق للدردشة وتأكيد النتائج", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val filters = listOf(
                    "ALL" to "الكل (${matches.size})",
                    "MINE" to "مبارياتي",
                    "OPEN" to "الغرف المفتوحة",
                    "DISPUTED" to "نزاعات",
                    "COMPLETED" to "المكتملة",
                )
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldContainer,
                            selectedLabelColor = RoyalGold,
                            containerColor = SlateCard,
                            labelColor = TextSecondary,
                        ),
                    )
                }
            }
        }

        if (filteredMatches.isEmpty()) {
            item {
                EclCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "لا توجد مباريات مطابقة لهذا التصنيف حالياً.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            items(filteredMatches, key = { it.id }) { match ->
                MatchItemCard(
                    match = match,
                    isUserParticipant = currentUser != null && (match.playerAId == currentUser.id || match.playerBId == currentUser.id),
                    onEnterLobby = { onOpenLobby(match.id) },
                )
            }
        }
    }
}

@Composable
private fun MatchItemCard(
    match: MatchEntity,
    isUserParticipant: Boolean,
    onEnterLobby: () -> Unit,
) {
    EclCard(
        borderColor = if (isUserParticipant) RoyalGold.copy(alpha = 0.55f) else SlateBorder,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${match.tournamentTitle} • ${match.roundName}",
                style = MaterialTheme.typography.labelSmall,
                color = AmberGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            MatchStatusBadge(status = match.status)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                ClubCrestBadge(teamName = match.playerATeam, size = 42.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(match.playerATeam, style = MaterialTheme.typography.titleSmall, color = TextPrimary, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(match.playerAName, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = if (match.scoreA != null && match.scoreB != null) "${match.scoreA} - ${match.scoreB}" else "VS",
                    style = MaterialTheme.typography.titleLarge,
                    color = RoyalGold,
                )
                Text(
                    text = FormatUtils.formatDateTime(match.scheduledTimeMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                ClubCrestBadge(teamName = match.playerBTeam, size = 42.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(match.playerBTeam, style = MaterialTheme.typography.titleSmall, color = TextPrimary, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(match.playerBName, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        EclGoldButton(
            text = if (match.status == MatchStatuses.COMPLETED || match.status == MatchStatuses.FORFEIT) {
                "عرض تفاصيل المباراة"
            } else {
                "دخول غرفة المباراة"
            },
            onClick = onEnterLobby,
            modifier = Modifier.fillMaxWidth().testTag("enter_lobby_${match.id}"),
            icon = Icons.Filled.LockOpen,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchLobbyScreen(
    match: MatchEntity,
    currentUser: UserEntity?,
    chatMessages: List<ChatMessageEntity>,
    onBack: () -> Unit,
    onSendChat: (String) -> Unit,
    onSubmitResult: (Int, Int, String?) -> Unit,
    onClaimForfeit: (String?) -> Unit,
    onShowToast: (String) -> Unit,
) {
    BackHandler { onBack() }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val clipboardManager = LocalClipboardManager.current
    var chatDraft by remember { mutableStateOf("") }
    var scoreA by remember(match.id) { mutableIntStateOf(match.submittedByAScoreA ?: match.submittedByBScoreA ?: 2) }
    var scoreB by remember(match.id) { mutableIntStateOf(match.submittedByAScoreB ?: match.submittedByBScoreB ?: 1) }
    var proofUri by remember(match.id) { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                proofUri = uri.toString()
            }
        },
    )

    val isParticipant = currentUser != null &&
        (currentUser.id == match.playerAId || currentUser.id == match.playerBId)
    val isFinished = match.status == MatchStatuses.COMPLETED || match.status == MatchStatuses.FORFEIT
    val minsSinceKickoff = (nowMillis - match.scheduledTimeMillis) / 60_000L
    val lobbyWindowOpen = nowMillis >= (match.scheduledTimeMillis - 15 * 60_000L)
    val forfeitEligible = !isFinished && isParticipant && minsSinceKickoff >= 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .imePadding(),
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "غرفة المباراة • ${match.roundName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = match.tournamentTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("lobby_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "العودة",
                        tint = RoyalGold,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateCard),
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Matchup & Room ID Card
            item {
                EclCard(borderColor = RoyalGold.copy(alpha = 0.5f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Timer, contentDescription = null, tint = EmeraldPitch, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isFinished -> "انتهت المباراة"
                                    lobbyWindowOpen -> "الغرفة مفتوحة للعب الآن"
                                    else -> "تُفتح قبل الانطلاق بـ 15 دقيقة"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (lobbyWindowOpen) EmeraldPitch else AmberGold,
                            )
                        }
                        MatchStatusBadge(status = match.status)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            ClubCrestBadge(teamName = match.playerATeam, size = 48.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(match.playerATeam, style = MaterialTheme.typography.titleSmall, color = TextPrimary, textAlign = TextAlign.Center)
                            Text(match.playerAName, style = MaterialTheme.typography.bodySmall, color = AmberGold)
                            Text(match.playerAKonamiId, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }

                        Text(
                            text = if (match.scoreA != null && match.scoreB != null) "${match.scoreA} : ${match.scoreB}" else "VS",
                            style = MaterialTheme.typography.headlineMedium,
                            color = RoyalGold,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            ClubCrestBadge(teamName = match.playerBTeam, size = 48.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(match.playerBTeam, style = MaterialTheme.typography.titleSmall, color = TextPrimary, textAlign = TextAlign.Center)
                            Text(match.playerBName, style = MaterialTheme.typography.bodySmall, color = AmberGold)
                            Text(match.playerBKonamiId, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("كود غرفة eFootball:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(match.roomId, style = MaterialTheme.typography.titleLarge, color = RoyalGold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(match.roomId))
                                        onShowToast("تم نسخ كود الغرفة: ${match.roomId}")
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                    border = BorderStroke(1.dp, SlateBorder),
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ", tint = RoyalGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نسخ", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                                }
                                if (isParticipant) {
                                    OutlinedButton(
                                        onClick = {
                                            onSendChat("كود غرفة eFootball الخاص بمباراتنا هو: ${match.roomId} — أنا جاهز!")
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize(),
                                        border = BorderStroke(1.dp, EmeraldPitch.copy(alpha = 0.6f)),
                                    ) {
                                        Text("مشاركة", color = EmeraldPitch, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    if (!match.disputeNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = if (match.status == MatchStatuses.DISPUTED) CrimsonContainer else EmeraldContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (match.status == MatchStatuses.DISPUTED) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (match.status == MatchStatuses.DISPUTED) CrimsonDispute else EmeraldPitch,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = match.disputeNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                )
                            }
                        }
                    }
                }
            }

            // 2. Private 1-on-1 Chat Section
            item {
                EclCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "الدردشة الخاصة المباشرة (1 ضد 1)",
                        style = MaterialTheme.typography.titleMedium,
                        color = RoyalGold,
                    )
                    Text(
                        text = "تواصل مع خصمك هنا لتبادل كود الغرفة والتأكد من الجاهزية",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (chatMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlateElevated)
                                .padding(14.dp),
                        ) {
                            Text(
                                text = "لا توجد رسائل بعد. أرسل كود الغرفة لخصمك لبدء اللقاء!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chatMessages.forEach { msg ->
                                val isMine = currentUser?.id == msg.senderId
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                                ) {
                                    Surface(
                                        color = if (isMine) GoldContainer else SlateElevated,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isMine) RoyalGold.copy(alpha = 0.45f) else SlateBorder),
                                        modifier = Modifier.fillMaxWidth(0.85f),
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = msg.senderName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isMine) RoyalGold else AmberGold,
                                                )
                                                Text(
                                                    text = FormatUtils.formatTime(msg.timestampMillis),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMuted,
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = msg.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isParticipant && !isFinished) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = chatDraft,
                                onValueChange = { chatDraft = it },
                                placeholder = { Text("اكتب رسالة للخصم...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("lobby_chat_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalGold,
                                    unfocusedBorderColor = SlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                            IconButton(
                                onClick = {
                                    if (chatDraft.isNotBlank()) {
                                        onSendChat(chatDraft)
                                        chatDraft = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RoyalGold)
                                    .testTag("lobby_chat_send_button"),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = ObsidianBlack)
                            }
                        }
                    }
                }
            }

            // 3. Anti-Cheat Post-Match Submission & Forfeit Claim
            if (isParticipant && !isFinished) {
                item {
                    EclCard(borderColor = RoyalGold.copy(alpha = 0.55f)) {
                        Text(
                            text = "تأكيد النتيجة والتحقق التلقائي",
                            style = MaterialTheme.typography.titleMedium,
                            color = RoyalGold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أدخل النتيجة وارفع صورة إثبات من المباراة. التطابق يعتمد النتيجة فوراً، والاختلاف يفتح نزاعاً للإدارة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )

                        // Opponent submission hint if already submitted
                        val opponentSubmittedText = when {
                            currentUser?.id == match.playerAId && match.submittedByBScoreA != null ->
                                "أرسل الخصم (${match.playerBName}) نتيجته: ${match.submittedByBScoreA} - ${match.submittedByBScoreB}. أرسل نفس النتيجة للموافقة التلقائية أو نتيجة مختلفة لفتح نزاع."
                            currentUser?.id == match.playerBId && match.submittedByAScoreA != null ->
                                "أرسل الخصم (${match.playerAName}) نتيجته: ${match.submittedByAScoreA} - ${match.submittedByAScoreB}."
                            else -> null
                        }
                        if (opponentSubmittedText != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = GoldContainer,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = opponentSubmittedText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AmberGold,
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ScoreStepperBox(
                                teamLabel = match.playerATeam,
                                score = scoreA,
                                onDecrement = { if (scoreA > 0) scoreA-- },
                                onIncrement = { if (scoreA < 20) scoreA++ },
                                modifier = Modifier.weight(1f),
                            )
                            ScoreStepperBox(
                                teamLabel = match.playerBTeam,
                                score = scoreB,
                                onDecrement = { if (scoreB > 0) scoreB-- },
                                onIncrement = { if (scoreB < 20) scoreB++ },
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "لقطة شاشة لإثبات النتيجة",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ProofImageViewer(
                            proofUri = proofUri,
                            contentDescription = "معاينة صورة إثبات المباراة",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("pick_proof_photo_button"),
                                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f)),
                            ) {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اختيار صورة", color = RoyalGold, style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { proofUri = "res:img_proof_sample_1" },
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("attach_sample_proof_button"),
                                border = BorderStroke(1.dp, SlateBorder),
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("صورة تجريبية", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        EclGoldButton(
                            text = "إرسال النتيجة النهائية",
                            onClick = { onSubmitResult(scoreA, scoreB, proofUri) },
                            modifier = Modifier.fillMaxWidth().testTag("submit_match_score_button"),
                            icon = Icons.Filled.CheckCircle,
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Forfeit claim section (> 15 mins absent)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "غياب الخصم (+15 دقيقة)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (forfeitEligible) CrimsonDispute else TextSecondary,
                                )
                                Text(
                                    text = if (forfeitEligible) {
                                        "مرت أكثر من 15 دقيقة دون حضور الخصم — يمكنك المطالبة بفوز 3-0."
                                    } else {
                                        "تُفعّل تلقائياً بعد مرور 15 دقيقة من موعد الانطلاق في حال غياب الخصم."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onClaimForfeit(proofUri) },
                            enabled = forfeitEligible,
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("claim_forfeit_button"),
                            border = BorderStroke(1.dp, if (forfeitEligible) CrimsonDispute else SlateBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CrimsonDispute,
                                disabledContentColor = TextMuted,
                            ),
                        ) {
                            Icon(Icons.Filled.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("المطالبة بفوز اعتباري (3 - 0)", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ScoreStepperBox(
    teamLabel: String,
    score: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = SlateElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SlateBorder),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = teamLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "إنقاص", tint = TextPrimary)
                }
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium,
                    color = RoyalGold,
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "زيادة", tint = RoyalGold)
                }
            }
        }
    }
}
