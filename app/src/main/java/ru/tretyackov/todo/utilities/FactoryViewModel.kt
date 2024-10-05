package ru.tretyackov.todo.utilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FactoryViewModel<T : ViewModel>(private val viewModel: T) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return viewModel as T
    }
}
