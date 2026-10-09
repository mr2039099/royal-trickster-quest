package com.royaltrickster.quest.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.royaltrickster.quest.R
import com.royaltrickster.quest.core.di.ServiceLocator
import com.royaltrickster.quest.databinding.DialogMaskWardrobeBinding
import com.royaltrickster.quest.domain.model.Mask

class MaskWardrobeDialog : DialogFragment() {

    private var _binding: DialogMaskWardrobeBinding? = null
    private val binding get() = _binding

    private var adapter: MaskAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_RoyalTricksterQuest_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = DialogMaskWardrobeBinding.inflate(inflater, container, false)
        _binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        ServiceLocator.init(view.context.applicationContext)
        val list = MaskAdapter { mask -> equip(mask) }
        adapter = list
        bound.wardrobeList.layoutManager = GridLayoutManager(view.context, 2)
        bound.wardrobeList.adapter = list
        bound.wardrobeClose.setOnClickListener { dismissSafely() }
        refresh()
    }

    override fun onStart() {
        super.onStart()
        try {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        } catch (e: Exception) {
            return
        }
    }

    private fun refresh() {
        val bound = binding ?: return
        val progress = ServiceLocator.getPlayerProgress()
        val masks = ServiceLocator.maskRepository.masks()
        adapter?.submit(
            masks,
            ServiceLocator.maskRepository.unlockedIds(),
            ServiceLocator.maskRepository.equippedId(),
            progress.shards
        )
        bound.wardrobeEmptyHint.visibility =
            if (progress.shards <= 0) View.VISIBLE else View.GONE
        bound.wardrobeShardTotal.text =
            getString(R.string.wardrobe_shard_progress, progress.shards, LAST_THRESHOLD)
    }

    private fun equip(mask: Mask) {
        try {
            if (!isAdded) {
                return
            }
            if (ServiceLocator.equipMask(mask.id)) {
                refresh()
            }
        } catch (e: Exception) {
            return
        }
    }

    private fun dismissSafely() {
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        _binding?.wardrobeList?.adapter = null
        adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "mask_wardrobe_dialog"
        private const val LAST_THRESHOLD = 50
    }
}
