package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

class BottomNavigation : AppCompatActivity(),
    HomeFragment.OnFragmentInteractionListener,
    ChildFragment.OnFragmentInteractionListener,
    CalendarFragment.OnFragmentInteractionListener,
    AlubamFragment.OnFragmentInteractionListener,
    DressFragment.OnFragmentInteractionListener {

//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        when (item.itemId) {
//            R.id.bottomNavigationView_home -> {
//                Toast.makeText(this, "ホームが選択されました", Toast.LENGTH_SHORT).show()
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.bottomNavigationView_child -> {
//                Toast.makeText(this, "子どもが選択されました", Toast.LENGTH_SHORT).show()
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.bottomNavigationView_calendar -> {
//                Toast.makeText(this, "カレンダーが選択されました", Toast.LENGTH_SHORT).show()
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.bottomNavigationView_album -> {
//                Toast.makeText(this, "アルバムが選択されました", Toast.LENGTH_SHORT).show()
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.bottomNavigationView_dress -> {
//                Toast.makeText(this, "着せ替えが選択されました", Toast.LENGTH_SHORT).show()
//                return@OnNavigationItemSelectedListener true
//            }
//        }
//        false
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
//        navigation.selectedItemId = R.id.bottomNavigationView_home

        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(navigation, navController)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}
