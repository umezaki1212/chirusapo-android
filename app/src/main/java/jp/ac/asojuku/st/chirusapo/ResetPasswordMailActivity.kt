package jp.ac.asojuku.st.chirusapo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_reset_password_mail.*
import kotlinx.android.synthetic.main.activity_sign_in.*


class ResetPasswordMailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password_mail)
    }
    /*
    "user_id":{
        "name": "post",
        "password": "password",
        "password_confirmation": "password"
    }
    */

    fun passwordReset() {
        val user_id = userid_mailaddress.text.toString()

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
                        it.getJSONObject("data").getString("token")//dataの中のtokenを取得する
                        //Realmにtokenを保存しホームに飛ばす// 処理を書く　ログイン時スタックを消す
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for(i in 0 until errorArray.length()){
                            when(errorArray.getString(i)){
                                ApiError.UNKNOWN_USER -> {
                                    ApiError.showEditTextError(text_input_user_id,errorArray.getString(i))
                                }
                            }
                        }
                    }
                }
            }

        }.execute(
            ApiParam(
            Api.SLIM + "account/password-reset",
            hashMapOf("user_id" to user_id)
            )
        )
    }
}
