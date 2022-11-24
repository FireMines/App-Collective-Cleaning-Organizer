package com.example.collectivecleaningorganizer.ui.collective

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.collectivecleaningorganizer.*
import com.example.collectivecleaningorganizer.database.Database
import com.example.collectivecleaningorganizer.ui.utilities.DatabaseRequestListener
import com.example.collectivecleaningorganizer.ui.utilities.OnDataChange
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_specific_collective.*
import java.lang.Exception
/**
 * This is an AppCompatActivity class for a SpecifricCollectiveActivity.
 * It is used to create a collective page that shows detailed information about the collective the user is in
 */
class SpecificCollectiveActivity : AppCompatActivity() {
    //Initializing a tag used for logging to know which file the log message came from
    private val tag = "SpecificCollectiveActivity"
    //Initializing the collective members data retrieved from the collective data in the DB
    private var collectiveMembersMap : MutableMap<String,String> = Database.userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>
    //Initializing the collective id retrieved from the collective data in the DB
    private val collectiveID : String = Database.userCollectiveData[0]?.id.toString()
    //Initializing the user id retrieved from the userData in the DB
    private val userID = Database.userData[0]?.id.toString()
    //Initializing the username retrieved from the userData in the DB
    private val username = Database.userData[0]?.get("username").toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting the content view of the activity
        setContentView(R.layout.activity_specific_collective)

        //Calling a function to check if the user is still supposed to be in the collective he/she is currently viewing
        Utilities().checkIfUserIsSupposedToBeInCollective(this)

        //Sending a database request to retrieve the latest collective information
        Database().getDataFromDB("collective", collectiveID, object : DatabaseRequestListener {
            @SuppressLint("LongLogTag")
            override fun onSuccess(data: MutableMap<String, Any?>?) {
                /**
                 * This function is triggered when the database request to retrieve the latest collective data is successful
                 * @param data returns the data in the form of a mutable map with the key being a string and the value being Any?
                 */
                //Checking if the collective data retrieved from the database request doesn't exist or is invalid and handling it accordingly
                if (data == null) {
                    Log.e(tag, "Could not find any collective data for the collective id: $collectiveID")
                    return
                }
                Log.d(tag, "Successfully retrieved collective data")
                //Updating the collectiveMembersMap used as a cache with the newest data
                collectiveMembersMap = data?.get("members") as MutableMap<String, String>

                //Calling the showDataToScreen() function to show the collective information on the screen/layout
                showDataToScreen()
            }

            @SuppressLint("LongLogTag")
            override fun onFailure(error: Exception) {
                /**
                 * This function is triggered when the database request to retrieve the latest collective data is a failure
                 * @param error returns the error exception
                 */
                Log.e(tag, "Failure to retrieve collective data from db", error)
            }

        })

        //An onclick listener for the "Invite members" button
        inviteMemberButton.setOnClickListener {
            //Starting the activity called "CollectiveInviteUsers"
            startActivity(Intent(this, CollectiveInviteUsers::class.java))
            //Finishing the current Activity
            this.finish()
        }
        //An onclick listener for the "Remove members" button
        removeMemberButton.setOnClickListener {
            //Checking if there is only one member in the collective and handling it accordingly
            if (collectiveMembersMap.size == 1) {
                Toast.makeText(this, "There are no members to remove" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Starting the activity called "CollectiveRemoveMembers"
            startActivity(Intent(this, CollectiveRemoveMembers::class.java))
            //Finishing the current Activity
            this.finish()
        }

        //Handles the applications navigation
        Utilities().navigation(this, R.id.collective, bottom_navigator_specific_collective)
    }




    /**
     * A function that sets the collective data to the contents of the layout
     */
    @SuppressLint("LongLogTag")
    fun showDataToScreen() {
        try {
            //Initializing a mutable list with roles
            val roleList : MutableList<String> = mutableListOf<String>("Owner","Member")
            //Initializing the collective name retrieved from the collective data in the DB
            val collectiveName : String = Database.userCollectiveData[0]?.data?.get("name").toString()

            //Initializing a variable to check if the user is an owner
            var isUserAnOwner : Boolean = checkIfUserIsAnOwner(collectiveMembersMap[username].toString())

            //Creating a BaseAdapter variable and initializing it with the custom built adapter
            val adapter : BaseAdapter = CollectiveMembersAdapter(this, collectiveMembersMap,username,roleList, isUserAnOwner,
                object : OnDataChange {
                    @SuppressLint("LongLogTag")
                    /**
                     * This function is triggered when the user changes a member's role
                     * @param updatedMembersMap is the updated membersmap containing the updated role
                     */
                    override fun collectiveMemberRolesChanged(updatedMembersMap: MutableMap<String, String>) {
                        //Checking if the user himself/herself isnt an owner anymore and handling it accordingly
                        if (!checkIfUserIsAnOwner(updatedMembersMap[username].toString())) {
                            //Setting the isUserAnOwner to false to remove disable the user's permission to change roles
                            isUserAnOwner = false

                            //Sending an message explaining to the user that they are no longer an owner
                            Toast.makeText(this@SpecificCollectiveActivity, "You are no longer an Owner. You cant remove members or delete the collective anymore"
                                , Toast.LENGTH_LONG).show()
                            Log.d(tag,"User is no longer an owner. Removing all owner abilities")
                        }
                        //Sending a database request to update the members map in the Database
                        Database().updateValueInDB("collective", collectiveID, "members", updatedMembersMap, null)
                    }
                })

            //Setting the contents of the layout
            collectiveNameTextView.text = collectiveName
            collectiveIDTextView.text = collectiveID
            //Attaching the custom made adapter to the listView
            collectiveMembersListView.adapter = adapter
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to add data to layout. $error ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the showDataToScreen() function",error)
        }
    }

    /**
     * A function that removes a user from a collective. This function is called when the user presses the "LEAVE COLLECTIVE" button
     * @param view is the button called "LEAVE COLLECTIVE"
     */
    @SuppressLint("LongLogTag")
    fun leaveCollective(view:View) {
        try {
            //Creating a string mutable variable for title used in alert dialog. And initializing it with the title
            val title : String = "Leaving confirmation"
            //Creating a string mutable variable for message used in alert dialog
            var message : String
            //Creating a string mutable variable for positive button text used in alert dialog
            var positiveButtonText : String
            //Checking if there is only one person in the collective
            if (collectiveMembersMap.size == 1) {
                /*
                Since there is only one person in the collective, the leave collective message will be different as the collective
                will be deleted when the user leaves
                 */

                //Initializing the message variable with the message of the alert dialog
                message  = "Are you sure you want to leave the collective? As you are the only person in this collective, " +
                        "the collective will be deleted when you leave!!"

                //Initializing the positiveButtonText variable with the button text of the alert dialog
                positiveButtonText = "CONFIRM LEAVING AND DELETING"
            }
            else {
                //Initializing the message variable with the message of the alert dialog
                message  = "Are you sure you want to leave the collective?"
                //Initializing the positiveButtonText variable with the button text of the alert dialog
                positiveButtonText = "CONFIRM LEAVING"
            }
            //Creating an alert dialog to confirm if the user wants to leave the collective or not
            Utilities().alertDialogBuilder(this,title,message, null)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    //Initializing a boolean variable to return true or false if the user is an owner or not
                    val isUserAnOwner : Boolean = checkIfUserIsAnOwner(collectiveMembersMap[username].toString())

                    //Initializing an int variable that returns the amount of owners in the collective
                    val amountOfOwnersInCollective : Int? = collectiveMembersMap.values.groupingBy { it }.eachCount()["Owner"]

                    //Checking if the user is the only owner and if there are more then 1 member in the collective
                    if (isUserAnOwner && amountOfOwnersInCollective == 1 && collectiveMembersMap.size != 1) {
                        /*
                        User is not able to leave the collective as there needs to be at least 1 owner in the collective only if
                        there are more than 1 person in the collective
                        */

                        //Creating an pop up message explaing why the user cant leave
                        Toast.makeText(this, "Unable to leave. Reason: Please make at least one member an owner before you leave" , Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }
                    //Else check if there are more than one person in the collective
                    else if(collectiveMembersMap.size > 1) {
                        /*
                        User is able to leave the collective. Since there are more than one person in the collective,
                        the collective is not deleted.
                        And in this else if statement we remove the user from all assigned tasks in the collective,
                        and we remove the user from the collective member's list
                         */

                        //Retrieving user's tasks stored in the cached user collective data
                        var collectiveTasks : ArrayList<MutableMap<String,String>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?

                        //Checking if the collective task data exists and if its not empty
                        if (collectiveTasks != null && collectiveTasks.isNotEmpty()) {
                            //Removing the user from all tasks that are assigned to him/her
                            collectiveTasks = Utilities().removeMemberFromTasks(collectiveTasks,username)
                            /*
                            Updating the collective task data in DB,
                            with a data that that has removed the user's name from all tasks the user was assigned to
                            */
                            Database().updateValueInDB("collective", collectiveID,"tasks",collectiveTasks,null)

                        }
                        //Removing the username from the collective members list
                        collectiveMembersMap.remove(username)

                        //Updating the collective members data in DB, with a data that that has removed the user's name
                        Database().updateValueInDB("collective", collectiveID,"members",collectiveMembersMap,null)

                    }
                    else {
                        /*
                        This else statement only triggers if the user is the only person in the collective.
                        In that case, the collective will be deleted
                         */

                        //Deleting the collective from the DB
                        Database().removeDocumentFromDB("collective", collectiveID,null)
                    }
                    //Removing the collectiveID from the userData
                    removeCollectiveIDfromUserData(userID)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to leave the collective. ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the leaveCollective() function",error)
        }
    }

    /**
     * A function that deletes the collective the user is in if the user confirms the delete action
     * This function is triggered when the user clicks the "Delete Collective" button
     * @param view is the "Delete Collective" button
     */
    @SuppressLint("LongLogTag")
    fun deleteCollective(view: View) {
        try {
            //Initializing the title of the confirmation dialog
            val title : String = "Delete confirmation"

            //Initializing the message of the confirmation dialog
            val message : String = "Are you sure you want to delete the collective? There is no turning back"

            //Creating an delete confirmation dialog
            Utilities().alertDialogBuilder(this, title, message, null)
                .setPositiveButton("Confirm deleting") { _, _ ->

                    //Deleting the collective from the DB
                    Database().removeDocumentFromDB("collective", collectiveID,null)

                    //Removing the collectiveID from the user's data
                    removeCollectiveIDfromUserData(userID)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to delete the collective.", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the deleteCollective() function",error)
        }
    }

    /**
     * A function that removes the collectiveID from the user in user's data in the DB
     * @param userID is the ID of the user
     */
    @SuppressLint("LongLogTag")
    private fun removeCollectiveIDfromUserData(userID : String) {
        try {
            //Sending a database request to remove the collectiveID from the user by updating it to null
            Database().updateValueInDB("users", userID,"collectiveID",null, object:
                ResultListener {
                @SuppressLint("LongLogTag")
                override fun onSuccess() {
                    /**
                     * This function is triggered when the database request to remove collective id from user data is successful
                     */
                    Log.d(tag, "the collectiveID has successfully been removed from the user")

                    //Removing the snapshot listener for the collective that the user left
                    Database.listenerMap["collectiveData"]?.remove()

                    //Sending a success msg to the user that they left
                    Toast.makeText(this@SpecificCollectiveActivity, "You have successfully left the collective" , Toast.LENGTH_LONG).show()

                    //Starting the CollectiveActivity
                    startActivity(Intent(this@SpecificCollectiveActivity, CollectiveActivity::class.java))
                    //Finishing the current Activity
                    this@SpecificCollectiveActivity.finish()
                }

                @SuppressLint("LongLogTag")
                override fun onFailure(error: Exception) {
                    /**
                     * This function is triggered when the database request to remove collective id from user data is a failure
                     * @param error returns the error exception
                     */
                    Log.e(tag, "Failed to remove the collectiveID from the user", error)

                    //Initializing a variable with the collectiveID found from the userdata
                    val collectiveID = Database.userData[0]?.data?.get("collectiveID").toString()

                    //Giving the member the lowest rank, member
                    collectiveMembersMap[username] = "Member"

                    //Adding the user back into the collective so they can try again. User will be set to lowest rank
                    Database().updateValueInDB("collective", collectiveID, "members", collectiveMembersMap, null
                    )
                }
            })
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred. Error: $error", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the removeCollectiveIDfromUserData() function",error)
        }
    }
    /**
     * A function that checks if the logged in user is an Owner. If the user isnt an owner, it also removes some buttons that is only available for the role Owner
     * @param userRole is the role of the user
     * @return Returns true if the user is an Owner or False if not
     */
    fun checkIfUserIsAnOwner(userRole : String) : Boolean {
        //If the user isn't an owner, then they cant remove members, change roles or delete collection
        if (userRole.lowercase() != "Owner".lowercase()) {
            //Removing the delete collection button as the user doesn't have permission for it
            deleteCollectiveButton.visibility = View.GONE

            //Removing the delete member button as the user doesn't have permission for it
            removeMemberButton.visibility = View.GONE
            return false
        }
        return true
    }
}



