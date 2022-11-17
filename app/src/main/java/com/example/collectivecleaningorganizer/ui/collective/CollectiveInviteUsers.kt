package com.example.collectivecleaningorganizer.ui.collective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.userCollectiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_collective_invite_users.*
import java.lang.Exception

class CollectiveInviteUsers : AppCompatActivity() {
    private val db = Firebase.firestore
    private val tag :String= "CollectiveInviteUsers"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collective_invite_users)

    }

    /**
     * A function that sends a join collective request to the username the user entered
     * @param view is the button named "Add" which calls this function when clicked
     */
    fun inviteUserByUsername(view: View) {
        //Initalizing a variable for the entered username
        val username : String = username.text.toString()
        //Checking if the entered username is invalid
        if (username.isBlank()) {
            Toast.makeText(this@CollectiveInviteUsers, "Please enter a username"
                , Toast.LENGTH_SHORT).show()
            return
        }
        //Initializing a variable with the collective ID
        val collectiveID : String = userCollectiveData[0]?.id.toString()
        //Calling a DB request on the "usernames" collection and on the document id of the entered username
        getDataFromDB("usernames", username, object : DatabaseRequestListener {
            /**
             * A function that is triggered when the database request is successful in retrieving the username data
             * from the collection "usernames"
             * @param data is the database data that is retrieved from the database request.
             * This data will contain the uid of the username if the username exists
             *
             */
            override fun onSuccess(data: MutableMap<String, Any?>?) {
                //Checking if the given username doesn't exist
                if (data == null) {
                    Log.d(tag, "The entered username doesn't exist")
                    return
                }
                //Checking if the username has an invalid uid field value
                else if (data["uid"] == "" || data["uid"] == null) {
                    Toast.makeText(this@CollectiveInviteUsers, "ERROR: cannot find a valid userid"
                        , Toast.LENGTH_SHORT).show()
                    Log.e(tag, "UserID is empty or a null value")
                    return
                }//Else retrieve the userdata of the given username
                else {
                    //Initializing a variable with the uid of the user that is getting invited
                    val userToInviteUID : String= data["uid"].toString()

                    /*
                    Calling a DB request on the "usernames" collection and on the document id of the entered username
                    to retrieve the userdata of the given username
                     */
                    getDataFromDB("users", userToInviteUID, object :DatabaseRequestListener {
                        /**
                         * This is a function that is triggered when the database request is successful in retrieving the userData
                         * of the given username
                         * @param data is the database data that is retrieved from the database request.
                         */
                        override fun onSuccess(data: MutableMap<String, Any?>?) {
                            //Checking if the user they want to add is apart of a collective already
                            if (data?.get("collectiveID") != null) {
                                Toast.makeText(this@CollectiveInviteUsers, "Failure to add member. Reason: User is already apart of a collective"
                                    , Toast.LENGTH_LONG).show()
                                return
                            }
                            //Creating an arraylist for the collective requests
                            var collectiveRequestsData  : ArrayList<String>

                            //Checking if the collectiveRequests data dosnt exists
                            if (data?.get("collectiveRequests") == null) {
                                //Initializing the collectiveRequestsData with an empty arraylist
                                collectiveRequestsData = arrayListOf()
                            }
                            else {
                                //Initializing the collectiveRequestsData with the data found in the DB
                                collectiveRequestsData  = data["collectiveRequests"] as ArrayList<String>
                            }

                            //Checking if the user has already been invited to the collective
                            if (collectiveRequestsData.contains(collectiveID)) {
                                Toast.makeText(this@CollectiveInviteUsers, "Failure to add member. Reason: User has already been invited"
                                    , Toast.LENGTH_SHORT).show()
                                return
                            }
                            //Adding the collectiveID to the arraylist
                            collectiveRequestsData.add(collectiveID)

                            //Updating the collectiveRequests data with the updated collectiveRequests data
                            Database().updateValueInDB("users", userToInviteUID, "collectiveRequests",collectiveRequestsData, null)


                        }
                        /**
                         * This function that is triggered when the database request failed when
                         * trying to retrieve the user data of the given username in the "users" collection
                         * @param error is the error exception given
                         */
                        override fun onFailure(error: Exception) {
                            Toast.makeText(this@CollectiveInviteUsers, "Failure to retrieve data from database. Error: $error"
                                , Toast.LENGTH_SHORT).show()
                            Log.e(tag, "Failure to get the userData from the given username", error)
                            return
                        }

                    })
                }
            }
            /**
             * This function that is triggered when the database request failed when
             * trying to retrieve username data in the "usernames" collection
             * @param error is the error exception given
             */
            override fun onFailure(error: Exception) {
                Toast.makeText(this@CollectiveInviteUsers, "Failure to retrieve data from database. Error: $error"
                    , Toast.LENGTH_SHORT).show()
                return
            }

        })

    }
    interface DatabaseRequestListener {
        fun onSuccess(data : MutableMap<String,Any?>?)
        fun onFailure(error :Exception)
    }
    fun getDataFromDB(collection: String, documentID: String,databaseRequestListener: DatabaseRequestListener?){
        db.collection(collection).document(documentID).get()
            .addOnSuccessListener {e->


                databaseRequestListener?.onSuccess(e.data)
            }
            .addOnFailureListener { e->
                databaseRequestListener?.onFailure(e)
            }

    }
}