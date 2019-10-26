package jp.ac.asojuku.st.chirusapo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.regex.Pattern
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import jp.ac.asojuku.st.chirusapo.apis.*
import sun.jvm.hotspot.utilities.IntArray



class SignInActivity : AppCompatActivity() {

    lateinit var realm:Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


        signin()

    }

    //自動ログイン処理
    private fun autoLogin() {
        if (realm.where<Book>().findFirst() != null) {
            //インスタンスを取得
            var mInstance = realm.where<Book>().findFirst()
            //インスタンスに保存されているトークンを取得
            var mtoken = mInstance?.Rtoken

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
                            //ログインを行う

                            //データを更新する

                        }
                        "400" -> {

                        }
                    }
                }
            }.execute(
                ApiParam(
                    Api.SLIM + "token/verify-token",
                    //ここに送るデータを記入する
                    hashMapOf("token" to mtoken)
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
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
                        var token = it.getJSONObject("data").getString("token")//dataの中のtokenを取得する
                        var user_id = it.getJSONObject("data").getJSONObject("user_info")
                            .getInt("user_id")
                        var user_name = it.getJSONObject("data").getJSONObject("user_info")
                            .getString("user_name")
                        var user_icon = it.getJSONObject("data").getJSONObject("user_info")
                            .getString("user_icon")

                        realm = Realm.getDefaultInstance()
                        realm.beginTransaction();
                        realm.executeTransaction {
                            if(realm.where<Book>().findFirst() == null) {

                            //Realmにtokenを保存しホームに飛ばす//処理を書く　ログイン時スタックを消す

                                it.createObject<Book>().apply {
                                    //ここに保存する内容を入れる処理
                                    //例)name = "Rex"など
                                    //user_id
                                    this.Ruser_id = user_id
                                    //user_name
                                    this.Ruser_name = user_name
                                    //icon_file_name
                                    ​this.Ruser_icon = user_icon
                                    //token
                                    ​this.Rtoken = token
                                }
// Persist your data in a transaction
//                                realm.beginTransaction();
//                                var book:Book = Book();
//                                book.Ruser_id = user_id;
//                                book.Ruser_name = user_name;
//                                book.Ruser_icon = user_icon;
//                                book.Rtoken = token;
//
//
//                                val managedDog = realm.copyToRealm(book) // Persist unmanaged objects
//                                val manageBook = realm.copyToRealm(book) // Create managed objects directly
//                                manageBook.get
//
//                                manageBook.getBooks().add(managedDog)
//                                realm.commitTransaction();
                            }
                            else {
                                Log.d("TEST", "nullじゃないです")

                                var users = realm.where<Book>().findFirst()
                                realm.executeTransaction {
                                    users?.Ruser_id = user_id
                                    users?.Ruser_name = user_name
                                    users?.Ruser_icon = user_icon
                                    users?.Rtoken = token

                                }
                            }
                        }
                    }
                    "400" -> {
                        //ログインができなかった場合の処理
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
