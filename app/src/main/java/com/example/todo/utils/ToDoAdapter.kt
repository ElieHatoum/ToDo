package com.example.todo.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.TodoItemBinding

class ToDoAdapter(private val list : MutableList<TodoData>) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {
    private var listener : ToDoAdapterInterface? = null

    inner class ToDoViewHolder(val binding : TodoItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                binding.todoTask.text = this.taskTitle
                binding.todoTaskDescription.text = this.taskDescription

                binding.deleteTask.setOnClickListener{
                    listener?.onDeleteTask(this)
                }

                binding.editTask.setOnClickListener{
                    listener?.onEditTask(this)
                }
            }
        }
    }

    fun setListener(listener : ToDoAdapterInterface){
        this.listener = listener
    }

    interface ToDoAdapterInterface{
        fun onDeleteTask(toDoData : TodoData)
        fun onEditTask(toDoData: TodoData)
    }
}