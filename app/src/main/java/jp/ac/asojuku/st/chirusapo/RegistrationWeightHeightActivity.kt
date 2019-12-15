package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataAdapter
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListItem
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListSub
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataSubAdapter
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_registration_weight_height.*
import kotlinx.android.synthetic.main.fragment_child_data_set.*

class RegistrationWeightHeightActivity : AppCompatActivity() {
    lateinit var realm: Realm
    private lateinit var userToken: String
    private  lateinit var childId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_weight_height)
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()

        val account = realm.where<Account>().findFirst()

        //Tokenが存在するか？
        if (account == null) {
            // 新規登録orログインが行われていないのでSignInActivityに遷移
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }else {
            userToken = account.Rtoken
            //現在見ているグループIDの取得
            val test = 1
            val group:JoinGroup? =
                realm.where<JoinGroup>().equalTo("Rgroup_flag", test).findFirst()
            //存在しなかった(グループに参加を促すようにする
            if(group == null){
                Toast.makeText(this, "グループ情報が取得できません", Toast.LENGTH_SHORT).show()
            }
            else{
                val groupId = group.Rgroup_id
                ApiGetTask {
                    if (it == null) {
                        Toast.makeText(this, "APIと通信できません", Toast.LENGTH_SHORT).show()
                    } else {
                        when (it.getString("status")) {
                            "200" -> {

                                val childData =
                                    it.getJSONObject("data").getJSONArray("child_list")
                                //書き換え
                                val i = 4
                                var item = childData.getJSONObject(i)

                                child_weight.setText(item.getString("body_weight"))
                                child_height.setText(item.getString("body_height"))

                            }
                            "400" -> {
                                //messageからエラー文を配列で取得し格納する
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        //グループ情報なし
                                        ApiError.UNKNOWN_GROUP -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //値が不足している場合
                                        ApiError.REQUIRED_PARAM -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //トークンの検証失敗
                                        ApiError.UNKNOWN_TOKEN -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //所属グループなし
                                        ApiError.UNREADY_BELONG_GROUP -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                    }
                                }
                            }
                            else ->Toast.makeText(this, "不明なエラーが発生しました", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.execute(
                    ApiParam(
                        Api.SLIM + "/child/list",
                        hashMapOf("token" to userToken, "group_id" to groupId)
                    )
                )
            }
        }
    }
}
