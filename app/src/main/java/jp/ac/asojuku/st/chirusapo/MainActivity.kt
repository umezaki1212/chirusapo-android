package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.accounts.Account
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import io.realm.Realm

class MainActivity : AppCompatActivity() {
    lateinit var realm: Realm
    private lateinit var menu: Menu

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_group_participation, R.id.nav_create_group,
                R.id.nav_config, R.id.nav_logout, R.id.nav_create_group
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        realm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //TODO 後でoverride fun onNavigationItemSelected(item: MenuItem): Booleanに修正
    fun onNavigationItemSelected(){
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        //when(item.itemId){
          //R.id.sign_out -> {
        AlertDialog.Builder(this)
            .setTitle("ログアウト")
            .setMessage("ログアウトしますか？")
            .setPositiveButton("ログアウト") { _, _ ->
                onRealmDelete()
                startActivity(intent)
                /*
                startActivity(
                    Intent(
                        this, TitleActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                */
            }
            .setNegativeButton("キャンセル", null)
            .show()
        //return false
        //}
        //}

    }
    //ログアウト時Realmで保存したデータをすべて削除する
    private fun onRealmDelete(){
        realm.executeTransaction{
            user = realm.where<Account>().findAll
            group = realm.where<JoinGroup>().findAll
            vaccine = realm.where<Vaccine>().findAll
            allergy = realm.where<Allergy>().findAll
            user.deleteAllFromRealm()
            group.deleteAllFromRealm()
            vaccine.deleteAllFromRealm()
            allergy.deleteAllFromRealm()
        }
    }
}
