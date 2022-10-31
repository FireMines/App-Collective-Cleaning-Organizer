package com.example.collectivecleaningorganizer

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_create_user.*


class CreateUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        init()
    }

    private fun init(){
        CreateUserButton.setOnClickListener{
            addUser()
        }
    }

    private fun addUser(){
        //Se i database om email allerede er i bruk

        //Se i database om brukernavn er unikt, dersom dette er et krav

        //Les alle tekstfelt og sørg for at confirm password og password har lik verdi

        //Send inn data til database dersom over er gyldig

        //En eller annen konfirmasjonsskjerm dersom handlingene over forekommer plettfritt

        //Opprett heller en error streng som fylles ettersom med krav
        if (CreatePassword.text.toString() != CreateConfirmPassword.text.toString()){
            val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle("Alert")
            var message = CreatePassword.text.toString() + " does not equal " + CreateConfirmPassword.text.toString()
            alertDialog.setMessage(message)
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            alertDialog.show()
        }
        else{
            CreateUserButton.text = "bruh"
        }
    }
}