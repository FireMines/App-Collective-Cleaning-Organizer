package com.example.collectivecleaningorganizer.ui.collective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.BaseAdapter
import com.example.collectivecleaningorganizer.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_specific_collective.*
import kotlinx.android.synthetic.main.collective_member_row.*

class SpecificCollectiveActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specific_collective)
        val userID : String = "tgtttt"
        val collectiveID : String = "NTNU#32"
        val roleList : MutableList<String> = mutableListOf<String>("Owner","Member")


        db.collection("collective").document(collectiveID).get()
            .addOnSuccessListener { e->

                val membersMap : MutableMap<String,String> = e.data?.get("members") as MutableMap<String, String>
                val collectiveName : String = e.data?.get("name").toString()

                val permissionToChangeMemberRoles : Boolean


                //If the user doesn't have high enough role (in this case the user isnt an owner), they cant remove members, change roles or delete collection
                if (!membersMap[userID].toString().lowercase().equals(roleList[0].lowercase())) {
                    deleteCollectiveButton.visibility = View.GONE
                    deleteMemberButton.visibility = View.GONE
                    permissionToChangeMemberRoles = false
                }
                else {
                    permissionToChangeMemberRoles = true
                }
                val adapter : BaseAdapter = CollectiveMembersAdapter(this, membersMap,roleList, permissionToChangeMemberRoles)

                collectiveNameTextView.text = collectiveName
                collectiveIDTextView.text = collectiveID

                collectiveMembersListView.adapter = adapter
            }


    }
}