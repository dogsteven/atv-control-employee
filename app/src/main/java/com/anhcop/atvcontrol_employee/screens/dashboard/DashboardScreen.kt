@file:OptIn(ExperimentalFoundationApi::class)

package com.anhcop.atvcontrol_employee.screens.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.anhcop.atvcontrol_employee.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val viewModel = hiltViewModel<DashboardViewModel>()

    val initializationStatus = viewModel.initializationStatus

    val vehicleEntries = viewModel.vehicleEntries

    val startSessionForm = viewModel.startSessionForm
    val isStartSessionFormOpen = viewModel.isStartSessionFormOpen
    val isSubmittingStartSessionForm = viewModel.isSubmittingStartSessionForm

    if (isStartSessionFormOpen) {
        Dialog(
            onDismissRequest = viewModel::closeStartSessionForm
        ) {
            ElevatedCard(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(6.dp)
            ) {
                var price by rememberSaveable { mutableLongStateOf(0L) }

                LaunchedEffect(startSessionForm.index) {
                    if (startSessionForm.index != -1) {
                        price = vehicleEntries[startSessionForm.index].vehicle.price
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Chọn số lượng vé",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(12.dp)
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.chooseNumberOfTickets(1L)
                        },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = startSessionForm.numberOfTickets == 1L,
                            onClick = {
                                viewModel.chooseNumberOfTickets(1L)
                            }
                        )

                        Text(
                            text = "1 vé (tổng $price VND)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.chooseNumberOfTickets(2L)
                        },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = startSessionForm.numberOfTickets == 2L,
                            onClick = {
                                viewModel.chooseNumberOfTickets(2L)
                            }
                        )

                        Text(
                            text = "2 vé (tổng ${2 * price} VND)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Row {
                        Button(
                            onClick = viewModel::submitStartSessionForm,
                            enabled = !isSubmittingStartSessionForm
                        ) {
                            Text(
                                text = "Xác nhận"
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (initializationStatus == InitializationStatus.Successful) {
                TopAppBar(
                    title = {
                        Text(text = "Bảng điều khiển")
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::fetchStatus
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_refresh_24),
                    contentDescription = null
                )
            }
        }
    ) { innerPaddings ->
        AnimatedVisibility(
            visible = initializationStatus == InitializationStatus.Uninitialized,
            modifier = Modifier.fillMaxSize().padding(innerPaddings)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        AnimatedVisibility(
            visible = initializationStatus == InitializationStatus.Failed,
            modifier = Modifier.fillMaxSize().padding(innerPaddings)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Đã có lỗi không xác định xảy ra",
                )
            }
        }

        AnimatedVisibility(
            visible = initializationStatus == InitializationStatus.Successful,
            modifier = Modifier.fillMaxSize().padding(innerPaddings)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(vehicleEntries.size, { index -> vehicleEntries[index].vehicle.id }) { index ->
                    val vehicleEntry = vehicleEntries[index]

                    VehicleEntryCard(
                        vehicleEntry = vehicleEntry,
                        fetchStatus = {
                            viewModel.fetchStatus(index)
                        },
                        uploadImage = { bitmap ->
                            viewModel.uploadImage(index, bitmap)
                        },
                        openStartSessionForm = {
                            viewModel.openStartSessionForm(index)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleEntryCard(
    vehicleEntry: VehicleEntry,
    fetchStatus: () -> Unit,
    uploadImage: (Bitmap) -> Unit,
    openStartSessionForm: () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        context.contentResolver.openInputStream(uri).use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream) ?: return@use

            uploadImage(bitmap)
        }
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 12.dp),
    ) {
        Box(
            modifier = Modifier.clickable(
                enabled = vehicleEntry.status == VehicleStatus.Idle || vehicleEntry.status == VehicleStatus.Running || vehicleEntry.status == VehicleStatus.Disconnected
            ) {
                when (vehicleEntry.status) {
                    VehicleStatus.Fresh, VehicleStatus.Fetching,  -> {}
                    VehicleStatus.Idle -> {
                        openStartSessionForm()
                    }
                    VehicleStatus.Running, VehicleStatus.Disconnected -> {
                        fetchStatus()
                    }
                }
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp, 64.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val image = vehicleEntry.image

                    if (image == null) {
                        Text(
                            text = vehicleEntry.vehicle.name.first().uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Image(
                            bitmap = image,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp, 64.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = vehicleEntry.vehicle.name,
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Text(
                        text = "Giá mỗi vé: ${vehicleEntry.vehicle.price} VND",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.padding(8.dp).size(24.dp)
                ) {
                    when (vehicleEntry.status) {
                        VehicleStatus.Fresh, VehicleStatus.Fetching -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        VehicleStatus.Idle -> {
                            Icon(
                                painter = painterResource(R.drawable.rounded_play_circle_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        VehicleStatus.Running -> {
                            Icon(
                                painter = painterResource(R.drawable.rounded_lock_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        VehicleStatus.Disconnected -> {
                            Icon(
                                painter = painterResource(R.drawable.rounded_wifi_off_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}