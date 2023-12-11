package mutnemom.android.kotlindemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.*
import okio.ByteString

class WebSocketActivity : AppCompatActivity() {

    companion object {
        @Suppress("unused")
        private val LOG_TAG = WebSocketActivity::class.java.simpleName

        private const val GDAX_URL = "wss://ws-feed.gdax.com"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_socket)
    }

    override fun onStart() {
        super.onStart()
        getCoinPrice()
    }

    private fun getCoinPrice() {
        val clientCoinPrice = OkHttpClient()
        val request = Request.Builder().url(GDAX_URL).build()

        val webSocketListenerCoinPrice = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val msg = """
                    |{
                    |  "type": "subscribe",
                    |  "channels": [{ "name": "ticker", "product_ids": ["ETH-USD"] }]
                    |}""".trimMargin()

                webSocket.send(msg)

                Log.e(LOG_TAG, "-> onOpen: $msg")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.e(LOG_TAG, "-> message: ${bytes.hex()}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e(LOG_TAG, "-> message: $text")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1_000, null)
                webSocket.cancel()

                Log.e(LOG_TAG, "-> close: $code $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.e(LOG_TAG, "-> closed")
                super.onClosed(webSocket, code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(LOG_TAG, "-> failure: ${response?.message}")
                t.printStackTrace()
                super.onFailure(webSocket, t, response)
            }
        }

        clientCoinPrice.newWebSocket(request, webSocketListenerCoinPrice)
        clientCoinPrice.dispatcher.executorService.shutdown()
    }
}
