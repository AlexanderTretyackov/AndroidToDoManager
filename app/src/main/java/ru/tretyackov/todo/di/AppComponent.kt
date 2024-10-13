package ru.tretyackov.todo.di

import android.content.Context
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.data.database.DatabaseModule
import ru.tretyackov.todo.data.network.ApiModule
import ru.tretyackov.todo.utilities.ConnectivityMonitor
import ru.tretyackov.todo.utilities.ConnectivityMonitorImpl
import javax.inject.Scope

@Scope
annotation class AppScope

@Component(modules = [ApiModule::class, DatabaseModule::class, ConnectivityModule::class, SubcomponentsModule::class])
@AppScope
interface AppComponent {
    fun toDoListFragmentComponentFactory(): ToDoListFragmentComponent.Factory
    fun toDoFragmentComponentFactory(): ToDoFragmentComponent.Factory
    fun todoItemsRepository(): TodoItemsRepository

    @Component.Factory
    interface AppComponentFactory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}

@Module
interface ConnectivityModule {
    @Binds
    fun bindConnectivityMonitor(impl: ConnectivityMonitorImpl): ConnectivityMonitor
}

@Module(subcomponents = [ToDoFragmentComponent::class, ToDoListFragmentComponent::class])
class SubcomponentsModule
