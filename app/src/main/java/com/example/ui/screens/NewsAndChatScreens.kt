package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.NewsEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import com.example.ui.components.EclCard
import com.example.ui.components.FormatUtils
import com.example.ui.theme.AmberGold
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

@Composable
fun NewsScreen(
    news: List<NewsEntity>,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf("الكل") }
    val categories = listOf("الكل", "بطولة", "قوانين", "تحديث")
    val filtered = remember(news, selectedCategory) {
        if (selectedCategory == "الكل") news else news.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Newspaper, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("أخبار وإعلانات ECL DZ", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                    Text("آخر المستجدات والقوانين الرسمية من إدارة المنصة", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
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

        items(filtered, key = { it.id }) { item ->
            EclCard(
                borderColor = if (item.isPinned) RoyalGold.copy(alpha = 0.6f) else SlateBorder,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(color = GoldContainer, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                        if (item.isPinned) {
                            Icon(Icons.Filled.PushPin, contentDescription = "مثبت", tint = RoyalGold, modifier = Modifier.size(16.dp))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(FormatUtils.formatDate(item.publishedAtMillis), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = item.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = item.body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "بواسطة: ${item.authorName}", style = MaterialTheme.typography.labelSmall, color = AmberGold)
            }
        }
    }
}

@Composable
fun CommunityChatScreen(
    currentUser: UserEntity?,
    messages: List<ChatMessageEntity>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Surface(
            color = SlateCard,
            border = BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Chat, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("الدردشة العامة للاعبين", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("تجمع أبطال eFootball DZ للتنسيق والتحديات", style = MaterialTheme.typography.bodySmall, color = EmeraldPitch)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMine = currentUser?.id == msg.senderId
                val isAdmin = msg.senderRole == UserRoles.ADMIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                ) {
                    Surface(
                        color = when {
                            isMine -> GoldContainer
                            isAdmin -> SlateElevated
                            else -> SlateCard
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isAdmin -> RoyalGold.copy(alpha = 0.7f)
                                isMine -> RoyalGold.copy(alpha = 0.4f)
                                else -> SlateBorder
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(0.84f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isAdmin) {
                                        Icon(
                                            imageVector = Icons.Filled.AdminPanelSettings,
                                            contentDescription = "مشرف",
                                            tint = RoyalGold,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "${msg.senderName} • ${msg.senderTeam}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isAdmin || isMine) RoyalGold else AmberGold,
                                    )
                                }
                                Text(
                                    text = FormatUtils.formatTime(msg.timestampMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
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

        Surface(
            color = SlateCard,
            border = BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("اكتب رسالتك في الدردشة...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("global_chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalGold,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            onSendMessage(draft)
                            draft = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalGold)
                        .testTag("global_chat_send_button"),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = ObsidianBlack)
                }
            }
        }
    }
}
