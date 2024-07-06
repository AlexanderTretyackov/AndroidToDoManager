package ru.tretyackov.todo.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.tretyackov.todo.data.ApiModule
import ru.tretyackov.todo.data.ConnectivityModule
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.viewmodels.ToDoListViewModel
import ru.tretyackov.todo.viewmodels.ToDoViewModel
import javax.inject.Singleton

@Component(modules = [ApiModule::class, ConnectivityModule::class])
@Singleton
interface AppComponent {
    fun todoItemsRepository() : TodoItemsRepository
    fun toDoListViewModel(): ToDoListViewModel
    fun toDoViewModelFactory(): ToDoViewModel.Factory
    @Component.Factory
    interface AppComponentFactory{
        fun create(@BindsInstance context: Context) : AppComponent
    }
}