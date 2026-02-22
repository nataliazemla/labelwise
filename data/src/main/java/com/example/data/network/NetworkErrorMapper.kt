package com.example.data.network

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.core.result.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
fun Throwable.toAppError(): AppError = when (this) {
    is SocketTimeoutException -> AppError.Timeout
    is IOException -> AppError.Network
    is HttpException -> AppError.Http(code())
    else -> AppError.Unknown(message)
}