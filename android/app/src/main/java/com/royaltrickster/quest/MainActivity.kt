package com.royaltrickster.quest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.royaltrickster.quest.core.di.ServiceLocator
import com.royaltrickster.quest.core.navigation.Navigator
import com.royaltrickster.quest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    val navigator: Navigator by lazy { Navigator(supportFragmentManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        val inflated = ActivityMainBinding.inflate(layoutInflater)
        binding = inflated
        setContentView(inflated.root)
        if (savedInstanceState == null) {
            navigator.showSplash()
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
