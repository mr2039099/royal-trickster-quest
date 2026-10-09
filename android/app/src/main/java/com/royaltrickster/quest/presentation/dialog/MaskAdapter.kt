package com.royaltrickster.quest.presentation.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.royaltrickster.quest.R
import com.royaltrickster.quest.core.ui.MaskBadgeView
import com.royaltrickster.quest.databinding.ItemMaskBinding
import com.royaltrickster.quest.domain.model.Mask

class MaskAdapter(
    private val onEquip: (Mask) -> Unit
) : RecyclerView.Adapter<MaskAdapter.MaskViewHolder>() {

    private val items = ArrayList<Mask>()
    private var unlockedIds: Set<String> = emptySet()
    private var equippedId: String = ""
    private var shards: Int = 0

    fun submit(masks: List<Mask>, unlocked: Set<String>, equipped: String, totalShards: Int) {
        items.clear()
        items.addAll(masks)
        unlockedIds = unlocked
        equippedId = equipped
        shards = totalShards
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaskViewHolder {
        val binding = ItemMaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaskViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MaskViewHolder(
        private val binding: ItemMaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mask: Mask) {
            val card = binding.root as com.google.android.material.card.MaterialCardView
            val context = card.context
            binding.maskSprite.setImageResource(MaskBadgeView.iconFor(mask.id))
            binding.maskName.text = mask.name
            binding.maskAbility.text = mask.tagline
            val unlocked = unlockedIds.contains(mask.id)
            val equipped = unlocked && mask.id == equippedId
            if (equipped) {
                card.setStrokeColor(ContextCompat.getColor(context, R.color.primary_purple))
                binding.maskLock.visibility = View.GONE
                binding.maskShardProgress.visibility = View.GONE
                binding.maskAction.visibility = View.GONE
                binding.maskStateFooter.visibility = View.VISIBLE
                binding.maskStateFooter.text = context.getString(R.string.wardrobe_equipped)
                card.alpha = 1f
                ViewCompat.setStateDescription(
                    card,
                    context.getString(R.string.wardrobe_equipped_state)
                )
            } else if (unlocked) {
                card.setStrokeColor(ContextCompat.getColor(context, R.color.stroke_purple_soft))
                binding.maskLock.visibility = View.GONE
                binding.maskShardProgress.visibility = View.GONE
                binding.maskAction.visibility = View.VISIBLE
                binding.maskStateFooter.visibility = View.GONE
                binding.maskAction.text = context.getString(R.string.wardrobe_equip)
                binding.maskAction.setOnClickListener { onEquip(mask) }
                card.alpha = 1f
                ViewCompat.setStateDescription(card, mask.ability.description)
            } else {
                val remaining = (mask.shardCost - shards).coerceAtLeast(0)
                card.setStrokeColor(ContextCompat.getColor(context, R.color.surface_lilac))
                binding.maskLock.visibility = View.VISIBLE
                binding.maskShardProgress.visibility = View.VISIBLE
                binding.maskShardProgress.max = mask.shardCost.coerceAtLeast(1)
                binding.maskShardProgress.progress = shards.coerceAtMost(mask.shardCost)
                binding.maskAction.visibility = View.GONE
                binding.maskStateFooter.visibility = View.VISIBLE
                binding.maskStateFooter.text =
                    context.getString(R.string.wardrobe_shard_progress, shards, mask.shardCost)
                card.alpha = 0.55f
                ViewCompat.setStateDescription(
                    card,
                    context.getString(R.string.wardrobe_locked_state, remaining)
                )
            }
            card.contentDescription = mask.name + " " + mask.tagline
        }
    }
}
