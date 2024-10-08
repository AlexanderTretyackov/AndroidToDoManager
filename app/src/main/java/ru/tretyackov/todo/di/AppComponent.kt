package ru.tretyackov.todo.di

import android.content.Context
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Subcomponent
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.data.database.DatabaseModule
import ru.tretyackov.todo.data.network.ApiModule
import ru.tretyackov.todo.utilities.ConnectivityMonitor
import ru.tretyackov.todo.utilities.ConnectivityMonitorImpl
import ru.tretyackov.todo.viewmodels.ToDoListViewModel
import ru.tretyackov.todo.viewmodels.ToDoViewModel
import javax.inject.Singleton

@Component(modules = [ApiModule::class, DatabaseModule::class, ConnectivityModule::class, SubcomponentsModule::class])
@Singleton
interface AppComponent {
    fun todoItemsRepository(): TodoItemsRepository
    fun toDoListViewModel(): ToDoListViewModel
    fun toDoComponent(): ToDoComponent.Factory

    @Component.Factory
    interface AppComponentFactory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}

@Module
interface ConnectivityModule {
    @Singleton
    @Binds
    fun bindConnectivityMonitor(impl: ConnectivityMonitorImpl): ConnectivityMonitor
}

@Module(subcomponents = [ToDoComponent::class])
class SubcomponentsModule

@Subcomponent
interface ToDoComponent {
    fun toDoViewModelFactory(): ToDoViewModel.Factory

    @Subcomponent.Factory
    interface Factory {
        fun create(): ToDoComponent
    }
}
