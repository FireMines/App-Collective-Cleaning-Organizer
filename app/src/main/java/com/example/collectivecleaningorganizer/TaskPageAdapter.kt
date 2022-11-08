package com.example.collectivecleaningorganizer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TaskPageAdapter(var dataList: List<TaskModel>): RecyclerView.Adapter<TaskPageAdapter.TaskViewHolder>() {
    inner class TaskViewHolder(view: View, listener: OnItemClickListener): RecyclerView.ViewHolder(view){
        val taskName_tv: TextView
        val dueDate_tv: TextView
        val description_tv: TextView
        init {
            Log.d("hei", view.toString())
            taskName_tv = view.findViewById(R.id.taskName)
            dueDate_tv = view.findViewById(R.id.taskDueDate)
            description_tv = view.findViewById(R.id.taskDescription)

            view.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_layout, parent, false)
        return  TaskViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = dataList[position]
        holder.taskName_tv.text = item.name
        holder.dueDate_tv.text = item.dueDate
        holder.description_tv.text = item.description
    }

    override fun getItemCount(): Int {
        return  dataList.size
    }

    fun updateData(updatedDataList: List<TaskModel>){
        dataList = updatedDataList
        notifyDataSetChanged()
    }
}