package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MatchEntity
import com.example.data.model.MatchStatuses
import com.example.data.model.NewsEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.TournamentFormats
import com.example.data.model.TournamentStatuses
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TournamentEntity::class,
        MatchEntity::class,
        ChatMessageEntity::class,
        NewsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class EclDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun matchDao(): MatchDao
    abstract fun chatDao(): ChatDao
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var INSTANCE: EclDatabase? = null

        fun getInstance(context: Context): EclDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EclDatabase::class.java,
                    "ecl_champions_dz.db",
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { SeedData.populate(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object SeedData {
    suspend fun populate(db: EclDatabase) {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        val users = listOf(
            UserEntity(
                id = "u_admin",
                username = "Admin_ECL",
                email = "admin@ecl.dz",
                password = "admin123",
                teamName = "إدارة ECL الجزائر",
                konamiId = "ECL-ADM-0001",
                role = UserRoles.ADMIN,
                level = 8,
                xp = 3750,
                played = 12,
                wins = 9,
                draws = 2,
                losses = 1,
                goalsFor = 28,
                goalsAgainst = 11,
                points = 29,
                clubTag = "ADM",
            ),
            UserEntity(
                id = "u_yacine",
                username = "Yacine_DZ",
                email = "yacine@ecl.dz",
                password = "123456",
                teamName = "مولودية الجزائر Esport",
                konamiId = "ASDQ-784-991-DZ",
                role = UserRoles.PLAYER,
                level = 3,
                xp = 1380,
                played = 7,
                wins = 5,
                draws = 1,
                losses = 1,
                goalsFor = 19,
                goalsAgainst = 8,
                points = 16,
                clubTag = "MCA",
            ),
            UserEntity(
                id = "u_riyad",
                username = "Riyad_Pro",
                email = "riyad@ecl.dz",
                password = "123456",
                teamName = "شبيبة القبائل Pro",
                konamiId = "JSK-442-118-DZ",
                role = UserRoles.PLAYER,
                level = 4,
                xp = 1720,
                played = 7,
                wins = 6,
                draws = 0,
                losses = 1,
                goalsFor = 22,
                goalsAgainst = 9,
                points = 18,
                clubTag = "JSK",
            ),
            UserEntity(
                id = "u_islam",
                username = "Islam_CRB",
                email = "islam@ecl.dz",
                password = "123456",
                teamName = "شباب بلوزداد DZ",
                konamiId = "CRB-905-331-DZ",
                role = UserRoles.PLAYER,
                level = 3,
                xp = 1120,
                played = 7,
                wins = 5,
                draws = 1,
                losses = 1,
                goalsFor = 16,
                goalsAgainst = 9,
                points = 16,
                clubTag = "CRB",
            ),
            UserEntity(
                id = "u_amir",
                username = "Amir_USMA",
                email = "amir@ecl.dz",
                password = "123456",
                teamName = "اتحاد العاصمة Legends",
                konamiId = "USM-228-610-DZ",
                role = UserRoles.PLAYER,
                level = 2,
                xp = 890,
                played = 7,
                wins = 4,
                draws = 2,
                losses = 1,
                goalsFor = 14,
                goalsAgainst = 8,
                points = 14,
                clubTag = "USMA",
            ),
            UserEntity(
                id = "u_sofiane",
                username = "Sofiane_ESS",
                email = "sofiane@ecl.dz",
                password = "123456",
                teamName = "وفاق سطيف Kings",
                konamiId = "ESS-519-804-DZ",
                role = UserRoles.PLAYER,
                level = 2,
                xp = 710,
                played = 7,
                wins = 3,
                draws = 2,
                losses = 2,
                goalsFor = 12,
                goalsAgainst = 10,
                points = 11,
                clubTag = "ESS",
            ),
            UserEntity(
                id = "u_mehdi",
                username = "Mehdi_CSC",
                email = "mehdi@ecl.dz",
                password = "123456",
                teamName = "شباب قسنطينة Titans",
                konamiId = "CSC-301-775-DZ",
                role = UserRoles.PLAYER,
                level = 2,
                xp = 540,
                played = 7,
                wins = 2,
                draws = 2,
                losses = 3,
                goalsFor = 10,
                goalsAgainst = 13,
                points = 8,
                clubTag = "CSC",
            ),
            UserEntity(
                id = "u_karim",
                username = "Karim_MCO",
                email = "karim@ecl.dz",
                password = "123456",
                teamName = "مولودية وهران Elite",
                konamiId = "MCO-663-209-DZ",
                role = UserRoles.PLAYER,
                level = 1,
                xp = 410,
                played = 7,
                wins = 1,
                draws = 2,
                losses = 4,
                goalsFor = 8,
                goalsAgainst = 15,
                points = 5,
                clubTag = "MCO",
            ),
            UserEntity(
                id = "u_walid",
                username = "Walid_PAC",
                email = "walid@ecl.dz",
                password = "123456",
                teamName = "نادي بارادو Academy",
                konamiId = "PAC-117-554-DZ",
                role = UserRoles.PLAYER,
                level = 1,
                xp = 290,
                played = 7,
                wins = 1,
                draws = 1,
                losses = 5,
                goalsFor = 7,
                goalsAgainst = 16,
                points = 4,
                clubTag = "PAC",
            ),
        )
        db.userDao().insertAll(users)

        val tournaments = listOf(
            TournamentEntity(
                id = "t_cup_global",
                title = "كأس الأبطال - بطولة القمة العالمية",
                description = "البطولة الكبرى بنظام خروج المغلوب بين أفضل 8 أندية جزائرية في إي فوتبول مع جوائز مالية ولقب القمة.",
                format = TournamentFormats.KNOCKOUT,
                maxPlayers = 8,
                status = TournamentStatuses.ONGOING,
                prizePool = "80,000 دج + كأس القمة",
                startTimeMillis = now + (2 * day) + (5 * hour) + (42 * 60_000L),
                participantIdsCsv = "u_yacine,u_riyad,u_islam,u_amir,u_sofiane,u_mehdi,u_karim,u_walid",
                createdAtMillis = now - (4 * day),
            ),
            TournamentEntity(
                id = "t_league_pro",
                title = "الدوري الجزائري الممتاز ECL - الموسم الرابع",
                description = "منافسة دوري ذهاب وإياب بنظام النقاط والترتيب التلقائي حسب النقاط وفارق الأهداف.",
                format = TournamentFormats.LEAGUE,
                maxPlayers = 8,
                status = TournamentStatuses.ONGOING,
                prizePool = "50,000 دج + التأهل القاري",
                startTimeMillis = now - (10 * day),
                participantIdsCsv = "u_yacine,u_riyad,u_islam,u_amir,u_sofiane,u_mehdi,u_karim,u_walid",
                createdAtMillis = now - (12 * day),
            ),
            TournamentEntity(
                id = "t_open_elite",
                title = "دوري النخبة السريع - 7/8 مقاعد مكتملة",
                description = "بطولة سريعة بنظام الدوري. انضم الآن لتكتمل القائمة (8/8) ويتم توليد جدول المباريات تلقائياً!",
                format = TournamentFormats.LEAGUE,
                maxPlayers = 8,
                status = TournamentStatuses.OPEN,
                prizePool = "25,000 دج + 500 XP",
                startTimeMillis = now + (1 * day) + (3 * hour),
                participantIdsCsv = "u_riyad,u_islam,u_amir,u_sofiane,u_mehdi,u_karim,u_walid",
                createdAtMillis = now - (1 * day),
            ),
            TournamentEntity(
                id = "t_cup_16",
                title = "كأس الجزائر المفتوح - 16 لاعباً",
                description = "بطولة إقصائية كبرى مفتوحة للتسجيل لجميع اللاعبين المصنفين في المنصة.",
                format = TournamentFormats.KNOCKOUT,
                maxPlayers = 16,
                status = TournamentStatuses.OPEN,
                prizePool = "100,000 دج + تذكرة النهائي",
                startTimeMillis = now + (5 * day),
                participantIdsCsv = "u_riyad,u_islam,u_amir,u_sofiane",
                createdAtMillis = now - (2 * hour),
            ),
        )
        db.tournamentDao().insertAll(tournaments)

        val matches = listOf(
            // Match of the Day for u_yacine: Lobby open now, opponent Riyad_Pro ALREADY submitted 2-1 so user can test Auto-Approve (2-1) or Dispute (other score)!
            MatchEntity(
                id = "m_today_1",
                tournamentId = "t_cup_global",
                tournamentTitle = "كأس الأبطال - بطولة القمة العالمية",
                roundName = "ربع النهائي",
                roundOrder = 1,
                bracketSlot = 1,
                playerAId = "u_yacine",
                playerATeam = "مولودية الجزائر Esport",
                playerAName = "Yacine_DZ",
                playerAKonamiId = "ASDQ-784-991-DZ",
                playerBId = "u_riyad",
                playerBTeam = "شبيبة القبائل Pro",
                playerBName = "Riyad_Pro",
                playerBKonamiId = "JSK-442-118-DZ",
                scheduledTimeMillis = now + (10 * 60_000L), // 10 mins from now -> Lobby open!
                roomId = "ECL-DZ-8842",
                status = MatchStatuses.WAITING_OPPONENT,
                submittedByBScoreA = 2,
                submittedByBScoreB = 1,
                proofUriB = "res:img_proof_sample_1",
                playerBPresentAtMillis = now - (4 * 60_000L),
            ),
            // Overdue match for u_yacine vs Islam_CRB (> 15 mins overdue, opponent absent) -> ready to test 3-0 Forfeit win claim!
            MatchEntity(
                id = "m_forfeit_test",
                tournamentId = "t_league_pro",
                tournamentTitle = "الدوري الجزائري الممتاز ECL - الموسم الرابع",
                roundName = "الجولة 8",
                roundOrder = 8,
                bracketSlot = 1,
                playerAId = "u_yacine",
                playerATeam = "مولودية الجزائر Esport",
                playerAName = "Yacine_DZ",
                playerAKonamiId = "ASDQ-784-991-DZ",
                playerBId = "u_islam",
                playerBTeam = "شباب بلوزداد DZ",
                playerBName = "Islam_CRB",
                playerBKonamiId = "CRB-905-331-DZ",
                scheduledTimeMillis = now - (25 * 60_000L), // 25 mins ago -> eligible for 3-0 forfeit!
                roomId = "ECL-DZ-3091",
                status = MatchStatuses.LOBBY_OPEN,
                playerAPresentAtMillis = now - (20 * 60_000L),
            ),
            // Pre-loaded Disputed match for Admin Panel testing with sample proof screenshots!
            MatchEntity(
                id = "m_dispute_1",
                tournamentId = "t_cup_global",
                tournamentTitle = "كأس الأبطال - بطولة القمة العالمية",
                roundName = "ربع النهائي",
                roundOrder = 1,
                bracketSlot = 2,
                playerAId = "u_islam",
                playerATeam = "شباب بلوزداد DZ",
                playerAName = "Islam_CRB",
                playerAKonamiId = "CRB-905-331-DZ",
                playerBId = "u_amir",
                playerBTeam = "اتحاد العاصمة Legends",
                playerBName = "Amir_USMA",
                playerBKonamiId = "USM-228-610-DZ",
                scheduledTimeMillis = now - (2 * hour),
                roomId = "ECL-DZ-5519",
                status = MatchStatuses.DISPUTED,
                submittedByAScoreA = 3,
                submittedByAScoreB = 1,
                proofUriA = "res:img_proof_sample_1",
                playerAPresentAtMillis = now - (2 * hour),
                submittedByBScoreA = 2,
                submittedByBScoreB = 2,
                proofUriB = "res:img_proof_sample_2",
                playerBPresentAtMillis = now - (2 * hour),
                disputeNote = "تعارض في النتيجة المرسلة: بلوزداد أرسل (3-1) واتحاد العاصمة أرسل (2-2).",
            ),
            // Completed QF matches for Champions Cup Bracket
            MatchEntity(
                id = "m_cup_qf_3",
                tournamentId = "t_cup_global",
                tournamentTitle = "كأس الأبطال - بطولة القمة العالمية",
                roundName = "ربع النهائي",
                roundOrder = 1,
                bracketSlot = 3,
                playerAId = "u_sofiane",
                playerATeam = "وفاق سطيف Kings",
                playerAName = "Sofiane_ESS",
                playerAKonamiId = "ESS-519-804-DZ",
                playerBId = "u_mehdi",
                playerBTeam = "شباب قسنطينة Titans",
                playerBName = "Mehdi_CSC",
                playerBKonamiId = "CSC-301-775-DZ",
                scheduledTimeMillis = now - (4 * hour),
                roomId = "ECL-DZ-4102",
                status = MatchStatuses.COMPLETED,
                scoreA = 3,
                scoreB = 2,
                submittedByAScoreA = 3,
                submittedByAScoreB = 2,
                submittedByBScoreA = 3,
                submittedByBScoreB = 2,
                proofUriA = "res:img_proof_sample_1",
                proofUriB = "res:img_proof_sample_1",
                winnerId = "u_sofiane",
            ),
            MatchEntity(
                id = "m_cup_qf_4",
                tournamentId = "t_cup_global",
                tournamentTitle = "كأس الأبطال - بطولة القمة العالمية",
                roundName = "ربع النهائي",
                roundOrder = 1,
                bracketSlot = 4,
                playerAId = "u_karim",
                playerATeam = "مولودية وهران Elite",
                playerAName = "Karim_MCO",
                playerAKonamiId = "MCO-663-209-DZ",
                playerBId = "u_walid",
                playerBTeam = "نادي بارادو Academy",
                playerBName = "Walid_PAC",
                playerBKonamiId = "PAC-117-554-DZ",
                scheduledTimeMillis = now - (5 * hour),
                roomId = "ECL-DZ-2277",
                status = MatchStatuses.COMPLETED,
                scoreA = 1,
                scoreB = 2,
                submittedByAScoreA = 1,
                submittedByAScoreB = 2,
                submittedByBScoreA = 1,
                submittedByBScoreB = 2,
                proofUriA = "res:img_proof_sample_2",
                proofUriB = "res:img_proof_sample_2",
                winnerId = "u_walid",
            ),
            // Semi-Final 2 already populated by QF3 & QF4 winners
            MatchEntity(
                id = "m_cup_sf_2",
                tournamentId = "t_cup_global",
                tournamentTitle = "كأس الأبطال - بطولة القمة العالمية",
                roundName = "نصف النهائي",
                roundOrder = 2,
                bracketSlot = 2,
                playerAId = "u_sofiane",
                playerATeam = "وفاق سطيف Kings",
                playerAName = "Sofiane_ESS",
                playerAKonamiId = "ESS-519-804-DZ",
                playerBId = "u_walid",
                playerBTeam = "نادي بارادو Academy",
                playerBName = "Walid_PAC",
                playerBKonamiId = "PAC-117-554-DZ",
                scheduledTimeMillis = now + (12 * hour),
                roomId = "ECL-DZ-7710",
                status = MatchStatuses.SCHEDULED,
            ),
            // Completed League match for rich history
            MatchEntity(
                id = "m_league_done_1",
                tournamentId = "t_league_pro",
                tournamentTitle = "الدوري الجزائري الممتاز ECL - الموسم الرابع",
                roundName = "الجولة 7",
                roundOrder = 7,
                bracketSlot = 2,
                playerAId = "u_amir",
                playerATeam = "اتحاد العاصمة Legends",
                playerAName = "Amir_USMA",
                playerAKonamiId = "USM-228-610-DZ",
                playerBId = "u_sofiane",
                playerBTeam = "وفاق سطيف Kings",
                playerBName = "Sofiane_ESS",
                playerBKonamiId = "ESS-519-804-DZ",
                scheduledTimeMillis = now - (1 * day),
                roomId = "ECL-DZ-1904",
                status = MatchStatuses.COMPLETED,
                scoreA = 2,
                scoreB = 0,
                winnerId = "u_amir",
            ),
        )
        db.matchDao().insertAll(matches)

        val chatMessages = listOf(
            // Global community chat
            ChatMessageEntity(
                id = "c_g_1",
                channelId = "GLOBAL",
                senderId = "u_admin",
                senderName = "Admin_ECL",
                senderTeam = "إدارة ECL الجزائر",
                senderRole = UserRoles.ADMIN,
                message = "مرحباً بكم أبطال الجزائر في الموسم الجديد! يرجى الالتزام برفع صور إثبات النتائج فور نهاية كل مباراة.",
                timestampMillis = now - (3 * hour),
            ),
            ChatMessageEntity(
                id = "c_g_2",
                channelId = "GLOBAL",
                senderId = "u_riyad",
                senderName = "Riyad_Pro",
                senderTeam = "شبيبة القبائل Pro",
                senderRole = UserRoles.PLAYER,
                message = "بالتوفيق للجميع في ربع نهائي كأس الأبطال! مستوى المنافسة هذا الموسم رهيب جداً.",
                timestampMillis = now - (2 * hour),
            ),
            ChatMessageEntity(
                id = "c_g_3",
                channelId = "GLOBAL",
                senderId = "u_amir",
                senderName = "Amir_USMA",
                senderTeam = "اتحاد العاصمة Legends",
                senderRole = UserRoles.PLAYER,
                message = "من جاهز للانضمام إلى دوري النخبة السريع؟ بقي مقعد واحد فقط (7/8)!",
                timestampMillis = now - (45 * 60_000L),
            ),
            // Private 1-on-1 Lobby chat for m_today_1
            ChatMessageEntity(
                id = "c_m1_1",
                channelId = "m_today_1",
                senderId = "u_riyad",
                senderName = "Riyad_Pro",
                senderTeam = "شبيبة القبائل Pro",
                senderRole = UserRoles.PLAYER,
                message = "أهلاً ياسين! أنشأت الغرفة في إي فوتبول، كود الغرفة: ECL-DZ-8842 — في انتظار دخولك.",
                timestampMillis = now - (12 * 60_000L),
            ),
            ChatMessageEntity(
                id = "c_m1_2",
                channelId = "m_today_1",
                senderId = "u_riyad",
                senderName = "Riyad_Pro",
                senderTeam = "شبيبة القبائل Pro",
                senderRole = UserRoles.PLAYER,
                message = "قمت بإرسال النتيجة (2-1) مع لقطة الشاشة، أرسل تأكيدك من فضلك.",
                timestampMillis = now - (4 * 60_000L),
            ),
        )
        db.chatDao().insertAll(chatMessages)

        val news = listOf(
            NewsEntity(
                id = "n_1",
                title = "انطلاق ربع نهائي كأس الأبطال - جوائز 80,000 دج",
                body = "تعلن إدارة ECL الجزائر عن افتتاح غرف مباريات ربع النهائي قبل 15 دقيقة من موعد كل لقاء. يرجى من جميع اللاعبين مشاركة كود غرفة eFootball داخل الدردشة الخاصة وتصوير شاشة النتيجة النهائية لضمان الاعتماد الفوري للنقاط ونقاط الخبرة XP.",
                category = "بطولة",
                authorName = "Admin_ECL",
                publishedAtMillis = now - (2 * hour),
                isPinned = true,
            ),
            NewsEntity(
                id = "n_2",
                title = "تحديث نظام التحقق التلقائي ومكافحة الغش",
                body = "تم تفعيل خوارزمية المطابقة المزدوجة: عند تطابق نتيجة اللاعبين تُعتمد المباراة فوراً وتُضاف 100 نقطة XP للفائز، وفي حال الاختلاف يتحول اللقاء إلى حالة (نزاع) وتتدخل الإدارة لمراجعة لقطات الشاشة.",
                category = "تحديث",
                authorName = "Admin_ECL",
                publishedAtMillis = now - (1 * day),
                isPinned = false,
            ),
            NewsEntity(
                id = "n_3",
                title = "قانون الانسحاب والغياب (أكثر من 15 دقيقة)",
                body = "في حال تأخر الخصم لأكثر من 15 دقيقة عن موعد الانطلاق الرسمي دون عذر في غرفة المباراة، يحق للاعب الحاضر تقديم إثبات التواجد والمطالبة بفوز اعتباري بنتيجة 3-0 مباشرة.",
                category = "قوانين",
                authorName = "Admin_ECL",
                publishedAtMillis = now - (3 * day),
                isPinned = false,
            ),
            NewsEntity(
                id = "n_4",
                title = "فتح باب التسجيل في دوري النخبة السريع (8 لاعبين)",
                body = "بقي مقعد واحد أخير في بطولة دوري النخبة السريع! بمجرد اكتمال العدد إلى 8 لاعبين، سيقوم النظام بتوليد جدول المباريات الكامل تلقائياً.",
                category = "بطولة",
                authorName = "Admin_ECL",
                publishedAtMillis = now - (4 * day),
                isPinned = false,
            ),
        )
        db.newsDao().insertAll(news)
    }
}
