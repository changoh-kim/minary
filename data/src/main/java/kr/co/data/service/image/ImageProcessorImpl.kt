package kr.co.data.service.image

import kr.co.core.common.logging.AppLogger
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.core.common.error.DomainError
import kr.co.domain.service.image.ImageProcessor
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessorImpl @Inject constructor(
    private val logger: AppLogger,
    @param:ApplicationContext private val context: Context,
) : ImageProcessor {

    override suspend fun resizeImage(
        sourceUrl: String,
        targetUrl: String,
        maxWidth: Int,
        maxHeight: Int,
    ): AppResult<String> = withContext(Dispatchers.IO) {
        try {
            val androidUri = sourceUrl.toUri()
            // 1. 이미지 크기 및 회전 정보 측정
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(androidUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return@withContext Err(DomainError.Unexpected)
            }
            // 회전 각도 계산
            val rotation = getRotation(androidUri)
            // 2. 효율적인 디코딩을 위한 inSampleSize 계산
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            // 3. 비트맵 디코딩
            val decodedBitmap = context.contentResolver.openInputStream(androidUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
                ?: return@withContext Err(DomainError.Unexpected)
            // 4. 회전 처리 및 정밀 리사이징
            val rotatedBitmap = rotateBitmapIfRequired(decodedBitmap, rotation)
            val finalBitmap = scaleBitmapToFit(rotatedBitmap, maxWidth, maxHeight)
            // 중간 비트맵들 정리
            if (rotatedBitmap != finalBitmap) rotatedBitmap.recycle()
            if (decodedBitmap != rotatedBitmap && decodedBitmap != finalBitmap) decodedBitmap.recycle()
            // 5. 파일 저장
            val targetFile = targetUrl.toUri().let { destUri ->
                if (destUri.scheme == "file") File(destUri.path!!) else File(targetUrl)
            }
            targetFile.parentFile?.mkdirs()

            FileOutputStream(targetFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            finalBitmap.recycle()

            Ok(targetFile.absolutePath)
        } catch (e: Exception) {
            logger.e(e, "Failed to resize image")
            Err(DomainError.Unexpected)
        }
    }

    private fun getRotation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use {
                val exif = ExifInterface(it)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it != bitmap) bitmap.recycle()
        }
    }

    private fun scaleBitmapToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val scale = (maxWidth.toFloat() / bitmap.width).coerceAtMost(maxHeight.toFloat() / bitmap.height)
        return if (scale < 1.0f) {
            bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt())
        } else {
            bitmap
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
