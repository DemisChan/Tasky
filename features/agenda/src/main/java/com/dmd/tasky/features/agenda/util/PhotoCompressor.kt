package com.dmd.tasky.features.agenda.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.domain.repository.AgendaError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.`-DeprecatedOkio`.source
import timber.log.Timber
import java.io.ByteArrayOutputStream

object PhotoCompressor {
    private const val MAX_SIZE_BYTES = 1_048_576 // 1MB
    private const val INITIAL_QUALITY = 90
    private const val QUALITY_STEP = 10
    private const val MIN_QUALITY = 50

    suspend fun compressPhoto(
        context: Context,
        uri: Uri
    ): Result<ByteArray, AgendaError> = withContext(Dispatchers.IO) {
        try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)

            var quality = INITIAL_QUALITY
            var compressedData = compressBitmap(bitmap, quality)

            // Try progressively lower quality
            while (compressedData.size > MAX_SIZE_BYTES && quality >= MIN_QUALITY) {
                quality -= QUALITY_STEP
                compressedData = compressBitmap(bitmap, quality)
            }

            if (compressedData.size > MAX_SIZE_BYTES) {
                Result.Error(AgendaError.PHOTO_TOO_LARGE)
            } else {
                Result.Success(compressedData)
            }
        } catch (e: Exception) {
            Timber.e("Photo compression failed: ${e.message}")
            Result.Error(AgendaError.UNKNOWN)
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }
}