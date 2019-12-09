package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.realm.Realm
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.regex.Pattern

class SignInActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //onCreateメソッドでRealmのインスタンスを取得する
        realm = Realm.getDefaultInstance()

        realm.executeTransaction{
            val user = realm.where<Account>().findAll()
            val group = realm.where<JoinGroup>().findAll()
            user.deleteAllFromRealm()
            group.deleteAllFromRealm()
        }
    }

    //Realmのインスタンスを解放
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onResume() {
        super.onResume()

        button_sign_in.setOnClickListener {
            signIn()
        }

        text_new_account.setOnClickListener {
            //新規作成へ
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        text_forget_password.setOnClickListener {
            //パスワードリセットへ
            val intent = Intent(this, ResetPasswordMailActivity::class.java)
            startActivity(intent)
        }
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
            !Pattern.compile("^[a-zA-Z0-9-_@.]*\$").matcher(userId).find() -> {
                text_input_user_id.error = "使用できない文字が含まれています"
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
            !Pattern.compile("^[a-zA-Z0-9-_]{5,30}\$").matcher(userPassword).find() -> {
                text_input_password.error = "5文字以上30文字以下の半角英数字で入力してください"
                false
            }
            !Pattern.compile("^[a-zA-Z0-9-_]*\$").matcher(userPassword).find() -> {
                text_input_password.error = "使用できない文字が含まれています"
                false
            }
            else -> {//なにもエラーなし
                text_input_password.error = null
                true
            }
        }
    }

    // サインイン
    private fun signIn() {
        // クリックを無効にする
        button_sign_in.isEnabled = false

        //ここでstring型に変換する。
        val userId = text_input_user_id.editText?.text.toString().trim()
        val password = text_input_password.editText?.text.toString().trim()

        //バリデートでfalseが返ってきたら処理を抜ける
        var check = true
        if (!validationUserId()) check = false
        if (!validationUserPassword()) check = false

        if (!check) {
            // クリックを有効にするm
            button_sign_in.isEnabled = true
            return
        }

        ApiPostTask {
            //データが取得できなかった場合
            if (it == null) {
                ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                button_sign_in.isEnabled = true
            }
            //なにかしら返答があった場合
            else {
                //statusを取得する
                when (it.getString("status")) {
                    "200" -> {
                        button_sign_in.isEnabled = true
                        //dataの中のtokenを取得する
                        //Realmにtokenを保存しホームに飛ばす
                        //Realmに保存する値を取得する
                        val token = it.getJSONObject("data").getString("token")//dataの中のtokenを取得する
                        val realmUserId = it.getJSONObject("data").getJSONObject("user_info")
                            .getString("user_id")
                        val userName = it.getJSONObject("data").getJSONObject("user_info")
                            .getString("user_name")
                        val userIcon = if (it.getJSONObject("data").getJSONObject("user_info").isNull("user_icon")) {
                            null
                        } else {
                            it.getJSONObject("data").getJSONObject("user_info").getString("user_icon")
                        }

                        //ログイン画面でのRealmの保存(ユーザーやグループのデータを保存)
                        //ユーザー情報をRealmに保存する
                        try {
                            realm.executeTransaction { realm ->
                                realm.createObject(Account::class.java, realmUserId).apply {
                                    this.Ruser_name = userName
                                    this.Ruser_icon = userIcon
                                    this.Rtoken = token
                                }
                            }
                        }catch (e: RealmPrimaryKeyConstraintException) {
                            Toast.makeText(this, "エラーが発生しました。", Toast.LENGTH_SHORT).show()
                        }
                        //参加・作成したグループ情報の取得
                        val belongGroup = it.getJSONObject("data").getJSONArray("belong_group")
                        for (i in 0 until belongGroup.length()) {
                            val groupInfo = belongGroup.getJSONObject(i)
                            val groupInfoId = groupInfo.getString("group_id")
                            val groupInfoName = groupInfo.getString("group_name")
                            val groupInfoPinCode = groupInfo.getString("pin_code")
                            //グループ情報をRealmに保存
                            realm.executeTransaction { realm ->
                                realm.createObject(JoinGroup::class.java, groupInfoId).apply {
                                    this.Rgroup_name = groupInfoName
                                    this.Rpin_code = groupInfoPinCode
                                    //フラグでログイン後参照するタイムラインを指定している、最初に所属しているグループのタイムラインを参照する
                                    if(i == 0) {
                                        this.Rgroup_flag = 1
                                    } else {
                                        this.Rgroup_flag = 0
                                    }
                                }
                            }
                        }

                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }
                    "400" -> {
                        //messageからエラー文を配列で取得し格納する
                        val errorArray = it.getJSONArray("message")
                        //エラーが出た分だけ回す。untilとは(int i = 0; i< 100; i++)と同じ意味
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                //ユーザー情報が見つからない場合に返される
                                //ユーザーIDに一致する項目があり、パスワードが誤っている場合でもUNKNOWN_USERとして返される
                                ApiError.UNKNOWN_USER -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                                //ユーザーIDがバリデーションに失敗した
                                ApiError.VALIDATION_USER_ID -> {
                                    ApiError.showEditTextError(
                                        text_input_user_id,
                                        errorArray.getString(i)
                                    )
                                }
                                //パスワードがバリデーションに失敗した
                                ApiError.VALIDATION_PASSWORD -> {
                                    ApiError.showEditTextError(
                                        text_input_password,
                                        errorArray.getString(i)
                                    )
                                }
                                //値が不足している
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                            }
                        }
                        // クリックを有効にする
                        button_sign_in.isEnabled = true
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "account/signin",
                //ここに送るデータを記入する
                hashMapOf("user_id" to userId, "password" to password)
            )
        )
    }

}
