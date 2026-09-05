package com.example.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

data class ExtractedVideoInfo(
    val title: String,
    val author: String,
    val thumbnailUri: String,
    val directVideoUrl: String?,
    val platform: String, // "INSTAGRAM", "FACEBOOK", "WEB"
    val originalUrl: String,
    val note: String = "",
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "video/mp4"
)

data class DownloadProgressState(
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val percent: Int = 0,
    val isCompleted: Boolean = false,
    val savedFile: File? = null,
    val downloadId: Long? = null,
    val error: String? = null,
    val statusMessage: String = ""
)

enum class PlatformType {
    INSTAGRAM,
    FACEBOOK,
    WEB
}

sealed class UrlValidationResult {
    data class ValidDirectMedia(
        val normalizedUrl: String,
        val suggestedFileName: String,
        val platform: PlatformType = PlatformType.WEB
    ) : UrlValidationResult()

    data class ValidSocialPage(
        val normalizedUrl: String,
        val platform: PlatformType,
        val isReel: Boolean
    ) : UrlValidationResult()

    data class Invalid(val reason: String) : UrlValidationResult()
}

data class PreflightResult(
    val isSuccess: Boolean,
    val httpCode: Int = 0,
    val contentType: String? = null,
    val contentLength: Long = 0L,
    val isMedia: Boolean = false,
    val suggestedTitle: String = "",
    val finalUrl: String = "",
    val error: Exception? = null
)

object VideoDownloadHelper {

    val RESOLVER_REQUIRED_MESSAGE =
        "Facebook and Instagram page links are not supported. Use a verified direct media URL."

    const val MAX_VIDEO_FILE_SIZE_BYTES = 500L * 1024L * 1024L // 500MB safety ceiling
    const val DOWNLOAD_DIR_NAME = "Download/TimepassDownloads"
    private const val MAX_REDIRECTS = 5

    // Strict allowed MIME types for downloaded media
    val ALLOWED_VIDEO_MIMES = setOf(
        "video/mp4",
        "video/webm",
        "video/x-matroska",
        "video/quicktime",
        "video/3gpp",
        "video/x-m4v"
    )

    val ALLOWED_MEDIA_MIMES = setOf(
        "video/mp4",
        "video/webm",
        "video/x-matroska",
        "video/quicktime",
        "video/3gpp",
        "video/x-m4v",
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif",
        "image/bmp",
        "audio/mpeg",
        "audio/mp3",
        "audio/wav",
        "audio/ogg",
        "audio/aac",
        "audio/flac",
        "audio/m4a",
        "audio/x-m4a"
    )

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(false) // Custom redirect handling for security validation
            .followSslRedirects(false)
            .build()
    }

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    /**
     * Identify target platform from URL
     */
    fun detectPlatform(url: String): String {
        val lower = url.trim().lowercase()
        return when {
            lower.contains("instagram.com") || lower.contains("instagr.am") || lower.contains("cdninstagram.com") -> "INSTAGRAM"
            lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> "FACEBOOK"
            else -> "WEB"
        }
    }

    /**
     * Check if a URL has an acceptable http or https protocol
     */
    fun isValidSocialUrl(url: String): Boolean {
        val trimmed = url.trim().lowercase()
        return trimmed.startsWith("http://") || trimmed.startsWith("https://")
    }

    /**
     * Check whether an IP address is in a reserved/private/loopback range (SSRF defense).
     */
    fun isPrivateOrLocalAddress(ip: InetAddress): Boolean {
        if (ip.isAnyLocalAddress || ip.isLoopbackAddress || ip.isLinkLocalAddress || ip.isSiteLocalAddress) {
            return true
        }
        val bytes = ip.address
        if (bytes.size == 4) {
            val b0 = bytes[0].toInt() and 0xFF
            val b1 = bytes[1].toInt() and 0xFF
            // 10.0.0.0/8
            if (b0 == 10) return true
            // 172.16.0.0/12
            if (b0 == 172 && b1 in 16..31) return true
            // 192.168.0.0/16
            if (b0 == 192 && b1 == 168) return true
            // 127.0.0.0/8
            if (b0 == 127) return true
            // 169.254.0.0/16 (Link Local / Cloud Metadata)
            if (b0 == 169 && b1 == 254) return true
            // 0.0.0.0/8
            if (b0 == 0) return true
        }
        return false
    }

    /**
     * Check if hostname or IP points to a local, private, or internal network destination.
     */
    fun isPrivateOrLocalHost(host: String): Boolean {
        val lowerHost = host.trim().lowercase()
        if (lowerHost == "localhost" ||
            lowerHost.endsWith(".localhost") ||
            lowerHost.endsWith(".local") ||
            lowerHost.endsWith(".internal") ||
            lowerHost == "127.0.0.1" ||
            lowerHost == "::1" ||
            lowerHost == "0.0.0.0"
        ) {
            return true
        }

        return try {
            val addresses = InetAddress.getAllByName(lowerHost)
            addresses.any { isPrivateOrLocalAddress(it) }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Comprehensive URL validation and classification with strict protocol and SSRF protection.
     */
    fun validateUrl(rawUrl: String): UrlValidationResult {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return UrlValidationResult.Invalid("Please enter a video URL.")
        }

        val uri = try {
            Uri.parse(trimmed)
        } catch (e: Exception) {
            return UrlValidationResult.Invalid("Malformed URL: ${e.localizedMessage ?: "invalid syntax"}")
        }

        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            return UrlValidationResult.Invalid("Invalid URL scheme '${scheme ?: ""}'. Only http:// and https:// URLs are supported.")
        }

        val host = uri.host?.lowercase()
        if (host.isNullOrBlank()) {
            return UrlValidationResult.Invalid("Invalid URL: missing domain host.")
        }

        // SSRF protection: reject local, private, loopback, and cloud metadata hostnames/IPs
        if (isPrivateOrLocalHost(host)) {
            return UrlValidationResult.Invalid("Invalid URL: private and loopback addresses are not permitted.")
        }

        val path = uri.path ?: ""
        val lastSegment = path.substringAfterLast("/").substringBefore("?")
        val extension = if (lastSegment.contains(".")) lastSegment.substringAfterLast(".").lowercase() else ""
        val directMediaExtensions = setOf(
            "mp4", "webm", "mkv", "mov", "m4v", "3gp",
            "jpg", "jpeg", "png", "webp", "gif", "bmp",
            "mp3", "wav", "m4a", "flac", "ogg", "aac"
        )

        // 1. Direct media file check by extension
        if (extension in directMediaExtensions) {
            val platform = when {
                host.contains("instagram.com") || host.contains("cdninstagram.com") -> PlatformType.INSTAGRAM
                host.contains("facebook.com") || host.contains("fbcdn.net") -> PlatformType.FACEBOOK
                else -> PlatformType.WEB
            }
            val sanitized = sanitizeFilename(lastSegment.ifEmpty { "video.$extension" })
            return UrlValidationResult.ValidDirectMedia(
                normalizedUrl = trimmed,
                suggestedFileName = sanitized,
                platform = platform
            )
        }

        // 2. Instagram page / Reel check
        if (host == "instagram.com" || host.endsWith(".instagram.com") || host == "instagr.am") {
            val lowerPath = path.lowercase()
            val isReel = lowerPath.contains("/reel") || lowerPath.contains("/reels") || lowerPath.contains("/p/") || lowerPath.contains("/tv/")
            return UrlValidationResult.ValidSocialPage(trimmed, PlatformType.INSTAGRAM, isReel)
        }

        // 3. Facebook page / Reel / Watch check
        if (host == "facebook.com" || host.endsWith(".facebook.com") || host == "fb.com" || host == "fb.watch" || host.endsWith(".fb.com")) {
            val lowerPath = path.lowercase()
            val isReel = lowerPath.contains("/reel") || lowerPath.contains("/watch") || lowerPath.contains("/share/r") || lowerPath.contains("/videos")
            return UrlValidationResult.ValidSocialPage(trimmed, PlatformType.FACEBOOK, isReel)
        }

        // 4. Other web URL
        return UrlValidationResult.ValidSocialPage(trimmed, PlatformType.WEB, false)
    }

    /**
     * Inspect remote response for media headers, MIME types, and status code,
     * following redirects securely (enforcing scheme, max hops, and no private IPs).
     */
    suspend fun checkDirectMediaResponse(initialUrl: String): PreflightResult = withContext(Dispatchers.IO) {
        var currentUrl = initialUrl
        var redirectCount = 0

        while (redirectCount <= MAX_REDIRECTS) {
            val uri = try {
                Uri.parse(currentUrl)
            } catch (e: Exception) {
                return@withContext PreflightResult(isSuccess = false, error = IOException("Invalid redirect URL: $currentUrl"))
            }

            val scheme = uri.scheme?.lowercase()
            if (scheme != "http" && scheme != "https") {
                return@withContext PreflightResult(
                    isSuccess = false,
                    error = IOException("Insecure or unsupported redirect scheme '$scheme'. Only http:// and https:// URLs are allowed.")
                )
            }

            val host = uri.host?.lowercase()
            if (host.isNullOrBlank() || isPrivateOrLocalHost(host)) {
                return@withContext PreflightResult(
                    isSuccess = false,
                    error = IOException("Redirect destination ($host) resolves to an invalid or private network address.")
                )
            }

            try {
                val headRequest = Request.Builder()
                    .url(currentUrl)
                    .head()
                    .header("User-Agent", USER_AGENT)
                    .build()

                var response: Response? = try {
                    httpClient.newCall(headRequest).execute()
                } catch (_: Exception) {
                    null
                }

                // If HEAD is not allowed (HTTP 405) or unsupported (501), fallback to GET Range
                if (response == null || response.code == 405 || response.code == 501) {
                    response?.close()
                    val getRangeRequest = Request.Builder()
                        .url(currentUrl)
                        .header("User-Agent", USER_AGENT)
                        .header("Range", "bytes=0-1024")
                        .build()
                    response = httpClient.newCall(getRangeRequest).execute()
                }

                response.use { res ->
                    val code = res.code

                    // Handle redirects manually
                    if (code in 300..399) {
                        val location = res.header("Location")
                        if (location.isNullOrBlank()) {
                            return@withContext PreflightResult(
                                isSuccess = false,
                                httpCode = code,
                                error = IOException("Redirect HTTP $code missing Location header.")
                            )
                        }

                        // Resolve relative redirect URL if needed
                        val resolvedUrl = try {
                            val resolvedUri = Uri.parse(location)
                            if (resolvedUri.isAbsolute) location else uri.buildUpon().encodedPath(location).build().toString()
                        } catch (e: Exception) {
                            return@withContext PreflightResult(
                                isSuccess = false,
                                error = IOException("Cannot parse redirect Location: $location")
                            )
                        }

                        currentUrl = resolvedUrl
                        redirectCount++
                        if (redirectCount > MAX_REDIRECTS) {
                            return@withContext PreflightResult(
                                isSuccess = false,
                                error = IOException("Too many redirects (exceeded limit of $MAX_REDIRECTS).")
                            )
                        }
                        // Continue next iteration of redirect loop
                        return@use
                    }

                    if (code == 404) {
                        return@withContext PreflightResult(
                            isSuccess = false,
                            httpCode = 404,
                            error = IOException("HTTP error 404: Media file not found on server.")
                        )
                    }
                    if (code == 401 || code == 403) {
                        return@withContext PreflightResult(
                            isSuccess = false,
                            httpCode = code,
                            error = IOException("HTTP error $code: Access forbidden or authorization required.")
                        )
                    }
                    if (code >= 500) {
                        return@withContext PreflightResult(
                            isSuccess = false,
                            httpCode = code,
                            error = IOException("Server error (HTTP $code). Please try again later.")
                        )
                    }
                    if (!res.isSuccessful && code != 206) {
                        return@withContext PreflightResult(
                            isSuccess = false,
                            httpCode = code,
                            error = IOException("HTTP request failed with status code $code.")
                        )
                    }

                    val rawContentType = res.header("Content-Type")?.lowercase()?.substringBefore(";")?.trim()
                    val contentLength = res.header("Content-Length")?.toLongOrNull() ?: 0L

                    // Check resource limits
                    if (contentLength > MAX_VIDEO_FILE_SIZE_BYTES) {
                        val sizeMb = contentLength / (1024 * 1024)
                        return@withContext PreflightResult(
                            isSuccess = false,
                            httpCode = code,
                            error = IOException("File size exceeds limit (${sizeMb}MB > 500MB).")
                        )
                    }

                    val isMedia = (rawContentType != null && (
                        rawContentType in ALLOWED_MEDIA_MIMES ||
                        rawContentType.startsWith("video/") ||
                        rawContentType.startsWith("image/") ||
                        rawContentType.startsWith("audio/")
                    ))

                    val uriPath = Uri.parse(currentUrl).path ?: ""
                    val rawName = uriPath.substringAfterLast("/").substringBefore("?").ifEmpty { "downloaded_video.mp4" }
                    val sanitizedName = sanitizeFilename(rawName)
                    val title = sanitizedName.substringBeforeLast(".").trim().ifEmpty { "Direct Media Video" }

                    return@withContext PreflightResult(
                        isSuccess = true,
                        httpCode = code,
                        contentType = rawContentType,
                        contentLength = contentLength,
                        isMedia = isMedia,
                        suggestedTitle = title,
                        finalUrl = currentUrl
                    )
                }
            } catch (e: UnknownHostException) {
                return@withContext PreflightResult(isSuccess = false, error = IOException("Network error: Unable to resolve host '${e.message}'. Check your connection."))
            } catch (e: SocketTimeoutException) {
                return@withContext PreflightResult(isSuccess = false, error = IOException("Network timeout connecting to media server. Please try again."))
            } catch (e: ConnectException) {
                return@withContext PreflightResult(isSuccess = false, error = IOException("Connection failed: Could not connect to media server."))
            } catch (e: Exception) {
                return@withContext PreflightResult(isSuccess = false, error = IOException("Network error: ${e.localizedMessage ?: "Failed to connect"}"))
            }
        }

        PreflightResult(isSuccess = false, error = IOException("Too many redirects."))
    }

    /**
     * Validate and extract video stream info.
     * Note: Does NOT use insecure scraping or bypasses for Facebook/Instagram.
     * When no authorized resolver exists, clearly informs the user that an authorized resolver is required.
     */
    suspend fun extractVideoInfo(context: Context, rawUrl: String): Result<ExtractedVideoInfo> =
        withContext(Dispatchers.IO) {
            val validation = validateUrl(rawUrl)

            when (validation) {
                is UrlValidationResult.Invalid -> {
                    Result.failure(IllegalArgumentException(validation.reason))
                }
                is UrlValidationResult.ValidSocialPage -> {
                    if (validation.platform == PlatformType.INSTAGRAM || validation.platform == PlatformType.FACEBOOK) {
                        Result.failure(IllegalStateException(RESOLVER_REQUIRED_MESSAGE))
                    } else {
                        // Check if generic web URL actually streams media
                        val preflight = checkDirectMediaResponse(validation.normalizedUrl)
                        if (!preflight.isSuccess) {
                            Result.failure(preflight.error ?: IOException("Unable to access URL."))
                        } else if (preflight.contentType?.startsWith("text/html") == true || !preflight.isMedia) {
                            Result.failure(
                                IllegalArgumentException(
                                    "Unsupported content type (${preflight.contentType ?: "text/html"}). The link is a web page, not a direct media file. Direct media URLs (.mp4, .webm) are supported."
                                )
                            )
                        } else {
                            Result.success(
                                ExtractedVideoInfo(
                                    title = preflight.suggestedTitle,
                                    author = "Direct Web Media",
                                    thumbnailUri = "",
                                    directVideoUrl = preflight.finalUrl.ifEmpty { validation.normalizedUrl },
                                    platform = "WEB",
                                    originalUrl = validation.normalizedUrl,
                                    note = "Verified direct media stream",
                                    fileSizeBytes = preflight.contentLength,
                                    mimeType = preflight.contentType ?: "video/mp4"
                                )
                            )
                        }
                    }
                }
                is UrlValidationResult.ValidDirectMedia -> {
                    val preflight = checkDirectMediaResponse(validation.normalizedUrl)
                    if (!preflight.isSuccess) {
                        return@withContext Result.failure(preflight.error ?: IOException("Failed to reach media server."))
                    }

                    if (preflight.contentType?.startsWith("text/html") == true || !preflight.isMedia) {
                        return@withContext Result.failure(
                            IllegalArgumentException("Server returned a web page (${preflight.contentType}) instead of a direct media file.")
                        )
                    }

                    val platformStr = when (validation.platform) {
                        PlatformType.INSTAGRAM -> "INSTAGRAM"
                        PlatformType.FACEBOOK -> "FACEBOOK"
                        PlatformType.WEB -> "WEB"
                    }

                    val cleanTitle = sanitizeFilename(validation.suggestedFileName)
                        .substringBeforeLast(".")
                        .trim()
                        .ifEmpty { "Direct Video Stream" }

                    Result.success(
                        ExtractedVideoInfo(
                            title = cleanTitle,
                            author = "Direct Media ($platformStr)",
                            thumbnailUri = "",
                            directVideoUrl = preflight.finalUrl.ifEmpty { validation.normalizedUrl },
                            platform = platformStr,
                            originalUrl = validation.normalizedUrl,
                            note = "Direct media stream",
                            fileSizeBytes = preflight.contentLength,
                            mimeType = preflight.contentType ?: "video/mp4"
                        )
                    )
                }
            }
        }

    /**
     * Check if network is connected
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Sanitize filename:
     * - Disallows path traversal sequences ("../", "..\\")
     * - Strips forbidden filesystem characters
     * - Limits base filename to 60 characters
     * - Enforces valid video extension
     */
    fun sanitizeFilename(raw: String, defaultExtension: String = ".mp4"): String {
        var base = raw
            .replace("\\", "/")
            .substringAfterLast("/")
            .replace("../", "")
            .replace("..", "")
            .trim()

        val ext = if (base.contains(".")) {
            val e = base.substringAfterLast(".").lowercase()
            if (e in setOf("mp4", "webm", "mkv", "mov", "m4v", "3gp")) ".$e" else defaultExtension
        } else {
            defaultExtension
        }

        val nameWithoutExt = if (base.contains(".")) base.substringBeforeLast(".") else base
        val cleanedName = nameWithoutExt
            .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            .trim('_', '.', ' ')
            .take(60)
            .ifEmpty { "downloaded_video" }

        return "$cleanedName$ext"
    }

    /**
     * Safely determine unique file in the app's dedicated public Downloads subdirectory (Download/TimepassDownloads).
     */
    fun resolveUniquePublicDownloadFile(baseName: String, extension: String = ".mp4"): Pair<File, String> {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = File(publicDownloads, "TimepassDownloads")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val sanitized = sanitizeFilename(baseName, extension)
        val cleanBase = sanitized.substringBeforeLast(".")
        val cleanExt = if (sanitized.contains(".")) ".${sanitized.substringAfterLast(".")}" else extension

        var candidateName = "$cleanBase$cleanExt"
        var candidateFile = File(targetDir, candidateName)
        var counter = 1
        while (candidateFile.exists()) {
            candidateName = "${cleanBase}_$counter$cleanExt"
            candidateFile = File(targetDir, candidateName)
            counter++
        }
        return Pair(candidateFile, candidateName)
    }

    /**
     * Verify that the downloaded file exists on disk, has a nonzero size,
     * does not exceed maximum limit, and has a valid media header/magic number signature.
     */
    fun verifyDownloadedFile(file: File?): Boolean {
        if (file == null || !file.exists() || file.length() <= 0L) {
            return false
        }
        if (file.length() > MAX_VIDEO_FILE_SIZE_BYTES) {
            return false
        }
        return verifyFileSignature(file)
    }

    /**
     * Validates file signature (magic bytes) to ensure file is an actual video container
     * rather than HTML, text, or executable script.
     */
    fun verifyFileSignature(file: File): Boolean {
        if (!file.exists() || file.length() < 12) return false
        return try {
            FileInputStream(file).use { input ->
                val header = ByteArray(64)
                val read = input.read(header)
                if (read < 12) return false

                // 1. MP4 / MOV / 3GP: bytes 4..7 is "ftyp" (0x66, 0x74, 0x79, 0x70) or "moov" (0x6D, 0x6F, 0x6F, 0x76)
                val isFtyp = header[4] == 0x66.toByte() && header[5] == 0x74.toByte() &&
                        header[6] == 0x79.toByte() && header[7] == 0x70.toByte()
                val isMoov = header[4] == 0x6D.toByte() && header[5] == 0x6F.toByte() &&
                        header[6] == 0x6F.toByte() && header[7] == 0x76.toByte()

                // 2. Matroska / WebM: starts with 0x1A, 0x45, 0xDF, 0xA3 (EBML ID)
                val isMatroska = header[0] == 0x1A.toByte() && header[1] == 0x45.toByte() &&
                        header[2] == 0xDF.toByte() && header[3] == 0xA3.toByte()

                // 3. AVI / RIFF: starts with 'RIFF' and bytes 8..11 are 'AVI '
                val isRiffAvi = header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() &&
                        header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte() &&
                        read >= 12 && header[8] == 'A'.code.toByte() && header[9] == 'V'.code.toByte() &&
                        header[10] == 'I'.code.toByte() && header[11] == ' '.code.toByte()

                isFtyp || isMoov || isMatroska || isRiffAvi
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Cancel download in DownloadManager and clean up any partial or temporary files.
     */
    fun cancelDownload(context: Context, downloadId: Long, targetFile: File? = null) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            dm?.remove(downloadId)
        } catch (_: Exception) {}

        try {
            if (targetFile != null && targetFile.exists()) {
                targetFile.delete()
            }
        } catch (_: Exception) {}
    }

    /**
     * Download direct media using Android DownloadManager with accurate progress and verification.
     * Enforces that the file exists, has valid magic bytes, is nonzero, and under size limits.
     */
    fun downloadVideoFlow(
        context: Context,
        info: ExtractedVideoInfo
    ): Flow<DownloadProgressState> = flow {
        emit(DownloadProgressState(bytesDownloaded = 0, totalBytes = 0, percent = 0, statusMessage = "Preparing download..."))

        // 1. Network check
        if (!isNetworkAvailable(context)) {
            emit(DownloadProgressState(error = "No network connection. Please check your internet connection and try again."))
            return@flow
        }

        // 2. Storage availability check
        val storageState = Environment.getExternalStorageState()
        if (storageState != Environment.MEDIA_MOUNTED) {
            emit(DownloadProgressState(error = "External storage is unavailable (state: $storageState). Cannot save download."))
            return@flow
        }

        val directUrl = info.directVideoUrl
        if (directUrl.isNullOrBlank()) {
            emit(DownloadProgressState(error = "Direct media URL is missing."))
            return@flow
        }

        // 3. Resolve destination file in dedicated public downloads subfolder
        val (targetFile, uniqueName) = resolveUniquePublicDownloadFile(info.title, ".mp4")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            emit(DownloadProgressState(error = "Android DownloadManager system service is unavailable."))
            return@flow
        }

        val subPath = "TimepassDownloads/$uniqueName"
        val request = try {
            DownloadManager.Request(Uri.parse(directUrl)).apply {
                setTitle(info.title)
                setDescription("Downloading $uniqueName to Timepass Downloads")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setMimeType(info.mimeType)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
        } catch (e: Exception) {
            emit(DownloadProgressState(error = "Failed to configure DownloadManager request: ${e.localizedMessage}"))
            return@flow
        }

        val downloadId = try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            emit(DownloadProgressState(error = "DownloadManager enqueue failed: ${e.localizedMessage}"))
            return@flow
        }

        emit(DownloadProgressState(downloadId = downloadId, bytesDownloaded = 0, totalBytes = info.fileSizeBytes, percent = 0, statusMessage = "Queued in DownloadManager..."))

        var isDone = false
        var lastPercent = -1

        try {
            while (!isDone && coroutineContext.isActive) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val reasonCol = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                    val soFarCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    val status = if (statusCol >= 0) cursor.getInt(statusCol) else -1
                    val soFar = if (soFarCol >= 0) cursor.getLong(soFarCol) else 0L
                    val total = if (totalCol >= 0) cursor.getLong(totalCol) else -1L

                    // Check resource limits during active download
                    if (soFar > MAX_VIDEO_FILE_SIZE_BYTES || (total > 0 && total > MAX_VIDEO_FILE_SIZE_BYTES)) {
                        isDone = true
                        cursor.close()
                        cancelDownload(context, downloadId, targetFile)
                        emit(DownloadProgressState(downloadId = downloadId, error = "Download exceeded maximum size limit of 500MB. Download cancelled."))
                        return@flow
                    }

                    when (status) {
                        DownloadManager.STATUS_PENDING -> {
                            emit(
                                DownloadProgressState(
                                    downloadId = downloadId,
                                    bytesDownloaded = 0,
                                    totalBytes = if (total > 0) total else info.fileSizeBytes,
                                    percent = 0,
                                    statusMessage = "Waiting for network..."
                                )
                            )
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            val reason = if (reasonCol >= 0) cursor.getInt(reasonCol) else 0
                            val reasonMsg = getDownloadManagerPausedReason(reason)
                            emit(
                                DownloadProgressState(
                                    downloadId = downloadId,
                                    bytesDownloaded = soFar,
                                    totalBytes = total,
                                    percent = lastPercent.coerceAtLeast(0),
                                    statusMessage = "Paused: $reasonMsg"
                                )
                            )
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            val calcTotal = if (total > 0) total else info.fileSizeBytes
                            val percent = if (calcTotal > 0) ((soFar * 100) / calcTotal).toInt().coerceIn(0, 99) else 0
                            if (percent != lastPercent || soFar % (512 * 1024) == 0L) {
                                lastPercent = percent
                                emit(
                                    DownloadProgressState(
                                        downloadId = downloadId,
                                        bytesDownloaded = soFar,
                                        totalBytes = calcTotal,
                                        percent = percent,
                                        statusMessage = "Downloading..."
                                    )
                                )
                            }
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            isDone = true
                            cursor.close()

                            // Check file exists on disk, nonzero, valid signature, under size limit
                            val verified = verifyCompletedDownload(context, downloadManager, downloadId, targetFile)
                            if (verified != null && verifyDownloadedFile(verified)) {
                                saveToPublicMediaStore(context, verified, info.title)
                                emit(
                                    DownloadProgressState(
                                        downloadId = downloadId,
                                        bytesDownloaded = verified.length(),
                                        totalBytes = verified.length(),
                                        percent = 100,
                                        isCompleted = true,
                                        savedFile = verified,
                                        statusMessage = "Download completed and verified."
                                    )
                                )
                            } else {
                                // Clean up corrupt/invalid file
                                if (verified != null && verified.exists()) {
                                    verified.delete()
                                } else if (targetFile.exists()) {
                                    targetFile.delete()
                                }
                                emit(
                                    DownloadProgressState(
                                        downloadId = downloadId,
                                        error = "Verification failed: Downloaded file is corrupt, invalid media type, or empty. Library not updated."
                                    )
                                )
                            }
                            return@flow
                        }
                        DownloadManager.STATUS_FAILED -> {
                            isDone = true
                            val reason = if (reasonCol >= 0) cursor.getInt(reasonCol) else 0
                            val errorMsg = getDownloadManagerErrorMessage(reason)
                            cursor.close()
                            if (targetFile.exists()) {
                                targetFile.delete()
                            }
                            emit(DownloadProgressState(downloadId = downloadId, error = "Download failed: $errorMsg"))
                            return@flow
                        }
                    }
                    cursor.close()
                }
                delay(300)
            }
        } finally {
            if (!isDone) {
                // Cancelled
                cancelDownload(context, downloadId, targetFile)
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Verify completed download via File or ContentResolver
     */
    private fun verifyCompletedDownload(
        context: Context,
        downloadManager: DownloadManager,
        downloadId: Long,
        targetFile: File
    ): File? {
        // Direct file exists with size > 0
        if (targetFile.exists() && targetFile.length() > 0L) {
            return targetFile
        }

        // Check through DownloadManager URI
        try {
            val uri = downloadManager.getUriForDownloadedFile(downloadId)
            if (uri != null) {
                val statSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                if (statSize > 0L) {
                    if (!targetFile.exists() || targetFile.length() == 0L) {
                        val internalDir = File(context.filesDir, "verified_downloads").apply { mkdirs() }
                        val localCopy = File(internalDir, targetFile.name)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            localCopy.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (localCopy.exists() && localCopy.length() > 0L) {
                            return localCopy
                        }
                    }
                    return targetFile
                }
            }
        } catch (_: Exception) {}

        return null
    }

    /**
     * Map DownloadManager failure reason codes to readable messages
     */
    private fun getDownloadManagerErrorMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume incomplete transfer."
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage device or SD card not found."
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "Destination file already exists."
            DownloadManager.ERROR_FILE_ERROR -> "Storage file system error writing media."
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data transfer failed."
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space on device."
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many HTTP server redirects."
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP server response."
            DownloadManager.ERROR_UNKNOWN -> "Unknown error in transfer."
            else -> "Error code $reason"
        }
    }

    /**
     * Map DownloadManager pause reasons
     */
    private fun getDownloadManagerPausedReason(reason: Int): String {
        return when (reason) {
            DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Queued for Wi-Fi connection."
            DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "Waiting for active network."
            DownloadManager.PAUSED_WAITING_TO_RETRY -> "Network glitch; retrying..."
            DownloadManager.PAUSED_UNKNOWN -> "Transfer paused."
            else -> "Paused (code $reason)"
        }
    }

    /**
     * Index downloaded video to MediaStore Movies directory
     */
    fun saveToPublicMediaStore(context: Context, file: File, title: String) {
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.TITLE, title)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/TimepassDownloads")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val itemUri: Uri? = resolver.insert(collection, contentValues)
            if (itemUri != null) {
                resolver.openOutputStream(itemUri)?.use { out ->
                    file.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(itemUri, contentValues, null, null)
                }
            }
        } catch (_: Exception) {}
    }
}
