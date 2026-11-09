package com.fixly.tp9comp
import androidx.work.*
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocation: FusedLocationProviderClient

    private lateinit var txtDuration: TextView
    private lateinit var txtDistance: TextView
    private lateinit var txtCalories: TextView
    private lateinit var txtStatus: TextView

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var spinnerSport: Spinner
    private lateinit var progressBar: ProgressBar

    private lateinit var database: AppDatabase

    private var running = false
    private var distance = 0.0
    private var calories = 0.0
    private var startTime = 0L
    private var lastLocation: Location? = null

    private val handler = Handler(Looper.getMainLooper())

    private val timer = object : Runnable {
        override fun run() {

            if (running) {

                val seconds =
                    (SystemClock.elapsedRealtime() - startTime) / 1000

                val minutes = seconds / 60
                val remaining = seconds % 60

                txtDuration.text =
                    "Durée : %02d:%02d".format(minutes, remaining)

                handler.postDelayed(this, 1000)
            }
        }
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(result: LocationResult) {

            for (location in result.locations) {

                val previous = lastLocation

                if (previous != null) {

                    val meters = previous.distanceTo(location)

                    if (meters > 0 && meters < 100) {
                        distance += meters / 1000.0
                    }
                }

                lastLocation = location

                calories = distance * 60

                txtDistance.text =
                    "Distance : %.2f km".format(distance)

                txtCalories.text =
                    "Calories : %.0f kcal".format(calories)
            }
        }
    }

    private fun scheduleSync() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request =
            PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "fitness_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtDuration = findViewById(R.id.txtDuration)
        txtDistance = findViewById(R.id.txtDistance)
        txtCalories = findViewById(R.id.txtCalories)
        txtStatus = findViewById(R.id.txtStatus)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        spinnerSport = findViewById(R.id.spinnerSport)
        progressBar = findViewById(R.id.progressBar)

        fusedLocation =
            LocationServices.getFusedLocationProviderClient(this)

        database = AppDatabase.getDatabase(this)

        btnStart.setOnClickListener {
            startWorkout()
        }

        btnStop.setOnClickListener {
            stopWorkout()
        }

        scheduleSync()
    }

    private fun startWorkout() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )

            return
        }

        distance = 0.0
        calories = 0.0
        lastLocation = null

        startTime = SystemClock.elapsedRealtime()
        running = true

        btnStart.isEnabled = false
        btnStop.isEnabled = true

        txtStatus.text = "Entraînement en cours..."

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).setMinUpdateIntervalMillis(3000)
            .build()

        fusedLocation.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        handler.post(timer)

        // Thread secondaire pour exemple
        Thread {
            Thread.sleep(1000)

            runOnUiThread {
                if (running) {
                    txtStatus.text = "GPS activé - En cours"
                }
            }
        }.start()
    }

    private fun stopWorkout() {

        if (!running) return

        running = false

        fusedLocation.removeLocationUpdates(locationCallback)

        handler.removeCallbacks(timer)

        val duration =
            (SystemClock.elapsedRealtime() - startTime) / 1000

        val sport = spinnerSport.selectedItem.toString()

        val workout = Workout(
            sport = sport,
            distance = distance,
            calories = calories,
            duration = duration
        )

        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                database.workoutDao().insert(workout)
            }

            txtStatus.text = "Entraînement enregistré !"
        }

        txtDistance.text = "Distance : %.2f km".format(distance)
        txtCalories.text = "Calories : %.0f kcal".format(calories)

        btnStart.isEnabled = true
        btnStop.isEnabled = false

        progressBar.progress = 100
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(timer)

        if (running) {
            fusedLocation.removeLocationUpdates(locationCallback)
        }
    }
}
