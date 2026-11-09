package com.fixly.tp9comp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)

            // Récupération des entraînements non synchronisés
            val workouts = db.workoutDao().getUnsynced()

            // Ici on envoie les données au serveur.
            // Exemple pédagogique : simulation de synchronisation.

            if (workouts.isNotEmpty()) {
                db.workoutDao().markAllSynced()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
