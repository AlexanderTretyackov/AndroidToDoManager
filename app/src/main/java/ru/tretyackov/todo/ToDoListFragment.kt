package ru.tretyackov.todo

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ToDoItemDecoration(private val leftOffset:Int = 0,
                         private val topOffset:Int = 0,
                         private val rightOffset:Int = 0,
                         private val bottomOffset:Int = 0,
    ) : RecyclerView.ItemDecoration(){
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(leftOffset, topOffset, rightOffset, bottomOffset)
    }
}

interface ToDoList{
    fun add(toDo: ToDo)
    fun remove(toDo: ToDo)
    fun update(oldToDo: ToDo, newToDo: ToDo)
}

class ToDoListFragment : Fragment(), ToDoList {
    private lateinit var toDoAdapter: ToDoAdapter
    private lateinit var completedTextView: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_do_list, container, false)
        completedTextView = view.findViewById(R.id.textViewCompleted)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        toDoAdapter = ToDoAdapter({toDo -> openToDo(toDo)}, { updateCompletedToDoText() })
        toDoAdapter.todos = ToDoRepository.todos.toMutableList()
        recyclerView.adapter = toDoAdapter
        val density = requireContext().resources.displayMetrics.density
        recyclerView.addItemDecoration(ToDoItemDecoration((16 *  density).toInt(),(12 *  density).toInt(),
            (16 *  density).toInt(),(12 *  density).toInt()))
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val addToDoButton = view.findViewById<FloatingActionButton>(R.id.createToDoButton)
        addToDoButton.setOnClickListener{ openToDo(null) }
        updateCompletedToDoText()
        return view
    }

    private fun openToDo(toDo: ToDo?)
    {
        parentFragmentManager.commit {
            add(R.id.fragment_container_view,
                ToDoFragment(this@ToDoListFragment, toDo))
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    override fun add(toDo: ToDo) {
        toDoAdapter.todos.add(toDo)
        updateToDos()
    }

    override fun remove(toDo: ToDo) {
        toDoAdapter.todos.remove(toDo)
        updateToDos()
    }

    override fun update(oldToDo: ToDo, newToDo: ToDo) {
        oldToDo.name = newToDo.name
        updateToDos()
    }

    private fun updateCompletedToDoText()
    {
        completedTextView.text =
        getString(R.string.completed, toDoAdapter.todos.count { toDo -> toDo.completed })
    }

    private fun updateToDos(){
        updateCompletedToDoText()
        toDoAdapter.notifyDataSetChanged()
    }
}