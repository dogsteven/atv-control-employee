package com.anhcop.atvcontrol_employee.screens.dashboard

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhcop.atvcontrol_employee.services.image.ImageEvent
import com.anhcop.atvcontrol_employee.services.image.ImageManager
import com.anhcop.atvcontrol_employee.services.vehicle.StartSessionResult
import com.anhcop.atvcontrol_employee.services.vehicle.VehicleService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vehicleService: VehicleService,
    private val imageManager: ImageManager
): ViewModel() {
    private val _vehicleEntries = mutableStateListOf<VehicleEntry>()
    val vehicleEntries: List<VehicleEntry> = _vehicleEntries

    private lateinit var listenToAssetsEventJob: Job

    private val _initializationStatus = mutableStateOf(InitializationStatus.Uninitialized)
    val initializationStatus by _initializationStatus

    private val _startSessionForm = mutableStateOf(StartSessionForm())
    val startSessionForm by _startSessionForm

    val isStartSessionFormOpen: Boolean
        get() = startSessionForm.index != -1

    private val _isSubmittingStartSessionForm = mutableStateOf(false)
    val isSubmittingStartSessionForm by _isSubmittingStartSessionForm

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            try {

                _vehicleEntries.addAll(
                    vehicleService.getAllVehicles().map { vehicle ->
                        VehicleEntry(vehicle, VehicleStatus.Fresh, imageManager.get(vehicle.id)?.asImageBitmap())
                    }.sortedBy { it.vehicle.name }
                )

                listenToAssetsEventJob = imageManager.events.onEach { event ->
                    when (event) {
                        is ImageEvent.ImageSaved -> {
                            val index = _vehicleEntries.indexOfFirst { it.vehicle.id == event.identifier }

                            if (index != -1) {
                                _vehicleEntries[index] = _vehicleEntries[index].copy(image = event.bitmap.asImageBitmap())
                            }
                        }
                    }
                }.launchIn(viewModelScope)

                launch {
                    withContext(Dispatchers.Default) {
                        imageManager.clearUnusedImages()
                    }
                }

                _initializationStatus.value = InitializationStatus.Successful

                fetchStatus()
            } catch (_: Throwable) {
                _initializationStatus.value = InitializationStatus.Failed
            }
        }
    }

    fun fetchStatus(index: Int) {
        if (index < 0 || index >= _vehicleEntries.size) {
            return
        }

        val vehicleEntry = _vehicleEntries[index]

        if (vehicleEntry.status == VehicleStatus.Fetching) {
            return
        }

        viewModelScope.launch {
            _vehicleEntries[index] = _vehicleEntries[index].copy(status = VehicleStatus.Fetching)

            try {
                val isIdle = withContext(Dispatchers.IO) {
                    vehicleService.checkAvailability(vehicleEntry.vehicle)
                }

                _vehicleEntries[index] = _vehicleEntries[index].copy(status = if (isIdle) VehicleStatus.Idle else VehicleStatus.Running)
            } catch (_: Throwable) {
                _vehicleEntries[index] = _vehicleEntries[index].copy(status = VehicleStatus.Disconnected)
            }
        }
    }

    fun fetchStatus() {
        for (index in _vehicleEntries.indices) {
            fetchStatus(index)
        }
    }

    fun uploadImage(index: Int, bitmap: Bitmap) {
        if (index < 0 || index >= _vehicleEntries.size) {
            return
        }

        val vehicle = _vehicleEntries[index].vehicle

        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                imageManager.save(vehicle.id, bitmap)
            }
        }
    }

    fun openStartSessionForm(index: Int) {
        if (_startSessionForm.value.index != -1) {
            return
        }

        if (index < 0 || index >= _vehicleEntries.size) {
            return
        }

        val status = _vehicleEntries[index].status

        if (status != VehicleStatus.Idle) {
            return
        }

        _startSessionForm.value = StartSessionForm(index = index)
    }

    fun closeStartSessionForm() {
        if (_startSessionForm.value.index == -1) {
            return
        }

        _startSessionForm.value = StartSessionForm()
    }

    fun chooseNumberOfTickets(numberOfTickets: Long) {
        if (_startSessionForm.value.index == -1) {
            return
        }

        if (numberOfTickets < 1 || numberOfTickets > 2) {
            return
        }

        _startSessionForm.value = _startSessionForm.value.copy(numberOfTickets = numberOfTickets)
    }

    fun submitStartSessionForm() {
        if (_startSessionForm.value.index == -1) {
            return
        }

        if (_isSubmittingStartSessionForm.value) {
            return
        }

        _isSubmittingStartSessionForm.value = true

        val (index, numberOfTickets) = _startSessionForm.value

        viewModelScope.launch {
            val vehicle = _vehicleEntries[index].vehicle

            val startSessionResult = withContext(Dispatchers.IO) {
                vehicleService.startSession(vehicle, numberOfTickets)
            }

            when (startSessionResult) {
                StartSessionResult.ConnectionError -> {
                    _vehicleEntries[index] = _vehicleEntries[index].copy(status = VehicleStatus.Disconnected)
                }

                StartSessionResult.Failed -> {
                    _vehicleEntries[index] = _vehicleEntries[index].copy(status = VehicleStatus.Running)
                }

                StartSessionResult.Successful -> {
                    _vehicleEntries[index] = _vehicleEntries[index].copy(status = VehicleStatus.Running)
                }

                StartSessionResult.Unauthorized -> {}
            }

            _isSubmittingStartSessionForm.value = false
            _startSessionForm.value = StartSessionForm()
        }
    }
}