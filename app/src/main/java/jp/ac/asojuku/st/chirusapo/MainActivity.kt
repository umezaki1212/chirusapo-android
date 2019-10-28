package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.accounts.Account
import android.app.AlertDialog
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
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import io.realm.Realm
import android.view.View
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.layout_group_join.*


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

    private fun onSignOut() {
        android.app.AlertDialog.Builder(this)
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
    }

    //TODO 後でoverride fun onNavigationItemSelected(item: MenuItem): Booleanに修正

    //ログアウト時Realmで保存したデータをすべて削除する
    private fun onRealmDelete(){
        realm.executeTransaction{
            var user = realm.where<jp.ac.asojuku.st.chirusapo.Account>().findAll()
            var group = realm.where<JoinGroup>().findAll()
            var vaccine = realm.where<Vaccine>().findAll()
            var allergy = realm.where<Allergy>().findAll()
            user.deleteAllFromRealm()
            group.deleteAllFromRealm()
            vaccine.deleteAllFromRealm()
            allergy.deleteAllFromRealm()
        }

        groupJoin()
    }

    // グループ作成
    private fun groupCreate(){

        val inputView = View.inflate(this, R.layout.layout_group_create, null)

        //関連付け
        val group_id = inputView.findViewById(R.id.group_id) as TextInputLayout
        val group_name = inputView.findViewById(R.id.group_name) as TextInputLayout

        //Dialog生成
        val alertDlg = android.app.AlertDialog.Builder(this)
            .setTitle("グループ作成")
            .setView(inputView)
            .setPositiveButton(
                "作成"
            ) { dialog, which ->
                val token = "5t0DUpyH4yrgPp8tv"
                val groupId = group_id.editText?.text.toString()
                val groupName = group_name.editText?.text.toString()

                // APIとの通信を行う
                ApiPostTask{
                    // 処理した結果が帰ってくる
                    if (it == null) {
                        ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                    }
                    //なにかしら返答があった場合
                    else {
                        //statusを取得する
                        when (it.getString("status")) {
                            "200" -> {
                                val belongGroup = it.getJSONObject("data").getJSONArray("belong_group")
                                for (i in 0 until belongGroup.length()) {
                                    val groupInfo = belongGroup.getJSONObject(i)
                                    val groupInfoId = groupInfo.getString("group_id")
                                    val groupInfoName = groupInfo.getString("group_name")

                                    // Log.d("TEST", groupInfoId)
                                    // Log.d("TEST", groupInfoName)
                                }
                            }
                            "400" -> {
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        ApiError.VALIDATION_GROUP_ID -> {
                                            // 指定した値が正しくない
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.VALIDATION_GROUP_NAME -> {
                                            // 指定して値が正しくない
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.UNKNOWN_TOKEN -> {
                                            // ログイントークンの検証失敗
                                            val intent = Intent(this, SignInActivity::class.java).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            startActivity(intent)
                                            /*
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                            */
                                        }
                                        ApiError.ALREADY_CREATE_GROUP -> {
                                            // 既に同じグループIDが登録している場合
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )

                                        }
                                        ApiError.REQUIRED_PARAM -> {
                                            // 必要な値が見つかりませんでした表示
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.execute(ApiParam(
                    Api.SLIM + "group/create" ,
                    hashMapOf("token" to token,"group_id" to groupId,"group_name" to groupName)
                ))
            }
            .setNegativeButton("キャンセル", null)
            .create()
            .show()
    }

    // グループ参加
    private fun groupJoin(){

        val inputView = View.inflate(this, R.layout.layout_group_join, null)

        //関連付け
        val group_id = inputView.findViewById(R.id.group_id) as TextInputLayout
        val group_pin = inputView.findViewById(R.id.group_name) as TextInputLayout

        //Dialog生成
        val alertDlg = AlertDialog.Builder(this)
            .setTitle("グループ参加")
            .setView(inputView)
            .setPositiveButton(
                "参加"
            ) { dialog, which ->
                val token = "5t0DUpyH4yrgPp8tv"
                val groupId = group_id.editText?.text.toString()
                val groupPin = group_name.editText?.text.toString()

                // APIとの通信を行う
                ApiPostTask{
                    // 処理した結果が帰ってくる
                    if (it == null) {
                        ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                    }
                    //なにかしら返答があった場合
                    else {
                        //statusを取得する
                        when (it.getString("status")) {
                            "200" -> {
                                val belongGroup = it.getJSONObject("data").getJSONArray("belong_group")
                                for (i in 0 until belongGroup.length()) {
                                    val groupInfo = belongGroup.getJSONObject(i)
                                    val groupInfoId = groupInfo.getString("group_id")
                                    val groupInfoPin = groupInfo.getString("group_name")
                                }
                            }
                            "400" -> {
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        ApiError.VALIDATION_GROUP_ID -> {
                                            // 指定した値が正しくない
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.UNKNOWN_TOKEN -> {
                                            // ログイントークンの検証失敗
                                            val intent = Intent(this, SignInActivity::class.java).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            startActivity(intent)
                                        }
                                        ApiError.VALIDATION_PIN_CODE -> {
                                            // 指定した値が正しくない
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.UNKNOWN_GROUP -> {
                                            // グループが見つかりませんでした表示
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.VERIFY_PIN_CODE -> {
                                            // PINコードの検証に失敗しました表示
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.ALREADY_BELONG_GROUP -> {
                                            // 既にグループに所属しています表示
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.REQUIRED_PARAM -> {
                                            // 必要な値が見つかりませんでした表示
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.execute(ApiParam(
                    Api.SLIM + "group/join" ,
                    hashMapOf("token" to token,"group_id" to groupId,"group_name" to groupPin)
                ))
            }
            .setNegativeButton("キャンセル", null)
            .create()
            .show()
    }
}
