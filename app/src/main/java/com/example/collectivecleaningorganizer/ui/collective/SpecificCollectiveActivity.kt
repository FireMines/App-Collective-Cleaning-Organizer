package com.example.collectivecleaningorganizer.ui.collective

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.BaseAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.core.view.forEach
import com.example.collectivecleaningorganizer.*
import com.example.collectivecleaningorganizer.ui.friends.FriendsActivity
import com.example.collectivecleaningorganizer.ui.login.CreateUserActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.OnDataChange
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.model.SnapshotVersion
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_specific_collective.*
import kotlinx.android.synthetic.main.activity_task_overview.*
import kotlinx.android.synthetic.main.task_layout.view.*
import java.lang.Exception

class SpecificCollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val tag = "SpecificCollectiveActivity"
    private val collectiveMembersMap : MutableMap<String,String> = userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>
    private val collectiveID : String = userCollectiveData[0]?.id.toString()
    private val userID = userData[0]?.id.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_collective)

        val roleList : MutableList<String> = mutableListOf<String>("Owner","Member")
        val collectiveName : String = userCollectiveData[0]?.data?.get("name").toString()


        var isUserAnOwner : Boolean = checkIfUserIsAnOwner(collectiveMembersMap[userID].toString())

        val adapter : BaseAdapter = CollectiveMembersAdapter(this, collectiveMembersMap,userID,roleList, isUserAnOwner,
            object : OnDataChange {
                @SuppressLint("LongLogTag")
                override fun collectiveMemberRolesChanged(updatedMembersMap: MutableMap<String, String>) {
                    if (!checkIfUserIsAnOwner(updatedMembersMap[userID].toString())) {
                        isUserAnOwner = false
                        Log.d(tag,"User is no longer an owner. Removing all owner abilities")
                    }

                }
            })

        //Setting the contents of the layout
        collectiveNameTextView.text = collectiveName
        collectiveIDTextView.text = collectiveID
        collectiveMembersListView.adapter = adapter

        //val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigator)

        //bottomNavigationView.setOnNavigationItemSelectedListener { item ->
        //    when(item.itemId) {
        //        R.id.friends -> {
        //            startActivity(Intent(this, FriendsActivity::class.java))
        //            true
        //        }
        //        R.id.tasks -> {
        //            startActivity(Intent(this, TaskOverviewActivity::class.java))
        //            true
        //        }
        //    }
        //    false
        //}


    }

    /**
     * A function that removes a user from a collective. This function is called when the user presses the "LEAVE COLLECTIVE" button
     * @param view is the button called "LEAVE COLLECTIVE"
     */
    fun leaveCollective(view:View) {
        //Creating a string mutable variable for title used in alert dialog. And initializing it with the title
        val title : String = "Leaving confirmation"
        //Creating a string mutable variable for message used in alert dialog
        var message : String

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
            positiveButtonText = "CONFIRM LEAVING AND DELETING"
        }
        else {
            message  = "Are you sure you want to leave the collective?"
            positiveButtonText = "CONFIRM LEAVING"
        }
        //Creating an alert dialog to confirm if the user wants to leave the collective or not
        Utilities().alertDialogBuilder(this,title,message, null)
            .setPositiveButton(positiveButtonText) { _, _ ->
                //Initializing a boolean variable to return true or false if the user is an owner or not
                val isUserAnOwner : Boolean = checkIfUserIsAnOwner(collectiveMembersMap[userID].toString())

                //Initializing an int variable that returns the amount of owners in the collective
                val amountOfOwnersInCollective : Int? = collectiveMembersMap.values.groupingBy { it }.eachCount()["Owner"]

                //Checking if the user is the only owner and if there are more then 1 member in the collective
                if (isUserAnOwner && amountOfOwnersInCollective == 1 && collectiveMembersMap.size != 1) {
                    /*
                    User is not able to leave the collective as there needs to be at least 1 owner in the collective only if
                    there are more than 1 person in the collective
                    */

                    //Creating an alert dialog with an error message on why they cant leave
                    Utilities().alertDialogBuilder(this,"Unable to leave","Please make at least one member an owner before you leave", null)
                        .setNegativeButton("OK", null)
                        .create()
                        .show()
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
                    val collectiveTasks : ArrayList<MutableMap<String,String>>? = userCollectiveData[0]?.data?.get("tasks") as ArrayList<MutableMap<String, String>>?

                    //Checking if the collective task data exists and if its not empty
                    if (collectiveTasks != null && collectiveTasks.isNotEmpty()) {
                        //Iterating through the collectiveTasks arraylist
                        for (task in collectiveTasks) {
                            //Initializing an arraylist with the assigned members list for the task
                            val assignedMembers : ArrayList<String> = task["assigned"] as ArrayList<String>

                            //If the assignedMembers arraylist for each task contains the userID, remove it from the assignedMembers list
                            if (assignedMembers.contains(userID)) {
                                //Removing the user from the assignedMembers arraylist
                                assignedMembers.remove(userID)
                            }
                        }
                        /*
                        Updating the collective task data in DB,
                        with a data that that has removed the user's name from all tasks the user was assigned to
                        */
                        Database().updateValueInDB("collective", collectiveID,"tasks",collectiveTasks,null)

                    }
                    //Removing the userID from the collective members list
                    collectiveMembersMap.remove(userID)
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
                Database().updateValueInDB("users", userID,"collectiveID",null, object:
                    ResultListener {
                    @SuppressLint("LongLogTag")
                    override fun onSuccess() {
                        Log.d(tag, "the collectiveID has successfully been removed from the user")
                        //Removing the snapshot listener for the collective that the user left
                        listenerMap["collectiveData"]?.remove()
                        //Starting the CollectiveActivity
                        startActivity(Intent(this@SpecificCollectiveActivity, CollectiveActivity::class.java))

                    }
                    @SuppressLint("LongLogTag")
                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Failed to remove the collectiveID from the user", error)

                        val collectiveID = userData[0]?.data?.get("collectiveID").toString()
                        collectiveMembersMap[userID] = "Member"
                        //Adding the user back into the collective so they can try again. User will be set to lowest rank
                        Database().updateValueInDB("collective", collectiveID,"members",collectiveMembersMap,null)
                    }
                })

            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    fun checkIfUserIsAnOwner(userRole : String) : Boolean {
        //If the user isn't an owner, then they cant remove members, change roles or delete collection
        if (userRole.lowercase() != "Owner".lowercase()) {
            //Removing the delete collection button as the user doesn't have permission for it
            deleteCollectiveButton.visibility = View.GONE
            //Removing the delete member button as the user doesn't have permission for it
            deleteMemberButton.visibility = View.GONE
            return false
        }
        return true
    }
}



