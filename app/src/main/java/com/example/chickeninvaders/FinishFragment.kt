package com.example.chickeninvaders

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

class FinishFragment : Fragment() {

    companion object {
        fun newInstance() = FinishFragment()
    }

    private val viewModel: FinishFragmentViewModel by viewModels()

    // שדה לניגון סאונד ה־“Game Over”
    private var gameOverPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // אין צורך בקוד נוסף כאן כרגע
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // נטען את ה־layout של fragment_finish.xml
        return inflater.inflate(R.layout.fragment_finish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) ניגון סאונד ה־“Game Over”
        // ודא שיש קובץ raw בשם track101.mp3 (או שנה ל־R.raw.loosing אם זה השם שלך)
        gameOverPlayer = MediaPlayer.create(requireContext(), R.raw.loosing)
        gameOverPlayer?.start()

        // 2) טיפול בלחיצת “Try Again” – חייב להיות בדיוק ID = btnPlayAgain
        val btnPlayAgain = view.findViewById<Button>(R.id.btnPlayAgain)
        btnPlayAgain.setOnClickListener {
            // עצירת הסאונד ושחרור המשאבים
            gameOverPlayer?.stop()
            gameOverPlayer?.release()
            gameOverPlayer = null

            // חזרה למסך המשחק (GameFragment)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GameFragment())
                .commit()
        }

        // 3) טיפול בלחיצת “Home” – מאזינים ל־View השקוף שקולט לחיצות (homeClickLayer)
        // כאן הקסט הוא ל-View ולא ל-FrameLayout
        val homeClickLayer = view.findViewById<View>(R.id.homeClickLayer)
        homeClickLayer.setOnClickListener {
            // עצור קודם את ה־gameOverPlayer אם עדיין רץ
            gameOverPlayer?.stop()
            gameOverPlayer?.release()
            gameOverPlayer = null

            // חזרה למסך הבית (HomeFragment)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment.newInstance())
                .commit()
        }
    }

    override fun onPause() {
        super.onPause()
        // וודא ששום MediaPlayer לא ממשיך לעבוד ברקע
        gameOverPlayer?.stop()
        gameOverPlayer?.release()
        gameOverPlayer = null
    }
}
