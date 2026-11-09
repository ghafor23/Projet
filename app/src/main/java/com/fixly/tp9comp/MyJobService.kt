package com.fixly.tp9comp


import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import kotlin.concurrent.thread

class MyJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {

        thread {

            Log.d("Fitness", "Job started")

            Thread.sleep(3000)

            Log.d("Fitness", "Job finished")

            jobFinished(params, false)
        }

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}
