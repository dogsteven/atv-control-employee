package com.anhcop.atvcontrol_employee.services.image

import android.graphics.Bitmap

sealed interface ImageEvent {
    data class ImageSaved(val identifier: String, val bitmap: Bitmap): ImageEvent
}