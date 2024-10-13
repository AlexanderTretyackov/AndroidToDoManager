package ru.tretyackov.todo.di

import dagger.Subcomponent
import ru.tretyackov.todo.viewmodels.ToDoViewModel

@Subcomponent
interface ToDoFragmentComponent {
    fun toDoViewModelFactory(): ToDoViewModel.Factory

    @Subcomponent.Factory
    interface Factory {
        fun create(): ToDoFragmentComponent
    }
}