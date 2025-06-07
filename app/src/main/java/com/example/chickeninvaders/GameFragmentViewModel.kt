package com.example.chickeninvaders

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random
import com.google.firebase.firestore.FirebaseFirestore

class GameFragmentViewModel : ViewModel() {


    companion object {
        const val NUM_COLUMNS = 5
        const val NUM_ROWS = 12   // 11 שורות לתרנגולות + שורה אחת לתחתית (חללית)
    }

    // -- 1) בסיס של השהיה (ms) בין גל לגל --
    private val BASE_DELAY_MS = 1000L
    // -- 2) מקדם מהירות (1f = רגיל, >1 = מהיר יותר, <1 = איטי יותר) --
    private var speedFactor = 1f
    fun setSpeedFactor(factor: Float) {
        speedFactor = factor.coerceIn(0.5f, 4f) // נגבילו בין 0.2 ל־3 כדי לא לעלות או למטה מדי
    }

    // כאן ניתן לשנות בקלות כמה תרנגולות יוצגו בכל גל
    private var chickensPerWave = 1

    // 3) משתנה לאחסון "עוף" (בונוס) שירד
    //     נשתמש במחלקת Chicken כדי לשמור row/col/drawableResId
    private val bonusList = mutableListOf<Chicken>()
    private val _bonusLiveData = MutableLiveData<List<Chicken>>(emptyList())
    val bonusLiveData: LiveData<List<Chicken>> = _bonusLiveData

    // 5) ניקוד (score)
    private val _scoreLiveData = MutableLiveData<Int>(0)
    val scoreLiveData: LiveData<Int> = _scoreLiveData


    // 2. פונקציה להוספת נקודות (נשתמש בה כשנקודות יצברו)
    fun increaseScore(by: Int) {
        // לוקחים את הערך הנוכחי, מוסיפים 'by', ומעדכנים
        val current = _scoreLiveData.value ?: 0
        _scoreLiveData.postValue(current + by)
    }

    // בסוף bloc ה‐companion (או מתחת לו) נוסיף:
    private var distance: Float = 0f

    // ניצור LiveData שתשדר את ערך ה‐distance הנוכחי:
    private val _distanceLiveData = MutableLiveData<Float>(0f)
    val distanceLiveData: LiveData<Float> = _distanceLiveData

    // הגדרת השדה:
    private var spawnSpeedMultiplier = 1.0f

    // המתודה שקוראים מה־Fragment:
    fun updateSpawnSpeedMultiplier(multiplier: Float) {
        spawnSpeedMultiplier = multiplier
    }

    // ממשיכים אחרי ההגדרות הקיימות בתוך המחלקה (ליד המתודות הקיימות):
    private val distanceHandler = Handler(Looper.getMainLooper())
    private val distanceRunnable = object : Runnable {
        override fun run() {
            // בכל פעם שה‐Runnable קורה, נוסיף אל distance את spawnSpeedMultiplier:
            distance += spawnSpeedMultiplier
            // נעדכן את ה־LiveData:
            _distanceLiveData.postValue(distance)
            // נקבע את הקריאה הבאה לעוד 1,000ms:
            distanceHandler.postDelayed(this, 1000L)
        }
    }

    // רשימת התרנגולות החיות (row=0..NUM_ROWS-2)
    private val chickensList = mutableListOf<Chicken>()
    private val _chickensLiveData = MutableLiveData<List<Chicken>>(emptyList())
    val chickensLiveData: LiveData<List<Chicken>> = _chickensLiveData

    // חיי החללית (לדוגמה מתחיל ב־3)
    private val _livesLiveData = MutableLiveData<Int>(3)
    val livesLiveData: LiveData<Int> = _livesLiveData

    private val _collisionEvent = MutableLiveData<Unit>()
    val collisionEvent: LiveData<Unit> = _collisionEvent

    // מיקום החללית בעמודת הגריד הנוכחית (יוזן מ־Fragment)
    private var spaceshipCol: Int = NUM_COLUMNS / 2
    fun updateSpaceshipCol(col: Int) {
        spaceshipCol = col
    }


    private val spawnHandler = Handler(Looper.getMainLooper())
    private val spawnRunnable = object : Runnable {
        override fun run() {
            spawnWave()
            moveChickensDown()


            // -- 3) חשב השהיה דינמית לפי speedFactor --
            val nextDelay = (BASE_DELAY_MS / speedFactor).toLong().coerceAtLeast(100L)
            spawnHandler.postDelayed(this, nextDelay)
        }
    }

    // אחרי השדה private val _collisionEvent...
    private val _bonusEvent = MutableLiveData<Unit>()
    val bonusEvent: LiveData<Unit> = _bonusEvent


    init {
        // מתחילים את הלולאה של יצירת תרנגולות ותזוזתן
        spawnHandler.post(spawnRunnable)

        // ** תוספת: נקרא גם ל־distanceRunnable (שיריץ את עצמו כל שנייה ) **
        distanceHandler.post(distanceRunnable)
    }

//    private fun spawnWave() {
//        // יוצרים גל חדש: chickensPerWave תרנגולות בשורה 0 בעמודות אקראיות
//        val availableCols = (0 until NUM_COLUMNS).shuffled().take(chickensPerWave)
//        for (col in availableCols) {
//            val drawableRes = if (Random.nextBoolean()) {
//                R.drawable.chicken_blue
//            } else {
//                R.drawable.chicken_red
//            }
//            val newChicken = Chicken(row = 0, col = col, drawableResId = drawableRes)
//            chickensList.add(newChicken)
//        }
//        _chickensLiveData.postValue(chickensList.toList())
//    }
private fun spawnWave() {
    // 1) בונים רשימה של העמודות שבהן נולדות התרנגולות הרגילות
    val availableCols = (0 until NUM_COLUMNS).shuffled().take(chickensPerWave).toMutableList()
    for (col in availableCols) {
        val drawableRes = if (Random.nextBoolean()) {
            R.drawable.chicken_blue
        } else {
            R.drawable.chicken_red
        }
        val newChicken = Chicken(row = 0, col = col, drawableResId = drawableRes)
        chickensList.add(newChicken)
    }

    // 2) מוצאים את כל העמודות שלא נוצלו עד כה (כדי שבונוס לא יתנגש בתחילת הגל)
    val remainingColsForBonus = (0 until NUM_COLUMNS).filter { it !in availableCols }
    if (remainingColsForBonus.isNotEmpty() && Random.nextFloat() < 0.5f) {
        // בוחרים עמודה אקראית מבין אלו שלא נבחרו
        val bonusCol = remainingColsForBonus.random()
        val bonusChicken = Chicken(row = 0, col = bonusCol, drawableResId = R.drawable.chicken)
        chickensList.add(bonusChicken)
    }

    // 3) עדכון ה־LiveData
    _chickensLiveData.postValue(chickensList.toList())
}

    private fun moveChickensDown() {
        // כדי לא לגרום ConcurrentModificationException, עוברים על ה־iterator של הרשימה
        val iterator = chickensList.iterator()

        while (iterator.hasNext()) {
            val chicken = iterator.next()

            // 1) הנמך שורה (עד השורה התחתונה)
            if (chicken.row < NUM_ROWS - 1) {
                chicken.row += 1
            }

            // 2) במידה והוא הגיע לשורה התחתונה (NUM_ROWS - 1), בודקים אם פוגע בחללית
            if (chicken.row == NUM_ROWS - 1) {
                // 2a) אם הוא באותה עמודה כמו החללית → נבדוק סוג (אויב או בונוס)
                if (chicken.col == spaceshipCol) {
                    when (chicken.drawableResId) {
                        // —————————————————— אויב רגיל (פוגע בחללית) ——————————————————
                        R.drawable.chicken_blue,
                        R.drawable.chicken_red -> {
                            // הסר חיים מהחללית
                            iterator.remove()
                            val currentLives = _livesLiveData.value ?: 0
                            _livesLiveData.postValue((currentLives - 1).coerceAtLeast(0))

                            // שגר אירוע התנגשות (Fragment יכול להקשיב ולהפעיל רטט/Toast וכד׳)
                            _collisionEvent.postValue(Unit)
                        }

                        // ———————————————— עוף־בונוס (ניקוד) ————————————————
                        R.drawable.chicken -> {

                            // כאן נוסיף קריאה ל־bonusEvent
                            _bonusEvent.postValue(Unit)

                            // הענק נקודה אחת (ולא נחליק חיים)
                            increaseScore(1)

                            // הסר את ה”עוף” מהרשימה, כי נאסף
                            iterator.remove()
                        }

                        // ——————————— איזו תמונה אחרת? במקרה קצה, פשוט נסיר אותה ———————————
                        else -> {
                            iterator.remove()
                        }
                    }
                }
                // 2b) אם הגיע לתחתית אך לא באותה עמודה (אין פגיעה) → פשוט מסיר אותו
                else {
                    iterator.remove()
                }
            }
        }

        // 3) עדכון ה־LiveData בסוף כדי שה־UI יתעדכן
        _chickensLiveData.postValue(chickensList.toList())

    }

    fun checkSpaceshipCollision() {
        val iterator = chickensList.iterator()
        while (iterator.hasNext()) {
            val chicken = iterator.next()
            // אם התרנגולת בשורה שלפני החללית ועמודה שלה = עמודת החללית
            if (chicken.row == NUM_ROWS - 1 && chicken.col == spaceshipCol) {
                when (chicken.drawableResId) {
                    // ————————— אויב רגיל (כחול/אדום) —————————
                    R.drawable.chicken_blue,
                    R.drawable.chicken_red -> {
                        iterator.remove()
                        val currentLives = _livesLiveData.value ?: 0
                        _livesLiveData.postValue((currentLives - 1).coerceAtLeast(0))
                        _collisionEvent.postValue(Unit)
                    }
                    // ———————— עוף-בונוס (drawable “chicken”) ————————
                    R.drawable.chicken -> {
                        iterator.remove()
                        increaseScore(1)
                        _bonusEvent.postValue(Unit)
                    }
                    // בכל מקרה אחר – נסיר ללא כל פעולה
                    else -> {
                        iterator.remove()
                    }
                }
            }
        }
        // חשוב לעדכן LiveData לאחר הסינון
        _chickensLiveData.postValue(chickensList.toList())
    }
    override fun onCleared() {
        super.onCleared()
        spawnHandler.removeCallbacks(spawnRunnable)
        distanceHandler.removeCallbacks(distanceRunnable)   // <--- תוספת עצירת ה־distanceRunnable
    }



    // עוצר את ה-Handlers
    fun pauseGame() {
        spawnHandler.removeCallbacks(spawnRunnable)
        distanceHandler.removeCallbacks(distanceRunnable)
    }

    // מפעיל אותם מחדש
    fun resumeGame() {
        spawnHandler.post(spawnRunnable)
        distanceHandler.post(distanceRunnable)
    }


}
