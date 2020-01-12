package jp.ac.asojuku.st.chirusapo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_check_friend.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CheckFriendActivity: AppCompatActivity(){

    private lateinit var realm: Realm
    private lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_friend)

        supportActionBar?.let {
            title = "友達情報"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }


        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        if (account != null) {
            userToken = account.Rtoken
        }

        getFriendInfo()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getFriendInfo() {
        val childID = intent.getStringExtra("childId")
        val checkID =  intent.getStringExtra("friendId")

        ApiGetTask {
            if (it == null) {
                ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (it.getString("status")) {
                    "200" -> {
                        val friendMember =
                            it.getJSONObject("data").getJSONArray("friend_list")
                        for (i in 0 until friendMember.length()){
                            val memberInfo = friendMember.getJSONObject(i)
                            if (memberInfo.getString("id") == checkID){
                                friend_name.text = memberInfo.getString("user_name")
                                friend_birthday.text = memberInfo.getString("birthday")
                                var userSex = "null"
                                when (memberInfo.getString("gender")) {
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
                                friend_gender.text = userSex
                                friend_memo.text = memberInfo.getString("memo")
                            }
                        }
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                // 値が不足している場合に表示
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                                ApiError.UNAUTHORIZED_OPERATION -> {
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
                Api.SLIM + "child/friend/get",
                hashMapOf("token" to userToken, "child_id" to childID)
            )
        )
    }

}