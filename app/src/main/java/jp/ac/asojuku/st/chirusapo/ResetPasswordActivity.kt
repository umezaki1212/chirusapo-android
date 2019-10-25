package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_reset_password.*
import java.util.*
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.AsyncTask.execute
import android.widget.Toast
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask


class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var spEditor: SharedPreferences.Editor
    var prefs: SharedPreferences = getSharedPreferences("password", Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
    }

    override fun onResume() {
        super.onResume()

        PasswordReset_Button.setOnClickListener { onPasswordReset() }
    }

    private fun onPasswordCheck():Boolean{
        val userPass = user_password.editText?.text.toString().trim()
        val userPassCheck = prefs.getString("password","")
        return when {
            userPass.isEmpty() -> {
                user_password.error = "パスワードが未入力です"
                false
            }
            userPass != userPassCheck -> {
                user_password.error = "パスワードが正しくありません"
                false
            }
            else -> {
                true
            }
        }
    }

    private fun onNewPasswordCheck():Boolean{
        val userNewPass = user_newpassword.editText?.text.toString().trim()
        return when {
            userNewPass.isEmpty() -> {
                user_newpassword.error = "新しいパスワードが未入力です"
                false
            }
            userNewPass.count() < 2 -> {
                user_newpassword.error = "新しいパスワードの文字数が不正です"
                false
            }
            userNewPass.count() > 30 -> {
                user_newpassword.error = "新しいパスワードの文字数が不正です"
                false
            }
            else -> {
                true
            }
        }
    }

    private fun onPasswordReset(){
        var check = true
        if(!onPasswordCheck())check = false
        if(!onNewPasswordCheck())check = false

        if(!check) return
        val param = hashMapOf("password" to user_password.editText?.text.toString())

        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else {
                when(it.getString("status")){
                    "200" -> {
                        val token = it.getJSONObject("data").getString("token")
                        val editor = getSharedPreferences("data", MODE_PRIVATE).edit()
                        editor.putString("token", token).apply()
                        startActivity(
                            Intent(
                                this, MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                    "400" -> {
                        val msgArray = it.getJSONArray("msg")
                        for (i in 0 until msgArray.length()) {
                            when (msgArray.getString(i)) {
                                "VALIDATION_PASSWORD" -> user_newpassword.error = "パスワードの入力規則に違反しています"
                                else -> Toast.makeText(applicationContext, "不明なエラーが発生しました", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                "account/password-reset"
            )
        )
    }
}
