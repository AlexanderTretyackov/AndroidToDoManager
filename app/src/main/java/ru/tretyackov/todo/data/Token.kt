package ru.tretyackov.todo.data

import com.yandex.authsdk.YandexAuthToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val COUNT_MILLIS_IN_SECOND = 1000

@Serializable
data class Token(
    @SerialName("value")
    val value: String,
    @SerialName("expiresAtUtc")
    val expiresAtUtc: Long
)

fun YandexAuthToken.toLocalToken() =
    Token(this.value, this.expiresIn * COUNT_MILLIS_IN_SECOND + System.currentTimeMillis())
