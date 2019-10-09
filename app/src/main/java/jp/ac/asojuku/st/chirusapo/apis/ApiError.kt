package jp.ac.asojuku.st.chirusapo.apis

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class ApiError {

    companion object {
        const val CONNECTION_ERROR = "CONNECTION_ERROR"
        const val REQUIRED_PARAM = "REQUIRED_PARAM"
        const val UNKNOWN_TOKEN = "UNKNOWN_TOKEN"
        const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
        const val VALIDATION_USER_ID = "VALIDATION_USER_ID"
        const val VALIDATION_USER_NAME = "VALIDATION_USER_NAME"
        const val VALIDATION_EMAIL = "VALIDATION_EMAIL"
        const val VALIDATION_PASSWORD = "VALIDATION_PASSWORD"
        const val VALIDATION_BIRTHDAY = "VALIDATION_BIRTHDAY"
        const val VALIDATION_GENDER = "VALIDATION_GENDER"
        const val ALREADY_USER_ID = "ALREADY_USER_ID"
        const val ALREADY_EMAIL = "ALREADY_EMAIL"
        const val UNKNOWN_USER = "UNKNOWN_USER"
        const val MAIL_SEND = "MAIL_SEND"

        fun showToast(context: Context, message: String, duration: Int) {
            Toast.makeText(context, switchMessage(message), duration).show()
        }

        fun showSnackBar(view: View, message: String, duration: Int) {
            Snackbar.make(view, switchMessage(message), duration).show()
        }

        fun showEditTextError(textInputLayout: TextInputLayout, message: String) {
            textInputLayout.error = switchMessage(message)
        }

        private fun switchMessage(message: String): String {
            return when (message) {
                CONNECTION_ERROR -> {
                    "APIとの通信に失敗しました"
                }
                REQUIRED_PARAM -> {
                    "必要な値が見つかりませんでした"
                }
                UNKNOWN_TOKEN -> {
                    "ログイントークンが不明です"
                }
                UNKNOWN_ERROR -> {
                    "不明なエラーが発生しました"
                }
                VALIDATION_USER_ID -> {
                    "ユーザーIDの書式が誤っています"
                }
                VALIDATION_USER_NAME -> {
                    "ユーザー名の書式が誤っています"
                }
                VALIDATION_EMAIL -> {
                    "メールアドレスの書式が誤っています"
                }
                VALIDATION_PASSWORD -> {
                    "パスワードの書式が誤っています"
                }
                VALIDATION_BIRTHDAY -> {
                    "誕生日の書式が誤っています"
                }
                VALIDATION_GENDER -> {
                    "性別の書式が誤っています"
                }
                ALREADY_USER_ID -> {
                    "入力されたユーザーIDは既に登録されています"
                }
                ALREADY_EMAIL -> {
                    "入力されたメールアドレスは既に登録されています"
                }
                UNKNOWN_USER -> {
                    "不明なユーザーです"
                }
                MAIL_SEND -> {
                    "メール送信に失敗しました"
                }
                else -> message
            }
        }
    }
}
