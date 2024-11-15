package com.anhcop.atvcontrol_employee.screens.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import com.anhcop.atvcontrol_employee.services.vehicle.Vehicle

@Immutable
data class VehicleEntry(
    val vehicle: Vehicle,
    val status: VehicleStatus,
    val image: ImageBitmap? = null,
)
