package ru.tretyackov.todo.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface IConnectivityMonitor{
    val isAvailableFlow: Flow<Boolean>
}

class ConnectivityMonitor(ctx: Context) : IConnectivityMonitor {
    override val isAvailableFlow = MutableStateFlow(false)
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            isAvailableFlow.update { true }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            isAvailableFlow.update { false }
        }
    }
    private val connectivityManager = getSystemService(ctx, ConnectivityManager::class.java) as ConnectivityManager
    init {
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
}
