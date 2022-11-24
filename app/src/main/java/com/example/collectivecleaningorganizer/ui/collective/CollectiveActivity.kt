package com.example.collectivecleaningorganizer.ui.collective

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.collectivecleaningorganizer.*
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.login.LoginActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.DatabaseRequestListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_collective.*
import kotlinx.android.synthetic.main.activity_collective.bottom_navigator_collective_invite
import kotlinx.android.synthetic.main.activity_collective_invite_users.*
import kotlin.Exception

/**
 * This is an AppCompatActivity class for a CollectiveActivity.
 * It is used to create a collective page for the user has the option to either create or join a collective
 */
class CollectiveActivity : AppCompatActivity() {
    //Initializing a tag used for logging to know which file the log message came from
    private var tag = "CollectiveActivity"
    //Initializing the userID retrieved from the userData in the DB
    private var userID = Database.userData[0]?.id.toString()
    //Initializing the username retrieved from the userData in the DB
    private var username = Database.userData[0]?.get("username").toString()

    /**
     * Creating the CollectiveActivity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting the content view of the activity
        setContentView(R.layout.activity_collective)

        //Handles the applications navigation
        Utilities().navigation(this, R.id.collective, bottom_navigator_collective_invite)

        //Sends user back to the login page if user does not create or join an existing collective
        backToLoginPageButton.setOnClickListener {
            //Creating a dialog confirming if the user wants to logout
            Utilities().alertDialogBuilder(this,"Log out", "Are you sure you want to log out?", null)
                .setPositiveButton("OK") { _, _ ->
                    //Calling the logout function
                    logout()
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()

        }
    }

    /**
     * A function that creates an popup window/dialog allowing the user to type a collective name and create one.
     * This function is triggered when the user presses the "Create Collective" button
     * @param view is the "Create Collective" button
     */
    fun createCollective(view:View) {
        try {
            //Initializing an Input edit text field
            val inputEditTextField = EditText(this)
            //Building an alert dialog allowing the user to enter a collective name and create a collective
            Utilities().alertDialogBuilder(this, "Create collective", "Enter the name of the collective you want to create", inputEditTextField)
                .setPositiveButton("OK") { _, _ ->
                    //Initializing the input received from the input edit text field as collectiveName
                    val collectiveName : String = inputEditTextField.text.toString().lowercase()
                    //Checking if the input is invalid by being empty
                    if (collectiveName.isBlank()) {
                        //Sending an error message to the user
                        Toast.makeText(this, "The collective name cannot be empty. Try again", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }//Else checking if the length of the name isnt between 4 and 16.. making it invalid
                    else if (collectiveName.length !in 4..16) {
                        //Sending an error message to the user
                        Toast.makeText(this, "The collective name must be between 4 and 16 characters long", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    else {
                        //Calling a function to generate a unique collectiveID
                        val collectiveID = generateUniqueCollectiveID(collectiveName)

                        //Sending a database request to check if there already exists a collective with the same collectiveID
                        Database().getDataFromDB("collective", collectiveID, object : DatabaseRequestListener {
                            /**
                             * This function is triggered when the database request is successful
                             * @param data returns the data in the form of a mutable map with the key being a string and the value being Any?
                             */
                            override fun onSuccess(data: MutableMap<String, Any?>?) {
                                //Checking if there exists a collective already with the same collectiveID
                                if (data != null) {
                                    //Sending an error message to the user
                                    Toast.makeText(this@CollectiveActivity, "Error trying to create a collective. Please try again", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                //Adding the newly created collective to the DB
                                addCollectiveToDB(collectiveName,collectiveID, userID)
                            }
                            /**
                             * This function is triggered when the database request is a failure
                             * @param error returns the error exception
                             */
                            override fun onFailure(error: Exception) {
                                Log.e(tag, "Database request failure", error)
                            }
                        })
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to create the collective. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the createCollective() function",error)
        }
    }

    /**
     * A function that is used to log out a new user that does not create or join a collective.
     *  The function calls the firebase auth.sigOut() method.
     *  The function removes both the database listener for userData and collectiveData
     */
    private fun logout() {
        try {
            //Signing out
            Firebase.auth.signOut()
            //Removing the listens for the userdata if it exists
            Database.listenerMap["userData"]?.remove()
            //Removing the listener for the collective data if it exists
            Database.listenerMap["collectiveData"]?.remove()
            startActivity(Intent(this, LoginActivity::class.java))

            Toast.makeText(this,"Successfully logged out"
                ,Toast.LENGTH_LONG).show()
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to log out. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the logout() function",error)
        }
    }

    /**
     * A function that creates an popup window/dialog allowing the user to see all collective invites the user has received in a spinner and choose one to join.
     * This function is triggered if the user clicks on the "See Collective Invites" Button.
     * @param view is the "See Collective Invites"  Button
     */
    fun seeCollectiveInviteRequests(view: View) {
        try {
            //Initializing an arraylist variable with the collectiveRequests data found in the DB
            val collectiveInviteRequests : ArrayList<String>? = Database.userData[0]?.data?.get("collectiveRequests") as ArrayList<String>?

            //Checking if the user doesn't have any invites from people to join a collective
            if (collectiveInviteRequests == null || collectiveInviteRequests.isEmpty()) {
                //Sending an error message to the user
                Toast.makeText(this, "You don't have any pending invites to join a collective", Toast.LENGTH_SHORT).show()
                return
            }

            //Initializing an Spinner widget
            val collectiveInvitesSpinner = Spinner(this)

            //Creating an ArrayAdapter for the spinner
            val spinnerAdapter = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,collectiveInviteRequests)

            //Attaching the ArrayAdapter to the spinner with the id "collectiveInvitesSpinner"
            collectiveInvitesSpinner.adapter = spinnerAdapter

            //Building an alert dialog that shows a view with a spinner where the user can choose a collective to join
            Utilities().alertDialogBuilder(this,"Collective invites", "Choose the collective you want to join", collectiveInvitesSpinner)
                .setPositiveButton("Join") { _, _ ->
                    //Initializing a variable to retrieve the ID of the collective the user selected
                    val selectedCollective : String = collectiveInvitesSpinner.selectedItem.toString()

                    //Removing the collective invite request from the collectiveRequests arraylist data
                    collectiveInviteRequests.remove(selectedCollective)

                    //Updating the collectiveRequests data with the updated contents of collectiveInviteRequests
                    Database().updateValueInDB("users",userID,"collectiveRequests",collectiveInviteRequests,null)

                    //Attempting to add the user to the collective if it exists
                    addUserToCollective(selectedCollective)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to see all collective invites. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the seeCollectiveInviteRequests() function",error)
        }
    }

    /**
     * A function used to generate an unique collectiveID used to identify collectives
     * @param collectiveName is the name of the collective the user wants to create
     * @return string of the generated unique collectiveID
     */
    private fun generateUniqueCollectiveID(collectiveName: String): String {
        //Creating a random 4 digit number
        val randomNumber: String = String.format("%04d", (0..9999).random())

        //Creating and returning a unique collective ID consisting of the collective name and the random number
        return "$collectiveName#$randomNumber"
    }

    /**
     * A function that allows a user to join a collective by entering a collectiveID.
     * This function triggers if the user presses the "Join" button
     * @param view is the "join" button
     */
    fun joinCollectiveByID(view:View) {
        try {
            //Initializing a variable of the collectiveID input the user wrote
            val enteredCollectiveID : String = collectiveIdEditText.text.toString()

            //Checking if the input is invalid by being blank
            if (enteredCollectiveID.isBlank()) {
                //Sending an error message to the user
                Toast.makeText(this, "Please enter a valid collective id", Toast.LENGTH_SHORT).show()
                return
            }
            //Attempting to add the user to the collective if it exists
            addUserToCollective(enteredCollectiveID)
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred when trying to join a collective by id. Try again ", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the joinCollectiveByID() function",error)
        }
    }

    /**
     * A function that adds the user's username to the collective member list in the database
     */
    private fun addUserToCollective(collectiveID: String) {
        try {
            //Sending a database request to retrieve data from the newly created collective
            Database().getDataFromDB("collective", collectiveID, object :DatabaseRequestListener {
                /**
                 * This function is triggered when the database request to retrieve the data of the newly created collective is successful
                 * @param data returns the data in the form of a mutable map with the key being a string and the value being Any?
                 */
                override fun onSuccess(data: MutableMap<String, Any?>?) {
                    //Checking if the given collective ID doesn't exist
                    if (data == null) {
                        Toast.makeText(this@CollectiveActivity, "There exists no collective with the given ID", Toast.LENGTH_SHORT).show()
                        Log.d(tag, "Cannot find the a collective with the given id")
                        return
                    }
                    //Initializing a map with the collective members data found in the DB
                    val membersMapData : MutableMap<String, String> = data["members"] as MutableMap<String, String>?
                        ?: return

                    //Adding the user to the membersMapData, with the role of member
                    membersMapData[username] = "Member"

                    //Sending a database request to update the collective members data
                    Database().updateValueInDB("collective", collectiveID, "members", membersMapData, object : ResultListener {
                        /**
                         * This function is triggered when the database request to update the collective members data is successful
                         */
                        override fun onSuccess() {
                            //Sending a database request to update the user's collectiveID with the ID of the newly joined collective
                            Database().updateValueInDB("users", userID, "collectiveID", collectiveID, object : ResultListener {
                                /**
                                 * This function is triggered when the database request to update the user's collectiveID is successful
                                 */
                                override fun onSuccess() {
                                    //Sending an message informing the user that they have successfully joined the collective
                                    Toast.makeText(this@CollectiveActivity, "You have successfully joined the collective!", Toast.LENGTH_LONG).show()
                                    startTaskOverviewActivity(userID)
                                    Log.d(tag, "Success in adding collective id to user")
                                }
                                /**
                                 * This function is triggered when the database request to update the user's collectiveID is a failure
                                 * @param error returns the error exception
                                 */
                                override fun onFailure(error: Exception) {
                                    //Sending an error message to the user
                                    Toast.makeText(this@CollectiveActivity, "Failure to join collective. Error: $error", Toast.LENGTH_LONG).show()
                                    Log.e(tag, "Failure in adding collective id to user", error)
                                }
                            })
                        }
                        /**
                         * This function is triggered when the database request to update the collective members data is a failure
                         * @param error returns the error exception
                         */
                        override fun onFailure(error: Exception) {
                            //Sending an error message to the user
                            Toast.makeText(this@CollectiveActivity, "Failure to join collective. Error: $error", Toast.LENGTH_LONG).show()
                            Log.e(tag, "Failure in adding member to the collective member's map", error)
                        }
                    })
                }
                /**
                 * This function is triggered when the database request to retrieve the data of the newly created collective is a failure
                 * @param error returns the error exception
                 */
                override fun onFailure(error: Exception) {
                    //Sending an error message to the user
                    Toast.makeText(this@CollectiveActivity, "Failure to send request to database. Error: $error", Toast.LENGTH_LONG).show()
                    Log.e(tag, "Database request failure", error)
                }
            })
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred. Error: $error", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the addUserToCollective() function",error)
        }
    }

    /**
     * A function that adds the data of the newly created collective to the database
     * @param collectiveName is the name of the collective
     * @param collectiveID is the ID of the collective
     * @param userID is the ID of the user that is logged on
     */
    private fun addCollectiveToDB(collectiveName: String,collectiveID : String, userID:String) {
        try {
            //Creating a mutable map for members
            val members = mutableMapOf<String,String>()
            //Creating a mutable map for requests
            val requests = mutableMapOf<String,String>()
            //Initializing a variable for the username of the logged
            val username  : String = Database.userData[0]?.data?.get("username").toString()

            //Adding the user to the members map with the role of Owner
            members[username] = "Owner"

            //Creating a hashmap with the collectiveInfo
            val collectiveInfo = hashMapOf(
                "name" to collectiveName,
                "members" to members,
                "requests" to requests
            )
            //Sending a database request to add the collective information to the database
            Database().addToDB("collective", collectiveID, collectiveInfo, object : ResultListener {
                /**
                 * This function is triggered when the database request to add the collective information data is successful
                 */
                override fun onSuccess() {
                    Log.d(tag, "Collective successfully added to DB!")
                    //Calling addCollectiveIDToUser() function to add the collectiveID to the user
                    addCollectiveIDToUser(collectiveID,userID)
                }
                /**
                 * This function is triggered when the database request to add the collective information data is a failure
                 * @param error returns the error exception
                 */
                override fun onFailure(error: Exception) {
                    Log.e(tag, "Error adding collective to DB", error)
                }
            })
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred. Error: $error", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the addCollectiveToDB() function",error)
        }
    }

    /**
     * A function that adds/updates the collectiveID of the user to match the ID of the collective the user created or joined
     * @param collectiveID is the ID of the collective
     * @param userID is the ID of the user
     */
    private fun addCollectiveIDToUser(collectiveID: String,userID: String) {
        try {
            //Sending a database request to update the collectiveID of the user to match the collective the user joined
            Database().updateValueInDB("users", userID, "collectiveID", collectiveID, object : ResultListener {
                /**
                 * This function is triggered when the database request to update the collectiveID of the user is successful
                 */
                override fun onSuccess() {
                    Log.d(tag, "Successfully added the collectiveID to user $userID")
                    Toast.makeText(this@CollectiveActivity, "You have successfully created a collective!", Toast.LENGTH_LONG).show()
                    //Calling the startTaskOverviewActivity() function to start the TaskOverView activity
                    startTaskOverviewActivity(userID)
                }
                /**
                 * This function is triggered when the database request to update the collectiveID of the user is a failure
                 * @param error returns the error exception
                 */
                override fun onFailure(error: Exception) {
                    Log.e(tag, "Error adding collectiveID to user: $userID", error)
                }
            })
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred. Error: $error", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the addCollectiveToDB() function",error)
        }

    }

    /**
     * This is a function that starts the activity called "TaskOverviewActivity" that shows all the Tasks
     * @param userID is the ID of the user
     */
    private fun startTaskOverviewActivity(userID:String) {
        try {
            val intent = Intent(this, TaskOverviewActivity::class.java)
            //Adding the userID to the intent
            intent.putExtra("uid", userID)
            startActivity(intent)
        }
        catch (error : Exception) {
            Toast.makeText(this, "An error occurred. Error: $error", Toast.LENGTH_LONG).show()
            Log.e(tag, "Error when trying to run the startTaskOverviewActivity() function",error)
        }
    }
}