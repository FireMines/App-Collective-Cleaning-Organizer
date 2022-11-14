package com.example.collectivecleaningorganizer

import android.annotation.SuppressLint
import android.util.Log
import com.example.collectivecleaningorganizer.ui.collective.ResultListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


var collectiveDocuments = mutableMapOf<String, QueryDocumentSnapshot>()
val userData = mutableListOf<DocumentSnapshot?>(null)
var userCollectiveData = mutableListOf<DocumentSnapshot?>(null)

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
    fun updateValueInDB(collection : String, documentID : String, field : String, updateValue : Any?, resultListener: ResultListener?) {
        db.collection(collection).document(documentID)
            .update(field,updateValue)
            .addOnSuccessListener {
                resultListener?.onResult(true)
                Log.d(tag,"Success in updating the field: $field in the path: $collection/$documentID")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failure in updating the field: $field in the path: $collection/$documentID")
            }
    }


    fun databaseDataChangeListener(collection:String, documentID:String, dataList:MutableList<DocumentSnapshot?>, resultListener: ResultListener?) {
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
                //Adding the DocumentSnapshot to the dataList
                dataList[0] = snapshot
                resultListener?.onResult(true)


            } else {
                Log.d("listener", "$source data: null")
            }
        }
    }


}