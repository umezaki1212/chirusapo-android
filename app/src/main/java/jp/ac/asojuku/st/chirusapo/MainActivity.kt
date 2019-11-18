package jp.ac.asojuku.st.chirusapo

import android.net.Uri
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import androidx.navigation.ui.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(),
    HomeFragment.OnFragmentInteractionListener,
    ChildFragment.OnFragmentInteractionListener,
    CalendarFragment.OnFragmentInteractionListener,
    AlbumFragment.OnFragmentInteractionListener,
    DressFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navBottomController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(navigation, navBottomController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}
