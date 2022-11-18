package com.example.collectivecleaningorganizer.ui.collective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import com.example.collectivecleaningorganizer.userCollectiveData
import com.example.collectivecleaningorganizer.userData
import kotlinx.android.synthetic.main.activity_collective_invite_users.*

class CollectiveRemoveMembers : AppCompatActivity() {
    private val collectiveMembersMap : MutableMap<String,String> = userCollectiveData[0]?.data?.get("members") as MutableMap<String, String>
    private val collectiveID : String = userCollectiveData[0]?.id.toString()
    private val userID = userData[0]?.id.toString()
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
                //Iterating through the membersListView and checking which members got selected
                for (i:Int in 0 until membersListView.count) {
                    //Statement checking if the member's check box is checked
                    if (membersListView.isItemChecked(i)) {
                        //Initializing the member's name retrieved from the membersListView row
                        val memberName :String = membersListView.getItemAtPosition(i).toString()

                        //Removing the selected member from the collectiveMembersmap
                        collectiveMembersMap.remove(memberName)
                    }
                }
                //Updating the collective members map with the updated contents that dosnt include the removed members
                Database().updateValueInDB("collective",collectiveID,"members",collectiveMembersMap,null)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}