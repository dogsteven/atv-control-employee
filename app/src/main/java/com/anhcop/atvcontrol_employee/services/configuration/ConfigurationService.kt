package com.anhcop.atvcontrol_employee.services.configuration

import com.anhcop.atvcontrol_employee.services.utils.FirestoreFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ConfigurationService @Inject constructor(
    private val firestoreFactory: FirestoreFactory
) {
    private val _sessionDuration = MutableStateFlow(20L)
    val sessionDuration = _sessionDuration.asStateFlow()

    private val _sessionIsAboutToEndAlertDuration = MutableStateFlow(5L)
    val sessionIsAboutToEndAlertDuration = _sessionIsAboutToEndAlertDuration.asStateFlow()

    init {
        val firestore = firestoreFactory.create()

        firestore.collection("configurations").document("session_duration")
            .addSnapshotListener { document, error ->
                if (document == null || error != null) {
                    return@addSnapshotListener
                }

                _sessionDuration.value = document.getLong("value") ?: return@addSnapshotListener
            }

        firestore.collection("configurations").document("session_is_about_to_end_duration")
            .addSnapshotListener { document, error ->
                if (document == null || error != null) {
                    return@addSnapshotListener
                }

                _sessionIsAboutToEndAlertDuration.value = document.getLong("value") ?: return@addSnapshotListener
            }
    }
}