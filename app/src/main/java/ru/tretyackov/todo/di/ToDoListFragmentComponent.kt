package ru.tretyackov.todo.di

import dagger.Subcomponent
import ru.tretyackov.todo.data.network.ApiModule
import ru.tretyackov.todo.viewmodels.ToDoListViewModel

@Subcomponent(modules = [ApiModule::class])
interface ToDoListFragmentComponent {
    fun toDoListViewModel(): ToDoListViewModel

    @Subcomponent.Factory
    interface Factory {
        fun create(): ToDoListFragmentComponent
    }
}