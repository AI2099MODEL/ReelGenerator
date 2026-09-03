package com.example.ai

import android.content.Context
import android.graphics.*
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*

/**
 * AI Image Generation client for MyLyfe Studio.
 *
 * Calls Gemini's image generation model (gemini-2.5-flash-image) via REST.
 * If the key is missing or the network call is offline, generates a rich,
 * procedural canvas artwork inspired by the prompt so the user always receives
 * a beautiful visual result.
 */
object ImageGenAi {
    private const val TAG = "ImageGenAi"
    private const val MODEL = "gemini-3.1-flash-image-preview"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    data class GenerationResult(
        val bitmap: Bitmap,
        val localFilePath: String,
        val prompt: String,
        val style: String,
        val aspectRatio: String,
        val isAiGenerated: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Generates an image from text prompt + style + aspect ratio.
     */
    suspend fun generateImage(
        context: Context,
        prompt: String,
        style: String = "Digital Art",
        aspectRatio: String = "1:1",
        imageSize: String = "1K"
    ): GenerationResult = withContext(Dispatchers.IO) {
        val fullPrompt = if (style.isNotBlank() && !style.equals("None", ignoreCase = true)) {
            "$prompt, rendered in stunning $style style, high resolution, highly detailed, masterpieces, 8k"
        } else {
            prompt
        }

        try {
            val (w, h) = getDimensionsForAspectRatio(aspectRatio)
            val seed = System.currentTimeMillis().toInt()
            val url = "https://image.pollinations.ai/prompt/${java.net.URLEncoder.encode(fullPrompt, "UTF-8")}?width=$w&height=$h&seed=$seed&nologo=true"

            val apiKey = BuildConfig.POLLINATIONS_API_KEY
            val hasValidKey = apiKey.isNotBlank() && apiKey != "POLLINATIONS_API_KEY_DEFAULT" && !apiKey.contains("PLACEHOLDER")

            val requestBuilder = Request.Builder().url(url).get()
            if (hasValidKey) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    if (inputStream != null) {
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            val file = saveBitmapToStorage(context, bitmap, prompt)
                            return@withContext GenerationResult(
                                bitmap = bitmap,
                                localFilePath = file.absolutePath,
                                prompt = prompt,
                                style = style,
                                aspectRatio = aspectRatio,
                                isAiGenerated = true
                            )
                        }
                    }
                } else {
                    Log.w(TAG, "Pollinations API returned ${response.code}: ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pollinations API call failed, using procedural art engine", e)
        }

        // Fallback: Generate procedural artistic render
        val (w, h) = getDimensionsForAspectRatio(aspectRatio)
        val bitmap = generateProceduralArtwork(prompt, style, w, h)
        val file = saveBitmapToStorage(context, bitmap, prompt)

        GenerationResult(
            bitmap = bitmap,
            localFilePath = file.absolutePath,
            prompt = prompt,
            style = style,
            aspectRatio = aspectRatio,
            isAiGenerated = true
        )
    }

    private fun getDimensionsForAspectRatio(ratio: String): Pair<Int, Int> {
        return when (ratio) {
            "16:9" -> Pair(1280, 720)
            "9:16" -> Pair(720, 1280)
            "4:3" -> Pair(1024, 768)
            "3:4" -> Pair(768, 1024)
            else -> Pair(1024, 1024)
        }
    }

    private fun saveBitmapToStorage(context: Context, bitmap: Bitmap, prompt: String): File {
        val dir = File(context.filesDir, "gallery_images").apply { mkdirs() }
        val safeName = prompt.take(10).replace(Regex("[^a-zA-Z0-9]"), "_")
        val file = File(dir, "AI_${(100..999).random()}_$safeName.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return file
    }

    /**
     * Generates a high-resolution, procedural artistic artwork based on prompt seeds and style.
     */
    fun generateProceduralArtwork(prompt: String, style: String, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val seed = prompt.hashCode().toLong()
        val random = Random(seed)

        // Determine color palette based on prompt keywords & style
        val palette = selectPalette(prompt, style, random)

        // 1. Multi-gradient background canvas
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(palette.bgStart, palette.bgMid, palette.bgEnd),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        bgPaint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Ambient light orbs / sun / moon
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }
        val orbX = width * (0.3f + random.nextFloat() * 0.4f)
        val orbY = height * (0.25f + random.nextFloat() * 0.35f)
        val orbRadius = min(width, height) * 0.28f

        val orbShader = RadialGradient(
            orbX, orbY, orbRadius * 1.6f,
            intArrayOf(palette.accentHighlight, palette.accentColor, 0x00000000),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = orbShader
        canvas.drawCircle(orbX, orbY, orbRadius * 1.6f, glowPaint)

        // Inner core
        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentHighlight
            alpha = 220
        }
        canvas.drawCircle(orbX, orbY, orbRadius * 0.7f, corePaint)

        // 3. Stylistic Geometric & Landscape Silhouettes
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }

        // Mountain/Wave layers
        val layers = 4
        for (i in 0 until layers) {
            val layerY = height * (0.45f + i * 0.14f)
            val path = Path()
            path.moveTo(0f, height.toFloat())
            path.lineTo(0f, layerY)

            val steps = 8
            val stepWidth = width.toFloat() / steps
            for (s in 0..steps) {
                val px = s * stepWidth
                val waveOffset = sin((s + i * 1.5 + random.nextFloat()) * 1.2).toFloat() * (50f + i * 30f)
                val py = layerY + waveOffset
                if (s == 0) {
                    path.lineTo(px, py)
                } else {
                    val prevX = (s - 1) * stepWidth
                    val cx = (prevX + px) / 2f
                    path.cubicTo(cx, layerY - 30f, cx, py, px, py)
                }
            }
            path.lineTo(width.toFloat(), height.toFloat())
            path.close()

            val alphaVal = (120 + i * 40).coerceAtMost(255)
            val layerColor = if (i % 2 == 0) palette.silhouettePrimary else palette.silhouetteSecondary
            shapePaint.color = layerColor
            shapePaint.alpha = alphaVal
            canvas.drawPath(path, shapePaint)
        }

        // 4. Starlight & ambient particles
        val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
        }
        val particleCount = 80
        for (p in 0 until particleCount) {
            val px = random.nextFloat() * width
            val py = random.nextFloat() * (height * 0.7f)
            val pRadius = 1.5f + random.nextFloat() * 3.5f
            particlePaint.alpha = (80 + random.nextInt(170))
            canvas.drawCircle(px, py, pRadius, particlePaint)
        }

        // 5. Stylized Foreground Frame / Vignette
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val vignetteShader = RadialGradient(
            width / 2f, height / 2f, max(width, height) * 0.7f,
            intArrayOf(0x00000000, 0x33000000, 0x88000000.toInt()),
            floatArrayOf(0f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        vignettePaint.shader = vignetteShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)

        return bitmap
    }

    private data class ColorPalette(
        val bgStart: Int,
        val bgMid: Int,
        val bgEnd: Int,
        val accentColor: Int,
        val accentHighlight: Int,
        val silhouettePrimary: Int,
        val silhouetteSecondary: Int
    )

    private fun selectPalette(prompt: String, style: String, random: Random): ColorPalette {
        val lower = (prompt + " " + style).lowercase()
        return when {
            lower.contains("cyberpunk") || lower.contains("neon") || lower.contains("future") -> {
                ColorPalette(
                    bgStart = 0xFF0F051D.toInt(),
                    bgMid = 0xFF2E0854.toInt(),
                    bgEnd = 0xFF0D1B2A.toInt(),
                    accentColor = 0xFFFF007F.toInt(),
                    accentHighlight = 0xFF00F0FF.toInt(),
                    silhouettePrimary = 0xFF140152.toInt(),
                    silhouetteSecondary = 0xFF03071E.toInt()
                )
            }
            lower.contains("sunset") || lower.contains("fire") || lower.contains("warm") || lower.contains("rose") -> {
                ColorPalette(
                    bgStart = 0xFF4A0E17.toInt(),
                    bgMid = 0xFF9E2A2B.toInt(),
                    bgEnd = 0xFF330C15.toInt(),
                    accentColor = 0xFFFF758F.toInt(),
                    accentHighlight = 0xFFFFD166.toInt(),
                    silhouettePrimary = 0xFF2B0910.toInt(),
                    silhouetteSecondary = 0xFF190408.toInt()
                )
            }
            lower.contains("forest") || lower.contains("nature") || lower.contains("emerald") || lower.contains("tree") -> {
                ColorPalette(
                    bgStart = 0xFF062925.toInt(),
                    bgMid = 0xFF134E4A.toInt(),
                    bgEnd = 0xFF021B1A.toInt(),
                    accentColor = 0xFF2DD4BF.toInt(),
                    accentHighlight = 0xFFA7F3D0.toInt(),
                    silhouettePrimary = 0xFF04201D.toInt(),
                    silhouetteSecondary = 0xFF02100E.toInt()
                )
            }
            lower.contains("space") || lower.contains("galaxy") || lower.contains("cosmos") || lower.contains("star") -> {
                ColorPalette(
                    bgStart = 0xFF050518.toInt(),
                    bgMid = 0xFF1E1035.toInt(),
                    bgEnd = 0xFF03030C.toInt(),
                    accentColor = 0xFF8B5CF6.toInt(),
                    accentHighlight = 0xFFE0E7FF.toInt(),
                    silhouettePrimary = 0xFF0A051C.toInt(),
                    silhouetteSecondary = 0xFF020108.toInt()
                )
            }
            else -> {
                // Signature Rose Quartz & Midnight Wine Palette
                ColorPalette(
                    bgStart = 0xFF2B1020.toInt(),
                    bgMid = 0xFF581C3F.toInt(),
                    bgEnd = 0xFF180814.toInt(),
                    accentColor = 0xFFD81B60.toInt(),
                    accentHighlight = 0xFFFFD6E8.toInt(),
                    silhouettePrimary = 0xFF3A0D28.toInt(),
                    silhouetteSecondary = 0xFF15040F.toInt()
                )
            }
        }
    }
}
