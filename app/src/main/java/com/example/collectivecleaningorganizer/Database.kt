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



    fun databaseDataChangeListener(collection:String, documentID:String, map:MutableMap<String,DocumentSnapshot>) {
        db.collection(collection).document(documentID)
            .addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("listener", "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                "Local"
            else
                "Server"

            if (snapshot != null && snapshot.exists()) {
                Log.d("listener", "$source data: ${snapshot.data}")
                //userData[userID] = snapshot
                map[documentID] = snapshot
                println(userData[documentID]?.data)
            } else {
                Log.d("listener", "$source data: null")
            }
        }
    }


}