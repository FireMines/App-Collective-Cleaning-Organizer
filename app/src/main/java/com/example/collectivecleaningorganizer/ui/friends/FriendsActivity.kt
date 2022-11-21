package com.example.collectivecleaningorganizer.ui.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.LogOutActivity
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.ui.collective.SpecificCollectiveActivity
import com.example.collectivecleaningorganizer.ui.task.TaskActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.FriendListListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.StringListener

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import kotlinx.android.synthetic.main.activity_create_task.*
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_specific_collective.*
import kotlinx.android.synthetic.main.activity_task_overview.*
import kotlinx.android.synthetic.main.friend.view.*
import java.lang.Exception


class FriendsActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        SendRequestButton.setOnClickListener{
            sendRequest()
        }
        SeeRequestsButton.setOnClickListener{
            seeRequests()
        }
        init()


        val navigationBarView = findViewById<BottomNavigationView>(R.id.bottom_navigator)
        navigationBarView.selectedItemId = R.id.friends

        navigationBarView.setOnItemSelectedListener { it ->
            when(it.itemId) {
                R.id.taskOverView -> {
                    startActivity(Intent(this, TaskOverviewActivity::class.java))
                    true
                }
                R.id.collective -> {
                    startActivity(Intent(this, SpecificCollectiveActivity::class.java))
                    true
                }
                R.id.friends -> {
                    true
                }
            }
            false
        }

    }

    private fun init(){
        //Hent venner fra database og vis i scrollview

        val uId = Database.userData[0]?.id.toString()

        Database().getFriendsFromDB("users", uId, object : FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                val it = friendList.iterator()
                while (it.hasNext()) {
                    addFriendScroll(it.next().toString())
                }
            }
            override fun onFailure(error: Exception) {
                Log.e("getFriends", "Failure to get friends")
            }

        })

    }

    private fun addFriendScroll(name: String){
        val view = layoutInflater.inflate(R.layout.friend, null)
        view.FriendName.text = name
        view.ButtonRemoveFriend.setOnClickListener{
            removeFriend(name)
        }
        FriendsScroll.addView(view)
    }

    private fun removeFriend(name:String){
        //Er du sikker alert maybe?
        //Om navn ikke er unikt kan en id legges til i hvert element og brukes isteden
        val i = FriendsScroll.iterator()
        while (i.hasNext()){
            val check = i.next()
            if (check.FriendName.text.toString() == name){
                i.remove()
            }
        }
        val username = Database.userData[0]?.data?.get("username").toString()
        val uId = Database.userData[0]?.id.toString()
        Database().getFriendsFromDB("users", uId, object : FriendListListener{
            override fun onSuccess(friendList: ArrayList<String>) {
                val it = friendList.iterator()
                while (it.hasNext()) {
                    if (it.next().toString() == name) {
                        it.remove()
                    }
                }
                    Database().updateValueInDB("users", uId, "Friends", friendList, object : ResultListener {
                        override fun onSuccess() {
                            //Gi konf på request sent
                            Log.e("Bruh", "Bruh")
                        }

                        override fun onFailure(error: Exception) {
                            Log.e("TaskOverviewActivity", "Failure with listener")
                        }
                    })
                }
            override fun onFailure(error: Exception) {
                Log.e("getFriends", "Failure to get friends")
            }
        })

        Database().getUid(name, object : StringListener {
            override fun onSuccess(uId: String) {
                Database().getFriendsFromDB("users", uId, object : FriendListListener {
                    override fun onSuccess(friendList: ArrayList<String>) {
                        val it = friendList.iterator()
                        while (it.hasNext()) {
                            if (it.next().toString() == username) {
                                it.remove()
                            }
                        }
                        Database().updateValueInDB("users", uId, "Friends", friendList, object : ResultListener {
                                override fun onSuccess() {
                                    //Gi konf på request sent
                                    Log.e("Bruh", "Bruh")
                                }

                                override fun onFailure(error: Exception) {
                                    Log.e("TaskOverviewActivity", "Failure with listener")
                                }
                            })
                    }

                    override fun onFailure(error: Exception) {
                        Log.e("getFriends", "Failure to get friends")
                    }
                })
            }

            override fun onFailure(error: Exception) {
                Log.e("getUid", "Failure to get user id")
            }
        })
    }


    private fun sendRequest(){
        //Steder som nevner userId burde endres til cache
        //val userId = FirebaseAuth.getInstance().uid.toString()
        val userId = Database.userData[0]?.data?.get("username").toString()
        //val idArr = arrayListOf<String>()
        //idArr.add(userId.toString())
        FriendRequest.text.toString()
        Database().getUid(FriendRequest.text.toString(), object : StringListener {
            override fun onSuccess(uId: String) {
                Database().getFriendRequestListFromDB("users", uId, object : FriendListListener {
                    override fun onSuccess(friendList: ArrayList<String>) {
                        val it = friendList.iterator()
                        var dupe = false
                        while (it.hasNext()) {
                            if (it.next().toString() == userId) {
                                dupe = true
                            }
                        }
                        if (!dupe) {
                            Database().getFriendsFromDB("users", uId, object : FriendListListener{
                                override fun onSuccess(friendList: ArrayList<String>) {
                                    val it = friendList.iterator()
                                    var dupe = false
                                    while (it.hasNext()) {
                                        if (it.next().toString() == userId) {
                                            dupe = true
                                        }
                                    }
                                    if (!dupe) {
                                        friendList.add(userId)
                                        Database().updateValueInDB("users", uId, "FriendRequests", friendList, object : ResultListener {
                                            override fun onSuccess() {
                                                //Gi konf på request sent
                                                Log.e("Bruh", "Bruh")
                                            }

                                            override fun onFailure(error: Exception) {
                                                Log.e("TaskOverviewActivity", "Failure with listener")
                                            }
                                        })
                                    }
                                }
                                override fun onFailure(error: Exception) {
                                    Log.e("getFriends", "Failure to get friends")
                                }
                                })
                            }
                        }
                    override fun onFailure(error: Exception) {
                        Log.e("getFriendRequests", "Failure to get friend requests")
                    }

                })
            }
            override fun onFailure(error: Exception) {
                Log.e("getUid", "Failure to get user id")
            }
        })


        /*Kode for senere
            Database().getFriendRequestListFromDB("users", "isetg", object :FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                friendList.add("jeff")

            }

            override fun onFailure(error: Exception) {

            }

        })
         */
    }
    private fun seeRequests(){
        val newIntent = Intent(this, FriendRequestsActivity::class.java)
        startActivity(newIntent)
    }
}