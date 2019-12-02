package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.*

class StartUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_up)
        realm = Realm.getDefaultInstance()

        masterData()
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
                                val userIcon = if (it.getJSONObject("data").getJSONObject("user_info").isNull("user_icon")) {
                                    null
                                } else {
                                    it.getJSONObject("data").getJSONObject("user_info").getString("user_icon")
                                }

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
                                    val groupInfoPinCode = groupInfo.getString("pin_code")

                                    realm.executeTransaction {
                                        if (realm.where<JoinGroup>().equalTo(
                                                "Rgroup_name",
                                                groupInfoName
                                            ).findFirst() != null
                                        ) {
                                            val updateJoinGroup = realm.where<JoinGroup>()
                                                .equalTo("Rgroup_name", groupInfoName).findFirst()
                                            updateJoinGroup?.Rgroup_name = groupInfoName
                                            updateJoinGroup?.Rpin_code = groupInfoPinCode
                                        } else {
                                            realm.createObject(JoinGroup::class.java, groupInfoId).apply {
                                                this.Rgroup_name = groupInfoName
                                                this.Rpin_code = groupInfoPinCode
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
                                    val user = realm.where<Account>().findAll()
                                    val group = realm.where<JoinGroup>().findAll()
                                    // val vaccine = realm.where<Vaccine>().findAll()
                                    // val allergy = realm.where<Allergy>().findAll()
                                    user.deleteAllFromRealm()
                                    group.deleteAllFromRealm()
                                    // vaccine.deleteAllFromRealm()
                                    // allergy.deleteAllFromRealm()
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

    private fun masterData(){
        //タイトル画面でのRealmの保存(ワクチン、アレルギー)
        //ApiGetTaskでマスターデータからワクチン,アレルギーそれぞれのデータを取得しRealmの各インスタンスに保存
        ApiGetTask{
            if (it == null) {
                ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            }
            else {
                val vaccineArray = it.getJSONObject("data").getJSONArray("vaccination")
                val allergyArray = it.getJSONObject("data").getJSONArray("allergy")
                //ワクチンのデータをRealmに保存する
                realm.executeTransaction { realm ->
                    val vaccine = realm.where<Vaccine>().findAll()
                    vaccine.deleteAllFromRealm()

                    for (i in 0 until vaccineArray.length()) {
                        realm.createObject(
                            Vaccine::class.java,
                            vaccineArray.getString(i)
                        ).apply {}
                    }
                }
                //アレルギーのデータをRealmに保存する
                realm.executeTransaction { realm ->
                    val allergy = realm.where<Allergy>().findAll()
                    allergy.deleteAllFromRealm()

                    for (j in 0 until allergyArray.length()) {
                        realm.createObject(
                            Allergy::class.java,
                            allergyArray.getString(j)
                        ).apply {}
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "start/master-download"
            )
        )
    }
}
