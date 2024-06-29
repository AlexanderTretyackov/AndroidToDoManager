package ru.tretyackov.todo.ui

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.databinding.FragmentToDoListBinding

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
    private lateinit var binding : FragmentToDoListBinding
    private var showOnlyUncompleted: Boolean = true
    private val stateToDoList = TodoItemsRepository.getAll()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToDoListBinding.inflate(layoutInflater, container, false)
        val recyclerView = binding.recyclerView
        toDoAdapter = ToDoAdapter({toDo -> openToDo(toDo)}, { refreshUI() })
        toDoAdapter.todos = stateToDoList.value
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                stateToDoList.collect{ refreshUI() }
            }
         }
        recyclerView.adapter = toDoAdapter
        val density = requireContext().resources.displayMetrics.density
        recyclerView.addItemDecoration(
            ToDoItemDecoration((16 *  density).toInt(),(12 *  density).toInt(),
            (16 *  density).toInt(),(12 *  density).toInt())
        )
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val addToDoButton = binding.createToDoButton
        addToDoButton.setOnClickListener{ openToDo(null) }
        updateCompletedToDoText()
        binding.showImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        binding.hideImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        return binding.root
    }

    private fun showHideCompletedToDoFilter(){
        showOnlyUncompleted = !showOnlyUncompleted
        binding.showImageButton.visibility = if(showOnlyUncompleted) View.VISIBLE else View.INVISIBLE
        binding.hideImageButton.visibility = if(showOnlyUncompleted) View.INVISIBLE else View.VISIBLE
        filterToDos()
    }

    private fun updateToDoList(){
        toDoAdapter.todos = if(showOnlyUncompleted)
            stateToDoList.value.filter { toDo -> !toDo.completed } else stateToDoList.value
    }

    private fun filterToDos(){
        val newList = stateToDoList.value
        toDoAdapter.todos = if(showOnlyUncompleted)
            newList.filter { toDo -> !toDo.completed } else newList
        refreshUI()
    }

    private fun openToDo(todoItem: TodoItem?)
    {
        // TODO: подумать как сделать чтобы навигация
        //  не происходила несколько раз при многкратном нажатии
        if(parentFragmentManager.fragments.any { fragment -> fragment is ToDoFragment })
        {
            return
        }
        parentFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in,0,0, R.anim.slide_out)
            add(
                R.id.fragment_container_view,
                ToDoFragment(todoItem)
            )
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun updateCompletedToDoText()
    {
        binding.textViewCompleted.text =
        getString(R.string.completed, stateToDoList.value.count { toDo -> toDo.completed })
    }

    private fun refreshUI(){
        updateCompletedToDoText()
        updateToDoList()
        toDoAdapter.notifyDataSetChanged()
    }
}