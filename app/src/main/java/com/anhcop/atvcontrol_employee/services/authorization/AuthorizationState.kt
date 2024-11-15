package com.anhcop.atvcontrol_employee.services.authorization

sealed interface AuthorizationState {
    data object Initializing: AuthorizationState
    data object Unauthorized: AuthorizationState
    data class Authorized(
        val id: String,
        val firstname: String,
        val lastname: String
    ): AuthorizationState
}