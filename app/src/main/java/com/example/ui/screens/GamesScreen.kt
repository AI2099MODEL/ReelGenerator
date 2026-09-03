package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LedgerTopHeader
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class GameType(val label: String, val iconEmoji: String) {
    TIC_TAC_TOE("Tic Tac Toe", "❌⭕"),
    MEMORY_MATCH("Memory Match", "🎴"),
    NUMBER_GUESS("Number Guess", "🎯"),
    REFLEX_TAP("Reflex Test", "⚡")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onMenuClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    globalSettings: com.example.ui.GlobalSettingsState? = null,
    onOpenGlobalSettings: (() -> Unit)? = null
) {
    var selectedGame by remember { mutableStateOf(GameType.TIC_TAC_TOE) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            LedgerTopHeader(
                title = "Games Hub",
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
            // Game Selector Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GameType.values()) { game ->
                    val isSelected = selectedGame == game
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGame = game },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(game.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(game.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseQuartzPrimaryContainer,
                            selectedLabelColor = RoseQuartzOnPrimaryContainer,
                            containerColor = RoseQuartzContainerLowest
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Board Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                when (selectedGame) {
                    GameType.TIC_TAC_TOE -> TicTacToeGame()
                    GameType.MEMORY_MATCH -> MemoryMatchGame()
                    GameType.NUMBER_GUESS -> NumberGuessGame()
                    GameType.REFLEX_TAP -> ReflexTapGame()
                }
            }
        }
    }
}

// ---------------- 1. TIC TAC TOE GAME ----------------
@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var isVsAi by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) } // "X", "O", "DRAW", null
    var scoreX by remember { mutableStateOf(0) }
    var scoreO by remember { mutableStateOf(0) }

    fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            if (b[line[0]].isNotEmpty() && b[line[0]] == b[line[1]] && b[line[1]] == b[line[2]]) {
                return b[line[0]]
            }
        }
        if (b.all { it.isNotEmpty() }) return "DRAW"
        return null
    }

    fun makeMove(index: Int) {
        if (board[index].isNotEmpty() || winner != null) return
        val newBoard = board.toMutableList()
        newBoard[index] = currentPlayer
        board = newBoard

        val w = checkWinner(newBoard)
        if (w != null) {
            winner = w
            if (w == "X") scoreX++
            else if (w == "O") scoreO++
        } else {
            currentPlayer = if (currentPlayer == "X") "O" else "X"

            // AI turn if vs AI
            if (isVsAi && currentPlayer == "O") {
                val emptyIndices = newBoard.indices.filter { newBoard[it].isEmpty() }
                if (emptyIndices.isNotEmpty()) {
                    val aiIndex = emptyIndices.random()
                    newBoard[aiIndex] = "O"
                    board = newBoard
                    val aiWin = checkWinner(newBoard)
                    if (aiWin != null) {
                        winner = aiWin
                        if (aiWin == "O") scoreO++
                    } else {
                        currentPlayer = "X"
                    }
                }
            }
        }
    }

    fun resetGame() {
        board = List(9) { "" }
        currentPlayer = "X"
        winner = null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RoseQuartzContainerLowest.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mode: ${if (isVsAi) "Vs AI (🤖)" else "2 Player (👥)"}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoseQuartzTextSecondary)
                TextButton(onClick = { isVsAi = !isVsAi; resetGame() }) {
                    Text("Switch Mode", fontSize = 12.sp, color = RoseQuartzPrimary)
                }
            }

            // Scoreboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Surface(shape = RoundedCornerShape(10.dp), color = RoseQuartzPrimaryContainer) {
                    Text("Player X: $scoreX", modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, color = RoseQuartzOnPrimaryContainer)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = RoseQuartzSecondaryContainer) {
                    Text("${if (isVsAi) "AI (O)" else "Player O"}: $scoreO", modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, color = RoseQuartzSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3x3 Grid
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RoseQuartzContainer)
                    .padding(6.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(9) { idx ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoseQuartzContainerLowest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clickable { makeMove(idx) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = board[idx],
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (board[idx] == "X") RoseQuartzPrimary else RoseQuartzSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (winner != null) {
                Text(
                    text = when (winner) {
                        "DRAW" -> "🤝 It's a Draw!"
                        "X" -> "🎉 Player X Wins!"
                        else -> if (isVsAi) "🤖 AI Wins!" else "🎉 Player O Wins!"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = "Turn: ${if (currentPlayer == "X") "Player X" else if (isVsAi) "AI thinking..." else "Player O"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoseQuartzTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { resetGame() },
                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restart Game")
            }
        }
    }
}

// ---------------- 2. MEMORY MATCH GAME ----------------
@Composable
fun MemoryMatchGame() {
    val iconsList = listOf("🌸", "🎨", "🎵", "💎", "🚀", "🍀")
    var cards by remember { mutableStateOf((iconsList + iconsList).shuffled()) }
    var flippedIndices by remember { mutableStateOf(listOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(setOf<Int>()) }
    var moves by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            isProcessing = true
            delay(800)
            val first = flippedIndices[0]
            val second = flippedIndices[1]
            if (cards[first] == cards[second]) {
                matchedIndices = matchedIndices + setOf(first, second)
            }
            flippedIndices = emptyList()
            isProcessing = false
        }
    }

    fun onCardClick(index: Int) {
        if (isProcessing || matchedIndices.contains(index) || flippedIndices.contains(index)) return
        if (flippedIndices.size < 2) {
            flippedIndices = flippedIndices + index
            if (flippedIndices.size == 2) moves++
        }
    }

    fun resetMemoryGame() {
        cards = (iconsList + iconsList).shuffled()
        flippedIndices = emptyList()
        matchedIndices = emptySet()
        moves = 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RoseQuartzContainerLowest.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Moves: $moves", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoseQuartzTextPrimary)
                Text("Matches: ${matchedIndices.size / 2} / ${iconsList.size}", fontSize = 14.sp, color = RoseQuartzPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4x3 Card Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(12) { idx ->
                    val isFlipped = flippedIndices.contains(idx) || matchedIndices.contains(idx)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFlipped) RoseQuartzPrimaryContainer else RoseQuartzContainer,
                        border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .clickable { onCardClick(idx) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isFlipped) cards[idx] else "❓",
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (matchedIndices.size == 12) {
                Text("🎉 Amazing! You completed in $moves moves!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RoseQuartzPrimary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { resetMemoryGame() },
                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Board")
            }
        }
    }
}

// ---------------- 3. NUMBER GUESSING GAME ----------------
@Composable
fun NumberGuessGame() {
    var targetNumber by remember { mutableStateOf(Random.nextInt(1, 101)) }
    var inputGuess by remember { mutableStateOf("") }
    var hintMessage by remember { mutableStateOf("Guess a number between 1 and 100!") }
    var attempts by remember { mutableStateOf(0) }
    var isGuessed by remember { mutableStateOf(false) }

    fun submitGuess() {
        val g = inputGuess.toIntOrNull() ?: return
        attempts++
        if (g == targetNumber) {
            isGuessed = true
            hintMessage = "🎉 Bingo! You guessed $targetNumber in $attempts attempts!"
        } else if (g < targetNumber) {
            hintMessage = "📉 Too Low! Try a higher number."
        } else {
            hintMessage = "📈 Too High! Try a lower number."
        }
        inputGuess = ""
    }

    fun resetGuessGame() {
        targetNumber = Random.nextInt(1, 101)
        inputGuess = ""
        hintMessage = "Guess a number between 1 and 100!"
        attempts = 0
        isGuessed = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RoseQuartzContainerLowest.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎯 Secret Number Guesser", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoseQuartzTextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RoseQuartzPrimaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hintMessage,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = RoseQuartzOnPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isGuessed) {
                OutlinedTextField(
                    value = inputGuess,
                    onValueChange = { inputGuess = it },
                    label = { Text("Enter your guess (1-100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { submitGuess() },
                    enabled = inputGuess.toIntOrNull() != null,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
                ) {
                    Text("Submit Guess")
                }
            } else {
                Button(
                    onClick = { resetGuessGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Again")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Attempts: $attempts", fontSize = 12.sp, color = RoseQuartzTextMuted)
        }
    }
}

// ---------------- 4. REFLEX TAP TEST GAME ----------------
@Composable
fun ReflexTapGame() {
    var gameState by remember { mutableStateOf("IDLE") } // IDLE, WAITING, READY, FINISHED
    var startTime by remember { mutableStateOf(0L) }
    var reactionTimeMs by remember { mutableStateOf<Long?>(null) }
    var bestTimeMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(gameState) {
        if (gameState == "WAITING") {
            val delayMs = Random.nextLong(2000, 4500)
            delay(delayMs)
            if (gameState == "WAITING") {
                gameState = "READY"
                startTime = System.currentTimeMillis()
            }
        }
    }

    fun handleTap() {
        when (gameState) {
            "IDLE", "FINISHED" -> {
                reactionTimeMs = null
                gameState = "WAITING"
            }
            "WAITING" -> {
                // Tapped too early!
                gameState = "TOO_EARLY"
            }
            "TOO_EARLY" -> {
                gameState = "WAITING"
            }
            "READY" -> {
                val end = System.currentTimeMillis()
                val diff = end - startTime
                reactionTimeMs = diff
                if (bestTimeMs == null || diff < bestTimeMs!!) {
                    bestTimeMs = diff
                }
                gameState = "FINISHED"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clickable { handleTap() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (gameState) {
                "READY" -> RoseQuartzSuccess
                "WAITING" -> RoseQuartzAccentAmber
                "TOO_EARLY" -> RoseQuartzError
                else -> RoseQuartzContainerLowest.copy(alpha = 0.95f)
            }
        ),
        border = BorderStroke(1.dp, RoseQuartzContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (gameState) {
                    "IDLE" -> {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(56.dp), tint = RoseQuartzPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Tap anywhere to start Reflex Test", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoseQuartzTextPrimary)
                        Text("Wait for the color to turn GREEN then tap fast!", fontSize = 12.sp, color = RoseQuartzTextMuted)
                    }
                    "WAITING" -> {
                        Text("WAIT FOR GREEN...", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color.White)
                    }
                    "READY" -> {
                        Text("TAP NOW! ⚡", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = Color.White)
                    }
                    "TOO_EARLY" -> {
                        Text("Too Early! ❌", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color.White)
                        Text("Tap to try again", fontSize = 14.sp, color = Color.White)
                    }
                    "FINISHED" -> {
                        Text("Reaction Time:", fontSize = 14.sp, color = RoseQuartzTextMuted)
                        Text("${reactionTimeMs ?: 0} ms", fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = RoseQuartzPrimary)
                        bestTimeMs?.let { best ->
                            Text("Best Record: $best ms 🏆", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoseQuartzSuccess)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { handleTap() },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}
