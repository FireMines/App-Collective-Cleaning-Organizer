package com.example.collectivecleaningorganizer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import com.example.collectivecleaningorganizer.ui.utilities.FriendListListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.UniqueUsernameListener

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase


var collectiveDocuments = mutableMapOf<String, QueryDocumentSnapshot>()
val userData = mutableListOf<DocumentSnapshot?>(null)
var userCollectiveData = mutableListOf<DocumentSnapshot?>(null)
var listenerMap = mutableMapOf<String,ListenerRegistration>()

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
                resultListener?.onSuccess()
                Log.d(tag,"Success in updating the field: $field in the path: $collection/$documentID")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failure in updating the field: $field in the path: $collection/$documentID")
            }
    }
    fun retrieveDataAndAddToCache(collection: String, documentID: String, dataList:MutableList<DocumentSnapshot?>, resultListener: ResultListener) {
        db.collection(collection).document(documentID).get()
            .addOnSuccessListener { data->
                dataList[0] = data
                resultListener.onSuccess()
            }
            .addOnFailureListener { e->
                resultListener.onFailure(e)
            }

    }
    fun checkUniqueUsername(username: String, uniqueUsernameListener: UniqueUsernameListener?) {
        db.collection("usernames").document(username).get()
            .addOnSuccessListener {e->
                if (e.data == null) {
                    uniqueUsernameListener?.onSuccess(true)
                }
                else {
                    uniqueUsernameListener?.onSuccess(false)
                }
            }
            .addOnFailureListener {e->
                uniqueUsernameListener?.onFailure(e)
            }
    }

    fun getFriendRequestListFromDB(collection: String, documentID: String, friendListListener: FriendListListener?){
        db.collection(collection).document(documentID).get()
            .addOnSuccessListener {e->
                val friendList : ArrayList<String> = e.get("FriendRequests") as ArrayList<String>
                friendListListener?.onSuccess(friendList)
            }
            .addOnFailureListener { e->
                friendListListener?.onFailure(e)
            }

    }
    fun addToDB(collection: String, documentID: String, data: Any, resultListener: ResultListener) {
        db.collection(collection).document(documentID).set(data)
            .addOnSuccessListener {
                resultListener.onSuccess()
            }
            .addOnFailureListener { e ->
                resultListener.onFailure(e)
            }
    }

    fun databaseDataChangeListener(collection:String, documentID:String, dataList:MutableList<DocumentSnapshot?>, listenerKey : String, resultListener: ResultListener?): MutableMap<String,ListenerRegistration>{
        listenerMap[listenerKey] = db.collection(collection).document(documentID)
            .addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("listener", "Listen failed.", e)
                resultListener?.onFailure(e)
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
                resultListener?.onSuccess()


            } else {
                Log.d("listener", "$source data: null")
            }
        }

        return listenerMap
    }

}