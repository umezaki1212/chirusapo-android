package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_sign_up.*
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import java.util.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    private val user_sexItems = arrayOf("未回答", "男性", "女性")
    private lateinit var spEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        } ?: IllegalAccessException("Toolbar cannot be null")
    }

    override fun onResume() {
        super.onResume()
        user_sex.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = parent as Spinner
                val select = spinner.selectedItem.toString()
                spEditor.putString("user_sex",select).apply()
            }
        }
        AccountCreate_button.setOnClickListener { onSignUp() }
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
            userID.count() <= 4  ->{
                user_id.error = "ユーザーIDの文字数が不正です"
                false
            }
            else ->{
                user_id.error = null
                true
            }
        }
    }

    private fun userEmailCheck():Boolean{
        val email = user_email.length()
        return when {
            email == 0 -> {
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
            else -> {
                user_password.error = null
                true
            }
        }
    }
    private fun onSignUp(){
        var check = true
        if(!userNameCheck())check == false
        if(!userIDCheck())check == false
        if(!userEmailCheck())check == false
        if(!userPassCheck())check == false

        if(!check) return


        //TODO エラーチェック終了後のアカウント作成登録処理

        ApiPostTask{
            if(it == null){
               //応答null
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else {
                when(it.getString("status")) {
                    "201" -> {
                        val token = it.getJSONObject("data").getString("token")
                        val editor = getSharedPreferences("data", MODE_PRIVATE).edit()
                        editor.putString("token", token).apply()
                        startActivity(
                            Intent(
                                this, MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                    "E00" -> {
                        val msgArray = it.getJSONArray("msg")
                        for (i in 0 until msgArray.length()) {
                            when (msgArray.getString(i)) {
                                "VALIDATION_USER_ID" -> user_id.error = "ユーザーIDの入力規則に違反しています"
                                "VALIDATION_USER_NAME" -> user_name.error = "ユーザー名の入力規則に違反しています"
                                "VALIDATION_EMAIL" -> user_email.error = "メールアドレスの入力規則に違反しています"
                                "VALIDATION_PASSWORD" -> user_password.error = "パスワードの入力規則に違反しています"
                                "ALREADY_USER_ID" -> user_id.error = "入力されたユーザーは既に登録されています"
                                "ALREADY_EMAIL" -> user_email.error = "入力されたメールアドレスは既に登録されています"
                                else -> Toast.makeText(applicationContext, "不明なエラーが発生しました", Toast.LENGTH_SHORT).show()
                            }
                        }

                            }
                        }
                    }
                }
            }
}
@Suppress("DEPRECATION")
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener{

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(SignUpActivity(), android.R.style.Theme_Holo_Dialog, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

    }

}