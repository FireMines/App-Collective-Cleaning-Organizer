package com.example.collectivecleaningorganizer.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.collectivecleaningorganizer.R
import com.example.collectivecleaningorganizer.collective.CollectiveActivity
import com.example.collectivecleaningorganizer.task.TaskOverviewActivity
import com.example.collectivecleaningorganizer.userData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // declares instance of firebaseAuth
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Starts ForgotPasswordActivity when clicking on forgotPassword
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        signup.setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
        // initializes instance of FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // activates login function when login button is clicked
        logInButton.setOnClickListener {
            login()
        }
    }

    // function to login user
    private fun login() {
        val email = emailLogin.text.toString()
        val password = passwordLogin.text.toString()

        // uses email and password as login credentials
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("log in", "login:success")
                Toast.makeText(this, "Successful login", Toast.LENGTH_SHORT).show()   // displays message to user

                //DB request to retrieve amy user data
                db.collection("users").document(task.result.user?.uid.toString()).get().addOnSuccessListener { e ->
                    //Adding the userData to a mutable map
                    userData[e.id] = e

                    val intent: Intent
                    //Checking if the user is apart of a collective or not
                    if (e.data?.get("collectiveID") == null) {
                        //Start the CollectiveActivity
                        intent = Intent(this,CollectiveActivity::class.java)
                    }
                    else {
                        //Start the TaskOverview activity
                        intent = Intent(this, TaskOverviewActivity::class.java)
                    }
                    //Adding the userID to the intent
                    intent.putExtra("uid",task.result.user?.uid)

                    //Starting the activity
                    startActivity(intent)
                }


            } else {
                Log.w("log in", "login:failure")
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

