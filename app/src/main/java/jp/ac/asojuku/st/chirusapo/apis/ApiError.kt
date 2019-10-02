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
                else -> message
            }
        }
    }
}
