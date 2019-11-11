package jp.ac.asojuku.st.chirusapo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_reset_password_mail.*
import java.util.regex.Pattern

class ResetPasswordMailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password_mail)
        send_btn.setOnClickListener { view ->
            passwordReset(view)
        }
    }

    private fun passwordReset(view: View) {
        val userId = userid_mailaddress.text.toString()

        when {
            userId.isEmpty() -> {
                // エラー処理
                Toast.makeText(this, "入力されていません", Toast.LENGTH_SHORT).show()
                return
            }
            !Pattern.compile("^[a-zA-Z0-9-.@_]{4,250}\$").matcher(userId).find() -> {
                // エラー処理
                Toast.makeText(this, "ユーザーIDかメールアドレスを入力してください", Toast.LENGTH_SHORT).show()
                return
            }
        }

        ApiPostTask {
            // 処理した結果が帰ってくる
            if (it == null) {
                ApiError.showSnackBar(view, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            }
            //なにかしら返答があった場合
            else {
                //statusを取得する
                when (it.getString("status")) {
                    "200" -> {
                        //ダイアログで成功を表示

                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.UNKNOWN_USER -> {
                                    // 不明なユーザーです表示
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.VALIDATION_USER_ID -> {
                                    // ユーザーIDの書式が誤っています表示
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.MAIL_SEND -> {
                                    // メール送信に失敗しました表示
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
                Api.SLIM + "account/password-reset",
                hashMapOf("user_id" to userId)
            )
        )
    }
}
