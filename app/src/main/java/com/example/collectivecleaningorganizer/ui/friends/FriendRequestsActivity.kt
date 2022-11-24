package com.example.collectivecleaningorganizer.ui.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.collective.SpecificCollectiveActivity
import com.example.collectivecleaningorganizer.ui.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.ui.utilities.FriendListListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.StringListener
import com.example.collectivecleaningorganizer.ui.utilities.Utilities
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_friendrequests.*
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.activity_specific_collective.*
import kotlinx.android.synthetic.main.friend.view.*
import java.lang.Exception

class FriendRequestsActivity: AppCompatActivity() {
    private var tag: String = "FriendRequestsActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friendrequests)

        init()

        //Handles the applications navigation
        Utilities().navigation(this, R.id.friends, bottom_navigator_friend_request)

    }

    private fun init() {
        //Hent venner fra database og vis i scrollview

        //Testdata

        val uId = Database.userData[0]?.id.toString()

        // Takes the user back to the Friends Activity
        friendRequestsBack_btn.setOnClickListener{
            this.finish()
        }

        Database().getFriendRequestListFromDB("users", uId, object : FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                val it = friendList.iterator()
                while (it.hasNext()) {
                    addRequestsScroll(it.next().toString())
                }
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "Failure to get friend requests")
            }

        })

    }

    private fun addRequestsScroll(name: String) {
        val view = layoutInflater.inflate(R.layout.friend, null)
        view.FriendName.text = name
        view.ButtonRemoveFriend.text = "Accept request"
        view.ButtonRemoveFriend.setOnClickListener {
            addFriend(name)
        }
        RequestsScroll.addView(view)
    }

    private fun dbError(){
        Toast.makeText(
            this@FriendRequestsActivity,
            "Error when communicating with the database",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun addFriend(name: String) {
        val it = RequestsScroll.iterator()
        while (it.hasNext()) {
            val check = it.next()
            if (check.FriendName.text.toString() == name) {
                it.remove()
            }
        }
        //Finn id i db
        //Sett friend felt i begge dber til å være hverandre sitt navn
        val userName = Database.userData[0]?.data?.get("username").toString()
        val userId = Database.userData[0]?.id.toString()
        Database().getUid(name, object : StringListener {
            override fun onSuccess(uId: String) {
                Database().getFriendsFromDB("users", uId, object : FriendListListener {
                    override fun onSuccess(friendList: ArrayList<String>) {
                        friendList.add(userName)
                        Database().updateValueInDB(
                            "users",
                            uId,
                            "Friends",
                            friendList,
                            object : ResultListener {
                                override fun onSuccess() {
                                }

                                override fun onFailure(error: Exception) {
                                    Log.e(tag, "Failure with listener")
                                    dbError()
                                }
                            })
                    }

                    override fun onFailure(error: Exception) {
                        Log.e(tag, "Failure to get friend requests")
                        dbError()
                    }

                })
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "not good")
                dbError()
            }
        })

        Database().getFriendsFromDB("users", userId, object : FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                friendList.add(name)
                Database().updateValueInDB(
                    "users",
                    userId,
                    "Friends",
                    friendList,
                    object : ResultListener {
                        override fun onSuccess() {
                        }

                        override fun onFailure(error: Exception) {
                            Log.e(tag, "Failure with listener")
                            dbError()
                        }
                    })
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "Failure to get friend requests")
                dbError()
            }

        })
        Database().getFriendRequestListFromDB("users", userId, object : FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                val it = friendList.iterator()
                while (it.hasNext()) {
                    if (it.next().toString() == name) {
                        it.remove()
                    }
                }
                Database().updateValueInDB(
                    "users",
                    userId,
                    "FriendRequests",
                    friendList,
                    object : ResultListener {
                        override fun onSuccess() {
                            Toast.makeText(
                                this@FriendRequestsActivity,
                                "Friend added",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onFailure(error: Exception) {
                            Log.e(tag, "Failure with listener")
                            dbError()
                        }
                    })
            }

            override fun onFailure(error: Exception) {
                Log.e(tag, "Failure to get friend requests")
                dbError()
            }
        })
    }
}
