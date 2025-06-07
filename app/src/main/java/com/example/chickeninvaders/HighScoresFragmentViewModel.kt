package com.example.chickeninvaders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query



/**
 * Data class המייצג רשומה בטבלת השיאים.
 * יש בו:
 *  - id: מזהה המסמך (צפויה לשמש ל-Map או עיבוד עתידי)
 *  - odometer: מרחק שצבר השחקן
 *  - score: מספר העופות (נקודות) שצבר
 *  - latitude / longitude: המיקום שבו הסתיים המשחק (או 0.0,0.0 אם לא ניתנה הרשאה)
 */
data class ScoreEntry(
    val odometer: Double = 0.0,
    val score: Long = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: com.google.firebase.Timestamp? = null
)

class HighScoresViewModel : ViewModel() {

    private val _highScores = MutableLiveData<List<ScoreEntry>>(emptyList())
    val highScores: LiveData<List<ScoreEntry>> = _highScores

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchTop10Scores()
    }

    private fun fetchTop10Scores() {
        // ניגש לאוסף "scores" בסדר יורד לפי "score" (גבוה → נמוך), מוגבל ל־10
        db.collection("scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val lat      = doc.getDouble("latitude")  ?: 0.0
                    val lon      = doc.getDouble("longitude") ?: 0.0
                    val scoreVal = doc.getLong("score")       ?: 0L
                    val odo      = doc.getDouble("odometer")  ?: 0.0    // <-- פה
                    val ts       = doc.getTimestamp("timestamp")
                    ScoreEntry(
                        odometer  = odo,       // <-- מולא
                        latitude  = lat,
                        longitude = lon,
                        score     = scoreVal,
                        timestamp = ts
                    )
                }

                _highScores.postValue(list)
            }
            .addOnFailureListener {
                // במקרה של שגיאה – נשאיר emptyList
                _highScores.postValue(emptyList())
            }
    }
}