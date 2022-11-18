package com.example.collectivecleaningorganizer.ui.friends

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.collectivecleaningorganizer.Database
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.ui.utilities.FriendListListener
import com.example.collectivecleaningorganizer.ui.utilities.ResultListener
import com.example.collectivecleaningorganizer.ui.utilities.StringListener
import kotlinx.android.synthetic.main.activity_friendrequests.*
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.friend.view.*
import java.lang.Exception

class FriendRequestsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friendrequests)
        init()
    }

    private fun init(){
        //Hent venner fra database og vis i scrollview

        //Testdata

        val uId = Database.userData[0]?.id.toString()

        Database().getFriendRequestListFromDB("users", uId, object : FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                val it = friendList.iterator()
                while (it.hasNext()) {
                    addRequestsScroll(it.next().toString())
                }
            }
            override fun onFailure(error: Exception) {
                Log.e("getFriendRequests", "Failure to get friend requests")
            }

        })

    }

    private fun addRequestsScroll(name: String){
        val view = layoutInflater.inflate(R.layout.friend, null)
        view.FriendName.text = name
        view.ButtonRemoveFriend.text = "Accept request"
        view.ButtonRemoveFriend.setOnClickListener{
            addFriend(name)
        }
        RequestsScroll.addView(view)
    }

    private fun addFriend(name: String) {
        //Finn id i db
        //Sett friend felt i begge dber til å være hverandre sitt navn
        val userName = Database.userData[0]?.data?.get("username").toString()
        val userId = Database.userData[0]?.id.toString()
        Database().getUid(name, object : StringListener {
            override fun onSuccess(uId: String) {
                Database().getFriendsFromDB("users", uId, object : FriendListListener {
                    override fun onSuccess(friendList: ArrayList<String>) {
                            friendList.add(userName)
                            Database().updateValueInDB("users", uId, "Friends", friendList, object : ResultListener {
                                override fun onSuccess() {
                                    Log.e("Bruh", "Bruh")
                                }
                                override fun onFailure(error: Exception) {
                                    Log.e("TaskOverviewActivity", "Failure with listener")
                                }
                            })
                    }
                    override fun onFailure(error: Exception) {
                        Log.e("getFriendRequests", "Failure to get friend requests")
                    }

                })
            }
            override fun onFailure(error: Exception) {
                Log.e("not good", "not good")
            }
        })

        Database().getFriendsFromDB("users", userId, object : FriendListListener {
            override fun onSuccess(friendList: ArrayList<String>) {
                friendList.add(name)
                Database().updateValueInDB("users", userId, "Friends", friendList, object : ResultListener {
                    override fun onSuccess() {
                        Log.e("Bruh", "Bruh")
                    }
                    override fun onFailure(error: Exception) {
                        Log.e("TaskOverviewActivity", "Failure with listener")
                    }
                })
            }
            override fun onFailure(error: Exception) {
                Log.e("getFriendRequests", "Failure to get friend requests")
            }

        })
    }
}