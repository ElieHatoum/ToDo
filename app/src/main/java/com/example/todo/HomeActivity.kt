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

    private fun fetchData(){
        val userId = auth.currentUser?.uid
        db.collection("users").document(userId.toString()).collection("tasks").get().addOnSuccessListener { snapshot ->
            for(document in snapshot.documents){
                val task = TodoData(
                    taskId = document.id,
                    taskTitle = document.getString("title")?: "untitled",
                    taskDescription = document.getString("description")?: "No description"
                )
                if(mList.indexOf(task) == -1){
                    mList.add(task)
                    adapter.notifyItemInserted(mList.indexOf(task))
                }
            }
        }.addOnFailureListener{ e ->
            Toast.makeText(this, "Failed to fetch tasks: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAddTask(
        taskTitle: String,
        taskDescription: String,
        taskTitleEt: TextInputEditText,
        taskDescriptionEt: TextInputEditText
    ) {
        val userId = auth.currentUser?.uid

        val taskData = mapOf(
            "title" to taskTitle,
            "description" to taskDescription,
        )

        db.collection("users").document(userId.toString()).collection("tasks")
            .add(taskData) // Generates a unique task ID
            .addOnSuccessListener {
                Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show()
                taskTitleEt.text = null
                taskDescriptionEt.text = null
                fetchData()
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

    override fun onEditTask(toDoData: TodoData) {
        TODO("Not yet implemented")
    }

}