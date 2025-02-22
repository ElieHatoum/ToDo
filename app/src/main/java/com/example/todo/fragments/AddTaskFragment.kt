package com.example.todo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todo.R
import com.example.todo.databinding.FragmentAddTaskBinding
import com.example.todo.utils.TodoData
import com.google.android.material.textfield.TextInputEditText

class AddTaskFragment : DialogFragment() {
    private lateinit var binding : FragmentAddTaskBinding
    private lateinit var listener : AddTaskButtonListener
    private var existingTask : TodoData? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        existingTask?.let { task ->
            binding.taskTitleInput.setText(task.taskTitle)
            binding.taskDescriptionInput.setText(task.taskDescription)
            binding.addTaskButton.text = getString(R.string.update_task)

        }

        binding.addTaskButton.setOnClickListener{
            addTask()
        }

        binding.todoClose.setOnClickListener{
            dismiss()
        }
    }

    private fun addTask(){
        val taskTitle = binding.taskTitleInput.text.toString()
        val taskDescription = binding.taskDescriptionInput.text.toString()

        if(taskTitle.isNotEmpty()){
            if (existingTask == null){
                listener.onAddTask(taskTitle, taskDescription, binding.taskTitleInput, binding.taskDescriptionInput)
            }else{
                listener.onUpdateTask(existingTask!!.taskId, taskTitle, taskDescription, binding.taskTitleInput, binding.taskDescriptionInput)
            }

        }else{
            Toast.makeText(context, "Task Title cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }

    fun setListener(listener : AddTaskButtonListener){
        this.listener = listener
    }

    fun setTaskToEdit(todoData : TodoData){
        this.existingTask = todoData
    }

    interface AddTaskButtonListener{
        fun onAddTask(taskTitle : String, taskDescription : String, taskTitleEt : TextInputEditText, taskDescriptionEt: TextInputEditText)
        fun onUpdateTask(taskId: String, newTaskTitle: String, newTaskDescription: String, taskTitleEt: TextInputEditText, taskDescriptionEt: TextInputEditText)
    }
}