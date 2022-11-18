package com.example.collectivecleaningorganizer.ui.collective

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.DatabaseRequestListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import kotlinx.android.synthetic.main.activity_collective_invite_users.*
import java.lang.Exception

class CollectiveRemoveMembers : AppCompatActivity() {
    private val collectiveMembersMap : MutableMap<String,String> = Database.userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>
    private val collectiveID : String = Database.userCollectiveData[0]?.id.toString()
    private val userID = Database.userData[0]?.id.toString()
    private val tag : String = "CollectiveRemoveMembers"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collective_remove_members)

        //Initializing an temporary arraylist which is empty
        val temporaryMembersToDeleteList : ArrayList<String> = arrayListOf()

        //Adding all the names from the collectiveMembersMap keys (which are the member names) to the temporaryMembersToDeleteList
        temporaryMembersToDeleteList.addAll(collectiveMembersMap.keys)

        //Removing the the own user from the list as they shouldn't be able to remove themselves
        temporaryMembersToDeleteList.remove(userID)

        //Creating an ArrayAdapter for the listview
        val listViewAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,temporaryMembersToDeleteList)
        //Attaching the ArrayAdapter to the listView
        membersListView.adapter = listViewAdapter
    }


    fun removeMembersFromCollective(view: View) {

        //Building an alert dialog that shows a view with a listview where the user can choose members to remove
        Utilities().alertDialogBuilder(this,"Removing confirmation", "Are you sure you want to remove the member(s)?", null)
            .setPositiveButton("Remove members") { _, _ ->

                //Retrieving user's tasks stored in the cached user collective data
                var collectiveTasks : ArrayList<MutableMap<String,String>>? = Database.userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?
                val temporaryRemovedMemberNameList : ArrayList<String> = arrayListOf<String>()
                //Iterating through the membersListView and checking which members got selected
                for (i:Int in 0 until membersListView.count) {
                    //Statement checking if the member's check box is checked
                    if (membersListView.isItemChecked(i)) {
                        //Initializing the member's name retrieved from the membersListView row
                        val memberName :String = membersListView.getItemAtPosition(i).toString()
                        temporaryRemovedMemberNameList.add(memberName)
                        //Removing the selected member from the collectiveMembersmap
                        collectiveMembersMap.remove(memberName)
                    }
                }
                //Checking if the collective task data exists and if its not empty
                if (collectiveTasks != null && collectiveTasks.isNotEmpty()) {
                    //Iterating through the list containing names of members that were removed
                    for (i:Int in 0 until temporaryRemovedMemberNameList.size) {
                        //Removing the each user from all tasks that are assigned to them
                        collectiveTasks = Utilities().removeMemberFromTasks(collectiveTasks!!,temporaryRemovedMemberNameList[i])

                        //Removing the collectiveID from the userData of the user that is being removed
                        removeCollectiveIDFromUser(temporaryRemovedMemberNameList[i])
                    }

                    /*
                    Updating the collective task data in DB,
                    with a data that that has removed all the removed users from all tasks that was assigned to them
                    */
                    Database().updateValueInDB("collective", collectiveID,"tasks",collectiveTasks,null)
                }

                //Updating the collective members map with the updated contents that doesn't include the removed members
                Database().updateValueInDB("collective",collectiveID,"members",collectiveMembersMap,object : ResultListener {
                    override fun onSuccess() {
                        Toast.makeText(this@CollectiveRemoveMembers, "The members were successfully removed", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@CollectiveRemoveMembers, SpecificCollectiveActivity::class.java))
                    }

                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Error when trying to update the collective members map ",error)
                    }

                })

            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun removeCollectiveIDFromUser(username : String) {
        Database().getDataFromDB("username",username, object :DatabaseRequestListener {
            override fun onSuccess(data: MutableMap<String, Any?>?) {
                //Checking if the given username doesn't exist in the usernames collection in DB
                if (data == null) {
                    Log.e(tag, "ERROR: Cannot find the username in the usernames collection")
                    return
                }
                //Else Checking if the username has an invalid uid field value
                else if (data["uid"] == "" || data["uid"] == null) {
                    Log.e(tag, "UserID is empty or a null value")
                    return
                }
                //Else retrieve the userdata of the given username
                else {
                    //Initializing a variable with the uid of the user that is getting invited
                    val userToInviteUID : String= data["uid"].toString()
                    Database().updateValueInDB("users", userToInviteUID,collectiveID,null, null)
                }
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "Failed to remove the collectiveID from the user", error)
            }

        } )
    }


}