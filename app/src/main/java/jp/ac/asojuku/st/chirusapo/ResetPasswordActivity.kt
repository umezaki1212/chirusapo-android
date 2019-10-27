package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_reset_password.*
import android.content.Intent
import android.widget.Toast
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
        val userPass = old_password.editText?.text.toString().trim()
        return when {
            userPass.isEmpty() -> {
                old_password.error = "パスワードが未入力です"
                false
            }
            else -> {
                true
            }
        }
    }

    private fun onNewPasswordCheck():Boolean{
        val userNewPass = new_password.editText?.text.toString().trim()
        return when {
            userNewPass.isEmpty() -> {
                new_password.error = "新しいパスワードが未入力です"
                false
            }
            userNewPass.count() < 2 -> {
                new_password.error = "新しいパスワードの文字数が不正です"
                false
            }
            userNewPass.count() > 30 -> {
                new_password.error = "新しいパスワードの文字数が不正です"
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

        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else {
                when(it.getString("status")){
                    "200" -> {
                        startActivity(
                            Intent(
                                this, MainActivity::class.java
                            )
                        )
                    }
                    "400" -> {
                        val msgArray = it.getJSONArray("msg")
                        for (i in 0 until msgArray.length()) {
                            when (msgArray.getString(i)) {
                                "VALIDATION_PASSWORD" -> new_password.error = "パスワードの入力規則に違反しています"
                                else -> Toast.makeText(applicationContext, "不明なエラーが発生しました", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                "account/password-change",
                        hashMapOf("password" to new_password.editText?.text.toString())
            )
        )
    }
}
