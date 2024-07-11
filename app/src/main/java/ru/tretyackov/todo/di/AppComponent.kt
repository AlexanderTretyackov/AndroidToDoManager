package ru.tretyackov.todo.di

import android.content.Context
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import ru.tretyackov.todo.data.network.ApiModule
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.data.database.DatabaseModule
import ru.tretyackov.todo.utilities.ConnectivityMonitor
import ru.tretyackov.todo.utilities.IConnectivityMonitor
import ru.tretyackov.todo.viewmodels.ToDoListViewModel
import ru.tretyackov.todo.viewmodels.ToDoViewModel
import javax.inject.Singleton

@Component(modules = [ApiModule::class, DatabaseModule::class, ConnectivityModule::class])
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

@Module
interface ConnectivityModule{
    @Singleton
    @Binds
    fun bindConnectivityMonitor(impl : ConnectivityMonitor): IConnectivityMonitor
}