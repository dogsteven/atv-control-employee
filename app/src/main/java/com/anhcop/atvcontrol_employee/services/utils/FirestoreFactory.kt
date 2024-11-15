package com.anhcop.atvcontrol_employee.services.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class FirestoreFactory @Inject constructor() {
    fun create(): FirebaseFirestore = Firebase.firestore
}