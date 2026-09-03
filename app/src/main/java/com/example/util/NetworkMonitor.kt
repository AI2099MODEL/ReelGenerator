package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NetworkSimulationMode(val label: String) {
    AUTO("Real Network Sensor"),
    FORCE_MOBILE_SIGNAL("Simulate Mobile Signal (Cellular 5G)"),
    FORCE_OFFLINE("Simulate Offline (No Signal)")
}

data class NetworkState(
    val isOnline: Boolean,
    val hasMobileSignal: Boolean,
    val connectionType: String, // "MOBILE_SIGNAL", "WIFI", "ETHERNET", "OFFLINE"
    val signalStrengthLabel: String, // "Strong 5G", "LTE", "Wi-Fi Fast", "Disconnected"
    val simulationMode: NetworkSimulationMode = NetworkSimulationMode.AUTO
)

class NetworkMonitor(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _simulationMode = MutableStateFlow(NetworkSimulationMode.AUTO)
    val simulationMode: StateFlow<NetworkSimulationMode> = _simulationMode.asStateFlow()

    private val _networkState = MutableStateFlow(resolveCurrentState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        if (connectivityManager == null) return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    refreshState()
                }

                override fun onLost(network: Network) {
                    refreshState()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    refreshState()
                }
            }

            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSimulationMode(mode: NetworkSimulationMode) {
        _simulationMode.value = mode
        refreshState()
    }

    fun refreshState() {
        scope.launch(Dispatchers.Default) {
            _networkState.value = resolveCurrentState()
        }
    }

    private fun resolveCurrentState(): NetworkState {
        val mode = _simulationMode.value
        when (mode) {
            NetworkSimulationMode.FORCE_MOBILE_SIGNAL -> {
                return NetworkState(
                    isOnline = true,
                    hasMobileSignal = true,
                    connectionType = "MOBILE_SIGNAL",
                    signalStrengthLabel = "Mobile Cellular (5G Active)",
                    simulationMode = mode
                )
            }
            NetworkSimulationMode.FORCE_OFFLINE -> {
                return NetworkState(
                    isOnline = false,
                    hasMobileSignal = false,
                    connectionType = "OFFLINE",
                    signalStrengthLabel = "Offline (No Signal)",
                    simulationMode = mode
                )
            }
            NetworkSimulationMode.AUTO -> {
                val cm = connectivityManager ?: return NetworkState(
                    isOnline = false,
                    hasMobileSignal = false,
                    connectionType = "OFFLINE",
                    signalStrengthLabel = "Offline",
                    simulationMode = mode
                )
                val activeNetwork = cm.activeNetwork
                val capabilities = cm.getNetworkCapabilities(activeNetwork)

                if (capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return NetworkState(
                        isOnline = false,
                        hasMobileSignal = false,
                        connectionType = "OFFLINE",
                        signalStrengthLabel = "Offline",
                        simulationMode = mode
                    )
                }

                val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val isEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

                val connType = when {
                    isCellular -> "MOBILE_SIGNAL"
                    isWifi -> "WIFI"
                    isEthernet -> "ETHERNET"
                    else -> "ONLINE"
                }

                val strength = when {
                    isCellular -> "Mobile Signal (Cellular Connected)"
                    isWifi -> "Wi-Fi Network Connected"
                    isEthernet -> "Ethernet Connected"
                    else -> "Connected"
                }

                return NetworkState(
                    isOnline = true,
                    hasMobileSignal = isCellular,
                    connectionType = connType,
                    signalStrengthLabel = strength,
                    simulationMode = mode
                )
            }
        }
    }

    fun unregister() {
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
