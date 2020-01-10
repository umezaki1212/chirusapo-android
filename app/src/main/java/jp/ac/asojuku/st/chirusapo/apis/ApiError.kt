package jp.ac.asojuku.st.chirusapo.apis

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class ApiError {

    companion object {
        // etc.
        const val CONNECTION_ERROR = "CONNECTION_ERROR"
        const val REQUIRED_PARAM = "REQUIRED_PARAM"
        const val ALLOW_EXTENSION = "ALLOW_EXTENSION"
        const val UPLOAD_FAILED = "UPLOAD_FAILED"
        const val MAIL_SEND = "MAIL_SEND"
        const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
        // Account
        const val VALIDATION_USER_ID = "VALIDATION_USER_ID"
        const val VALIDATION_USER_NAME = "VALIDATION_USER_NAME"
        const val VALIDATION_EMAIL = "VALIDATION_EMAIL"
        const val VALIDATION_PASSWORD = "VALIDATION_PASSWORD"
        const val VALIDATION_OLD_PASSWORD = "VALIDATION_OLD_PASSWORD"
        const val VALIDATION_NEW_PASSWORD = "VALIDATION_NEW_PASSWORD"
        const val VERIFY_PASSWORD_FAILED = "VERIFY_PASSWORD_FAILED"
        const val VALIDATION_BIRTHDAY = "VALIDATION_BIRTHDAY"
        const val VALIDATION_GENDER = "VALIDATION_GENDER"
        const val VALIDATION_LINE_ID = "VALIDATION_LINE_ID"
        const val VALIDATION_INTRODUCTION = "VALIDATION_INTRODUCTION"
        const val ALREADY_USER_ID = "ALREADY_USER_ID"
        const val ALREADY_EMAIL = "ALREADY_EMAIL"
        const val UNKNOWN_USER = "UNKNOWN_USER"
        // Token
        const val UNKNOWN_TOKEN = "UNKNOWN_TOKEN"
        // Group
        const val UNKNOWN_GROUP = "UNKNOWN_GROUP"
        const val ALREADY_CREATE_GROUP = "ALREADY_CREATE_GROUP"
        const val ALREADY_BELONG_GROUP = "ALREADY_BELONG_GROUP"
        const val UNREADY_BELONG_GROUP = "UNREADY_BELONG_GROUP"
        const val VALIDATION_GROUP_ID = "VALIDATION_GROUP_ID"
        const val VALIDATION_GROUP_NAME = "VALIDATION_GROUP_NAME"
        const val VALIDATION_PIN_CODE = "VALIDATION_PIN_CODE"
        const val VERIFY_PIN_CODE = "VERIFY_PIN_CODE"
        // Timeline
        const val NOT_FIND_POST_CONTENT = "NOT_FIND_POST_CONTENT"
        const val DUPLICATE_MEDIA_FILE = "DUPLICATE_MEDIA_FILE"
        const val VALIDATION_TIMELINE_POST_CONTENT = "VALIDATION_TIMELINE_POST_CONTENT"
        const val GENERATE_THUMBNAIL = "GENERATE_THUMBNAIL"
        const val UNKNOWN_POST = "UNKNOWN_POST"
        const val UNAUTHORIZED_OPERATION = "UNAUTHORIZED_OPERATION"
        const val VALIDATION_TIMELINE_POST_COMMENT = "VALIDATION_TIMELINE_POST_COMMENT"
        const val UNKNOWN_COMMENT = "UNKNOWN_COMMENT"
        // Child
        const val VALIDATION_AGE = "VALIDATION_AGE"
        const val VALIDATION_BLOOD_TYPE = "VALIDATION_BLOOD_TYPE"
        const val VALIDATION_BODY_HEIGHT = "VALIDATION_BODY_HEIGHT"
        const val VALIDATION_BODY_WEIGHT = "VALIDATION_BODY_WEIGHT"
        const val VALIDATION_CLOTHES_SIZE = "VALIDATION_CLOTHES_SIZE"
        const val VALIDATION_SHOES_SIZE = "VALIDATION_SHOES_SIZE"
        const val VALIDATION_VACCINATION = "VALIDATION_VACCINATION"
        const val VALIDATION_ALLERGY = "VALIDATION_ALLERGY"
        const val UNKNOWN_CHILD = "UNKNOWN_CHILD"
        const val ALREADY_RECORD = "ALREADY_RECORD"

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
                UNKNOWN_GROUP -> {
                    "不明なグループです"
                }
                ALREADY_CREATE_GROUP -> {
                    "既に入力されたグループIDは使用されています"
                }
                ALREADY_BELONG_GROUP -> {
                    "既にグループに所属しています"
                }
                UNREADY_BELONG_GROUP -> {
                    "グループに所属していない為取得できません"
                }
                VALIDATION_GROUP_ID -> {
                    "グループIDに使用できない文字が含まれています"
                }
                VALIDATION_GROUP_NAME -> {
                    "グループ名に使用できない文字が含まれています"
                }
                VALIDATION_PIN_CODE -> {
                    "PINコードに使用できない文字が含まれています"
                }
                VERIFY_PIN_CODE -> {
                    "PINコードの検証に失敗しました"
                }
                ALLOW_EXTENSION -> {
                    "選択されたファイルをアップロードすることはできません"
                }
                UPLOAD_FAILED -> {
                    "アップロードに失敗しました"
                }
                VALIDATION_OLD_PASSWORD -> {
                    "旧パスワードに使用できない文字が含まれています"
                }
                VALIDATION_NEW_PASSWORD -> {
                    "新パスワードに使用できない文字が含まれています"
                }
                VERIFY_PASSWORD_FAILED -> {
                    "旧パスワードの検証に失敗しました"
                }
                VALIDATION_LINE_ID -> {
                    "LINE IDに使用できない文字が含まれています"
                }
                VALIDATION_INTRODUCTION -> {
                    "自己紹介に使用できない文字が含まれています"
                }
                NOT_FIND_POST_CONTENT -> {
                    "投稿しようとしている情報が見つかりません"
                }
                DUPLICATE_MEDIA_FILE -> {
                    "画像と動画を同時にアップロードすることはできません"
                }
                VALIDATION_TIMELINE_POST_CONTENT -> {
                    "投稿文章に使用できない文字が含まれています"
                }
                GENERATE_THUMBNAIL -> {
                    "サムネイルの生成に失敗しました"
                }
                UNKNOWN_POST -> {
                    "投稿が見つかりません"
                }
                UNAUTHORIZED_OPERATION -> {
                    "許可がないため実行できません"
                }
                VALIDATION_TIMELINE_POST_COMMENT -> {
                    "コメントに使用できない文字が含まれています"
                }
                UNKNOWN_COMMENT -> {
                    "コメントが見つかりません"
                }
                // Child
                VALIDATION_AGE -> {
                    "年齢に使用できない文字が含まれています"
                }
                VALIDATION_BLOOD_TYPE -> {
                    "血液型に使用できない文字が含まれています"
                }
                VALIDATION_BODY_HEIGHT -> {
                    "身長に使用できない文字が含まれています"
                }
                VALIDATION_BODY_WEIGHT -> {
                    "体重に使用できない文字が含まれています"
                }
                VALIDATION_CLOTHES_SIZE -> {
                    "服のサイズに使用できない文字が含まれています"
                }
                VALIDATION_SHOES_SIZE -> {
                    "靴のサイズに使用できない文字が含まれています"
                }
                VALIDATION_VACCINATION -> {
                    "予防接種に使用できない文字が含まれています"
                }
                VALIDATION_ALLERGY -> {
                    "アレルギーに使用できない文字が含まれています"
                }
                UNKNOWN_CHILD -> {
                    "子ども情報が見つかりません"
                }
                ALREADY_RECORD -> {
                    "成長記録の更新一日一回までです"
                }
                else -> message
            }
        }
    }
}
