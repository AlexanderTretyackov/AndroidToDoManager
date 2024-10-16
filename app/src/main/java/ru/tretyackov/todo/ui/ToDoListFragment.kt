package ru.tretyackov.todo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.THEME_MODE_KEY
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.dataStore
import ru.tretyackov.todo.utilities.FactoryViewModelSimple
import ru.tretyackov.todo.utilities.getAppComponent
import ru.tretyackov.todo.viewmodels.DataState
import ru.tretyackov.todo.viewmodels.ToDoListViewModel

class ToDoListFragment : Fragment() {
    private val fragmentComponent by lazy {
        getAppComponent().toDoListFragmentComponentFactory().create()
    }
    private val vm: ToDoListViewModel by viewModels { FactoryViewModelSimple { fragmentComponent.toDoListViewModel() } }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                vm.dataState.collect { dataState ->
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
        return ComposeView(requireContext()).apply {
            setContent {
                ToDoListComponent(
                    vm,
                    createToDo = { openToDo(null) },
                    openToDo = { toDo -> openToDo(toDo) },
                    openAboutApp = ::openAboutApp,
                    openSettings = ::showThemeSettings
                )
            }
        }
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
}
