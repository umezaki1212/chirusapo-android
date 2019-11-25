package jp.ac.asojuku.st.chirusapo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import io.realm.Realm
import io.realm.RealmResults
import io.realm.exceptions.RealmException
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception
import java.util.regex.Pattern

class MainActivity : AppCompatActivity(),
    HomeFragment.OnFragmentInteractionListener,
    ChildFragment.OnFragmentInteractionListener,
    CalendarFragment.OnFragmentInteractionListener,
    AlbumFragment.OnFragmentInteractionListener,
    DressFragment.OnFragmentInteractionListener {

    lateinit var realm:Realm
    private lateinit var userToken:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        realm = Realm.getDefaultInstance()

        val drawerLayout:DrawerLayout = findViewById(R.id.drawer_layout)
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        //Bottom Navigation View
        val navigationView:NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_group_participation -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    groupJoin()
                    return@setNavigationItemSelectedListener false
                }
                R.id.nav_create_group -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    groupCreate()
                    return@setNavigationItemSelectedListener false
                }
                R.id.nav_config -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener false
                }
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener false
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }

        //SideMenuの表示情報取得
        val account = realm.where(Account::class.java).findFirst()
        if (account != null) {
            val headerView = navigationView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.account_user_id).text = account.Ruser_id
            headerView.findViewById<TextView>(R.id.account_name).text = account.Ruser_name
            userToken = account.Rtoken

            if (!account.Ruser_icon.isNullOrEmpty()) {
                Picasso.get().load(account.Ruser_icon).into(headerView.findViewById<ImageView>(R.id.account_image))
            }
        }

        val navBottomController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(navigation, navBottomController)

        //Side Menuの所属グループ取得
        val belongGroup = realm.where(JoinGroup::class.java).findAll()
        val menu = navigationView.menu
        val menuGroup = menu.addSubMenu("所属グループ")

        if (belongGroup != null) {
            for (group in belongGroup) {
                val groupId = group.Rgroup_id
                val groupName = group.Rgroup_name

                val menuItem = menuGroup.add("$groupName ($groupId)")
                menuItem.setOnMenuItemClickListener {
//                    Toast.makeText(this, groupId, Toast.LENGTH_SHORT).show()

                    try {
                        val showNumber = 1
                        val oldGroup: RealmResults<JoinGroup>? =
                            realm.where<JoinGroup>().equalTo("Rgroup_flag", showNumber).findAll()
                        if (oldGroup == null) {
                            Toast.makeText(this, "グループの取得に失敗しました", Toast.LENGTH_LONG).show()
                        } else {
                            for (g in oldGroup) {
                                realm.executeTransaction {
                                    g.Rgroup_flag = 0
                                }
                            }
                        }
                        val newGroup: JoinGroup? =
                            realm.where<JoinGroup>().equalTo("Rgroup_id", groupId).findFirst()

                        if (newGroup == null) {
                            Toast.makeText(this, "グループの取得に失敗しました", Toast.LENGTH_LONG).show()
                        } else {
                            realm.executeTransaction {
                                newGroup.Rgroup_flag = 1
                            }
                        }

                    }catch (e:Exception){
                        when(e){
                            //テスト中エラーがおきたらここに追加する
                            RealmException::class.java -> {
                                Toast.makeText(this, "nya-n", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }finally {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }

                    drawerLayout.closeDrawer(GravityCompat.START)
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    //Line起動
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_Line -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("line://nv/chat")
                    startActivity(intent)
                } catch (e:ActivityNotFoundException) {
                    Toast.makeText(this, "LINEが見つからないため起動できません", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_MenberList -> {
                true
            }
            R.id.action_Group_invitation -> {
                true
            }
            R.id.action_config -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFragmentInteraction(uri: Uri) { }

    //Realmのインスタンスを解放
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    // グループ作成
    private fun groupCreate(){
        val inputView = View.inflate(this, R.layout.layout_group_create, null)
        //関連付け
        val layoutGroupId = inputView.findViewById(R.id.group_id) as TextInputLayout
        val layoutGroupName = inputView.findViewById(R.id.group_name) as TextInputLayout

        layoutGroupId.editText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val inputGroupId = layoutGroupId.editText?.text.toString().trim()

                if (inputGroupId.length < 5) {
                    layoutGroupId.error = "5文字以上で入力してください"
                } else if (inputGroupId.length > 30) {
                    layoutGroupId.error = "30文字以下で入力してください"
                } else if (!Pattern.compile("^[a-zA-Z0-9-_]{1,30}\$").matcher(inputGroupId).find()) {
                    layoutGroupId.error = "使用できない文字が含まれています"
                } else {
                    layoutGroupId.error = null
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        layoutGroupName.editText?.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val inputGroupName = layoutGroupName.editText?.text.toString().trim()

                if (inputGroupName.isEmpty()) {
                    layoutGroupName.error = "1文字以上で入力してください"
                } else if (inputGroupName.length > 30) {
                    layoutGroupName.error = "30文字以下で入力してください"
                } else if (!Pattern.compile("^.{1,30}\$").matcher(inputGroupName).find()) {
                    layoutGroupName.error = "使用できない文字が含まれています"
                } else {
                    layoutGroupName.error = null
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        // Dialog生成
        AlertDialog.Builder(this)
            .setTitle("グループ作成")
            .setView(inputView)
            .setPositiveButton(
                "作成"
            ) { _, _ ->
                val groupId = layoutGroupId.editText?.text.toString()
                val groupName = layoutGroupName.editText?.text.toString()

                // APIとの通信を行う
                ApiPostTask{
                    // 処理した結果が帰ってくる
                    if (it == null) {
                        ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                    }
                    // なにかしら返答があった場合
                    else {
                        //statusを取得する
                        when (it.getString("status")) {
                            "200" -> {
                                val num1 = 1
                                val num2 = 0
                                realm.executeTransaction{
                                    val group = realm.where<JoinGroup>().equalTo("Rgroup_flag",num1).findAll()
                                    if(group != null){
                                        for(x in group){
                                            x.Rgroup_flag = num2
                                        }
                                    }
                                }
                                // 参加・作成したグループ情報の取得
                                val belongGroup = it.getJSONObject("data").getJSONArray("belong_group")
                                realm.executeTransaction{
                                    for (i in 0 until belongGroup.length()) {
                                        val groupInfo = belongGroup.getJSONObject(i)
                                        val groupInfoGroupId = groupInfo.getString("group_id")
                                        val groupInfoGroupName = groupInfo.getString("group_name")
                                        if(realm.where<JoinGroup>().equalTo("Rgroup_id",groupInfoGroupId).findFirst() == null) {
                                            // realmに保存する
                                            realm.createObject(JoinGroup::class.java, groupInfoGroupId).apply {
                                                Rgroup_name = groupInfoGroupName
                                                // 現在見ているグループに設定するためフラグを(1)にする
                                                Rgroup_flag = num1
                                            }
                                        }
                                    }
                                }

                                // メッセージ表示
                                AlertDialog.Builder(this)
                                    .setMessage("グループを作成しました")
                                    .setNegativeButton("閉じる", null)
                                    .create()
                                    .show()
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
                    hashMapOf("token" to userToken,"group_id" to groupId,"group_name" to groupName)
                ))
            }
            .setNegativeButton("キャンセル", null)
            .create()
            .show()
    }

    // グループ参加
    private fun groupJoin(){
        val inputView = View.inflate(this, R.layout.layout_group_join, null)

        // 関連付け
        val layoutGroupId = inputView.findViewById(R.id.group_id) as TextInputLayout
        val layoutGroupPin = inputView.findViewById(R.id.pin_code) as TextInputLayout

        layoutGroupId.editText?.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val inputGroupId = layoutGroupId.editText?.text.toString().trim()

                if(inputGroupId.length < 5){
                    layoutGroupId.error = "5文字以上入力してください"
                }else if (inputGroupId.length > 30) {
                    layoutGroupId.error = "30文字以下で入力してください"
                } else if (!Pattern.compile("^[a-zA-Z0-9-_]{1,30}\$").matcher(inputGroupId).find()) {
                    layoutGroupId.error = "使用できない文字が含まれています"
                } else {
                    layoutGroupId.error = null
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        layoutGroupPin.editText?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val inputGroupPin = layoutGroupPin.editText?.text.toString().trim()

                if(!Pattern.compile("^[0-9]{4}$").matcher(inputGroupPin).find()){
                    layoutGroupPin.error = "4文字の数字で入力してください"
                }else{
                    layoutGroupPin.error = null
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })


        // Dialog生成
        AlertDialog.Builder(this)
            .setTitle("グループ参加")
            .setView(inputView)
            .setPositiveButton(
                "参加"
            ) { _, _ ->
                val groupId = layoutGroupId.editText?.text.toString()
                val groupPin = layoutGroupPin.editText?.text.toString()

                // APIとの通信を行う
                ApiPostTask{
                    // 処理した結果が帰ってくる
                    if (it == null) {
                        ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                    }
                    // なにかしら返答があった場合
                    else {
                        //statusを取得する
                        when (it.getString("status")) {
                            "200" -> {
                                val num1 = 1
                                val num2 = 0
                                realm.executeTransaction{
                                    val group = realm.where<JoinGroup>().equalTo("Rgroup_flag", num1).findAll()
                                    if(group != null){
                                        for(x in group) {
                                            x.Rgroup_flag = num2
                                        }
                                    }
                                }
                                //参加・作成したグループ情報の取得
                                val belongGroup = it.getJSONObject("data").getJSONArray("belong_group")
                                realm.executeTransaction{
                                    for (i in 0 until belongGroup.length()) {
                                        val groupInfo = belongGroup.getJSONObject(i)
                                        val groupInfoGroupId = groupInfo.getString("group_id")
                                        val groupInfoGroupName = groupInfo.getString("group_name")
                                        if(realm.where<JoinGroup>().equalTo("Rgroup_id",groupInfoGroupId).findFirst() == null){
                                            // realmに保存する
                                            realm.createObject<JoinGroup>().apply{
                                                Rgroup_id = groupInfoGroupId
                                                Rgroup_name = groupInfoGroupName
                                                //現在見ているグループに設定するためフラグを(1)にする
                                                Rgroup_flag = num1
                                            }
                                        }
                                    }
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
                }.execute(
                    ApiParam(
                        Api.SLIM + "group/join" ,
                        hashMapOf("token" to userToken,"group_id" to groupId,"pin_code" to groupPin)
                    )
                )
            }
            .setNegativeButton("キャンセル", null)
            .create()
            .show()
    }
}
