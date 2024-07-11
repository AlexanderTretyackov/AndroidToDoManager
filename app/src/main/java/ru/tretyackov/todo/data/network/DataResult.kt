package ru.tretyackov.todo.data.network

sealed class DataResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T>() : DataResult<T>()
    class Success<T>(data: T) : DataResult<T>(data = data)
    sealed class Error<T>(errorMessage: String) : DataResult<T>(message = errorMessage)
    {
        class NetworkError<T>(errorMessage: String) : Error<T>(errorMessage = errorMessage)
        class UnsynchronizedDaraError<T>(errorMessage: String) : Error<T>(errorMessage = errorMessage)
        class AnotherError<T>(errorMessage: String) : Error<T>(errorMessage = errorMessage)
    }
}