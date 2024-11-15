package com.anhcop.atvcontrol_employee.services.authorization

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthorizationComposable(
    authorizationService: AuthorizationService,
    content: @Composable () -> Unit
) {
    val authorizationState by authorizationService.authorizationState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    AnimatedVisibility(
        visible = authorizationState == AuthorizationState.Initializing,
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
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
    }

    AnimatedVisibility(
        visible = authorizationState == AuthorizationState.Unauthorized,
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Thiết bị của bạn không có quyền để sử dụng ứng dụng này, vui lòng liên hệ quản trị viên để được cấp quyền sử dụng.",
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Mã thiết bị: ${authorizationService.deviceIdentifier}",
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return@TextButton

                        val clip = ClipData.newPlainText("Device Identifier", authorizationService.deviceIdentifier)
                        clipboard.setPrimaryClip(clip)
                    }
                ) {
                    Text(
                        text = "Sao chép"
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = authorizationState is AuthorizationState.Authorized,
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}