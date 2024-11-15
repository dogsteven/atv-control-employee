package com.anhcop.atvcontrol_employee.services.vehicle

import com.anhcop.atvcontrol_employee.services.authorization.AuthorizationService
import com.anhcop.atvcontrol_employee.services.authorization.AuthorizationState
import com.anhcop.atvcontrol_employee.services.configuration.ConfigurationService
import com.anhcop.atvcontrol_employee.services.utils.FirestoreFactory
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VehicleService @Inject constructor(
    private val firestoreFactory: FirestoreFactory,
    private val authorizationService: AuthorizationService,
    private val configurationService: ConfigurationService
) {
    companion object {
        private const val NAME = "name"
        private const val LOCAL_IP = "local_ip"
        private const val PRICE = "price"
        private const val TIMESTAMP = "timestamp"
        private const val NUMBER_OF_TICKETS = "number_of_tickets"
        private const val SESSION_DURATION = "session_duration"
        private const val EMPLOYEE_NAME = "employee_name"

        private val client = HttpClient(Android) {
            engine {
                request {
                    timeout {
                        requestTimeoutMillis = 5000
                    }
                }
            }
        }
    }

    private val collection: CollectionReference
        get() = firestoreFactory.create().collection("vehicles")

    suspend fun getAllVehicles(): List<Vehicle> {
        return collection.get().await().mapNotNull { document ->
            val id = document.id
            val name = document.getString(NAME) ?: return@mapNotNull null
            val localIP = document.getString(LOCAL_IP) ?: return@mapNotNull null
            val price = document.getLong(PRICE) ?: return@mapNotNull null

            Vehicle(id, name, localIP, price)
        }
    }

    suspend fun checkAvailability(vehicle: Vehicle): Boolean {
        return withContext(Dispatchers.IO) {
            client.get {
                url {
                    protocol = URLProtocol.HTTP
                    host = vehicle.localIP
                    path("getStatus")
                }
            }.bodyAsText() == "idle"
        }
    }

    suspend fun startSession(vehicle: Vehicle, numberOfTickets: Long): StartSessionResult {
        val authorized = authorizationService.authorizationState.value as? AuthorizationState.Authorized ?: return StartSessionResult.Unauthorized

        val sessionDuration = configurationService.sessionDuration.value
        val sessionIsAboutToEndAlertDuration = configurationService.sessionIsAboutToEndAlertDuration.value

        val success = try {
            client.get {
                url {
                    protocol = URLProtocol.HTTP
                    host = vehicle.localIP
                    path("startSession")
                    parameter("span", numberOfTickets)
                    parameter("session_duration", sessionDuration)
                    parameter("session_is_about_to_end_alert_duration", sessionIsAboutToEndAlertDuration)
                }
            }.bodyAsText() == "successful"
        } catch (_: Throwable) {
            return StartSessionResult.ConnectionError
        }

        if (success) {
            collection.document(vehicle.id).collection("session_histories")
                .add(mapOf(
                    NUMBER_OF_TICKETS to numberOfTickets,
                    PRICE to vehicle.price,
                    SESSION_DURATION to sessionDuration,
                    EMPLOYEE_NAME to "${authorized.lastname} ${authorized.firstname}",
                    TIMESTAMP to Timestamp.now()
                ))

            return StartSessionResult.Successful
        } else {
            return StartSessionResult.Failed
        }
    }
}