package com.example.chickeninvaders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chickeninvaders.databinding.FragmentHighScoresBinding

/**
 * מציג RecyclerView עם עשרת השיאים הגבוהים מ‐Firestore.
 * בלחיצה על פריט כלשהו – ה־Parent החליף ל‐MapFragment (שמיקם Marker)
 */
class HighScoresFragment : Fragment(R.layout.fragment_high_scores) {

    private val vm: HighScoresViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvHighScores)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = HighScoresAdapter(emptyList()) { /* no-op */ }

        vm.highScores.observe(viewLifecycleOwner) { list ->
            rv.adapter = HighScoresAdapter(list) { entry ->
                // לחיצה על שורה → מעבירה פרמטרים ל-MapFragment במיכל
                (parentFragment as? HighScoresParentFragment)?.childFragmentManager
                    ?.beginTransaction()
                    ?.replace(
                        R.id.mapContainer,
                        MapFragment().apply {
                            arguments = Bundle().apply {
                                putDouble("latitude", entry.latitude)
                                putDouble("longitude", entry.longitude)
                                putInt("score", entry.score.toInt())
                            }
                        }
                    )
                    ?.commit()
            }
        }
    }
}

