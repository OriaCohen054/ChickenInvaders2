package com.example.chickeninvaders

import android.media.MediaPlayer
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.FragmentTransaction

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel if needed
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // בונים את ה־UI מתוך fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) כפתור Play → מעבר ל־GameFragment
        val btnPlay = view.findViewById<Button>(R.id.btnPlay)
        btnPlay.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GameFragment())
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }

        // 2) כפתור Trophy (High Scores) → כרגע רק TODO (בעתיד תטעינו HighScoresFragment)
        val btnTrophy = view.findViewById<ImageView>(R.id.btnTrophy)
        // כפתור Trophy (High Scores): יחליף ל־HighScoresFragment
        btnTrophy.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HighScoresParentFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    private var homeMusicPlayer: MediaPlayer? = null
    override fun onResume() {
        super.onResume()
        // ------------ 1) הפעלת מוסיקת רקע של Home ------------
        if (homeMusicPlayer == null) {

            homeMusicPlayer = MediaPlayer.create(requireContext(), R.raw.track1)
            homeMusicPlayer?.isLooping = true
            homeMusicPlayer?.setVolume(0.1f, 0.1f)
            homeMusicPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        // ------------ 2) עצירת המוסיקה ושחרור המשאבים ------------
        homeMusicPlayer?.stop()
        homeMusicPlayer?.release()
        homeMusicPlayer = null
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }

}
