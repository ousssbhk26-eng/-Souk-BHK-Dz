package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MatchStatuses
import com.example.data.repository.LevelUpEvent
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonDispute
import com.example.ui.theme.DeepGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPitch
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SkyContainer
import com.example.ui.theme.SkyDraw
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun EclCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SlateBorder,
    containerColor: Color = SlateCard,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun EclGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.minimumInteractiveComponentSize().height(48.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = RoyalGold,
            contentColor = ObsidianBlack,
            disabledContainerColor = SlateBorder,
            disabledContentColor = TextMuted,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ClubCrestBadge(
    teamName: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val palette = listOf(
        Color(0xFF10B981) to Color(0xFF064E3B),
        Color(0xFFF59E0B) to Color(0xFF451A03),
        Color(0xFFEF4444) to Color(0xFF450A0A),
        Color(0xFF3B82F6) to Color(0xFF172554),
        Color(0xFFA855F7) to Color(0xFF3B0764),
        Color(0xFFEC4899) to Color(0xFF500724),
    )
    val pair = palette[abs(teamName.hashCode()) % palette.size]
    val initials = remember(teamName) {
        val parts = teamName.trim().split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first()}${parts[1].first()}"
        } else {
            teamName.take(2)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(pair.second, ObsidianBlack)
                )
            )
            .border(1.5.dp, pair.first.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            tint = pair.first.copy(alpha = 0.22f),
            modifier = Modifier.size(size * 0.72f),
        )
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            maxLines = 1,
        )
    }
}

@Composable
fun MatchStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status) {
        MatchStatuses.LOBBY_OPEN -> Triple("الغرفة مفتوحة", EmeraldContainer, EmeraldPitch)
        MatchStatuses.WAITING_OPPONENT -> Triple("بانتظار الخصم", GoldContainer, RoyalGold)
        MatchStatuses.DISPUTED -> Triple("نزاع - قيد المراجعة", CrimsonContainer, CrimsonDispute)
        MatchStatuses.COMPLETED -> Triple("مكتملة", SkyContainer, SkyDraw)
        MatchStatuses.FORFEIT -> Triple("فوز اعتباري 3-0", GoldContainer, AmberGold)
        else -> Triple("مجدولة", SlateElevated, TextSecondary)
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.45f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}

@Composable
fun ProofImageViewer(
    proofUri: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SlateElevated)
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            proofUri == "res:img_proof_sample_1" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_proof_sample_1),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            proofUri == "res:img_proof_sample_2" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_proof_sample_2),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            !proofUri.isNullOrBlank() -> {
                AsyncImage(
                    model = proofUri,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "لم تُرفع صورة بعد",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
fun LevelUpCelebrationDialog(
    event: LevelUpEvent,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        EclCard(
            borderColor = RoyalGold,
            containerColor = SlateCard,
            contentPadding = PaddingValues(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GoldContainer)
                        .border(2.dp, RoyalGold, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "ترقية المستوى",
                        tint = RoyalGold,
                        modifier = Modifier.size(38.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "تهانينا! ارتقاء مستوى جديد",
                    style = MaterialTheme.typography.headlineSmall,
                    color = RoyalGold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "وصل ${event.username} إلى المستوى ${event.newLevel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "إجمالي نقاط الخبرة: ${event.totalXp} XP\nالهدف القادم: ${event.newLevel * 500} XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                EclGoldButton(
                    text = "متابعة التحدي",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("level_up_confirm_button"),
                    icon = Icons.Filled.EmojiEvents,
                )
            }
        }
    }
}

@Composable
fun AuthModalDialog(
    canDismiss: Boolean,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, String) -> Unit,
    onQuickSwitch: (String) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var loginIdentifier by remember { mutableStateOf("yacine@ecl.dz") }
    var loginPassword by remember { mutableStateOf("123456") }

    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regTeam by remember { mutableStateOf("") }
    var regKonamiId by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.large,
            color = SlateCard,
            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.4f)),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "بوابة اللاعبين ECL DZ",
                            style = MaterialTheme.typography.titleLarge,
                            color = RoyalGold,
                        )
                        Text(
                            text = "دوري أبطال إي فوتبول الجزائر",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    if (canDismiss) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_auth_dialog_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق",
                                tint = TextSecondary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Demo Role Switcher for immediate Player/Admin testing
                Text(
                    text = "دخول سريع للتجربة الفورية:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { onQuickSwitch("u_yacine") },
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("quick_login_player"),
                        border = BorderStroke(1.dp, EmeraldPitch.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPitch),
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("لاعب (Yacine)", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { onQuickSwitch("u_admin") },
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("quick_login_admin"),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalGold),
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشرف (Admin)", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SlateBorder)
                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateElevated,
                    contentColor = RoyalGold,
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
                        text = { Text("تسجيل الدخول", style = MaterialTheme.typography.labelLarge) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("حساب جديد", style = MaterialTheme.typography.labelLarge) },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalGold,
                    unfocusedBorderColor = SlateBorder,
                    focusedLabelColor = RoyalGold,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = RoyalGold,
                )

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = { loginIdentifier = it },
                        label = { Text("البريد الإلكتروني أو اسم المستخدم") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("login_identifier_input"),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EclGoldButton(
                        text = "دخول إلى المنصة",
                        onClick = { onLogin(loginIdentifier, loginPassword) },
                        modifier = Modifier.fillMaxWidth().testTag("login_submit_button"),
                        icon = Icons.Filled.CheckCircle,
                    )
                } else {
                    OutlinedTextField(
                        value = regUsername,
                        onValueChange = { regUsername = it },
                        label = { Text("اسم المستخدم (Username)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("register_username_input"),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regTeam,
                        onValueChange = { regTeam = it },
                        label = { Text("اسم النادي / الفريق") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("register_team_input"),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regKonamiId,
                        onValueChange = { regKonamiId = it },
                        label = { Text("معرف كونامي eFootball Konami ID") },
                        placeholder = { Text("مثال: ASDQ-123-456-DZ") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("register_konami_input"),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EclGoldButton(
                        text = "إنشاء الحساب (المستوى 1)",
                        onClick = { onRegister(regUsername, regEmail, regPassword, regTeam, regKonamiId) },
                        modifier = Modifier.fillMaxWidth().testTag("register_submit_button"),
                    )
                }
            }
        }
    }
}

object FormatUtils {
    private val arabicLocale = Locale("ar", "DZ")

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", arabicLocale)
        return sdf.format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return sdf.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM • HH:mm", arabicLocale)
        return sdf.format(Date(millis))
    }
}
