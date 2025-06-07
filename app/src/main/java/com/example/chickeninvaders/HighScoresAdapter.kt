package com.example.chickeninvaders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chickeninvaders.databinding.ItemHighScoreBinding

/**
 * Adapter שמקבל רשימת ScoreEntry (מ‐Firestore), מציג עד 10 פריטים.
 * כל ViewHolder מראה:
 *   • tvRank – "מקום ${position+1}"
 *   • tvScore – "נקודות ${entry.score}"
 *
 * ה־callback נשלח ל־HighScoresFragment/ParentFragment כשלוחצים על פריט.
 */
class HighScoresAdapter(
    private val items: List<ScoreEntry>,
    private val onClick: (ScoreEntry) -> Unit
) : RecyclerView.Adapter<HighScoresAdapter.HighScoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighScoreViewHolder {
        val binding = ItemHighScoreBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return HighScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HighScoreViewHolder, position: Int) {
        val entry = items[position]
        holder.bind(entry, position, onClick)
    }

    override fun getItemCount(): Int = items.size.coerceAtMost(10)

    inner class HighScoreViewHolder(
        private val binding: ItemHighScoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: ScoreEntry, position: Int, onClick: (ScoreEntry) -> Unit) {
            binding.tvRank.text = "Rank ${position + 1}"

            binding.tvScore.text = "Score ${entry.score}"

            binding.tvOdometer.text = "Dist: ${entry.odometer.toInt()}"

            binding.root.setOnClickListener {
                onClick(entry)
            }
        }
    }
}
