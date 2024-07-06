package ru.tretyackov.todo.utilities

import androidx.fragment.app.Fragment
import ru.tretyackov.todo.App
import ru.tretyackov.todo.di.AppComponent

fun Fragment.getAppComponent(): AppComponent = (activity?.application as App).appComponent
