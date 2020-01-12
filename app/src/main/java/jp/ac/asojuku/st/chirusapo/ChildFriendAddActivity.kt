package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_check_friend.*
import kotlinx.android.synthetic.main.activity_child_friend_add.*
import kotlinx.android.synthetic.main.activity_child_friend_add.friend_name
import kotlinx.android.synthetic.main.activity_child_registration.*
import java.util.*

class ChildFriendAddActivity : AppCompatActivity() {

    lateinit var realm: Realm

    //誕生日取得のためのデータ取得
    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)

    //性別
    private var gender = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_friend_add)

        realm = Realm.getDefaultInstance()
        friend_birthday_add.setOnClickListener { onBirthdaySetting() }

        supportActionBar?.let {
            title = "友達追加"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        friendAdd_Button.setOnClickListener { addFriend() }

    }

    override fun onResume() {
        super.onResume()

        friend_birthday_add.isFocusable = false

        //性別選択
        friend_gender_add.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.friend_gender_add)
                when (spinner.selectedItem.toString()) {
                    "未回答" -> gender = 0
                    "男性" -> gender = 1
                    "女性" -> gender = 2
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                gender = 0
            }
        }

    }

    private fun onBirthdaySetting(){
        val birthday = findViewById<EditText>(R.id.friend_birthday_add)
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener{ _, y, m, d ->
            val year = y.toString()
            var month = (m+1).toString()
            var day = d.toString()
            if(m < 9 ){
                month = "0$month"
            }
            if(d < 10){
                day = "0$day"
            }
            birthday.setText("%s-%s-%s".format(year, month, day))
        }, year,month,day
        ).show()
    }

    private fun userNameCheck():Boolean{
        val friendName= friend_name.editText?.text.toString().trim()

        return when {
            friendName.isEmpty() -> {
                friend_name.error = "ユーザー名が入力されていません"
                false
            }
            friendName.count() < 2 -> {
                friend_name.error = "ユーザー名の文字数が不正です"
                false
            }
            friendName.count() > 30 -> {
                friend_name.error = "ユーザー名の文字数が不正です"
                false
            }
            else -> {
                friend_name.error = null
                true
            }
        }
    }

    private fun memoCheck():Boolean{
        val memo= child_memo.editText?.text.toString().trim()

        return when {
            memo.isEmpty() -> {
                child_memo.error = "メモが入力されていません"
                false
            }
            else -> {
                child_memo.error = null
                true
            }
        }
    }

    private fun childBirthdayCheck():Boolean {
        val friendBirthday = friend_birthday_add.text.toString().trim()
        return when {
            friendBirthday.isEmpty() -> {
                friend_birthday_add.error = ""
                friend_error_add.text = "誕生日が未入力です"
                false
            }
            else -> {
                friend_birthday_add.error = null
                friend_error_add.text = null
                true
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addFriend(){

        var check = true
        if(!userNameCheck())check = false
        if(!childBirthdayCheck())check = false
        if(!check)return

        val account: Account? = realm.where<Account>().findFirst()
        val token = account!!.Rtoken
        val childId = intent.getStringExtra("childId")


        val params = hashMapOf(
            "token" to token,
            "child_id" to childId,
            "user_name" to friend_name.editText?.text.toString(),
            "birthday" to friend_birthday_add.text.toString().trim(),
            "gender" to gender.toString(),
            "memo" to child_memo.editText?.text.toString()
        )

        val paramImage = arrayListOf<ApiParamImage>()

        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else{
                when(it.getString("status")){
                    "200" -> {
                        Toast.makeText(applicationContext, "登録しました", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_CHILD -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNAUTHORIZED_OPERATION -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_USER_NAME -> {
                                    ApiError.showEditTextError(child_name,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_BIRTHDAY -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_GENDER -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_FRIEND_MEMO -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                    Toast.makeText(applicationContext, "メモ情報エラー", Toast.LENGTH_SHORT).show()
                                }
                                ApiError.ALLOW_EXTENSION -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UPLOAD_FAILED -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(ApiParam(Api.SLIM + "/child/friend/add", params,paramImage))
    }
}
