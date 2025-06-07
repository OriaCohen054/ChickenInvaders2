package com.example.chickeninvaders

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.chickeninvaders.databinding.FragmentHighScoresParentBinding

/**
 * HighScoresParentFragment – Fragment "על" שמכיל:
 * 1. HighScoresFragment (טבלת 10 השיאים)
 * 2. MapFragment (המפה שמציגה מיקום הפריט שנלחץ)
 * 3. כפתור Back to Home + נגינת שיר "דיסקו" כרקע
 */
class HighScoresParentFragment : Fragment(R.layout.fragment_high_scores_parent) {

    private var _binding: FragmentHighScoresParentBinding? = null
    private val binding get() = _binding!!

    // MediaPlayer שינגן את "שיר דיסקו" כרקע
    private var discoPlayer: MediaPlayer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHighScoresParentBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) כפתור HOME חזרה
        view.findViewById<ImageView>(R.id.ivHomeIcon).setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, HomeFragment.newInstance())
                .commit()
        }

        // 2) טען HighScoresFragment לתוך highScoresListContainer
        childFragmentManager.beginTransaction()
            .replace(R.id.highScoresListContainer, HighScoresFragment())
            .commit()

        // 3) טען MapFragment לתוך mapContainer
        childFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, MapFragment())
            .commit()

        // 4) התחל לנגן את "שיר הדיסקו" (R.raw.disco)
        if (discoPlayer == null) {
            // נניח ששמרתם את קובץ ה־MP3 בשם disco.mp3 בתיקיית res/raw/
            discoPlayer = MediaPlayer.create(requireContext(), R.raw.disco)
            discoPlayer?.isLooping = true
            discoPlayer?.setVolume(0.1f, 0.1f)  // ווליום נמוך כרקע
            discoPlayer?.start()
        }
    }

    override fun onResume() {
        super.onResume()
        if (discoPlayer == null) {
            discoPlayer = MediaPlayer.create(requireContext(), R.raw.disco)
            discoPlayer?.isLooping = true
            discoPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        discoPlayer?.stop()
        discoPlayer?.release()
        discoPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

