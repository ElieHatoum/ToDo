package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.databinding.ActivityHomeBinding
import com.example.todo.fragments.AddTaskFragment
import com.example.todo.utils.ToDoAdapter
import com.example.todo.utils.TodoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HomeActivity : AppCompatActivity(), AddTaskFragment.AddTaskButtonListener,
    ToDoAdapter.ToDoAdapterInterface {
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var binding : ActivityHomeBinding
    private lateinit var popUpFragment : AddTaskFragment
    private lateinit var adapter : ToDoAdapter
    private lateinit var mList : MutableList<TodoData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        fetchData()

        binding.logoutButton.setOnClickListener{
            logout()
        }

        binding.FAB.setOnClickListener{
            addTaskDialog()
        }
    }

    private fun init(){
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        mList = mutableListOf()
        adapter = ToDoAdapter(mList)
        adapter.setListener(this)

        binding.recyclerView.adapter = adapter
    }

    private fun logout(){
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun addTaskDialog(){
        popUpFragment = AddTaskFragment()
        popUpFragment.setListener(this)
        popUpFragment.show(supportFragmentManager, "AddTaskFragment")
    }

    private fun fetchData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("tasks")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->
                mList.clear()
                for (document in snapshot.documents) {
                    val task = TodoData(
                        taskId = document.id,
                        taskTitle = document.getString("title") ?: "Untitled",
                        taskDescription = document.getString("description") ?: "No description"
                    )
                    mList.add(task)
                    adapter.notifyItemInserted(mList.size - 1)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onAddTask(
        taskTitle: String,
        taskDescription: String,
        taskTitleEt: TextInputEditText,
        taskDescriptionEt: TextInputEditText
    ) {
        val userId = auth.currentUser?.uid ?: return

        val taskData = mapOf(
            "title" to taskTitle,
            "description" to taskDescription,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("tasks")
            .add(taskData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show()
                taskTitleEt.text = null
                taskDescriptionEt.text = null

                val newTask = TodoData(
                    taskId = documentReference.id,
                    taskTitle = taskTitle,
                    taskDescription = taskDescription
                )
                mList.add(newTask)
                adapter.notifyItemInserted(mList.size - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add task: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        popUpFragment.dismiss()
    }


    override fun onDeleteTask(toDoData: TodoData) {
        val userId = auth.currentUser?.uid
        db.collection("users").document(userId.toString()).collection("tasks").document(toDoData.taskId)
            .delete().addOnSuccessListener {
                Toast.makeText(this, "Task deleted !", Toast.LENGTH_SHORT).show()
                val position = mList.indexOf(toDoData)
                if(position != -1){
                    mList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onUpdateTask(
        taskId: String,
        newTaskTitle: String,
        newTaskDescription: String,
        taskTitleEt: TextInputEditText,
        taskDescriptionEt: TextInputEditText
    ) {
        val userId = auth.currentUser?.uid ?: return
        val updatedData = mapOf(
            "tile" to newTaskTitle,
            "description" to newTaskDescription
        )

        db.collection("users").document(userId).collection("tasks").document(taskId)
            .update(updatedData).addOnSuccessListener {
                Toast.makeText(this, "Task updated !", Toast.LENGTH_SHORT).show()

                val index = mList.indexOfFirst { it.taskId == taskId }
                if(index != -1){
                    mList[index] = mList[index].copy(taskTitle = newTaskTitle, taskDescription = newTaskDescription)
                    adapter.notifyItemChanged(index)
                }
                popUpFragment.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update task: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onEditTask(toDoData: TodoData) {
        popUpFragment = AddTaskFragment()
        popUpFragment.setListener(this)
        popUpFragment.setTaskToEdit(toDoData)
        popUpFragment.show(supportFragmentManager, "EditTaskFragment")
    }

}