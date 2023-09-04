package mutnemom.android.kotlindemo

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mutnemom.android.kotlindemo.databinding.ActivityNetworkStateBinding
import java.net.InetSocketAddress
import java.net.Socket

class NetworkStateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNetworkStateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNetworkStateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NetworkStatusHelper(this).observe(this) { networkStatus ->
            networkStatus ?: return@observe
            when (networkStatus) {
                NetworkStatus.Unavailable -> showOfflineStatus()
                NetworkStatus.Available -> showOnlineStatus()
            }
        }
    }

    private fun showOfflineStatus() {
        binding.apply {
            txtNetworkStatus.text = NetworkStatus.Unavailable.alias
            val drawable = txtNetworkStatus.compoundDrawables[2]
            drawable.colorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun showOnlineStatus() {
        binding.apply {
            txtNetworkStatus.text = NetworkStatus.Available.alias
            val drawable = txtNetworkStatus.compoundDrawables[2]
            drawable.colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP)
        }
    }

    sealed class NetworkStatus(val alias: String) {
        object Available : NetworkStatus("online")
        object Unavailable : NetworkStatus("offline")
    }

    /*
    - We want it to be responsive with the view and Lifecycle-aware.
    - It should be easily reusable across components.
    - It should always have updated network status information.
     */
    class NetworkStatusHelper(context: Context) : LiveData<NetworkStatus>() {

        private var connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback
        private val validNetworkConnections: ArrayList<Network> = ArrayList()

        override fun onActive() {
            super.onActive()
            connectivityManagerCallback = getConnectivityManagerCallback()

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager
                .registerNetworkCallback(networkRequest, connectivityManagerCallback)
        }

        override fun onInactive() {
            super.onInactive()
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
        }

        private fun announceStatus() {
            if (validNetworkConnections.isNotEmpty()) {
                postValue(NetworkStatus.Available)
            } else {
                postValue(NetworkStatus.Unavailable)
            }
        }

        private fun getConnectivityManagerCallback() = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                val networkCapability = connectivityManager.getNetworkCapabilities(network)
                val hasNetworkConnection = networkCapability
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    ?: false

                /* normal version
                if (hasNetworkConnection) {
                    if (validNetworkConnections.isEmpty()) {
                        validNetworkConnections.add(network)
                    }

                    announceStatus()
                }
                 */

                // with socket version
                if (hasNetworkConnection) {
                    determineInternetAccess(network)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                validNetworkConnections.remove(network)
                announceStatus()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)

                val hasNetworkConnection = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                when {
                    /* normal version
                    hasNetworkConnection -> if (validNetworkConnections.isEmpty()) {
                        validNetworkConnections.add(network)
                    }
                     */

                    // with socket version
                    hasNetworkConnection -> determineInternetAccess(network)

                    else -> validNetworkConnections.remove(network)
                }

                announceStatus()
            }

        }

        private fun determineInternetAccess(network: Network) {
            CoroutineScope(Dispatchers.IO).launch {
                if (InternetAvailability.check()) {
                    withContext(Dispatchers.Main) {
                        if (validNetworkConnections.isEmpty()) {
                            validNetworkConnections.add(network)
                        }

                        announceStatus()
                    }
                }
            }
        }

    }

    object InternetAvailability {

        fun check(): Boolean = try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53))
            socket.close()
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }

    }

}
