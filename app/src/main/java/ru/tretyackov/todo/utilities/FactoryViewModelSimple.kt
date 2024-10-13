package ru.tretyackov.todo.utilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FactoryViewModelSimple<T : ViewModel>(private val simpleFactory: () -> T) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return simpleFactory() as T
    }
}
