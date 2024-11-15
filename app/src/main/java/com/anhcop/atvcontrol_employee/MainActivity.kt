package com.anhcop.atvcontrol_employee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anhcop.atvcontrol_employee.screens.dashboard.DashboardScreen
import com.anhcop.atvcontrol_employee.services.authorization.AuthorizationComposable
import com.anhcop.atvcontrol_employee.services.authorization.AuthorizationService
import com.anhcop.atvcontrol_employee.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authorizationService: AuthorizationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            AppTheme {
                AuthorizationComposable(authorizationService = authorizationService) {
                    NavHost(navController = navController, startDestination = DashboardRoute) {
                        composable<DashboardRoute> {
                            DashboardScreen()
                        }
                    }
                }
            }
        }
    }
}