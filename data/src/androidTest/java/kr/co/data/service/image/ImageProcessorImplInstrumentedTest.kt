package kr.co.data.service.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kr.co.core.common.error.DomainError
import kr.co.data.testing.AndroidFakeAppLogger
import kr.co.data.testing.BaseDataInstrumentationTest
import kr.co.data.testing.assertAndroidErr
import kr.co.data.testing.assertAndroidOk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ImageProcessorImplInstrumentedTest : BaseDataInstrumentationTest() {
    private lateinit var testDir: File
    private lateinit var imageProcessor: ImageProcessorImpl

    @Before
    fun setUpImageProcessor() {
        testDir = File(context.cacheDir, "image-processor-test").apply {
            deleteRecursively()
            mkdirs()
        }
        imageProcessor = ImageProcessorImpl(
            logger = AndroidFakeAppLogger(),
            context = context,
        )
    }

    @After
    fun tearDownImageProcessor() {
        if (::testDir.isInitialized) {
            testDir.deleteRecursively()
        }
    }

    @Test
    fun resizeImage_downscales_large_image_within_max_bounds() = runDataAndroidTest {
        val source = jpegFile("large-source.jpg", width = 800, height = 400)
        val target = File(testDir, "large-target.jpg")

        val resultPath = imageProcessor.resizeImage(
            sourceUrl = Uri.fromFile(source).toString(),
            targetUrl = Uri.fromFile(target).toString(),
            maxWidth = 200,
            maxHeight = 200,
        ).assertAndroidOk()

        assertEquals(target.absolutePath, resultPath)
        assertTrue(target.exists())

        val decoded = BitmapFactory.decodeFile(target.absolutePath)
        try {
            assertTrue(decoded.width <= 200)
            assertTrue(decoded.height <= 200)
            assertEquals(200, decoded.width)
            assertEquals(100, decoded.height)
        } finally {
            decoded.recycle()
        }
    }

    @Test
    fun resizeImage_does_not_upscale_image_smaller_than_max_bounds() = runDataAndroidTest {
        val source = jpegFile("small-source.jpg", width = 80, height = 40)
        val target = File(testDir, "small-target.jpg")

        imageProcessor.resizeImage(
            sourceUrl = Uri.fromFile(source).toString(),
            targetUrl = target.absolutePath,
            maxWidth = 200,
            maxHeight = 200,
        ).assertAndroidOk(target.absolutePath)

        val decoded = BitmapFactory.decodeFile(target.absolutePath)
        try {
            assertEquals(80, decoded.width)
            assertEquals(40, decoded.height)
        } finally {
            decoded.recycle()
        }
    }

    @Test
    fun resizeImage_applies_exif_rotation_before_saving_target() = runDataAndroidTest {
        val source = jpegFile("rotated-source.jpg", width = 120, height = 60)
        ExifInterface(source.absolutePath).apply {
            setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_ROTATE_90.toString(),
            )
            saveAttributes()
        }
        val target = File(testDir, "rotated-target.jpg")

        imageProcessor.resizeImage(
            sourceUrl = Uri.fromFile(source).toString(),
            targetUrl = Uri.fromFile(target).toString(),
            maxWidth = 200,
            maxHeight = 200,
        ).assertAndroidOk(target.absolutePath)

        val decoded = BitmapFactory.decodeFile(target.absolutePath)
        try {
            assertEquals(60, decoded.width)
            assertEquals(120, decoded.height)
        } finally {
            decoded.recycle()
        }
    }

    @Test
    fun resizeImage_returns_unexpected_when_source_is_not_an_image() = runDataAndroidTest {
        val source = File(testDir, "invalid-source.txt").apply {
            writeText("not-image-test")
        }
        val target = File(testDir, "invalid-target.jpg")

        imageProcessor.resizeImage(
            sourceUrl = Uri.fromFile(source).toString(),
            targetUrl = Uri.fromFile(target).toString(),
            maxWidth = 200,
            maxHeight = 200,
        ).assertAndroidErr(DomainError.Unexpected)

        assertFalse(target.exists())
    }

    private fun jpegFile(
        name: String,
        width: Int,
        height: Int,
    ): File {
        val file = File(testDir, name)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(80, 120, 160))
        try {
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
            }
        } finally {
            bitmap.recycle()
        }
        return file
    }
}
