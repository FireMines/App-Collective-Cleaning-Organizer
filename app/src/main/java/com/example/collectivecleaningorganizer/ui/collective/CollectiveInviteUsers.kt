package com.example.collectivecleaningorganizer.ui.collective

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.DatabaseRequestListener
import com.example.collectivecleaningorganizer.ui.utilities.FriendListListener
import com.example.collectivecleaningorganizer.ui.utilities.StringListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_collective_invite_users.*
import java.lang.Exception
/**
 * This is an AppCompatActivity class for a CollectiveActivity.
 * It is used to create a page where collective members can invite users
 */
class CollectiveInviteUsers : AppCompatActivity() {
    //Initializing a tag used for logging to know which file the log message came from
    private val tag :String= "CollectiveInviteUsers"
    //Initializing the userID retrieved from the userData in the DB
    private val userID : String = Database.userData[0]?.id.toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting the content view of the activity
        setContentView(R.layout.activity_collective_invite_users)

        //Calling a function to check if the user is still supposed to be in the collective he/she is currently viewing
        Utilities().checkIfUserIsSupposedToBeInCollective(this)

        //Handles the applications navigation
        Utilities().navigation(this, R.id.collective, bottom_navigator_collective_invite)

        //An onclick listener for the back arrow
        backButton.setOnClickListener {
            this.finish()
        }

        //Calling the showFriends() function to populate the listview with user's friends
        showFriends()

    }

    /**
     * This is a function that populates the listview with the id "friendsListView" with friends
     */
    private fun showFriends() {
        try {
            //Sends a database request to retrieve a list of user's friends
            Database().getFriendsFromDB("users",userID, object : FriendListListener {
                @SuppressLint("SetTextI18n")
                /**
                 * This function is triggered when the database request to retrieve the friends list the is successful
                 * @param friendList is the retrieved friendslist
                 */
                override fun onSuccess(friendList: ArrayList<String>) {
                    //Checking if the friendlist is empty and handling it accordingly
                    if (friendList.isEmpty()) {
                        noFriendsTextView.text = "You have no friends to invite"
                        //Removing the listview used to display friends
                        friendsListView.visibility = View.GONE
                        //Removing the button used to invite friends
                        removeMembersButton.visibility = View.GONE
                        return
                    }
                    //Removing the textview used to display that the user doesn't have friends
                    noFriendsTextView.visibility = View.GONE
                    //Creating an arrayadapter with the friendslist
                    val arrayAdapter = ArrayAdapter<String>(this@CollectiveInviteUsers,android.R.layout.simple_list_item_multiple_choice,friendList)
                    //Attaching the adapter to the friends listView
                    friendsListView.adapter = arrayAdapter
                }
                /**
                 * This function is triggered when the database request to retrieve the friends list is a failure
                 * @param error returns the error exception
                 */
                override fun onFailure(error: Exception) {
                    Log.e(tag, "Database failure to retrieve user: $userID's friends")
                }
            })
        }
        catch (error : Exception) {
            Log.e(tag, "Error when trying to run the showFriends() function",error)
        }
    }

    /**
     * A function that invites a person by using the friendlist and the friends the user selected
     * This function triggers when the user presses the "Invite selected friends" button
     * @param view is the "Invite selected friends" button
     */
    fun inviteFromFriendsList(view : View) {
        try {
            //Initializing an counter
            var amountOfSelectedFriends = 0
            //Iterating through the friendsListView and checking which friend got selected
            for (i:Int in 0 until friendsListView.count) {
                //Statement checking if the item's check box is checked
                if (friendsListView.isItemChecked(i)) {
                    //Initializing the friend's name retrieved from the friendsListView row
                    val friendUsername :String = friendsListView.getItemAtPosition(i).toString()

                    //Incrementing the counter by 1
                    amountOfSelectedFriends = amountOfSelectedFriends.inc()

                    //Calling the sendInviteUsers() to attempt to send a invite request to join the collective
                    sendInviteUsers(friendUsername)
                }
            }
            if (amountOfSelectedFriends ==0) {
                Toast.makeText(this@CollectiveInviteUsers, "Please select atleast 1 member", Toast.LENGTH_SHORT).show()
                return
            }
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to invite members from the friendliest. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the inviteFromFriendsList() function",error)
        }
    }

    /**
     * A function that invites a person by using the given username the user entered
     * @param view is the button named "Invite" which calls this function when clicked
     */
    fun inviteUserByUsername(view: View) {
        try {
            //Initializing a variable for the entered username
            val username : String = username.text.toString()

            //Checking if the entered username is invalid
            if (username.isBlank()) {
                Toast.makeText(this@CollectiveInviteUsers, "Please enter a username"
                    , Toast.LENGTH_SHORT).show()
                return
            }
            //Calling the sendInviteUsers() to attempt to send a invite request to join the collective
            sendInviteUsers(username)
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to invite members by username. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the inviteUserByUsername() function",error)
        }
    }

    /**
     * A function to attempt to send a user an invite to join a collective
     * @param username is the username of the person we want to invite
     */
    private fun sendInviteUsers(username : String) {
        try {
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
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to send an invite to the user. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the sendInviteUsers() function",error)
        }
    }
}