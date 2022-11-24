package com.example.collectivecleaningorganizer

import android.util.Log
import com.example.collectivecleaningorganizer.ui.utilities.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


/**
 * A class used to hold all database related functions that is used.
 */
class Database {
    //Creating an singleton object to define and hold static variables
    companion object {
        //Initializing a mutable list used as a database cache for the user data
        val userData = mutableListOf<DocumentSnapshot?>(null)
        //Initializing a mutable list used as a database data cache for the collective data
        var userCollectiveData = mutableListOf<DocumentSnapshot?>(null)
        //Initializing a mutable map used to store the snapshot listener
        var listenerMap = mutableMapOf<String,ListenerRegistration>()
    }
    //Initializing the firestore database
    private val db = Firebase.firestore
    //Initializing a tag used for logging to know which file the log message came from
    private val tag = "Database"

    /**
     * This is a function that updates a database field value
     * @param collection is the name of the database collection we want to update
     * @param documentID is the ID of the document we want to update
     * @param field is the field in the document we want to update
     * @param updateValue is the value we are updating to
     * @param resultListener is the interface we use as a custom event listener
     */
    fun updateValueInDB(collection : String, documentID : String, field : String, updateValue : Any?, resultListener: ResultListener?) {
        //Sending the database request to update the value
        db.collection(collection).document(documentID)
            .update(field,updateValue)
            .addOnSuccessListener {
                //Triggering the interface resultListener's onSuccess() function
                resultListener?.onSuccess()
                Log.d(tag,"Success in updating the field: $field in the path: $collection/$documentID")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failure in updating the field: $field in the path: $collection/$documentID")
            }
    }

    /**
     * This is a function that retrieves database data and adds it to an List we use as a cache
     * @param collection is the name of the database collection the data is in
     * @param documentID is the ID of the document the data is in
     * @param dataList is the list we want to store the snapshot in
     * @param resultListener is the interface we use as a custom event listener
     */
    fun retrieveDataAndAddToCache(collection: String, documentID: String, dataList:MutableList<DocumentSnapshot?>, resultListener: ResultListener) {
        //Sending a database request to retrieve the data
        db.collection(collection).document(documentID).get()
            .addOnSuccessListener { data->
                //Adding the data to the list used as a cache
                dataList[0] = data
                //Triggering the interface resultListener's onSuccess() function
                resultListener.onSuccess()
            }
            .addOnFailureListener { e->
                //Triggering the interface resultListener's onFailure() function with the error exception
                resultListener.onFailure(e)
            }

    }

    /**
     * This is a function that checks if a username is unique by checking the database if the username exists
     * @param username is the username we are checking
     * @param uniqueUsernameListener is the interface we use as an listener to give a true/false value depending on if its an unique username or not
     */
    fun checkUniqueUsername(username: String, uniqueUsernameListener: UniqueUsernameListener?) {
        //Sending a database request to check if the username already exists in the database
        db.collection("usernames").document(username).get()
            .addOnSuccessListener {e->
                //Checking if the data is null, meaning the username is unique
                if (e.data == null) {
                    //Triggering the interface uniqueUsernameListener's onSuccess() function with true value
                    uniqueUsernameListener?.onSuccess(true)
                }
                else {
                    //Triggering the interface uniqueUsernameListener's onSuccess() function with false value
                    uniqueUsernameListener?.onSuccess(false)
                }
            }
            .addOnFailureListener {e->
                //Triggering the interface uniqueUsernameListener's onFailure() function with the error exception
                uniqueUsernameListener?.onFailure(e)
            }
    }

    /**
     * This is a function to retrieve a user's friend requests as a list from the database.
     * @param collection is the name of the database collection the data is in
     * @param documentID is the ID of the document the data is in
     * @param friendListListener is the interface we use as a custom event listener
     */
    fun getFriendRequestListFromDB(collection: String, documentID: String, friendListListener: FriendListListener?){
        //Sending a database request to retrieve the user's friend requests
        db.collection(collection).document(documentID).get()
            .addOnSuccessListener {e->
                //Checking if the friend requests data exists
                if(e.get("FriendRequests") != null) {
                    //Casting the friend requests data as an arraylist
                    val friendList: ArrayList<String> = e.get("FriendRequests") as ArrayList<String>
                    //Triggering the interface friendListListener's onSuccess() function and sending the friend request list along with it
                    friendListListener?.onSuccess(friendList)
                }
                else {
                    //Initializing an empty list
                    val list = arrayListOf<String>()
                    //Triggering the interface friendListListener's onSuccess() function and sending the empty list along with it
                    friendListListener?.onSuccess(list)
                }
            }
            .addOnFailureListener { e->
                //Triggering the interface friendListListener's onFailure() function with the error exception
                friendListListener?.onFailure(e)
            }

    }

    /**
     * This is a function that retrieves a user's Friends data as a list from the database
     * @param collection is the name of the database collection the data is in
     * @param documentID is the ID of the document the data is in
     * @param friendListListener is the interface we use as a custom event listener
     */
    fun getFriendsFromDB(collection: String, documentID: String, friendListListener: FriendListListener?){
        //Sending a database request to retrieve the user's friends
        db.collection(collection).document(documentID).get()
            .addOnSuccessListener {e->
                //Checking if the friends data exists
                if(e.get("Friends") != null) {
                    //Casting the friends data as an arraylist
                    val friendList: ArrayList<String> = e.get("Friends") as ArrayList<String>
                    //Triggering the interface friendListListener's onSuccess() function and sending the friends list along with it
                    friendListListener?.onSuccess(friendList)
                }
                else {
                    //Initializing an empty list
                    val list = arrayListOf<String>()
                    //Triggering the interface friendListListener's onSuccess() function and sending the empty list along with it
                    friendListListener?.onSuccess(list)
                }
            }
            .addOnFailureListener { e->
                //Triggering the interface friendListListener's onFailure() function with the error exception
                friendListListener?.onFailure(e)
            }
    }

    /**
     * This is a function that retrieves a username's userID from the database
     * @param username is the username we want to find the UID for
     * @param stringListener is the interface used as an custom event listener
     */
    fun getUid(username: String, stringListener: StringListener){
        //Sending a database request to retrieve the user id from the given username
        db.collection("usernames").document(username).get()
            .addOnSuccessListener {e->
                //Checking if the uid data for the given username exists
                if(e.get("uid") != null) {
                    //Initializing a variable for the user id
                    val uid = e.get("uid").toString()
                    //Triggering the interface stringListener's onSuccess() function and sending the user id along with it
                    stringListener?.onSuccess(uid)
                }
                else {
                    //Initializing an empty string
                    val empty = ""
                    //Triggering the interface stringListener's onSuccess() function and sending the the empty string along with it
                    stringListener?.onSuccess(empty)
                }
            }
            .addOnFailureListener { e->
                //Triggering the interface stringLister's onFailure() function with the error exception
                stringListener?.onFailure(e)
            }
    }

    /**
     * This is a function that adds data to the database
     * @param collection is the name of the database collection the data will be added to
     * @param documentID is the ID of the document the data will be added to
     * @param data is the data we want to add
     * @param resultListener is the interface used as an custom event listener
     */
    fun addToDB(collection: String, documentID: String, data: Any, resultListener: ResultListener) {
        //Sending a database request to to add the data to the database
        db.collection(collection).document(documentID).set(data)
            .addOnSuccessListener {
                //Triggering the interface resultListener's onSuccess() function
                resultListener.onSuccess()
            }
            .addOnFailureListener { e ->
                //Triggering the interface resultListener's onFailure() function with the error exception
                resultListener.onFailure(e)
            }
    }

    /**
     * A function that deletes/removes a document from the database
     * @param collection is the name of the database collection the document is in
     * @param documentID is the ID of the document we want to delete
     * @param resultListener is the interface used as an custom event listener
     */
    fun removeDocumentFromDB(collection: String, documentID: String, resultListener: ResultListener?) {
        //Sending a database request to to delete the document in the database
        db.collection(collection).document(documentID).delete()
            .addOnSuccessListener {
                //Triggering the interface resultListener's onSuccess() function
                resultListener?.onSuccess()
                Log.d(tag, "Successfully deleted the collection called $collection, with the document id: $documentID")
            }
            .addOnFailureListener { e->
                //Triggering the interface resultListener's onFailure() function with the error exception
                resultListener?.onFailure(e)
                Log.e(tag, "Failure to delete the collection called $collection, with the document id: $documentID", e)
            }
    }

    /**
     * A function that creates a snapshotlistener for a specific document in a specific collection.
     * This function is used to get realtime updates done to the userdata and collective data and update the cache accordingly
     * @param collection is the name of the database collection
     * @param documentID is the ID of the database document
     * @param dataList is the list we want to store the snapshot in
     */
    fun databaseDataChangeListener(collection:String, documentID:String, dataList:MutableList<DocumentSnapshot?>, listenerKey : String, resultListener: ResultListener?): MutableMap<String,ListenerRegistration>{
        //Creating the snapshot listener and adding it to a listenerMap so that we can remove it when needed
        listenerMap[listenerKey] = db.collection(collection).document(documentID)
            .addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(tag, "databaseDataChangeListener failed.", e)
                //Triggering the interface resultListener's onFailure() function with the error exception
                resultListener?.onFailure(e)
                return@addSnapshotListener
            }
            //Checking that the snapshot isnt invalid and that it exists
            if (snapshot != null && snapshot.exists()) {
                Log.d("listener", "data: ${snapshot.data}")
                //Adding the DocumentSnapshot to the dataList
                dataList[0] = snapshot
                //Triggering the interface resultListener's onSuccess() function
                resultListener?.onSuccess()
            }
        }

        return listenerMap
    }

    /**
     * A function to get data from the database.
     * @param collection is the name of the database collection
     * @param documentID is the ID of the database document
     * @param databaseRequestListener is the interface we use as an event listener
     */
    fun getDataFromDB(collection: String, documentID: String,databaseRequestListener: DatabaseRequestListener?){
        //Sending a database request to to retrieve the wanted data
        db.collection(collection).document(documentID).get()
            .addOnSuccessListener {e->
                //Triggering the interface databaseRequestListener's onSuccess() function with the data along with it
                databaseRequestListener?.onSuccess(e.data)
            }
            .addOnFailureListener { e->
                //Triggering the interface databaseRequestListener's onFailure() function with the error exception
                databaseRequestListener?.onFailure(e)
            }

    }
}