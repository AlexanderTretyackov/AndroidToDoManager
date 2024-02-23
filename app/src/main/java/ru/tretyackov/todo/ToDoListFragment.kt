package ru.tretyackov.todo

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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

class ToDoListFragment : Fragment() {
    private lateinit var toDoAdapter: ToDoAdapter
    private lateinit var completedTextView: TextView

    private lateinit var showImageButton: ImageButton
    private lateinit var hideImageButton: ImageButton

    private var showOnlyUncompleted: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_do_list, container, false)
        completedTextView = view.findViewById(R.id.textViewCompleted)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        toDoAdapter = ToDoAdapter({toDo -> openToDo(toDo)}, { filterToDos() })
        toDoAdapter.todos = ToDoRepository.todos.value ?: listOf()
        ToDoRepository.todos.observe(viewLifecycleOwner){
            filterToDos()
        }
        recyclerView.adapter = toDoAdapter
        val density = requireContext().resources.displayMetrics.density
        recyclerView.addItemDecoration(ToDoItemDecoration((16 *  density).toInt(),(12 *  density).toInt(),
            (16 *  density).toInt(),(12 *  density).toInt()))
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val addToDoButton = view.findViewById<FloatingActionButton>(R.id.createToDoButton)
        addToDoButton.setOnClickListener{ openToDo(null) }
        updateCompletedToDoText()

        showImageButton = view.findViewById(R.id.showImageButton)
        hideImageButton = view.findViewById(R.id.hideImageButton)
        showImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        hideImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        return view
    }

    private fun showHideCompletedToDoFilter(){
        showOnlyUncompleted = !showOnlyUncompleted
        showImageButton.visibility = if(showOnlyUncompleted) View.VISIBLE else View.INVISIBLE
        hideImageButton.visibility = if(showOnlyUncompleted) View.INVISIBLE else View.VISIBLE
        filterToDos()
    }

    private fun filterToDos(){
        val newList = ToDoRepository.todos.value ?: listOf()
        toDoAdapter.todos = if(showOnlyUncompleted)
            newList.filter { toDo -> !toDo.completed } else newList
        refreshUI()
    }

    private fun openToDo(toDo: ToDo?)
    {
        if(parentFragmentManager.fragments.any { fragment -> fragment is ToDoFragment  })
        {
            return
        }
        parentFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in,0,0,R.anim.slide_out)
            add(R.id.fragment_container_view,
                ToDoFragment(toDo))
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun updateCompletedToDoText()
    {
        completedTextView.text =
        getString(R.string.completed,
            (ToDoRepository.todos.value ?: listOf()).count { toDo -> toDo.completed })
    }

    private fun refreshUI(){
        updateCompletedToDoText()
        toDoAdapter.notifyDataSetChanged()
    }
}