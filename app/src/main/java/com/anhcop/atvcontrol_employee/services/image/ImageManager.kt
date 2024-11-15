package com.anhcop.atvcontrol_employee.services.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.anhcop.atvcontrol_employee.services.vehicle.VehicleService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.min

class ImageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vehicleService: VehicleService
) {
    private val _cache = ConcurrentHashMap<String, Bitmap>()

    private val _events = MutableSharedFlow<ImageEvent>()
    val events = _events.asSharedFlow()

    private val names: List<String>
        get() {
            val assetsDirectory = context.getDir("images", Context.MODE_PRIVATE) ?: return emptyList()

            val files = assetsDirectory.listFiles() ?: return emptyList()

            return files.mapNotNull { file -> file?.name?.split(".")?.get(0) }
        }

    fun get(name: String): Bitmap? {
        val cached = _cache[name]

        if (cached != null) {
            return cached
        }

        val assetsDirectory = context.getDir("images", Context.MODE_PRIVATE) ?: return null
        val imageFile = File(assetsDirectory, "$name.jpg")

        try {
            val bitmap = context.contentResolver.openInputStream(imageFile.toUri())?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return null

            return bitmap.apply {
                _cache[name] = this
            }
        } catch (_: Throwable) {
            return null
        }
    }

    private fun Bitmap.centerCropped(): Bitmap {
        val (x, y) = if (width >= height) {
            ((width - height) / 2) to 0
        } else {
            0 to ((height - width) / 2)
        }

        val size = min(width, height)

        return Bitmap.createBitmap(this, x, y, size, size)
    }

    suspend fun save(name: String, bitmap: Bitmap) {
        val processedBitmap = bitmap.centerCropped().scale(128, 128, true)

        val assetsDirectory = context.getDir("images", Context.MODE_PRIVATE) ?: return
        val imageFile = File(assetsDirectory, "$name.jpg")

        try {
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(imageFile.toUri())?.use { stream ->
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
            } ?: return

            _cache[name] = processedBitmap
            _events.emit(ImageEvent.ImageSaved(name, processedBitmap))
        } catch (_: Throwable) {
            return
        }
    }

    suspend fun clearUnusedImages() {
        val unusedNames = names.toSet().minus(vehicleService.getAllVehicles().map { it.id }.toSet())

        val assetsDirectory = context.getDir("images", Context.MODE_PRIVATE) ?: return

        for (name in unusedNames) {
            try {
                val imageFile = File(assetsDirectory, "$name.jpg")
                if (imageFile.exists()) {
                    imageFile.delete()
                }
            } catch (_: Throwable) {
                continue
            }
        }
    }
}