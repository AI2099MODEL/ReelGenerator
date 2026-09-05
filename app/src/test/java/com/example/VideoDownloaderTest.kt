package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.PlatformType
import com.example.util.UrlValidationResult
import com.example.util.VideoDownloadHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class VideoDownloaderTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testEmptyAndBlankUrlValidation() {
        val emptyResult = VideoDownloadHelper.validateUrl("")
        assertTrue("Empty string should be invalid", emptyResult is UrlValidationResult.Invalid)

        val whitespaceResult = VideoDownloadHelper.validateUrl("   \n\t   ")
        assertTrue("Whitespace-only string should be invalid", whitespaceResult is UrlValidationResult.Invalid)
    }

    @Test
    fun testInvalidSchemeUrl() {
        val ftpResult = VideoDownloadHelper.validateUrl("ftp://example.com/video.mp4")
        assertTrue("FTP scheme should be rejected", ftpResult is UrlValidationResult.Invalid)
        assertEquals(
            "Invalid URL scheme 'ftp'. Only http:// and https:// URLs are supported.",
            (ftpResult as UrlValidationResult.Invalid).reason
        )

        val fileResult = VideoDownloadHelper.validateUrl("file:///sdcard/video.mp4")
        assertTrue("File scheme should be rejected", fileResult is UrlValidationResult.Invalid)

        val javascriptResult = VideoDownloadHelper.validateUrl("javascript:alert(1)")
        assertTrue("Javascript pseudo-scheme should be rejected", javascriptResult is UrlValidationResult.Invalid)
    }

    @Test
    fun testMissingHostUrl() {
        val noHostResult = VideoDownloadHelper.validateUrl("https:///path/only/video.mp4")
        assertTrue("Missing host domain should be invalid", noHostResult is UrlValidationResult.Invalid)
    }

    @Test
    fun testInstagramReelIdentification() {
        val reelUrl = "https://www.instagram.com/reel/C3_test_reel_123/"
        val result = VideoDownloadHelper.validateUrl(reelUrl)
        assertTrue("Should validate as social page", result is UrlValidationResult.ValidSocialPage)
        val socialPage = result as UrlValidationResult.ValidSocialPage
        assertEquals(PlatformType.INSTAGRAM, socialPage.platform)
        assertTrue("Should be identified as Reel", socialPage.isReel)

        val postUrl = "https://instagram.com/p/C3_sample_post/"
        val postResult = VideoDownloadHelper.validateUrl(postUrl)
        assertTrue(postResult is UrlValidationResult.ValidSocialPage)
        assertEquals(PlatformType.INSTAGRAM, (postResult as UrlValidationResult.ValidSocialPage).platform)
    }

    @Test
    fun testFacebookVideoIdentification() {
        val watchUrl = "https://www.facebook.com/watch/?v=9876543210"
        val result = VideoDownloadHelper.validateUrl(watchUrl)
        assertTrue("Should validate as social page", result is UrlValidationResult.ValidSocialPage)
        val socialPage = result as UrlValidationResult.ValidSocialPage
        assertEquals(PlatformType.FACEBOOK, socialPage.platform)
        assertTrue("Should be identified as Reel or Watch video", socialPage.isReel)

        val reelUrl = "https://www.facebook.com/reel/123456789"
        val reelResult = VideoDownloadHelper.validateUrl(reelUrl)
        assertTrue(reelResult is UrlValidationResult.ValidSocialPage)
        assertEquals(PlatformType.FACEBOOK, (reelResult as UrlValidationResult.ValidSocialPage).platform)
        assertTrue((reelResult as UrlValidationResult.ValidSocialPage).isReel)

        val fbWatchUrl = "https://fb.watch/sample_watch_code/"
        val fbWatchResult = VideoDownloadHelper.validateUrl(fbWatchUrl)
        assertTrue(fbWatchResult is UrlValidationResult.ValidSocialPage)
        assertEquals(PlatformType.FACEBOOK, (fbWatchResult as UrlValidationResult.ValidSocialPage).platform)
    }

    @Test
    fun testDirectMediaIdentification() {
        val directMp4 = "https://example.com/multimedia/sample_clip.mp4"
        val result = VideoDownloadHelper.validateUrl(directMp4)
        assertTrue("Should be recognized as direct media", result is UrlValidationResult.ValidDirectMedia)
        val direct = result as UrlValidationResult.ValidDirectMedia
        assertEquals("sample_clip.mp4", direct.suggestedFileName)

        val directWithQuery = "https://storage.googleapis.com/test-bucket/video.webm?signature=123"
        val webmResult = VideoDownloadHelper.validateUrl(directWithQuery)
        assertTrue("Should recognize .webm direct media with query params", webmResult is UrlValidationResult.ValidDirectMedia)
        assertEquals("video.webm", (webmResult as UrlValidationResult.ValidDirectMedia).suggestedFileName)
    }

    @Test
    fun testSocialPagesRequireAuthorizedResolver() = runBlocking {
        // Facebook page URL
        val fbResult = VideoDownloadHelper.extractVideoInfo(context, "https://www.facebook.com/watch/?v=123456")
        assertTrue("Facebook page without direct media link must fail safely", fbResult.isFailure)
        val fbException = fbResult.exceptionOrNull()
        assertEquals(VideoDownloadHelper.RESOLVER_REQUIRED_MESSAGE, fbException?.message)

        // Instagram page URL
        val igResult = VideoDownloadHelper.extractVideoInfo(context, "https://www.instagram.com/reel/sample_code/")
        assertTrue("Instagram page without direct media link must fail safely", igResult.isFailure)
        val igException = igResult.exceptionOrNull()
        assertEquals(VideoDownloadHelper.RESOLVER_REQUIRED_MESSAGE, igException?.message)
    }

    @Test
    fun testDuplicateFilenameResolution() {
        val testDir = File(context.cacheDir, "test_downloads").apply { mkdirs() }
        val baseName = "vacation_clip"

        // Create initial collision file
        val firstFile = File(testDir, "$baseName.mp4")
        firstFile.writeText("sample data")

        // Unique filename generation algorithm
        var candidateName = "$baseName.mp4"
        var candidateFile = File(testDir, candidateName)
        var counter = 1
        while (candidateFile.exists()) {
            candidateName = "${baseName}_$counter.mp4"
            candidateFile = File(testDir, candidateName)
            counter++
        }

        assertEquals("vacation_clip_1.mp4", candidateName)
        assertFalse("Unique candidate file should not yet exist", candidateFile.exists())

        // Clean up
        firstFile.delete()
        testDir.deleteRecursively()
    }

    @Test
    fun testVerifyDownloadedFile() {
        // 1. Null file
        assertFalse(VideoDownloadHelper.verifyDownloadedFile(null))

        // 2. Non-existent file
        val nonExistent = File(context.cacheDir, "non_existent_${System.currentTimeMillis()}.mp4")
        assertFalse(VideoDownloadHelper.verifyDownloadedFile(nonExistent))

        // 3. Zero-byte empty file
        val emptyFile = File(context.cacheDir, "empty_${System.currentTimeMillis()}.mp4").apply {
            createNewFile()
        }
        assertFalse(
            "Empty 0-byte file must fail verification and NEVER be added to library",
            VideoDownloadHelper.verifyDownloadedFile(emptyFile)
        )
        emptyFile.delete()

        // 4. Valid MP4 file with valid ftyp box and minimum size >= 12 bytes
        val validFile = File(context.cacheDir, "valid_${System.currentTimeMillis()}.mp4").apply {
            // Box size: 16 bytes (0x00, 0x00, 0x00, 0x10), type "ftyp", major brand "isom"
            writeBytes(byteArrayOf(
                0x00, 0x00, 0x00, 0x10,
                0x66, 0x74, 0x79, 0x70, // 'f', 't', 'y', 'p'
                0x69, 0x73, 0x6F, 0x6D, // 'i', 's', 'o', 'm'
                0x00, 0x00, 0x00, 0x01
            ))
        }
        assertTrue("Valid non-empty file must pass verification", VideoDownloadHelper.verifyDownloadedFile(validFile))
        validFile.delete()
    }

    @Test
    fun testVerifyFileSignatures() {
        // Matroska / WebM signature (EBML ID: 0x1A, 0x45, 0xDF, 0xA3)
        val webmFile = File(context.cacheDir, "test_${System.currentTimeMillis()}.webm").apply {
            writeBytes(byteArrayOf(
                0x1A.toByte(), 0x45.toByte(), 0xDF.toByte(), 0xA3.toByte(),
                0x9F.toByte(), 0x42.toByte(), 0x86.toByte(), 0x81.toByte(),
                0x01.toByte(), 0x42.toByte(), 0xF7.toByte(), 0x81.toByte(),
                0x01.toByte(), 0x42.toByte(), 0xF2.toByte(), 0x81.toByte()
            ))
        }
        assertTrue("Valid WebM EBML signature should pass", VideoDownloadHelper.verifyFileSignature(webmFile))
        assertTrue("WebM file should pass verifyDownloadedFile", VideoDownloadHelper.verifyDownloadedFile(webmFile))
        webmFile.delete()

        // AVI / RIFF signature (RIFF....AVI )
        val aviFile = File(context.cacheDir, "test_${System.currentTimeMillis()}.avi").apply {
            writeBytes(byteArrayOf(
                'R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte(),
                0x00, 0x01, 0x00, 0x00,
                'A'.code.toByte(), 'V'.code.toByte(), 'I'.code.toByte(), ' '.code.toByte(),
                0x00, 0x00, 0x00, 0x00
            ))
        }
        assertTrue("Valid AVI RIFF signature should pass", VideoDownloadHelper.verifyFileSignature(aviFile))
        assertTrue("AVI file should pass verifyDownloadedFile", VideoDownloadHelper.verifyDownloadedFile(aviFile))
        aviFile.delete()

        // HTML text file (spoofed video extension) should FAIL verification
        val fakeHtmlFile = File(context.cacheDir, "fake_${System.currentTimeMillis()}.mp4").apply {
            writeText("<!DOCTYPE html><html><body><h1>Not a video</h1></body></html>")
        }
        assertFalse("HTML payload masquerading as video should fail signature check", VideoDownloadHelper.verifyFileSignature(fakeHtmlFile))
        assertFalse("Fake HTML file should fail verifyDownloadedFile", VideoDownloadHelper.verifyDownloadedFile(fakeHtmlFile))
        fakeHtmlFile.delete()
    }

    @Test
    fun testSsrfLocalAndPrivateHostBlocking() {
        // Localhost and loopback
        assertTrue("localhost should be recognized as local", VideoDownloadHelper.isPrivateOrLocalHost("localhost"))
        assertTrue("127.0.0.1 should be recognized as local", VideoDownloadHelper.isPrivateOrLocalHost("127.0.0.1"))
        assertTrue("::1 should be recognized as local", VideoDownloadHelper.isPrivateOrLocalHost("::1"))
        assertTrue("0.0.0.0 should be recognized as local", VideoDownloadHelper.isPrivateOrLocalHost("0.0.0.0"))

        // URL validation rejecting SSRF targets
        val localhostUrl = VideoDownloadHelper.validateUrl("http://localhost/video.mp4")
        assertTrue("localhost URL must be rejected", localhostUrl is UrlValidationResult.Invalid)

        val loopbackUrl = VideoDownloadHelper.validateUrl("http://127.0.0.1:8080/exploit.mp4")
        assertTrue("127.0.0.1 URL must be rejected", loopbackUrl is UrlValidationResult.Invalid)

        val linkLocalUrl = VideoDownloadHelper.validateUrl("http://169.254.169.254/latest/meta-data/")
        assertTrue("Cloud metadata / link-local URL must be rejected", linkLocalUrl is UrlValidationResult.Invalid)
    }

    @Test
    fun testFilenameSanitization() {
        // Path traversal attempts
        val traversal1 = VideoDownloadHelper.sanitizeFilename("../../../secret.mp4")
        assertFalse("Sanitized filename must not contain ../", traversal1.contains(".."))
        assertFalse("Sanitized filename must not contain /", traversal1.contains("/"))

        val traversal2 = VideoDownloadHelper.sanitizeFilename("..\\..\\windows_file.mp4")
        assertFalse("Sanitized filename must not contain \\", traversal2.contains("\\"))
        assertFalse("Sanitized filename must not contain ..", traversal2.contains(".."))

        // Special characters
        val dirtyName = VideoDownloadHelper.sanitizeFilename("my:fancy*video?<name>|.mp4")
        assertEquals("my_fancy_video__name.mp4", dirtyName)
        assertFalse("Special characters must not appear in sanitized name", dirtyName.contains(":") || dirtyName.contains("*") || dirtyName.contains("<") || dirtyName.contains(">") || dirtyName.contains("|"))

        // Missing extension fallback
        val noExt = VideoDownloadHelper.sanitizeFilename("plain_video_file")
        assertTrue("Should append default extension", noExt.endsWith(".mp4"))

        // Length truncation (max 60 chars base name)
        val longName = "a".repeat(100) + ".mp4"
        val sanitizedLong = VideoDownloadHelper.sanitizeFilename(longName)
        val basePart = sanitizedLong.substringBeforeLast(".")
        assertTrue("Base name should not exceed 60 characters", basePart.length <= 60)
        assertTrue(sanitizedLong.endsWith(".mp4"))
    }

    @Test
    fun testAllowedVideoMimes() {
        assertTrue("video/mp4 must be allowed", VideoDownloadHelper.ALLOWED_VIDEO_MIMES.contains("video/mp4"))
        assertTrue("video/webm must be allowed", VideoDownloadHelper.ALLOWED_VIDEO_MIMES.contains("video/webm"))
        assertTrue("video/x-matroska must be allowed", VideoDownloadHelper.ALLOWED_VIDEO_MIMES.contains("video/x-matroska"))
        assertFalse("text/html must NOT be in allowed mimes", VideoDownloadHelper.ALLOWED_VIDEO_MIMES.contains("text/html"))
        assertFalse("application/json must NOT be in allowed mimes", VideoDownloadHelper.ALLOWED_VIDEO_MIMES.contains("application/json"))
        assertFalse("application/octet-stream must NOT be in allowed mimes", VideoDownloadHelper.ALLOWED_VIDEO_MIMES.contains("application/octet-stream"))
    }

    @Test
    fun testPlatformDetection() {
        assertEquals("INSTAGRAM", VideoDownloadHelper.detectPlatform("https://www.instagram.com/reel/123/"))
        assertEquals("INSTAGRAM", VideoDownloadHelper.detectPlatform("https://instagr.am/p/123/"))
        assertEquals("FACEBOOK", VideoDownloadHelper.detectPlatform("https://www.facebook.com/watch/?v=123/"))
        assertEquals("FACEBOOK", VideoDownloadHelper.detectPlatform("https://fb.watch/123/"))
        assertEquals("WEB", VideoDownloadHelper.detectPlatform("https://example.com/video.mp4"))
    }
}
