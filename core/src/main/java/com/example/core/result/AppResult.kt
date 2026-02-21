package com.example.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data class Http(val code: Int) : AppError
    data object Timeout : AppError
    data object Serialization : AppError
    data object Database : AppError
    data object Unauthorized : AppError
    data class Unknown(val message: String? = null) : AppError
}