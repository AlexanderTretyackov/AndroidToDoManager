package ru.tretyackov.todo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.tretyackov.todo.data.ToDoPriority
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.dataStore
import ru.tretyackov.todo.data.toFormattedString
import ru.tretyackov.todo.theme.AppTheme
import ru.tretyackov.todo.theme.accentBackgroundColor
import ru.tretyackov.todo.theme.backgroundColor
import ru.tretyackov.todo.theme.blueColor
import ru.tretyackov.todo.theme.checkedColor
import ru.tretyackov.todo.theme.highPriorityUncheckedBoxColor
import ru.tretyackov.todo.theme.highPriorityUncheckedColor
import ru.tretyackov.todo.theme.iconsColor
import ru.tretyackov.todo.theme.uncheckedColor
import ru.tretyackov.todo.utilities.FactoryViewModel
import ru.tretyackov.todo.utilities.getAppComponent
import ru.tretyackov.todo.viewmodels.DataState
import ru.tretyackov.todo.viewmodels.ToDoListViewModel

@Preview(showBackground = true)
@Composable
private fun ToDoListItemPreview() {
    ToDoListItem(
        toDo = TodoItem(
            "Купить что-то, где-то, зачем-то, но зачем не очень понятно, но точно чтобы показать как обрезается длинный текст в несколько строк, если больше 3-х",
            false,
            priority = ToDoPriority.High
        ),
        onSwitchToDoCompleted = {},
        onClick = {}
    )
}

@Composable
private fun ToDoListItem(toDo: TodoItem, onSwitchToDoCompleted: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp, 12.dp)
    ) {
        Checkbox(
            checked = toDo.completed,
            onCheckedChange = { onSwitchToDoCompleted() },
            modifier = Modifier
                .height(24.dp)
                .width(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = checkedColor,
                uncheckedColor = if (toDo.priority == ToDoPriority.High) highPriorityUncheckedColor else uncheckedColor,
                checkmarkColor = Color.White,
            )
                .copy(uncheckedBoxColor = if (toDo.priority == ToDoPriority.High) highPriorityUncheckedBoxColor else Color.Transparent)
        )
        Spacer(modifier = Modifier.size(12.dp))
        when (toDo.priority) {
            ToDoPriority.Low -> R.string.low_priority
            ToDoPriority.Medium -> R.string.medium_priority
            ToDoPriority.High -> R.string.high_priority
        }
        if (toDo.priority != ToDoPriority.Medium)
            Image(
                painter = painterResource(id = if (toDo.priority == ToDoPriority.Low) R.drawable.low else R.drawable.high),
                contentDescription = "",
                modifier = Modifier
                    .padding(top = 2.dp, end = 3.dp)
                    .height(20.dp)
                    .width(16.dp)
            )
        Column {
            Text(
                text = toDo.name,
                style = if (toDo.completed) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .alpha(if (toDo.completed) 0.4f else 1.0f),
            )
            val deadline = toDo.deadline
            if (deadline != null) {
                Text(
                    text = deadline.toFormattedString(),
                    style = if (toDo.completed) MaterialTheme.typography.bodySmall.copy(
                        textDecoration = TextDecoration.LineThrough
                    ) else MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .alpha(0.4f)
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    vm: ToDoListViewModel,
    openAboutApp: () -> Unit,
    openSettings: () -> Unit,
) {
    val showWithCompleted by vm.showWithCompletedState.collectAsState()
    val isLoading = vm.dataState.collectAsState().value == DataState.Loading
    val completedCount by vm.completedCountState.collectAsState()
    Column(
        modifier = Modifier.padding(
            start = 60.dp,
            top = 0.dp,
            end = 24.dp,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(30.dp)
                        .align(Alignment.CenterStart),
                    color = blueColor,
                    trackColor = blueColor.copy(alpha = 0.3f),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "",
                    alpha = 0.4f,
                    modifier = Modifier
                        .clickable { openSettings() }
                        .padding(8.dp)
                        .size(24.dp),
                    colorFilter = ColorFilter.tint(iconsColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "",
                    alpha = 0.4f,
                    modifier = Modifier
                        .clickable { openAboutApp() }
                        .padding(8.dp)
                        .size(24.dp),
                    colorFilter = ColorFilter.tint(iconsColor)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.my_todos),
                    fontSize = 32.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(id = R.string.completed, completedCount))
                Spacer(modifier = Modifier.height(16.dp))
            }
            Image(painter = painterResource(id = if (showWithCompleted) R.drawable.hide else R.drawable.show),
                contentDescription = stringResource(id = R.string.close_content_description),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        vm.filter(!showWithCompleted)
                    }
                    .padding(12.dp)
                    .height(24.dp)
                    .width(24.dp))
        }
    }
}

@Composable
private fun ToDoList(
    vm: ToDoListViewModel,
    createToDo: () -> Unit,
    openToDo: (TodoItem) -> Unit,
    openAboutApp: () -> Unit,
    openSettings: () -> Unit,
) {
    val todoItemList by vm.toDoListFilteredState.collectAsState()
    AppTheme {
        Surface {
            Scaffold(
                modifier = Modifier.background(backgroundColor),
                topBar = {
                    TopBar(vm, openAboutApp = openAboutApp, openSettings = openSettings)
                },
                containerColor = backgroundColor,
            ) { padding ->
                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxHeight()
                ) {
                    Surface(
                        color = accentBackgroundColor,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp),
                    ) {
                        LazyColumn {
                            items(todoItemList)
                            { toDo ->
                                ToDoListItem(
                                    toDo,
                                    { vm.onSwitchToDoCompleted(toDo) },
                                    onClick = { openToDo(toDo) }
                                )
                            }
                        }
                    }
                    FloatingActionButton(
                        containerColor = blueColor,
                        onClick = { createToDo() },
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 40.dp)
                            .align(Alignment.BottomEnd)
                            .width(56.dp)
                            .height(56.dp),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "",
                            modifier = Modifier
                                .height(24.dp)
                                .height(24.dp)
                        )
                    }
                }
            }
        }
    }
}


class ToDoListFragment : Fragment() {
    private val vm: ToDoListViewModel by viewModels { FactoryViewModel(getAppComponent().toDoListViewModel()) }
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
                ToDoList(
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