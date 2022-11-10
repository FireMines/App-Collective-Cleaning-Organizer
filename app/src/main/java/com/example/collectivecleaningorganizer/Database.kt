package com.example.collectivecleaningorganizer

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

var userData = mutableMapOf<String, DocumentSnapshot>()
var collectiveDocuments = mutableMapOf<String, QueryDocumentSnapshot>()

class Database {
    private val db = Firebase.firestore
    private val tag = "Database"
    fun getAllCollectivesFromDB() {
        db.collection("collective").get()
            .addOnSuccessListener { documents ->
                Log.d(tag,"Success in retrieving all collective data from DB")
                for (document in documents) {
                    collectiveDocuments[document.id] = document
                }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error retrieving all collective data from DB", e)
            }
    }

    @SuppressLint("LongLogTag")
    fun updateUserData(userID:String) {
        db.collection("usersExample").document(userID)
            .get()
            .addOnSuccessListener { document ->
                Log.d(tag, "Success in retrieving user data from DB")
                userData[document.id] = document
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error retrieving user data from DB", e)
            }
    }

}