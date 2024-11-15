package com.anhcop.atvcontrol_employee.services.authorization

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import com.anhcop.atvcontrol_employee.services.utils.FirestoreFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthorizationService @Inject constructor(
    @ApplicationContext context: Context,
    firestoreFactory: FirestoreFactory
) {
    companion object {
        private const val FIRSTNAME = "firstname"
        private const val LASTNAME = "lastname"
        private const val DEVICE_IDENTIFIER = "device_identifier"
    }

    @SuppressLint("HardwareIds")
    val deviceIdentifier: String = Secure.getString(context.contentResolver, Secure.ANDROID_ID)

    private val _authorizationState = MutableStateFlow<AuthorizationState>(AuthorizationState.Initializing)
    val authorizationState = _authorizationState.asStateFlow()

    init {
        val firestore = firestoreFactory.create()

        firestore.collection("employees").whereEqualTo(DEVICE_IDENTIFIER, deviceIdentifier)
            .addSnapshotListener { snapshot, error ->
                if (snapshot == null || error != null) {
                    return@addSnapshotListener
                }

                val document = snapshot.documents.firstOrNull()

                if (document == null) {
                    _authorizationState.value = AuthorizationState.Unauthorized
                } else {
                    val id = document.id
                    val firstname = document.getString(FIRSTNAME)!!
                    val lastname = document.getString(LASTNAME)!!

                    _authorizationState.value = AuthorizationState.Authorized(id, firstname, lastname)
                }
            }
    }
}