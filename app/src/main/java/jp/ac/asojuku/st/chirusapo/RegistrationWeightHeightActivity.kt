package jp.ac.asojuku.st.chirusapo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_child_registration.*
import kotlinx.android.synthetic.main.activity_registration_weight_height.*
import kotlinx.android.synthetic.main.activity_registration_weight_height.child_height
import kotlinx.android.synthetic.main.activity_registration_weight_height.child_weight
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class RegistrationWeightHeightActivity : AppCompatActivity() {
    lateinit var realm: Realm
    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR).toString()
    private val month = calender.get(Calendar.MONTH)+1
    private val day = calender.get(Calendar.DAY_OF_MONTH).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_weight_height)

        child_weight.setText(intent.getStringExtra("bodyWeight"))
        child_height.setText(intent.getStringExtra("bodyHeight"))
        clothes_size.setText(intent.getStringExtra("clothesSize"))
        shoes_size.setText(intent.getStringExtra("shoesSize"))
        val today:String = year + "年" + month + "月" + day + "日"
        today_time.text = today

    }

    override fun onResume() {
        super.onResume()

        button_set.setOnClickListener { update() }
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
            height.toDouble() < 9 -> {
                child_height_input.error = "身長は10～200までの間で入力してください"
                false
            }
            height.toDouble() > 201 -> {
                child_height_input.error = "身長は10～200までの間で入力してください"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9.]*\$").matcher(height).find() -> {
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
            weight.toDouble() < 0 -> {
                child_weight_input.error = "体重は1～150までの間で入力してください"
                false
            }
            weight.toDouble() > 151 -> {
                child_weight_input.error = "体重は1～150までの間で入力してください"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9.]*\$").matcher(weight).find() -> {
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
            clothes.toDouble() < 49 -> {
                child_clothes_input.error = "服のサイズは50～160までの間で入力してください"
                false
            }
            clothes.toDouble() > 161 -> {
                child_clothes_input.error = "服のサイズは50～160までの間で入力してください"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9]*\$").matcher(clothes).find() -> {
                child_clothes_input.error = "使用できない文字が含まれています"
                false
            }
            !Pattern.compile("^[0-9]{2}0$").matcher(clothes).find() -> {
                child_clothes_input.error = "10単位で入力してください"
                false
            }
            //なにもエラーなし
            else -> {
                child_clothes_input.error = null
                true
            }
        }
    }

    private fun validationShoes(): Boolean {
        //これで入力されたuser_idをstring型に変換して代入する。
        val shoes =
            child_shoes_input.editText?.text.toString().trim()

        return when {
            shoes.isEmpty() -> {
                //何も入力されていないなら
                child_shoes_input.error = "靴のサイズが入力されていません"
                false
            }
            shoes.toDouble() <= 5 -> {
                child_shoes_input.error = "靴のサイズは5～30までの間で入力してください"
                false
            }
            shoes.toDouble() >= 30 -> {
                child_shoes_input.error = "靴のサイズは5～30までの間で入力してください"
                false
            }
            //半角数字_で4文字から10文字以外なら
            !Pattern.compile("^[0-9]*\$").matcher(shoes).find() -> {
                child_shoes_input.error = "使用できない文字が含まれています"
                false
            }
            //なにもエラーなし
            else -> {
                child_shoes_input.error = null
                true
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun  update(){
        button_set.isEnabled = false

        val height = child_height_input.editText?.text.toString().trim()
        val weight = child_weight_input.editText?.text.toString().trim()
        val clothes = child_clothes_input.editText?.text.toString().trim()
        val shoes = child_shoes_input.editText?.text.toString().trim()
        val time = SimpleDateFormat("yyyy-MM-dd").format(Date())

        //バリデートでfalseが返ってきたら処理を抜ける
        var check = true
        if (!validationHeight()) check = false
        if (!validationWeight()) check = false
        if (!validationClothes()) check = false
        if (!validationShoes()) check = false

        if (!check) {
            // クリックを有効にする
            button_set.isEnabled = true
            return
        }

        realm = Realm.getDefaultInstance()

        val account = realm.where<Account>().findFirst()
        val token = account!!.Rtoken
        val childId = intent.getStringExtra("user_id")

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
                                ApiError.UNREADY_BELONG_GROUP -> {
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
                                ApiError.ALREADY_RECORD -> {
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
                hashMapOf("token" to token, "child_id" to childId,"body_height" to height,"body_weight" to weight,"clothes_size" to clothes,"shoes_size" to shoes,"add_date" to time)
            )
        )
    }
}
