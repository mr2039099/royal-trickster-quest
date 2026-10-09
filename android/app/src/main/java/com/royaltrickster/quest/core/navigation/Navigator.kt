package com.royaltrickster.quest.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.royaltrickster.quest.R
import com.royaltrickster.quest.presentation.game.GameFragment
import com.royaltrickster.quest.presentation.gameover.GameOverFragment
import com.royaltrickster.quest.presentation.menu.MenuFragment
import com.royaltrickster.quest.presentation.splash.SplashFragment

class Navigator(private val fragmentManager: FragmentManager) {

    fun showSplash(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SplashFragment(), TAG_SPLASH)
            .commit()
        return true
    }

    fun showMenu(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        clearBackStack()
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in_scale,
                R.anim.fade_out_down,
                R.anim.fade_in_scale,
                R.anim.fade_out_down
            )
            .replace(R.id.fragment_container, MenuFragment(), TAG_MENU)
            .commit()
        return true
    }

    fun showGame(questNumber: Int): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.fade_in_scale,
                R.anim.fade_out_down
            )
            .replace(R.id.fragment_container, GameFragment.newInstance(questNumber), TAG_GAME)
            .addToBackStack(TAG_GAME)
            .commit()
        return true
    }

    fun showResult(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        popEntry(TAG_GAME)
        popEntry(TAG_RESULT)
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in_up,
                R.anim.fade_out_scale,
                R.anim.fade_in_scale,
                R.anim.fade_out_down
            )
            .replace(R.id.fragment_container, GameOverFragment(), TAG_RESULT)
            .addToBackStack(TAG_RESULT)
            .commit()
        return true
    }

    fun restartGame(questNumber: Int): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        popEntry(TAG_RESULT)
        return showGame(questNumber)
    }

    fun backToMenu(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        clearBackStack()
        val menu = fragmentManager.findFragmentByTag(TAG_MENU)
        if (menu != null && menu.isAdded) {
            return true
        }
        return showMenu()
    }

    fun currentFragment(): Fragment? = fragmentManager.findFragmentById(R.id.fragment_container)

    private fun popEntry(tag: String) {
        try {
            if (fragmentManager.isStateSaved) {
                return
            }
            fragmentManager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (e: Exception) {
            return
        }
    }

    private fun clearBackStack() {
        try {
            if (fragmentManager.isStateSaved) {
                return
            }
            while (fragmentManager.backStackEntryCount > 0) {
                if (!fragmentManager.popBackStackImmediate()) {
                    return
                }
            }
        } catch (e: Exception) {
            return
        }
    }

    private companion object {
        const val TAG_SPLASH = "splash"
        const val TAG_MENU = "menu"
        const val TAG_GAME = "game"
        const val TAG_RESULT = "result"
    }
}
