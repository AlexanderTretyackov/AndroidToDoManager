package ru.tretyackov.todo.data

import com.yandex.authsdk.YandexAuthToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Token(
    @SerialName("value")
    val value: String,
    @SerialName("expiresAtUtc")
    val expiresAtUtc: Long
)

fun YandexAuthToken.toLocalToken() = Token(this.value, this.expiresIn * 1000 + System.currentTimeMillis())
