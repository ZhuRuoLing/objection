package icu.takeneko.objection

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import icu.takeneko.objection.ui.theme.ObjectionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "OBJECTION"

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var activity: MainActivity
    private lateinit var mediaPlayer: MediaPlayer
    private val coroutineScope by lazy {
        activity.lifecycleScope
    }
    private val imageAlpha = mutableFloatStateOf(0f)

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            if ((x + y + z) <= 1e-3) return
            if (z > 0.4) {
                Log.i(TAG, "onSensorChanged: Objection!")
                coroutineScope.launch(Dispatchers.Main) {
                    //mediaPlayer.stop()
                    imageAlpha.floatValue = 1f
                    mediaPlayer.start()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        activity = this

        setContent {
            val alpha by remember {
                this.imageAlpha
            }
            ObjectionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.objection_japanese_pc_trilogy),
                        contentDescription = "Objection!",
                        alpha = alpha
                    )
                }
            }
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.objection_japanese_pc_trilogy_phoenix)
        mediaPlayer.setOnCompletionListener {
            coroutineScope.launch(Dispatchers.Main) {
                imageAlpha.floatValue = 0f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}