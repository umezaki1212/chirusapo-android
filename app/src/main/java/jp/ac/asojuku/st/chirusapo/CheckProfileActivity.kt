package jp.ac.asojuku.st.chirusapo

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.adapters.ChildListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.SampleChildItem
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_check_profile.*
import kotlinx.android.synthetic.main.child_list.*

class CheckProfileActivity : AppCompatActivity(){
    private lateinit var realm:Realm
    private lateinit var userToken:String
    private lateinit var targetUserId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_profile)

        val intent = intent
        if (intent.getStringExtra("USER_ID") == null) {
            finish()
        } else {
            targetUserId = intent.getStringExtra("USER_ID")!!
        }
//        targetUserId =
//        Log.d("TEST", targetUserId!!)
//        if (targetUserId == null) {
//            finish()
//        }

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        if (account != null) {
            userToken = account.Rtoken
        }

        getUserInfo()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getUserInfo() {
        ApiGetTask{
//            Log.d("TEST", it.toString())
            if(it == null){

            }
            else{
                when(it.getString("status")){
                    "200" -> {
                        // ユーザー情報取得
                        val userInfo = it.getJSONObject("data").getJSONObject("user_info")
                        // 名前情報取得・表示
                        val userName = userInfo.getString("user_name")
                        findViewById<TextView>(R.id.user_name).text = userName
                        // 誕生日情報取得・表示
                        val birthDay = userInfo.getString("birthday")
                        findViewById<TextView>(R.id.birthday).text = birthDay
                        // 性別情報取得・表示
                        var userSex = "null"
                        val userSexValue = userInfo.getString("gender")
                        when (userSexValue) {
                            "1" -> {
                                userSex = "男性"
                            }
                            "2" -> {
                                userSex = "女性"
                            }
                            "0" -> {
                                userSex = "未回答"
                            }
                        }
                        findViewById<TextView>(R.id.user_sex).text = userSex
                        // 自己紹介取得・表示
                        val userIntroduction = if (userInfo.isNull("introduction")) {
                            "未入力"
                        } else {
                            userInfo.getString("introduction")
                        }
                        findViewById<TextView>(R.id.introduction).text = userIntroduction

                        val list = ArrayList<SampleChildItem>()
                        // 子供情報取得・表示
                        val userChild = it.getJSONObject("data").getJSONArray("child_info")
                        for (i in 0 until userChild.length()){
                            val childInfo = userChild.getJSONObject(i)
                            val childId = childInfo.getString("user_id")
                            val childName = childInfo.getString("user_name")
                            val childAge = childInfo.getString("age")

                            val item = SampleChildItem()
                            item.id = i.toLong()
                            item.childId = childId
                            item.childName = childName
                            item.childAge = childAge

                            list.add(item)
                        }
                        val listView = user_child
                        val adapter = ChildListAdapter(this)
                        adapter.setSampleChildItem(list)
                        adapter.notifyDataSetChanged()
                        listView.adapter = adapter

//                        findViewById<TextView>(R.id.userchild).text = userChild

                        // LINE ID取得・表示
                        val user_Lineid = if(userInfo.isNull("line_id")){
                            null
                        }else{
                            userInfo.getString("line_id")
                        }
                        findViewById<TextView>(R.id.line_id).text = user_Lineid

                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for(i in 0 until errorArray.length()){
                            when(errorArray.getString(i)){
                                "REQUIRED_PARAM" -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                                "UNKNOWN_TOKEN" -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                                "UNKNOWN_TARGET_USER" -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                                "UNAUTHORIZED_OPERATION" -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "account/member-user-info",
                hashMapOf("token" to userToken, "target_user_id" to targetUserId)
            )
        )
    }

}