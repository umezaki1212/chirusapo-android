package jp.ac.asojuku.st.chirusapo

import android.accounts.Account
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import io.realm.Realm
import io.realm.kotlin.createObject
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*


class SignUpActivity : AppCompatActivity() {
    private lateinit var spEditor: SharedPreferences.Editor
    lateinit var realm:Realm
    val calender = Calendar.getInstance()
    val year = calender.get(Calendar.YEAR)
    val month = calender.get(Calendar.MONTH)
    val day = calender.get(Calendar.DAY_OF_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        } ?: IllegalAccessException("Toolbar cannot be null")
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
        return true
    }

    override fun onResume() {
        super.onResume()
        user_gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = parent as Spinner
                val select = spinner.selectedItem.toString()
                if(select == "男性") {
                    spEditor.putString("user_gender", "1").apply()
                }
                else if(select == "女性"){
                    spEditor.putString("user_gender","2").apply()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spEditor.putString("user_gender","0").apply()
            }
        }
        user_birthday.setOnClickListener { onBirthdaySetting() }
        AccountCreate_button.setOnClickListener { onSignUp() }
    }

    private fun onBirthdaySetting(){
        val birthday = findViewById (R.id.user_birthday) as EditText
        DatePickerDialog(this,DatePickerDialog.OnDateSetListener{view,y,m,d ->
            val year = y.toString()
            val month = m.toString()
            val day = d.toString()
            birthday.setText(year+"-"+month+"-"+day)
        }, year,month,day
        ).show()
    }

    private fun userNameCheck():Boolean{
        val userName = user_name.editText?.text.toString().trim()

        return when {
            userName.isEmpty() -> {
                user_name.error = "ユーザー名が入力されていません"
                false
            }
            userName.count() < 2 -> {
                user_name.error = "ユーザー名の文字数が不正です"
                false
            }
            userName.count() > 30 -> {
                user_name.error = "ユーザー名の文字数が不正です"
                false
            }
            else -> {
                user_name.error = null
                true
            }
        }
    }

    private fun userIDCheck():Boolean{
        val userID = user_id.editText?.text.toString().trim()

        return when {
            userID.isEmpty() -> {
                user_id.error = "ユーザーIDが入力されていません"
                false
            }
            userID.count() < 5  ->{
                user_id.error = "ユーザーIDの文字数が不正です"
                false
            }
            userID.count() > 30  ->{
                user_id.error = "ユーザーIDの文字数が不正です"
                false
            }
            userID.equals("\"^[0-9a-zA-Z]+\$") ->{
                user_id.error = null
                true
            }
            else -> {
                user_id.error = "使用できない文字が含まれています"
                false
            }
        }
    }

    private fun userEmailCheck():Boolean{
        val email = user_email.editText?.text.toString().trim()
        return when {
            email.count() == 0 -> {
                user_email.error = "メールアドレスが入力されていません"
                false
            }
            else -> {
                user_email.error = null
                true
            }
        }
    }

    private fun userPassCheck():Boolean{
        val userPass = user_password.editText?.text.toString().trim()
        return when {
            userPass.isEmpty() -> {
                user_password.error = "パスワードが入力されていません"
                false
            }
            userPass.count() < 5 -> {
                user_password.error = "パスワードの文字数が不正です"
                false
            }
            userPass.count() > 30 -> {
                user_password.error = "パスワードの文字数が不正です"
                false
            }
            userPass.equals("\"^[0-9a-zA-Z]+\$") -> {
                user_password.error = null
                true
            }
            else -> {
                user_password.error = "使用できない文字が含まれています"
                false
            }
        }
    }

    private fun onSignUp(){
        var check = true
        if(!userNameCheck())check = false
        if(!userIDCheck())check = false
        if(!userEmailCheck())check = false
        if(!userPassCheck())check = false

        if(!check) return

        val param = hashMapOf(
            "user_id" to user_id.editText?.text.toString(),
            "user_name" to user_name.editText?.text.toString(),
            "email" to user_email.editText?.text.toString(),
            "password" to user_password.editText?.text.toString(),
            "gender" to user_gender.toString()
        )
        if (user_birthday.text != null) {
            param.put("birthday",user_birthday.text.toString())
        }

        ApiPostTask{
            if(it == null){
               //応答null
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else {
                when(it.getString("status")) {
                    "200" -> {
                        //Realmに保存する値を取得する
                        var token = it.getJSONObject("data").getString("token")//dataの中のtokenを取得する
                        var user_id = it.getJSONObject("data").getJSONObject("user_info").getInt("user_id")
                        var user_name = it.getJSONObject("data").getJSONObject("user_info").getString("user_name")
                        var user_icon = it.getJSONObject("data").getJSONObject("user_info").getString("user_icon")
                        //ユーザー情報をRealmに保存する
                        //ID,Name,Token
                        realm = Realm.getDefaultInstance()
                        realm.executeTransaction{
                            it.createObject<jp.ac.asojuku.st.chirusapo.Account>().apply {
                                //user_id
                                this.Ruser_id = user_id

                                //user_name
                                this.Ruser_name = user_name

                                //token
                                this.Rtoken = token

                            }
                        }
                        startActivity(
                            Intent(
                                this, MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i),Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_USER_ID -> {
                                    ApiError.showEditTextError(user_id,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_USER_NAME -> {
                                    ApiError.showEditTextError(user_name,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_EMAIL -> {
                                    ApiError.showEditTextError(user_email,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_PASSWORD -> {
                                    ApiError.showEditTextError(user_password,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_GENDER -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.VALIDATION_BIRTHDAY -> {
                                    ApiError.showToast(this,errorArray.getString(i), LENGTH_LONG)
                                }
                                ApiError.ALREADY_USER_ID -> {
                                    ApiError.showEditTextError(user_id,errorArray.getString(i))
                                }
                                ApiError.ALREADY_EMAIL -> {
                                    ApiError.showEditTextError(user_email,errorArray.getString(i))
                                }
                                    }
                                }
                            }
                        }
                    }
                }.execute(
                    ApiParam(
                        Api.SLIM + "account/signup",
                        param
                    )
                )
            }
}