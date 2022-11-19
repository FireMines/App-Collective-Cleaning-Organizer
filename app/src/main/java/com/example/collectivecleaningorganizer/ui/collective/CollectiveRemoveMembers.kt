package com.example.collectivecleaningorganizer.ui.collective

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.StringListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_collective_invite_users.*
import java.lang.Exception

class CollectiveRemoveMembers : AppCompatActivity() {
    //Initializing the members map retrieved from the collectiveData in the DB
    private val collectiveMembersMap : MutableMap<String,String> = Database.userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>

    //Initializing the collectiveID retrieved from the collectiveData in the DB
    private val collectiveID : String = Database.userCollectiveData[0]?.id.toString()

    //Initializing the username retrieved from the userData in the DB
    private val username = Database.userData[0]?.get("username").toString()

    //Initializing a tag used for logging to know which file the log message came from
    private val tag : String = "CollectiveRemoveMembers"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting the content view of the activity
        setContentView(R.layout.activity_collective_remove_members)

        //Initializing a temporary arraylist which is empty
        val temporaryMembersToDeleteList : ArrayList<String> = arrayListOf()

        //Adding all the names from the collectiveMembersMap keys (which are the member names) to the temporaryMembersToDeleteList
        temporaryMembersToDeleteList.addAll(collectiveMembersMap.keys)

        //Removing the the own user from the list as they shouldn't be able to remove themselves
        temporaryMembersToDeleteList.remove(username)

        //Creating an ArrayAdapter for the listview
        val listViewAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,temporaryMembersToDeleteList)

        //Attaching the ArrayAdapter to the listView
        membersListView.adapter = listViewAdapter
    }

    /**
     * This is a function that removes selected members from the collective if the user confirms the action.
     * This function is triggered when the user presses the "Remove Members" button
     * @param view is the "Remove Members" button
     */
    fun removeMembersFromCollective(view: View) {
        //Initializing a variable to keep track of amount of names that were checked
        var amountOfCheckedNames : Int = 0

        //Iterating through the membersListView
        for (i:Int in 0 until membersListView.count) {
            //Statement checking if the member's check box is checked
            if (membersListView.isItemChecked(i)) {
                //Incrementing the value of amountOfCheckedNames by 1
                amountOfCheckedNames = amountOfCheckedNames.inc()
            }
        }
        //Checking if the amount of checked boxes/names are 0
        if (amountOfCheckedNames == 0) {
            //Sending an error message to the user
            Toast.makeText(this, "Please select atlas one member in order to remove", Toast.LENGTH_SHORT).show()
            return
        }

        //Building an dialog to give the user a confirmation message confirming if they want to remove the members or not
        Utilities().alertDialogBuilder(this,"Removing confirmation", "Are you sure you want to remove the member(s)?", null)
            .setPositiveButton("Remove members") { _, _ ->

                //Retrieving user's tasks stored in the cached collective data
                var collectiveTasks : ArrayList<MutableMap<String,String>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?

                //Creating an temporary arraylist and initializing it as an empty arraylist
                val temporaryRemovedMemberNameList : ArrayList<String> = arrayListOf<String>()

                //Iterating through the membersListView
                for (i:Int in 0 until membersListView.count) {

                    //Checking if the member's check box is checked
                    if (membersListView.isItemChecked(i)) {

                        //Initializing a variable with the member's name retrieved from the membersListView row
                        val memberName :String = membersListView.getItemAtPosition(i).toString()

                        //Adding the checked member's name to the temporaryRemovedMemberNameList Arraylist
                        temporaryRemovedMemberNameList.add(memberName)

                        //Removing the selected member from the collective members map data
                        collectiveMembersMap.remove(memberName)
                    }
                }
                //Checking if the collective task data exists and if its not empty
                if (collectiveTasks != null && collectiveTasks.isNotEmpty()) {

                    //Iterating through the list containing names of members that were removed
                    for (i:Int in 0 until temporaryRemovedMemberNameList.size) {
                        //Removing each removed user from all tasks that are assigned to them
                        collectiveTasks = Utilities().removeMemberFromTasks(collectiveTasks!!,temporaryRemovedMemberNameList[i])

                        //Removing the collectiveID from the userData of those removed users
                        removeCollectiveIDFromRemovedMembers(temporaryRemovedMemberNameList[i])
                    }

                    /*
                    Updating the collective task data in DB,
                    with a data that that has removed all the removed users from all tasks that was assigned to them
                    */
                    Database().updateValueInDB("collective", collectiveID,"tasks",collectiveTasks,null)
                }

                //Updating the collective members map with the updated contents that doesn't include the removed members
                Database().updateValueInDB("collective",collectiveID,"members",collectiveMembersMap,object : ResultListener {
                    /**
                     * This function is triggered when the database request to update the collective members data is successful
                     */
                    override fun onSuccess() {
                        //Sending a success msg to user
                        Toast.makeText(this@CollectiveRemoveMembers, "The members were successfully removed", Toast.LENGTH_LONG).show()

                        //Starting the SpecificCollectiveActivity activity
                        startActivity(Intent(this@CollectiveRemoveMembers, SpecificCollectiveActivity::class.java))
                    }

                    /**
                     * This function is triggered when the database request to update the collective members data is a failure
                     * @param error returns the error exception
                     */
                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Error when trying to update the collective members map ",error)
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    /**
     * A function that removes the collectiveID from the removed member's user data
     * @param username is the username of the removed member
     */
    private fun removeCollectiveIDFromRemovedMembers(username : String) {
        //Sending a database request to retrieve the user ID from the given username
        Database().getUid(username, object : StringListener {
            /**
             * This function is triggered when the database request to retrieve the User ID from the given username is successful
             * @param uId is the user ID of the given username
             */
            override fun onSuccess(uId: String) {
                //Checking and handling if the given username doesn't exist in the DB
                if (uId == "") {
                    Log.e(tag, "ERROR. cannot find any userID for the given username. Thus cannot remove collectiveID from the following username: $username")
                    return
                }
                //Updating the removed user's collectiveID to remove the ID of the collective the user was removed from
                Database().updateValueInDB("users", uId,"collectiveID",null, null)
            }

            /**
             * This function is triggered when the database request to retrieve the User ID from the given username is a failure
             * @param error returns the error exception
             */
            override fun onFailure(error: Exception) {
                Log.e(tag, "Database request failure to retrieve userID from the username: $username")
            }
        })
    }



}