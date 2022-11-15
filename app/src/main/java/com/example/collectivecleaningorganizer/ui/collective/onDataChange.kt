package com.example.collectivecleaningorganizer.ui.collective

import android.widget.Spinner
import java.lang.Exception

interface OnDataChange {
    fun collectiveMemberRolesChanged(updatedMembersMap: MutableMap<String, String>)

}
interface ResultListener {
    fun onSuccess()
    fun onFailure(error :Exception)
}

