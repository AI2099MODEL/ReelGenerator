package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.LedgerTopHeader
import com.example.ui.components.roseQuartz3dCardEffect
import com.example.ui.components.waterRippleTouch
import com.example.ui.theme.*
import java.util.*

data class AppLanguage(
    val name: String,
    val code: String,
    val flag: String
)

val SUPPORTED_LANGUAGES = listOf(
    AppLanguage("English", "en", "🇺🇸"),
    AppLanguage("Español", "es", "🇪🇸"),
    AppLanguage("Français", "fr", "🇫🇷"),
    AppLanguage("Deutsch", "de", "🇩🇪"),
    AppLanguage("हिंदी", "hi", "🇮🇳"),
    AppLanguage("日本語", "ja", "🇯🇵"),
    AppLanguage("中文", "zh", "🇨🇳"),
    AppLanguage("Português", "pt", "🇧🇷"),
    AppLanguage("Italiano", "it", "🇮🇹")
)

val POPULAR_LOCATIONS = listOf(
    "New York, USA",
    "London, UK",
    "Tokyo, Japan",
    "Paris, France",
    "Delhi, India",
    "Berlin, Germany",
    "Sydney, Australia",
    "Toronto, Canada",
    "Global News"
)

data class NewsArticle(
    val id: String = UUID.randomUUID().toString(),
    val locationTag: String,
    val headlineEn: String,
    val summaryEn: String,
    val fullContentEn: String,
    val source: String,
    val timeAgo: String,
    val isBookmarked: Boolean = false,
    val readTimeMinutes: Int = 3,
    // Translations map code -> Pair(Headline, Summary)
    val translations: Map<String, Pair<String, String>> = emptyMap()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onMenuClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    globalSettings: com.example.ui.GlobalSettingsState? = null,
    onOpenGlobalSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var currentLocation by remember { mutableStateOf("New York, USA") }
    var selectedLanguage by remember { mutableStateOf(SUPPORTED_LANGUAGES[0]) }

    // Sync location & language from Global Settings if enabled
    LaunchedEffect(globalSettings?.selectedLocation, globalSettings?.targetLanguageCode, globalSettings?.isLocationEnabled, globalSettings?.isTranslationEnabled) {
        if (globalSettings != null) {
            if (globalSettings.isLocationEnabled && globalSettings.selectedLocation.isNotBlank()) {
                currentLocation = globalSettings.selectedLocation
            }
            if (globalSettings.isTranslationEnabled && globalSettings.targetLanguageCode.isNotBlank()) {
                SUPPORTED_LANGUAGES.find { it.code == globalSettings.targetLanguageCode }?.let { lang ->
                    selectedLanguage = lang
                }
            }
        }
    }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyBookmarked by remember { mutableStateOf(false) }
    var activeArticleDetail by remember { mutableStateOf<NewsArticle?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var customLocationInput by remember { mutableStateOf("") }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            detectGpsLocation(context) { loc ->
                currentLocation = loc
            }
        }
    }

    var articles by remember {
        mutableStateOf(
            listOf(
                NewsArticle(
                    locationTag = "New York, USA",
                    headlineEn = "New York Tech Week Spotlights AI & Urban Mobility Innovations",
                    summaryEn = "Over 500 startups gather in Manhattan to present sustainable grid computing and automated electric transit solutions.",
                    fullContentEn = "New York Tech Week kicked off in downtown Manhattan today with keynotes focused on smart city infrastructure, renewable urban grid power, and machine learning models for municipal transit scheduling. Engineers and municipal leaders highlighted public-private initiatives expected to deploy next summer.",
                    source = "NYC Metro Tech",
                    timeAgo = "10m ago",
                    readTimeMinutes = 4,
                    translations = mapOf(
                        "es" to Pair("La Semana de la Tecnología de Nueva York destaca las innovaciones en IA y movilidad urbana", "Más de 500 empresas emergentes se reúnen en Manhattan para presentar soluciones de redes sostenibles y transporte eléctrico automatizado."),
                        "fr" to Pair("La semaine tech de New York met en lumière l'IA et la mobilité urbaine", "Plus de 500 startups se réunissent à Manhattan pour présenter des solutions de réseaux durables et de transit électrique automatisé."),
                        "de" to Pair("New Yorker Tech-Woche bringt KI und urbane Mobilität in den Fokus", "Über 500 Startups versammeln sich in Manhattan, um nachhaltige Technologien und automatisierte Transportlösungen vorzustellen."),
                        "hi" to Pair("न्यू यॉर्क टेक वीक ने एआई और शहरी गतिशीलता नवाचारों पर प्रकाश डाला", "मैनहट्टन में 500 से अधिक स्टार्टअप्स टिकाऊ ग्रिड कंप्यूटिंग और स्वचालित इलेक्ट्रिक ट्रांजिट समाधान प्रस्तुत करने के लिए एकत्र हुए।"),
                        "ja" to Pair("ニューヨーク・テックウィークでAIと都市モビリティの革新が注目を集める", "マンハッタンに500社以上のスタートアップが集まり、持続可能なグリッドコンピューティングと自動電気輸送ソリューションを披露。"),
                        "zh" to Pair("纽约科技周聚焦人工智能与城市出行创新", "超过500家初创企业汇聚曼哈顿，展示可持续电网计算与自动化电力公交解决方案。")
                    )
                ),
                NewsArticle(
                    locationTag = "London, UK",
                    headlineEn = "London Clean Energy Initiative Reaches Milestone Green Power Record",
                    summaryEn = "Wind and solar contribution to the regional grid surges past 65% during peak generation hours.",
                    fullContentEn = "Energy authorities in the United Kingdom reported a historic clean power generation record. Enhanced offshore wind farms off the East Coast combined with smart battery storage facilities supplied two-thirds of regional energy demand without coal or gas backup.",
                    source = "London Climate Journal",
                    timeAgo = "45m ago",
                    readTimeMinutes = 3,
                    translations = mapOf(
                        "es" to Pair("La iniciativa de energía limpia de Londres alcanza un récord histórico", "La contribución eólica y solar a la red regional supera el 65% durante las horas pico de generación."),
                        "fr" to Pair("L'initiative d'énergie propre de Londres atteint un record vert historique", "La contribution éolienne et solaire au réseau régional dépasse 65% pendant les heures de pointe."),
                        "de" to Pair("Londoner Öko-Energie-Initiative erreicht historischen Grünstrom-Rekord", "Wind- und Solaranteil am regionalen Stromnetz steigt in Spitzenzeiten auf über 65%."),
                        "hi" to Pair("लंदन स्वच्छ ऊर्जा पहल ने ऐतिहासिक रिकॉर्ड बनाया", "पीक जनरेशन घंटों के दौरान क्षेत्रीय ग्रिड में पवन और सौर योगदान 65% को पार कर गया।"),
                        "ja" to Pair("ロンドンのクリーンエネルギー・イニシアチブが歴史的な緑の電力記録を達成", "ピーク時の地域の送電網における風力と太陽光の貢献度が65％を超過。"),
                        "zh" to Pair("伦敦清洁能源倡议创下历史性绿电纪录", "在发电高峰时段，风能和太阳能对区域电网的贡献率突破65%。")
                    )
                ),
                NewsArticle(
                    locationTag = "Tokyo, Japan",
                    headlineEn = "Tokyo Robotics Expo Unveils Next-Gen Healthcare Assistants",
                    summaryEn = "Compact robotic systems designed for elderly care assistance and automated pharmacy dispensation.",
                    fullContentEn = "The annual Tokyo Robotics Summit showcased breakthrough healthcare robotics featuring soft tactile sensors, micro-actuators, and localized natural language processing. The devices aim to assist medical staff with patient monitoring and precise medication delivery.",
                    source = "Nikkei Tech Wire",
                    timeAgo = "2h ago",
                    readTimeMinutes = 5,
                    translations = mapOf(
                        "es" to Pair("Expo de Robótica de Tokio presenta asistentes de salud de próxima generación", "Sistemas robóticos compactos diseñados para asistencia a ancianos y dispensación automatizada de medicamentos."),
                        "fr" to Pair("L'exposition de robotique de Tokyo dévoile des assistants de santé nouvelle génération", "Systèmes robotiques compacts conçus pour l'assistance aux personnes âgées et la dispensation de médicaments."),
                        "de" to Pair("Tokioter Robotik-Messe enthüllt Pflegeroboter der nächsten Generation", "Kompakte Robotersysteme für die Altenpflege und automatische Medikamentenausgabe vorgestellt."),
                        "hi" to Pair("टोक्यो रोबोटिक्स एक्सपो ने अगली पीढ़ी के स्वास्थ्य देखभाल सहायकों का अनावरण किया", "बुजुर्गों की देखभाल सहायता और स्वचालित फार्मेसी वितरण के लिए डिज़ाइन किए गए कॉम्पैक्ट रोबोटिक सिस्टम।"),
                        "ja" to Pair("東京ロボティクスエキスポで次世代ヘルスケアアシスタントが初公開", "高齢者介護支援と自動調剤用に設計された小型ロボットシステム。"),
                        "zh" to Pair("东京机器人展发布下一代医疗护理助手", "专为养老护理协助和自动化药房配药设计的紧凑型机器人系统。")
                    )
                ),
                NewsArticle(
                    locationTag = "Delhi, India",
                    headlineEn = "National Digital Infrastructure Network Expands Solar Smart Microgrids",
                    summaryEn = "Rural electrification push connects 1,200 agricultural villages to high-capacity solar battery storage.",
                    fullContentEn = "India's renewable energy development board announced the completion of Phase 4 solar microgrid expansion. The project brings reliable 24/7 power to agricultural pump stations and local schools across northern states.",
                    source = "Hindustan Business Standard",
                    timeAgo = "3h ago",
                    readTimeMinutes = 4,
                    translations = mapOf(
                        "es" to Pair("La red nacional de infraestructura digital expande microredes solares inteligentes", "El impulso de electrificación rural conecta 1,200 pueblos agrícolas a almacenamiento solar de alta capacidad."),
                        "fr" to Pair("Le réseau national d'infrastructure numérique étend les micro-réseaux solaires", "L'électrification rurale relie 1 200 villages agricoles à un stockage solaire haute capacité."),
                        "de" to Pair("Nationale digitale Infrastruktur erweitert intelligente Solar-Mikronetze", "Ländliche Elektrifizierung schließt 1.200 Dörfer an Hochkapazitäts-Solarspeicher an."),
                        "hi" to Pair("राष्ट्रीय डिजिटल इंफ्रास्ट्रक्चर नेटवर्क ने सोलर स्मार्ट माइक्रोग्रिड का विस्तार किया", "ग्रामीण विद्युतीकरण अभियान ने 1,200 कृषि गांवों को उच्च क्षमता वाले सौर बैटरी भंडारण से जोड़ा।"),
                        "ja" to Pair("国家デジタルインフラ網が太陽光スマートマイクログリッドを拡大", "農村電化推進により1,200の農業村落が大容量太陽光蓄電池に接続される。"),
                        "zh" to Pair("国家数字基础设施网络扩大太阳能智能微电网", "农村电化工程将1200个农业村庄接入高容量太阳能电池储能系统。")
                    )
                ),
                NewsArticle(
                    locationTag = "Paris, France",
                    headlineEn = "Paris Green Architecture Summit Finalizes Eco-District Urban Plan",
                    summaryEn = "New zoning guidelines require green roofs, pedestrian parkways, and timber building frameworks.",
                    fullContentEn = "City planners in Paris adopted sweeping environmental guidelines for all upcoming commercial developments. Buildings exceeding five stories must incorporate solar integration or rooftop urban agriculture plots starting next year.",
                    source = "Le Journal de Paris",
                    timeAgo = "4h ago",
                    readTimeMinutes = 3,
                    translations = mapOf(
                        "es" to Pair("Cumbre de Arquitectura Verde de París finaliza plan urbano de ecodistritos", "Nuevas pautas exigen techos verdes, paseos peatonales y estructuras de madera sostenible."),
                        "fr" to Pair("Le sommet de l'architecture verte de Paris finalise le plan urbain des éco-quartiers", "De nouvelles directives exigent des toits végétalisés, des promenades piétonnes et des structures en bois."),
                        "de" to Pair("Pariser Gipfel für grüne Architektur beschließt Öko-Stadtteilplan", "Neue Bauvorschriften verlangen Gründächer, Fußgängerzonen und Holzbauweisen."),
                        "hi" to Pair("पेरिस ग्रीन आर्किटेक्चर समिट ने इको-डिस्ट्रिक्ट शहरी योजना को अंतिम रूप दिया", "नए नियमों के तहत हरी छतें, पैदल पथ और लकड़ी के भवन निर्माण आवश्यक बनाए गए।"),
                        "ja" to Pair("パリ・グリーン建築サミットがエコ地区都市計画を最終決定", "新しい都市計画ガイドラインにより、屋上緑化と歩行者専用道路の設置が義務付けられる。"),
                        "zh" to Pair("巴黎绿色建筑峰会敲定生态街区城市规划", "新分区指南要求建设绿色屋顶、步行林荫道和木结构建筑框架。")
                    )
                ),
                NewsArticle(
                    locationTag = "Sydney, Australia",
                    headlineEn = "Australian Marine Conservation Project Deploys Autonomous Reef Monitors",
                    summaryEn = "Underwater acoustic sensors and solar-powered buoys provide real-time water temperature telemetry.",
                    fullContentEn = "Marine biologists off the Queensland coast launched an array of solar autonomous surface vessels to track coral health parameters. Real-time telemetry allows researchers to predict heat stress events days before thermal bleaching occurs.",
                    source = "Pacific Marine Post",
                    timeAgo = "6h ago",
                    readTimeMinutes = 4,
                    translations = mapOf(
                        "es" to Pair("Proyecto de conservación marina australiana despliega monitores de arrecifes autónomos", "Sensores acústicos submarinos y boyas solares entregan telemetría de temperatura en tiempo real."),
                        "fr" to Pair("Un projet australien de conservation marine déploie des moniteurs de récifs autonomes", "Des capteurs acoustiques sous-marins et des bouées solaires fournissent une télémétrie thermique en temps réel."),
                        "de" to Pair("Australisches Meeres-Schutzprojekt setzt autonome Riff-Monitore ein", "Unterwasser-Akustiksensoren und Solarbojen liefern Wassertemperatur-Telemetrie in Echtzeit."),
                        "hi" to Pair("ऑस्ट्रेलियाई समुद्री संरक्षण परियोजना ने स्वायत्त रीफ मॉनिटर्स तैनात किए", "पानी के भीतर ध्वनिक सेंसर और सौर ऊर्जा से चलने वाले बोय वास्तविक समय में तापमान प्रदान करते हैं।"),
                        "ja" to Pair("オーストラリア海洋保全プロジェクトが自律型サンゴ礁モニターを導入", "水中音響センサーと太陽光発電ブイがリアルタイムで海水温データを送信。"),
                        "zh" to Pair("澳大利亚海洋保护项目部署自主礁石监测仪", "水下声学传感器和太阳能浮标提供实时水温遥测数据。")
                    )
                ),
                NewsArticle(
                    locationTag = "Global News",
                    headlineEn = "Global Space Observatory Unveils Deepest View of Early Galaxy Formation",
                    summaryEn = "Astronomers measure cosmic background radiation shifts confirming early stellar nucleosynthesis rates.",
                    fullContentEn = "An international consortium of astrophysicists published high-resolution spectra capturing early galaxies formed just 300 million years after the Big Bang. The data provides unprecedented clarity regarding heavy element distribution in primordial star clusters.",
                    source = "World Science Wire",
                    timeAgo = "8h ago",
                    readTimeMinutes = 5,
                    translations = mapOf(
                        "es" to Pair("Observatorio espacial global revela la vista más profunda del origen de galaxias", "Astrónomos miden desplazamientos de radiación cósmica confirmando tasas de nucleosíntesis estelar."),
                        "fr" to Pair("L'observatoire spatial mondial dévoile la vue la plus profonde de la formation des galaxies", "Les astronomes mesurent les rayonnements cosmiques confirmant la synthèse stellaire précoce."),
                        "de" to Pair("Weltraumobservatorium enthüllt tiefsten Einblick in frühe Galaxienbildung", "Astronomen messen kosmische Strahlung und bestätigen Modelle zur frühen Elemententstehung."),
                        "hi" to Pair("ग्लोबल स्पेस ऑब्जर्वेटरी ने शुरुआती आकाशगंगा निर्माण का सबसे गहरा दृश्य अनावरण किया", "खगोलविदों ने प्रारंभिक तारकीय न्यूक्लियोसिंथेसिस दरों की पुष्टि करने वाले ब्रह्मांडीय विकिरण बदलाव का अध्ययन किया।"),
                        "ja" to Pair("世界宇宙天文台が初期銀河形成の最も深い観測写真を公開", "天文学者が宇宙背景放射を測定し、初期の元素合成率を確認。"),
                        "zh" to Pair("全球空间天文台公布早期星系形成的最深邃视角", "天文学家通过测量宇宙背景辐射偏移，证实了早期恒星核合成速率。")
                    )
                )
            )
        )
    }

    // Filter articles by location search or location tag match (or show location stories prioritized)
    val filteredArticles = articles.filter { article ->
        val matchesLocation = currentLocation == "Global News" ||
                article.locationTag.contains(currentLocation.split(",")[0], ignoreCase = true) ||
                article.headlineEn.contains(currentLocation.split(",")[0], ignoreCase = true) ||
                article.locationTag == "Global News"
        val matchesSearch = searchQuery.isBlank() ||
                article.headlineEn.contains(searchQuery, ignoreCase = true) ||
                article.summaryEn.contains(searchQuery, ignoreCase = true)
        val matchesBookmark = !showOnlyBookmarked || article.isBookmarked
        matchesLocation && matchesSearch && matchesBookmark
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            LedgerTopHeader(
                title = "News Reader",
                actionIcon = if (showOnlyBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                onActionClick = { showOnlyBookmarked = !showOnlyBookmarked },
                onMenuClick = onMenuClick,
                globalSettings = globalSettings,
                onOpenGlobalSettings = onOpenGlobalSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Location & Language Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location Selector Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoseQuartzPrimaryContainer,
                    border = BorderStroke(1.dp, RoseQuartzPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showLocationPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentLocation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzOnPrimaryContainer,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Language Selector Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoseQuartzContainerLowest,
                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                    modifier = Modifier.clickable { showLanguagePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedLanguage.flag, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedLanguage.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoseQuartzTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate",
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("news_search_input"),
                placeholder = { Text("Search local or global news...", color = RoseQuartzTextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = RoseQuartzTextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = RoseQuartzTextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoseQuartzPrimary,
                    unfocusedBorderColor = RoseQuartzContainerHighest,
                    focusedContainerColor = RoseQuartzContainerLowest,
                    unfocusedContainerColor = RoseQuartzContainerLowest
                )
            )

            // News Feed List (NO CATEGORIES)
            if (filteredArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Newspaper,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = RoseQuartzTextMuted.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showOnlyBookmarked) "No bookmarked news stories" else "No news articles found for $currentLocation",
                            fontSize = 14.sp,
                            color = RoseQuartzTextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { currentLocation = "Global News" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseQuartzPrimary)
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Switch to Global News Feed", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                ) {
                    items(filteredArticles, key = { it.id }) { article ->
                        val (headline, summary) = getArticleHeadlineAndSummary(article, selectedLanguage.code)
                        ArticleCard(
                            article = article,
                            displayHeadline = headline,
                            displaySummary = summary,
                            selectedLanguage = selectedLanguage,
                            onArticleClick = { activeArticleDetail = article },
                            onBookmarkToggle = {
                                articles = articles.map { if (it.id == article.id) it.copy(isBookmarked = !it.isBookmarked) else it }
                            },
                            onGoogleTranslateClick = {
                                openInGoogleTranslate(context, headline, summary, selectedLanguage.code)
                            }
                        )
                    }
                }
            }
        }
    }

    // Location Picker Dialog
    if (showLocationPicker) {
        AlertDialog(
            onDismissRequest = { showLocationPicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoseQuartzPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select News Location", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Auto-detect GPS button
                    Button(
                        onClick = {
                            val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
                                detectGpsLocation(context) { loc -> currentLocation = loc }
                                showLocationPicker = false
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                                showLocationPicker = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto-Detect Location (GPS)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Or Choose Region:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseQuartzTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(POPULAR_LOCATIONS) { loc ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentLocation == loc) RoseQuartzPrimaryContainer else RoseQuartzContainerLowest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentLocation = loc
                                        showLocationPicker = false
                                    }
                            ) {
                                Text(
                                    text = loc,
                                    fontSize = 13.sp,
                                    fontWeight = if (currentLocation == loc) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentLocation == loc) RoseQuartzOnPrimaryContainer else RoseQuartzTextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customLocationInput,
                        onValueChange = { customLocationInput = it },
                        placeholder = { Text("Enter city or country...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (customLocationInput.isNotBlank()) {
                                IconButton(onClick = {
                                    currentLocation = customLocationInput.trim()
                                    customLocationInput = ""
                                    showLocationPicker = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Apply", tint = RoseQuartzPrimary)
                                }
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationPicker = false }) {
                    Text("Done", color = RoseQuartzPrimary)
                }
            },
            containerColor = RoseQuartzContainerHigh,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Language Picker Dialog
    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = RoseQuartzPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Translation Language", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Translate news feed into:", fontSize = 12.sp, color = RoseQuartzTextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(SUPPORTED_LANGUAGES) { lang ->
                            val isSelected = selectedLanguage.code == lang.code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) RoseQuartzPrimaryContainer else RoseQuartzContainerLowest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = lang
                                        showLanguagePicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(lang.flag, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = lang.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) RoseQuartzOnPrimaryContainer else RoseQuartzTextPrimary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoseQuartzPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguagePicker = false }) {
                    Text("Close", color = RoseQuartzPrimary)
                }
            },
            containerColor = RoseQuartzContainerHigh,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Article Detail Modal with Google Translation
    activeArticleDetail?.let { article ->
        val (headline, summary) = getArticleHeadlineAndSummary(article, selectedLanguage.code)
        AlertDialog(
            onDismissRequest = { activeArticleDetail = null },
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoseQuartzPrimaryContainer
                        ) {
                            Text(
                                text = article.locationTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseQuartzOnPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        IconButton(onClick = {
                            articles = articles.map { if (it.id == article.id) it.copy(isBookmarked = !it.isBookmarked) else it }
                            activeArticleDetail = article.copy(isBookmarked = !article.isBookmarked)
                        }) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = RoseQuartzPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = headline,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = RoseQuartzTextPrimary
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(article.source, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = RoseQuartzTextSecondary)
                        Text(" • ", fontSize = 12.sp, color = RoseQuartzTextMuted)
                        Text(article.timeAgo, fontSize = 12.sp, color = RoseQuartzTextMuted)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = article.fullContentEn,
                        fontSize = 14.sp,
                        color = RoseQuartzTextPrimary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Translate Launch Button inside article
                    OutlinedButton(
                        onClick = {
                            openInGoogleTranslate(context, headline, article.fullContentEn, selectedLanguage.code)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseQuartzPrimary)
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Full Text in Google Translate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, headline)
                            putExtra(Intent.EXTRA_TEXT, "$headline\n\n$summary\n\nRead more via Rose Quartz News Reader.")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeArticleDetail = null }) {
                    Text("Close", color = RoseQuartzTextMuted)
                }
            },
            containerColor = RoseQuartzContainerHigh,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ArticleCard(
    article: NewsArticle,
    displayHeadline: String,
    displaySummary: String,
    selectedLanguage: AppLanguage,
    onArticleClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onGoogleTranslateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .roseQuartz3dCardEffect(elevation = 5.dp, shape = RoundedCornerShape(16.dp))
            .waterRippleTouch { onArticleClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoseQuartzContainerLowest.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, RoseQuartzContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RoseQuartzPrimaryContainer.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoseQuartzOnPrimaryContainer, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = article.locationTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseQuartzOnPrimaryContainer
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.timeAgo,
                        fontSize = 11.sp,
                        color = RoseQuartzTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (article.isBookmarked) RoseQuartzPrimary else RoseQuartzTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = displayHeadline,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = RoseQuartzTextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = displaySummary,
                fontSize = 13.sp,
                color = RoseQuartzTextSecondary,
                lineHeight = 18.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${article.source} • ${selectedLanguage.flag} ${selectedLanguage.name}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = RoseQuartzTextMuted
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onGoogleTranslateClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate via Google",
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Read →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseQuartzPrimary
                    )
                }
            }
        }
    }
}

// Helper to get translated headline and summary
private fun getArticleHeadlineAndSummary(article: NewsArticle, langCode: String): Pair<String, String> {
    if (langCode == "en") return Pair(article.headlineEn, article.summaryEn)
    return article.translations[langCode] ?: Pair(
        "[${langCode.uppercase()}] ${article.headlineEn}",
        "[${langCode.uppercase()}] ${article.summaryEn}"
    )
}

// Helper to open Google Translate in Browser
private fun openInGoogleTranslate(context: Context, headline: String, text: String, targetLang: String) {
    try {
        val queryText = "$headline\n\n$text"
        val translateUri = Uri.parse("https://translate.google.com/?sl=auto&tl=$targetLang&text=${Uri.encode(queryText)}")
        val intent = Intent(Intent.ACTION_VIEW, translateUri)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
    }
}

// Helper to detect GPS location
private fun detectGpsLocation(context: Context, onResult: (String) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null) {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality ?: addresses[0].adminArea ?: "Local Area"
                    val country = addresses[0].countryName ?: ""
                    val result = if (country.isNotBlank()) "$city, $country" else city
                    onResult(result)
                    return
                }
            }
        }
    } catch (e: Exception) {
        // Ignore exception
    }
    onResult("New York, USA")
}
