package com.anhcop.atvcontrol_employee.services.vehicle

import androidx.compose.runtime.Immutable

@Immutable
data class Vehicle(
    val id: String,
    val name: String,
    val localIP: String,
    val price: Long
)
