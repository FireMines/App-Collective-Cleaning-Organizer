package com.example.collectivecleaningorganizer.ui.collective

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.BaseAdapter
import android.widget.Spinner
import androidx.core.view.forEach
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.login.CreateUserActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_specific_collective.*

class SpecificCollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_collective)
        val userID : String = "testhjguh38u39"
        val collectiveID : String = "NTNU#32"
        val roleList : MutableList<String> = mutableListOf<String>("Owner","Member")


        db.collection("collective").document(collectiveID).get()
            .addOnSuccessListener { e->

                val membersMap : MutableMap<String,String> = e.data?.get("members") as MutableMap<String, String>
                val collectiveName : String = e.data?.get("name").toString()


                var isUserAnOwner : Boolean = checkIfUserIsAnOwner(membersMap[userID].toString(), roleList[0])
                val adapter : BaseAdapter = CollectiveMembersAdapter(this, membersMap,userID,roleList, isUserAnOwner,
                    object : onDataChange {
                        override fun collectiveMemberRolesChanged(updatedMembersMap: MutableMap<String, String>) {
                            Log.e("updatd", "$updatedMembersMap")

                            checkIfUserIsAnOwner(updatedMembersMap[userID].toString(), roleList[0])
                            isUserAnOwner = false



                        }
                    })

                collectiveNameTextView.text = collectiveName
                collectiveIDTextView.text = collectiveID
                collectiveMembersListView.isClickable = true
                collectiveMembersListView.adapter = adapter


                //Log.e("t", "${collectiveRolesSpinner.getItemAtPosition(0)}")

                Log.e("test", "ggg")
                /*
                collectiveRolesSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                        Log.e("test", "$position")
                    }

                }

                 */


            }


    }
    fun checkIfUserIsAnOwner(userRole : String, ownerRole : String ) : Boolean {
        //If the user isn't an owner, then they cant remove members, change roles or delete collection
        if (userRole.lowercase() != ownerRole.lowercase()) {
            //Removing the delete collection button as the user doesn't have permission for it
            deleteCollectiveButton.visibility = View.GONE
            //Removing the delete member button as the user doesn't have permission for it
            deleteMemberButton.visibility = View.GONE
            return false
        }

        return true
    }
}



