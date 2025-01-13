package se.sverigekanalen

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private var playerView: PlayerView? = null
    private var player: ExoPlayer? = null
    private val client = OkHttpClient()
    private val sharedPreferences by lazy {
        getSharedPreferences("SverigekanalenPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)

        // Try fetching the HLS URL from SharedPreferences first
        val cachedUrl = getCachedHlsUrl()

        if (cachedUrl != null) {
            // If the URL is cached, validate if it is playable
            if (isUrlPlayable(cachedUrl)) {
                initializePlayer(cachedUrl)
            } else {
                // Fetch a new URL if cached URL is not playable
                fetchHlsUrl { hlsUrl ->
                    if (hlsUrl != null) {
                        cacheHlsUrl(hlsUrl) // Cache the fetched URL
                        initializePlayer(hlsUrl)
                    } else {
                        Log.e("Sverigekanalen", "Failed to fetch HLS URL")
                    }
                }
            }
        } else {
            // Fetch the HLS URL from the Cloudflare Worker if not cached
            fetchHlsUrl { hlsUrl ->
                if (hlsUrl != null) {
                    cacheHlsUrl(hlsUrl) // Cache the fetched URL
                    initializePlayer(hlsUrl)
                } else {
                    Log.e("Sverigekanalen", "Failed to fetch HLS URL")
                }
            }
        }
    }

    private fun fetchHlsUrl(callback: (String?) -> Unit) {
        val request = Request.Builder()
            .url("https://sverigekanalen-hls.chandrabose.workers.dev/") // Cloudflare Worker URL
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val hlsUrl = response.body?.string() // Get plain text response
                    runOnUiThread { callback(hlsUrl) }
                } else {
                    runOnUiThread { callback(null) }
                }
            } catch (e: Exception) {
                Log.e("Sverigekanalen", "Error fetching HLS URL", e)
                runOnUiThread { callback(null) }
            }
        }.start()
    }

    private fun initializePlayer(hlsUrl: String) {
        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build().apply {
            playerView?.player = this

            // Set the HLS stream URL
            val uri = Uri.parse(hlsUrl)
            val mediaItem = MediaItem.fromUri(uri)

            // Set the media item to be played
            setMediaItem(mediaItem)

            // Prepare the player
            prepare()

            // Start playback
            play()
        }
    }

    private fun getCachedHlsUrl(): String? {
        // Retrieve the cached URL from SharedPreferences
        return sharedPreferences.getString("HLS_URL", null)
    }

    private fun cacheHlsUrl(hlsUrl: String) {
        // Save the HLS URL to SharedPreferences
        sharedPreferences.edit().putString("HLS_URL", hlsUrl).apply()
    }

    private fun isUrlPlayable(url: String): Boolean {
        val request = Request.Builder().url(url).head().build()
        return try {
            val response = client.newCall(request).execute()
            response.isSuccessful // Returns true if status code is 2xx
        } catch (e: Exception) {
            false // In case of network error or invalid URL
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}