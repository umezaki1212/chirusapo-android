package jp.ac.asojuku.st.chirusapo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.regex.Pattern

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
    }

    //IDをバリデートをする
    private fun validationUserId(): Boolean {
        //これで入力されたuser_idをstring型に変換して代入する。
        val userId =
            text_input_user_id.editText?.text.toString().trim()

        return when {
            userId.isEmpty() -> {
                //何も入力されていないなら
                text_input_user_id.error = "ユーザーIDかメールアドレスが入力されていません"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[a-zA-Z0-9-_]\$").matcher(userId).find() -> {
                text_input_user_id.error = "4文字以上30文字以下の半角英数字で入力してください"
                false
            }
            //なにもエラーなし
            else -> {
                text_input_user_id.error = null
                true
            }
        }
    }

    //パスワードをバリデートをする
    private fun validationUserPassword(): Boolean {
        //これで入力されたuser_idをstring型に変換して代入する。
        val userPassword =
            text_input_password.editText?.text.toString().trim()

        //何も入力されていないなら
        return when {
            userPassword.isEmpty() -> {
                text_input_password.error = "パスワードが入力されていません"
                false
            }
            //半角数字_で5文字から30文字以外なら
            !Pattern.compile("^[a-zA-Z0-9-_]\$").matcher(userPassword).find() -> {
                text_input_password.error = "5文字以上30文字以下の半角英数字で入力してください"
                false
            }
            else -> {//なにもエラーなし
                text_input_password.error = null
                true
            }
        }
    }

    private fun signin() {//サインイン

        //ここでstring型に変換する。
        val user_id = text_input_user_id.editText?.text.toString().trim()
        val password = text_input_password.editText?.text.toString().trim()

        //バリデートでfalseが返ってきたら処理を抜ける
        if (!validationUserId()) {
            return
        }
        if (!validationUserPassword()) {
            return
        }
        ApiPostTask {
            //データが取得できなかった場合
            if (it == null) {
                ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            }
            //なにかしら返答があった場合
            else {
                //statusを取得する
                when (it.getString("status")) {
                    "200" -> {
                        it.getJSONObject("data").getString("token")//dataの中のtokenを取得する
                        //Realmにtokenを保存しホームに飛ばす// 処理を書く　ログイン時スタックを消す
                    }
                    "400" -> {

                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "account/signin",
                //ここに送るデータを記入する
                hashMapOf("user_id" to user_id, "password" to password)
            )
        )
    }

}
