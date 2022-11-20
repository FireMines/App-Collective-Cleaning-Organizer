package com.example.collectivecleaningorganizer.ui.utilities

import android.app.AlertDialog
import android.content.Context
import android.view.View
import com.example.collectivecleaningorganizer.R

/**
 * A class that hold functions considered as a utility for the app
 */
class Utilities {
    /**
     * A function that builds an alert dialog
     * @param context is the context we want to show the dialog
     * @param title is the title of the alert dialog
     * @param message is the message which is shown in the alert dialog
     * @param view is the layout widgets we want to add to the dialog if there any
     */
    fun alertDialogBuilder(context : Context, title: String?, message: String?, view: View?): AlertDialog.Builder {

        return AlertDialog.Builder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background)
            .setTitle(title)
            .setMessage(message)
            .setView(view)
    }

    /**
     * A function that removes a member from tasks assigned to them
     * @param collectiveTasks is the arraylist containing the collective tasks
     * @param username is the username we want to remove from the tasks
     * @return returns an arraylist with a mutable<String,String> as its value
     */
    fun removeMemberFromTasks(collectiveTasks : ArrayList<MutableMap<String,String>>, username : String) : ArrayList<MutableMap<String,String>> {
        //Iterating through the collectiveTasks arraylist
        for (task in collectiveTasks) {
            //Initializing an arraylist with the assigned members list for the task
            val assignedMembers : ArrayList<String> = task["assigned"] as ArrayList<String>

            //If the assignedMembers arraylist for each task contains the userID, remove it from the assignedMembers list
            if (assignedMembers.contains(username)) {
                //Removing the user from the assignedMembers arraylist
                assignedMembers.remove(username)
            }
        }
        return collectiveTasks
    }
}