package jp.ac.asojuku.st.chirusapo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

class BottomNavigation : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.bottomNavigationView_home -> {
                message.setText(R.string.bottom_navigation_var_title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.bottomNavigationView_child -> {
                message.setText(R.string.bottom_navigation_var_title_child)
                return@OnNavigationItemSelectedListener true
            }
            R.id.bottomNavigationView_calendar -> {
                message.setText(R.string.bottom_navigation_var_title_calendar)
                return@OnNavigationItemSelectedListener true
            }
            R.id.bottomNavigationView_album -> {
                message.setText(R.string.bottom_navigation_var_title_album)
                return@OnNavigationItemSelectedListener true
            }
            R.id.bottomNavigationView_dress -> {
                message.setText(R.string.bottom_navigation_var_title_dress)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.bottomNavigationView_home
    }
}
