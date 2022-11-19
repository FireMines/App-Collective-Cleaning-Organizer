package com.example.collectivecleaningorganizer.ui.collective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.DatabaseRequestListener
import com.example.collectivecleaningorganizer.ui.utilities.StringListener
import kotlinx.android.synthetic.main.activity_collective_invite_users.*
import java.lang.Exception
/**
 * This is an AppCompatActivity class for a CollectiveActivity.
 * It is used to create a page where collective members can invite users
 */
class CollectiveInviteUsers : AppCompatActivity() {
    //Initializing a tag used for logging to know which file the log message came from
    private val tag :String= "CollectiveInviteUsers"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting the content view of the activity
        setContentView(R.layout.activity_collective_invite_users)
    }

    /**
     * A function that sends a user an invite request to join the collective. This invite is sent if the user enters a valid username to send the invite to
     * @param view is the button named "Invite" which calls this function when clicked
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
        val collectiveID : String = Database.userCollectiveData[0]?.id.toString()

        //Sending a database request to get the userID of the given username
        Database().getUid(username, object:StringListener {
            /**
             * This function is triggered when the database request to get the UID from the given username is successful
             * @param uId is the user ID of the given username
             */
            override fun onSuccess(uId: String) {
                //Checking if the given username doesn't exist
                if (uId == "" ) {
                    //Sending an error message to the user
                    Toast.makeText(this@CollectiveInviteUsers, "There is no user with that username"
                        , Toast.LENGTH_SHORT).show()
                    Log.d(tag, "The entered username doesn't exist")
                    return
                }

                /*
                Calling a DB request on the "usernames" collection and on the document id of the entered username
                to retrieve the userdata of the given username
                 */
                Database().getDataFromDB("users", uId, object :DatabaseRequestListener {
                    /**
                     * This is a function that is triggered when the database request is successful in retrieving the userData
                     * of the given username
                     * @param data is the database data that is retrieved from the database request.
                     */
                    override fun onSuccess(data: MutableMap<String, Any?>?) {
                        //Checking if the user they want to add is apart of a collective already
                        if (data?.get("collectiveID") != null) {
                            //Sending an error message to the user
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

                        //Sending a database request to update the collectiveRequests data with the updated collectiveRequests data
                        Database().updateValueInDB("users", uId, "collectiveRequests",collectiveRequestsData, null)

                        //Sending a success msg to user if invite is successful
                        Toast.makeText(this@CollectiveInviteUsers, "Success in inviting $username to the collective", Toast.LENGTH_LONG).show()
                    }
                    /**
                     * This function that is triggered when the database request failed when
                     * trying to retrieve the user data of the given username in the "users" collection
                     * @param error is the error exception given
                     */
                    override fun onFailure(error: Exception) {
                        //Sending an error message to the user
                        Toast.makeText(this@CollectiveInviteUsers, "Failure to retrieve data from database. Error: $error"
                            , Toast.LENGTH_SHORT).show()
                        Log.e(tag, "Failure to get the userData from the given username", error)
                        return
                    }
                })

            }
            /**
             * This function is triggered when the database request to get the UID from the given username is a failure
             * @param error returns the error exception
             */
            override fun onFailure(error: Exception) {
                Log.e(tag, "Database request failure", error)
            }
        })
    }
}