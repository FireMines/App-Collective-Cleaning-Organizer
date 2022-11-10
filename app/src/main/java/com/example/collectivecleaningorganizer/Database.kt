package com.example.collectivecleaningorganizer

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

var userData = mutableMapOf<String, DocumentSnapshot>()
var collectiveDocuments = mutableMapOf<String, QueryDocumentSnapshot>()

class database {
   private val db = Firebase.firestore
    fun getAllCollectivesFromDB() {
        db.collection("collective").get().addOnSuccessListener { documents ->
            for (document in documents) {
                //println(document)
                collectiveDocuments[document.id] = document
            }
        }
    }

    @SuppressLint("LongLogTag")
    fun updateUserData(userID:String) {
        db.collection("usersExample").document(userID)
            .get()
            .addOnSuccessListener { document ->
                //Log.d(tag, "Success in getting user's document data")
                userData[document.id] = document
            }
            .addOnFailureListener { e ->
                //Log.e(tag, "Error retrieving user's document data", e)
            }
    }

}