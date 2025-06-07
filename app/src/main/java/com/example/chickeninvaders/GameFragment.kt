package com.example.chickeninvaders

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlin.math.roundToInt
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest


class GameFragment : Fragment(), SensorEventListener {

    private val viewModel: GameFragmentViewModel by viewModels()

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gridLayout: GridLayout
    private lateinit var bottomRow: LinearLayout
    private lateinit var livesLayout: LinearLayout

    private var bonusPlayer: MediaPlayer? = null

    // מערך של ImageView‐ים שמייצגים את משבצות החללית בתחתית
    private val spaceshipSlots = arrayOfNulls<ImageView>(GameFragmentViewModel.NUM_COLUMNS)
    private var spaceshipCol = GameFragmentViewModel.NUM_COLUMNS / 2 // מתחילים באמצע

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private val TILT_THRESHOLD = 2.0f
    private val MOVE_COOLDOWN_MS = 300L
    private var lastMoveTime = 0L
    // סף הטייה למעלה/למטה
    private val VERTICAL_TILT_THRESHOLD = 0.8f

    private lateinit var odometerTextView: TextView


    // -------------- שדה לניגון המוסיקה ברקע --------------
    private var gameMusicPlayer: MediaPlayer? = null

    // -------------- שדה לניגון סאונד התנגשות --------------
    // אפשר להיעזר ב־MediaPlayer בינארי לכל התנגשות (לא האופטימלי מבחינת ביצועים,
    // אבל פותר בקלות באם אין הרבה התנגשות בו זמנית).
    private var collisionPlayer: MediaPlayer? = null





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // נטען את ה־layout של ה־Fragment
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridLayout = view.findViewById(R.id.gridLayout)
        bottomRow = view.findViewById(R.id.bottomRow)
        livesLayout = view.findViewById(R.id.livesLayout)

        // 2. אתחיל את ה־TextView של הניקוד
        val scoreTextView = view.findViewById<TextView>(R.id.scoreText)

        // 3. הרשמה כ־Observer עבור scoreLiveData
        viewModel.scoreLiveData.observe(viewLifecycleOwner) { score ->
            // כאן רק מציבים את המספר (למשל "10"), בלי הטקסט "Score: "
            scoreTextView.text = "$score"
        }

        odometerTextView = view.findViewById(R.id.odometerText)

        // מאפסים את חלונות החללית (slot0..slot4)
        for (i in 0 until GameFragmentViewModel.NUM_COLUMNS) {
            val slotId = resources.getIdentifier("slot$i", "id", requireContext().packageName)
            spaceshipSlots[i] = view.findViewById(slotId)
        }

        // מוציאים את כל המשבצות (invisible) אלא רק זו שנבחרה
        updateSpaceshipPosition()

        // נדאג להתעכב עד ש־gridLayout יתאפיין במידותיו כדי שנוכל לחשב את גודל המשבצת
        gridLayout.post {
            subscribeToViewModel()
        }

        // הכנה ל־Tilt באמצעות SensorManager
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    // קריאה לקבלת המיקום
    private fun fetchLocationAndSaveGame() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        // ברגע שיש לנו את הקואורדינטות – שומרים
                        saveGameToFirestore(
                            odometer = viewModel.distanceLiveData.value ?: 0.0f,
                            score = viewModel.scoreLiveData.value ?: 0,
                            latitude = lat,
                            longitude = lon
                        )
                    } else {
                        // לא קיבלנו מיקום (יכול לקרות במצבים מסוימים) → נשמור 0.0, 0.0
                        saveGameToFirestore(
                            odometer = viewModel.distanceLiveData.value ?: 0.0f,
                            score = viewModel.scoreLiveData.value ?: 0,
                            latitude = 0.0,
                            longitude = 0.0
                        )
                    }
                }
                .addOnFailureListener {
                    // אם קרתה שגיאה בקבלת המיקום, שמור 0.0, 0.0
                    saveGameToFirestore(
                        odometer = viewModel.distanceLiveData.value ?: 0.0f,
                        score = viewModel.scoreLiveData.value ?: 0,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                }
        } catch (e: SecurityException) {
            // אם בכל זאת חסרה הרשאה – שמור 0.0, 0.0
            saveGameToFirestore(
                odometer = viewModel.distanceLiveData.value ?: 0.0f,
                score = viewModel.scoreLiveData.value ?: 0,
                latitude = 0.0,
                longitude = 0.0
            )
        }
    }
    private fun saveGameToFirestore(odometer:Float,score: Int, latitude: Double, longitude: Double) {
        // א. מחלקת הנתונים שנשלח ל־Firestore
        val data = hashMapOf<String, Any>(
            "odometer" to odometer,
            "score" to score,
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to FieldValue.serverTimestamp()  // מוסיף זמן יצירה אוטומטית
        )

        // ב. שולחים למסד ה־Firestore בתור מסמך חדש בתוך אוסף "scores"
        val db = FirebaseFirestore.getInstance()
        db.collection("scores")
            .add(data)
            .addOnSuccessListener { documentReference ->
                // השמירה בוצעה בהצלחה – אפשר לעשות לוג או הודעה קלה
                Log.d("Firebase", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // משהו קרה בשמירה – אפשר לוג של השגיאה
                Log.e("Firebase", "Error adding document", e)
            }
    }
    private fun subscribeToViewModel() {
        // מאזינים לשינויי התרנגולות
        viewModel.chickensLiveData.observe(viewLifecycleOwner, Observer { chickens ->
            updateChickensUI(chickens)
        })

        // מעלים לחיים (hearts) וכן בודקים מתי מגיע לערך 0
        viewModel.livesLiveData.observe(viewLifecycleOwner, Observer { lives ->
            updateLivesUI(lives)

            // ברגע שהלבבות == 0, נפנה ל־FinishFragment
            if (lives <= 0) {
                // ראשון: נקרא לפונקציה שבודקת הרשאה, מקבלת מיקום ושומרת
                checkLocationPermissionAndSaveIfNeeded()

                // מבצעים החלפת Fragment למסך הסיום
                // R.id.fragment_container הוא אותו מזהה של ה־FrameLayout ב־activity_main.xml
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FinishFragment.newInstance())
                    .addToBackStack(null)  // אם תרצו לאפשר חזור בלחצן Back
                    .commit()
            }
        })

        // ניקוד – הצגה של המשתנה
        viewModel.scoreLiveData.observe(viewLifecycleOwner, Observer { score ->
            view?.findViewById<TextView>(R.id.scoreText)?.text = score.toString()
        })

        // מד המרחק (odometer)
        viewModel.distanceLiveData.observe(viewLifecycleOwner, Observer { distanceValue ->
            odometerTextView.text = String.format("%.1f", distanceValue)
        })

        // אירוע התנגשות – רטט + Toast
        viewModel.collisionEvent.observe(viewLifecycleOwner, Observer {
            triggerCollisionFeedback()
        })


        viewModel.bonusEvent.observe(viewLifecycleOwner) {
            playBonusSound()
        }
    }
    // בדיקה אם יש הרשאה, ואם לא – לבקש
    private fun checkLocationPermissionAndSaveIfNeeded() {
        val ctx = requireContext()
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // יש כבר הרשאה → נקבל מיקום ונשמור
            fetchLocationAndSaveGame()
        } else {
            // אין הרשאה – נבקש למשתמש
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }
    // --------------------------------------------
// 2) Callback לתוצאת בקשת ההרשאה
// --------------------------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndSaveGame()
            } else {
                // אם המשתמש סירב או קרתה שגיאה – שמירה עם מיקום 0.0, 0.0
                saveGameToFirestore(
                    odometer = viewModel.distanceLiveData.value ?: 0.0f,
                    score = viewModel.scoreLiveData.value ?: 0,
                    latitude = 0.0,
                    longitude = 0.0
                )
            }
        }
    }

    private fun playBonusSound() {
        // עצירת השחקן הקודם (אם רץ) ושחרור משאבים
        bonusPlayer?.stop()
        bonusPlayer?.release()
        bonusPlayer = null

        // יצירת MediaPlayer חדש לצליל הבונוס
        bonusPlayer = MediaPlayer.create(requireContext(), R.raw.coins)
        bonusPlayer?.start()

        // כשסיים לנגן, לשחרר אוטומטית
        bonusPlayer?.setOnCompletionListener { mp ->
            mp.release()
            bonusPlayer = null
        }
    }


    private fun updateChickensUI(chickens: List<Chicken>) {
        // מנקים את כל ה‐Views ב־GridLayout
        gridLayout.removeAllViews()

        // מחשבים את רוחב המשבצת לפי רוחב ה־GridLayout
        val totalWidth = gridLayout.width - gridLayout.paddingLeft - gridLayout.paddingRight
        val cellSize = (totalWidth / GameFragmentViewModel.NUM_COLUMNS.toFloat()).roundToInt()

        // מוסיפים ImageView לכל תרנגולת
        for (chicken in chickens) {
            val imageView = ImageView(requireContext()).apply {
                setImageResource(chicken.drawableResId)
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            val params = GridLayout.LayoutParams(
                GridLayout.spec(chicken.row),
                GridLayout.spec(chicken.col)
            ).apply {
                width = cellSize
                height = cellSize
            }
            imageView.layoutParams = params
            gridLayout.addView(imageView)
        }
        // נשמור על מיקום החללית בתצוגה
        updateSpaceshipPosition()
    }

    private fun updateSpaceshipPosition() {
        // קודם מסתירים את כל המשבצות
        for (i in 0 until GameFragmentViewModel.NUM_COLUMNS) {
            spaceshipSlots[i]?.visibility = View.INVISIBLE
        }
        // ואז מציגים רק את זו המתאימה ל־spaceshipCol
        val slot = spaceshipSlots[spaceshipCol]
        slot?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.spaceship_1)
        }

        // לעדכן את ה־ViewModel במיקום העמודה הנוכחי של החללית
        viewModel.updateSpaceshipCol(spaceshipCol)

       viewModel.checkSpaceshipCollision()
    }

    private fun updateLivesUI(lives: Int) {
        // מנקים ומוסיפים לבבות לפי lives
        livesLayout.removeAllViews()
        for (i in 0 until lives) {
            val heart = ImageView(requireContext()).apply {
                setImageResource(R.drawable.heart)
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            val sizeDp = 32
            val scale = resources.displayMetrics.density
            val sizePx = (sizeDp * scale).roundToInt()
            heart.layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                marginEnd = (4 * scale).roundToInt()
            }
            livesLayout.addView(heart)
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.resumeGame()

        // ------------------ 1) ניגון מוסיקת רקע של המשחק ------------------
        if (gameMusicPlayer == null) {
            gameMusicPlayer = MediaPlayer.create(requireContext(), R.raw.track3)
            gameMusicPlayer?.isLooping = true
            gameMusicPlayer?.setVolume(0.1f,0.1f)
            gameMusicPlayer?.start()
        }

        accelSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseGame()
        // ------------------ עצירת מוסיקת הרקע של המשחק ------------------
        gameMusicPlayer?.stop()
        gameMusicPlayer?.release()
        gameMusicPlayer = null

        // אםCollisionPlayer עדיין רץ, נעצור אותו ונשחרר
        collisionPlayer?.stop()
        collisionPlayer?.release()
        collisionPlayer = null

        bonusPlayer?.stop()
        bonusPlayer?.release()
        bonusPlayer = null

        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]      // X: הטיית הטלפון ימינה/שמאלה
        val y = event.values[1]      // Y: הטיית הטלפון קדימה/אחורה
        val now = System.currentTimeMillis()

        // — 1) עדכון תזוזת החללית (X-axis) כפי שהיה קודם, עם cooldown —
        if (now - lastMoveTime >= MOVE_COOLDOWN_MS) {
            when {
                // שימו לב: אנחנו הופכים את הכיוונים, כפי שביקשתם:
                x < -TILT_THRESHOLD && spaceshipCol > 0 -> {
                    spaceshipCol--
                    updateSpaceshipPosition()
                    lastMoveTime = now
                }
                x > TILT_THRESHOLD && spaceshipCol < GameFragmentViewModel.NUM_COLUMNS - 1 -> {
                    spaceshipCol++
                    updateSpaceshipPosition()
                    lastMoveTime = now
                }
                // אחרת: לא זזים חללית
            }
        }

        // — 2) עדכון מהירות המשחק (Y-axis: קדימה מאיץ, אחורה מאט) —
        when {
            y < -VERTICAL_TILT_THRESHOLD -> {
                // הטייה קדימה → משחק מהיר יותר
                viewModel.setSpeedFactor(3f)
                viewModel.updateSpawnSpeedMultiplier( 3f)
            }
            y > VERTICAL_TILT_THRESHOLD -> {
                // הטייה אחורה → משחק איטי יותר
                viewModel.setSpeedFactor(0.3f)
                viewModel.updateSpawnSpeedMultiplier( 0.3f)
            }
            else -> {
                // בערך ניטרלי → מהירות רגילה
                viewModel.setSpeedFactor(1f)
                viewModel.updateSpawnSpeedMultiplier(1f )
            }
        }
    }


    /**
     * פונקציה שמבצעת רטט קצר (200מ״ש) + Toast
     * נקראת ברגע שחל התנגשות
     */
    private fun triggerCollisionFeedback() {
        // 1. רטט קצר (200 מ״ש)
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }

        // 2. הודעת Toast קצרה
        Toast.makeText(requireContext(), "Ouch! A chicken got you!", Toast.LENGTH_SHORT).show()

        // 3. ניגון סאונד התנגשות (Track 7 או Track 8)
        collisionPlayer?.stop()
        collisionPlayer?.release()
        collisionPlayer = MediaPlayer.create(requireContext(), R.raw.track7)
        collisionPlayer?.start()
        collisionPlayer?.setOnCompletionListener { mp ->
            mp.release()
            collisionPlayer = null
        }

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // אין צורך לממש במקרה הזה
    }
}
