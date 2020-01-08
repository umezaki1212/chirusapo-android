package jp.ac.asojuku.st.chirusapo

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataAdapter
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListItem
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListSub
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataSubAdapter
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_registration_weight_height.*
import kotlinx.android.synthetic.main.fragment_child_data_set.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

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
                                clothes_size.setText(item.getString("clothes_size"))
                                shoes_size.setText(item.getString("shoes_size"))

                                button_set.setOnClickListener {
                                    set()
                                }

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

    private fun validationHeight(): Boolean {
        //これで入力されたuser_idをstring型に変換して代入する。
        val height =
            child_height_input.editText?.text.toString().trim()

        return when {
            height.isEmpty() -> {
                //何も入力されていないなら
                child_height_input.error = "身長が入力されていません"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9-.]*\$").matcher(height).find() -> {
                child_height_input.error = "使用できない文字が含まれています"
                false
            }
            //なにもエラーなし
            else -> {
                child_height_input.error = null
                true
            }
        }
    }

    private fun validationWeight(): Boolean {
        //これで入力されたuser_idをstring型に変換して代入する。
        val weight =
            child_weight_input.editText?.text.toString().trim()

        return when {
            weight.isEmpty() -> {
                //何も入力されていないなら
                child_weight_input.error = "体重が入力されていません"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9-.]*\$").matcher(weight).find() -> {
                child_weight_input.error = "使用できない文字が含まれています"
                false
            }
            //なにもエラーなし
            else -> {
                child_height_input.error = null
                true
            }
        }
    }

    private fun validationClothes(): Boolean {
        //これで入力されたuser_idをstring型に変換して代入する。
        val clothes =
            child_clothes_input.editText?.text.toString().trim()

        return when {
            clothes.isEmpty() -> {
                //何も入力されていないなら
                child_clothes_input.error = "服のサイズが入力されていません"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9]*\$").matcher(clothes).find() -> {
                child_clothes_input.error = "使用できない文字が含まれています"
                false
            }
            //なにもエラーなし
            else -> {
                child_clothes_input.error = null
                true
            }
        }
    }

    private fun validationShose(): Boolean {
        //これで入力されたuser_idをstring型に変換して代入する。
        val shose =
            child_shose_input.editText?.text.toString().trim()

        return when {
            shose.isEmpty() -> {
                //何も入力されていないなら
                child_shose_input.error = "靴のサイズが入力されていません"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9]*\$").matcher(shose).find() -> {
                child_shose_input.error = "使用できない文字が含まれています"
                false
            }
            //なにもエラーなし
            else -> {
                child_shose_input.error = null
                true
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun  set(){
        button_set.isEnabled = false

        val height = child_height_input.editText?.text.toString().trim()
        val weight = child_weight_input.editText?.text.toString().trim()
        val clothes = child_clothes_input.editText?.text.toString().trim()
        val shose = child_shose_input.editText?.text.toString().trim()
        val time = SimpleDateFormat("yyyy-MM-dd").format(Date())

        //バリデートでfalseが返ってきたら処理を抜ける
        var check = true
        if (!validationHeight()) check = false
        if (!validationWeight()) check = false
        if (!validationClothes()) check = false
        if (!validationShose()) check = false

        if (!check) {
            // クリックを有効にする
            button_set.isEnabled = true
            return
        }

        realm = Realm.getDefaultInstance()

        val account = realm.where<Account>().findFirst()
        val Token = account!!.Rtoken
        val childId = intent.getStringExtra("user_id")

        Log.d("TEST", childId)

        ApiPostTask {
            //データが取得できなかった場合
            if (it == null) {
                ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                button_set.isEnabled = true
            }
            //なにかしら返答があった場合
            else {
                //statusを取得する
                when (it.getString("status")) {
                    "200" -> {
                        button_set.isEnabled = true

//                        val intent = Intent(this, ChildFragment::class.java).apply {
//                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        }
//                        startActivity(intent)
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
                                //値が不足している
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                                else -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_LONG
                                    )
                                }
                            }
                        }
                        // クリックを有効にする
                        button_set.isEnabled = true
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "/child/growth/history/add",
                //ここに送るデータを記入する
                hashMapOf("token" to Token, "child_id" to childId,"body_height" to height,"body_weight" to weight,"clothes_size" to clothes,"shoes_size" to shose,"add_date" to time)
            )
        )
    }
}
