package ru.tretyackov.todo.ui

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.databinding.FragmentToDoListBinding
import ru.tretyackov.todo.viewmodels.DataState
import ru.tretyackov.todo.viewmodels.ToDoListViewModel

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
    private val vm: ToDoListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToDoListBinding.inflate(layoutInflater, container, false)
        val recyclerView = binding.recyclerView
        toDoAdapter = ToDoAdapter({toDo -> openToDo(toDo)}, { toDo -> vm.onSwitchToDoCompleted(toDo) })
        toDoAdapter.todos = vm.toDoListFilteredState.value
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                vm.toDoListFilteredState.collect {
                    toDoAdapter.todos = vm.toDoListFilteredState.value
                    refreshToDoList()
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                vm.showWithCompletedState.collect{ withCompleted ->
                    binding.showImageButton.visibility = if(withCompleted) View.INVISIBLE else View.VISIBLE
                    binding.hideImageButton.visibility = if(withCompleted) View.VISIBLE else View.INVISIBLE
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                vm.completedCountState.collect { countCompleted ->
                    binding.textViewCompleted.text = getString(R.string.completed, countCompleted)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                vm.dataState.collect { s ->
                    binding.loadingLayout.visibility = if(s == DataState.Loading) View.VISIBLE else View.GONE
                    binding.loadedLayout.visibility =  if(s == DataState.Loaded) View.VISIBLE else View.GONE
                    binding.errorLayout.visibility =  if(s == DataState.Error) View.VISIBLE else View.GONE
                }
            }
        }
        binding.btnRefresh.setOnClickListener{
            lifecycleScope.launch {
                vm.refresh()
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
        binding.showImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        binding.hideImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        return binding.root
    }

    private fun showHideCompletedToDoFilter(){
        vm.filter(!vm.showWithCompletedState.value)
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

    private fun refreshToDoList(){
        toDoAdapter.notifyDataSetChanged()
    }
}