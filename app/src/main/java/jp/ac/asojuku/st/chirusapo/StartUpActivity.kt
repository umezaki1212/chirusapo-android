package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask

class StartUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_up)
        realm = Realm.getDefaultInstance()

        autoLogin()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    lateinit var realm: Realm

    // 自動ログイン処理
    private fun autoLogin() {
        realm.executeTransaction {
            val account: Account? = realm.where<Account>().findFirst()
            if (account != null) {
                val token = account.Rtoken
                ApiPostTask {
                    // データが取得できなかった場合
                    if (it == null) {
                        ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                    }
                    // なにかしら返答があった場合
                    else {
                        // statusを取得する
                        when (it.getString("status")) {
                            "200" -> {
                                val userName = it.getJSONObject("data").getJSONObject("user_info")
                                    .getString("user_name")
                                val userIcon = it.getJSONObject("data").getJSONObject("user_info")
                                    .getString("user_icon")

                                // ユーザー情報を保存する処理
                                realm.executeTransaction {
                                    account.Ruser_name = userName
                                    account.Ruser_icon = userIcon
                                }

                                val belongGroup =
                                    it.getJSONObject("data").getJSONArray("belong_group")
                                // 所属グループを保存する処理
                                for (i in 0 until belongGroup.length()) {
                                    val groupInfo = belongGroup.getJSONObject(i)
                                    val groupInfoId = groupInfo.getString("group_id")
                                    val groupInfoName = groupInfo.getString("group_name")

                                    realm.executeTransaction {
                                        if (realm.where<JoinGroup>().equalTo(
                                                "Rgroup_name",
                                                groupInfoName
                                            ).findFirst() != null
                                        ) {
                                            val updateJoinGroup = realm.where<JoinGroup>()
                                                .equalTo("Rgroup_name", groupInfoName).findFirst()
                                            updateJoinGroup?.Rgroup_name = groupInfoName
                                            updateJoinGroup?.Rgroup_flag = 1

                                        } else {
                                            realm.createObject(JoinGroup::class.java, groupInfoId)
                                                .apply {
                                                    this.Rgroup_name = groupInfoName
                                                    this.Rgroup_flag = 1
                                                }
                                        }
                                    }
                                }

                                // タイムラインの画面MainActivityに遷移
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(intent)
                            }
                            "400" -> {
                                // トークンが正しくないのでSignInActivityに遷移
                                val intent = Intent(this, SignInActivity::class.java)
                                startActivity(intent)

                                // realmを削除する処理
                                realm.executeTransaction {
                                    realm.executeTransaction {
                                        val user = realm.where<Account>().findAll()
                                        val group = realm.where<JoinGroup>().findAll()
                                        val vaccine = realm.where<Vaccine>().findAll()
                                        val allergy = realm.where<Allergy>().findAll()
                                        user.deleteAllFromRealm()
                                        group.deleteAllFromRealm()
                                        vaccine.deleteAllFromRealm()
                                        allergy.deleteAllFromRealm()
                                    }
                                }
                            }
                        }
                    }
                }.execute(
                    ApiParam(
                        Api.SLIM + "token/verify-token",
                        // ここに送るデータを記入する
                        hashMapOf("token" to token)
                    )
                )
            } else {
                // 新規登録orログインが行われていないのでSignInActivityに遷移
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
