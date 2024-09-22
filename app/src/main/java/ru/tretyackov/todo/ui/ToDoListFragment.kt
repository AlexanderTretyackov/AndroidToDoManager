package ru.tretyackov.todo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.THEME_MODE_KEY
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.dataStore
import ru.tretyackov.todo.databinding.FragmentToDoListBinding
import ru.tretyackov.todo.utilities.FactoryViewModel
import ru.tretyackov.todo.utilities.getAppComponent
import ru.tretyackov.todo.viewmodels.DataState
import ru.tretyackov.todo.viewmodels.ToDoListViewModel

private const val TOP_BOTTOM_ITEM_PADDING_DP = 12
private const val START_END_ITEM_PADDING_DP = 16

class ToDoListFragment : Fragment() {
    private lateinit var toDoAdapter: ToDoAdapter
    private lateinit var binding: FragmentToDoListBinding
    private val vm: ToDoListViewModel by viewModels { FactoryViewModel(getAppComponent().toDoListViewModel()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToDoListBinding.inflate(layoutInflater, container, false)
        val recyclerView = binding.recyclerView
        toDoAdapter =
            ToDoAdapter({ toDo -> openToDo(toDo) }, { toDo -> vm.onSwitchToDoCompleted(toDo) })
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
                vm.showWithCompletedState.collect { withCompleted ->
                    binding.showImageButton.visibility =
                        if (withCompleted) View.INVISIBLE else View.VISIBLE
                    binding.hideImageButton.visibility =
                        if (withCompleted) View.VISIBLE else View.INVISIBLE
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
                vm.dataState.collect { dataState ->
                    binding.loadingLayout.visibility =
                        if (dataState == DataState.Loading) View.VISIBLE else View.GONE
                    if (dataState == DataState.Error) {
                        Toast.makeText(
                            this@ToDoListFragment.requireContext(),
                            getString(R.string.loading_error_text), Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
        binding.btnAboutApp.setOnClickListener {
            openAboutApp()
        }
        binding.settingsButton.setOnClickListener {
            showThemeSettings()
        }
        recyclerView.adapter = toDoAdapter
        ToDoItemDecoration(
            requireContext(),
            vm::onSwitchToDoCompleted,
            TOP_BOTTOM_ITEM_PADDING_DP,
            START_END_ITEM_PADDING_DP,
        ).attachToRecyclerView(recyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val addToDoButton = binding.createToDoButton
        addToDoButton.setOnClickListener {
            openToDo(null)
        }
        binding.showImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        binding.hideImageButton.setOnClickListener {
            showHideCompletedToDoFilter()
        }
        return binding.root
    }

    private fun showHideCompletedToDoFilter() {
        vm.filter(!vm.showWithCompletedState.value)
    }

    private fun showThemeSettings() {
        val currentThemeMode = AppCompatDelegate.getDefaultNightMode()
        val choiceItems = arrayOf(
            getString(R.string.day_theme),
            getString(R.string.night_theme),
            getString(R.string.follow_system_theme)
        )
        val checkedChoice = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 2
            else -> 2
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder
            .setTitle(R.string.theme_choosing)
            .setSingleChoiceItems(
                choiceItems, checkedChoice
            ) { dialog, selectedModeIndex ->
                dialog.cancel()
                val themeMode = when (selectedModeIndex) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    2 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                lifecycleScope.launch {
                    requireContext().dataStore.edit { settings ->
                        settings[THEME_MODE_KEY] = themeMode
                    }
                }
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun openAboutApp() {
        if (parentFragmentManager.fragments.any { fragment -> fragment is AboutAppFragment }) {
            return
        }
        parentFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in, 0, 0, R.anim.slide_out)
            add(
                R.id.fragment_container_view,
                AboutAppFragment()
            )
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun openToDo(todoItem: TodoItem?) {
        // TODO: подумать как сделать чтобы навигация
        //  не происходила несколько раз при многкратном нажатии
        if (parentFragmentManager.fragments.any { fragment -> fragment is ToDoFragment }) {
            return
        }
        parentFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in, 0, 0, R.anim.slide_out)
            add(
                R.id.fragment_container_view,
                ToDoFragment(todoItem)
            )
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private fun refreshToDoList() {
        toDoAdapter.notifyDataSetChanged()
    }
}