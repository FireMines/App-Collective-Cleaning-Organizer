package com.example.collectivecleaningorganizer.ui.task

import android.os.Parcel
import android.os.Parcelable


data class TaskModel(
    var name: String,
    var dueDate: String,
    var description: String,
    var isMarked:   Boolean

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(dueDate)
        parcel.writeString(description)
        parcel.writeByte(if (isMarked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskModel> {
        override fun createFromParcel(parcel: Parcel): TaskModel {
            return TaskModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskModel?> {
            return arrayOfNulls(size)
        }
    }
}



