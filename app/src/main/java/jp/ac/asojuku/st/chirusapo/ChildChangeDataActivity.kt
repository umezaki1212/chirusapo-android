package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_child_change_data.*
import kotlinx.android.synthetic.main.activity_child_registration.*
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ChildChangeDataActivity : AppCompatActivity() {

    lateinit var realm: Realm

    private var vaccineArray = 0
    private var allergyArray = 0

    private var vaccineNameTexts = ArrayList<String>(vaccineArray)
    private var vaccineDateTexts = ArrayList<String>(vaccineArray)
    private val allergyNameTexts = ArrayList<String>(allergyArray)

    private var vaccineTextArray = 0
    private var allergyTextArray = 0

    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_change_data)
        realm = Realm.getDefaultInstance()

        val vaccine = realm.where<Vaccine>().findAll()
        val allergy = realm.where<Allergy>().findAll()
        vaccineArray = vaccine!!.size
        allergyArray = allergy.size

        supportActionBar?.let {
            it.title = "子供情報変更"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        // idがdialogButtonのButtonを取得
        val vaccineChangBtn = findViewById<View>(R.id.vaccineDataChang) as Button
        val allergyChangBtn = findViewById<View>(R.id.allergyDataChang) as Button
        // clickイベント追加
        vaccineChangBtn.setOnClickListener {
            // ダイアログクラスをインスタンス化
            val vaccineDialog = VaccineName(vaccine)

            // 表示  getFragmentManager()は固定、sampleは識別タグ
            vaccineDialog.show()
        }
        allergyChangBtn.setOnClickListener {
            val allergyDialog = Allergy(allergy)
            allergyDialog.show()
        }

        childChangButton.setOnClickListener {
            childChang()
        }
    }

    private fun VaccineName(vaccine: RealmResults<Vaccine>): Dialog {
        val layoutName: LinearLayout = findViewById(R.id.vaccine_name_array_chang)
        val textVaccineName = TextView(this)
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        val dialogBuilder = AlertDialog.Builder(this)

        // リスト項目生成
        val items = arrayOfNulls<String>(vaccineArray)
        for (i in 0 until vaccineArray) {
            //ダイアログ内のリストにワクチン一覧をセット
            items[i] = vaccine[i]!!.vaccine_name
        }
        // タイトル設定
        dialogBuilder.setTitle("ワクチンを選択してください")
        // リスト項目を設定 & クリック時の処理を設定
        dialogBuilder.setItems(
            items
        ) { _, which ->
            // whichには選択したリスト項目の順番が入っているので、それを使用して値を取得
            val selectedVal = items[which]
            textVaccineName.text = items[which]
            vaccineNameTexts.add(selectedVal.toString())
            VaccineDate(which)
            layoutName.addView(textVaccineName,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        // dialogBalderを返す
        return dialogBuilder.create()
    }


    private fun VaccineDate(which: Int) {
        val layoutDate: LinearLayout = findViewById(R.id.vaccine_date_array_chang)
        val textVaccineDate = TextView(this)
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener{ _, y, m, d ->
            val year = y.toString()
            var month = (m+1).toString()
            var day = d.toString()
            if(m < 9 ){
                month = "0$month"
            }
            if(d < 10){
                day = "0$day"
            }

            vaccineDateTexts.add("%s-%s-%s".format(year, month, day))
            textVaccineDate.text = "%s-%s-%s".format(year, month, day)
            layoutDate.addView(textVaccineDate,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

            vaccineTextArray += 1
        }, year,month,day
        ).show()
    }
    private fun Allergy(allergy: RealmResults<Allergy>): Dialog {
        val layoutAllergy: LinearLayout = findViewById(R.id.AllergyListChang)
        val textAllergyName = TextView(this)
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        val dialogBuilder = AlertDialog.Builder(this)

        // リスト項目生成
        val items = arrayOfNulls<String>(allergyArray)
        for (i in 0 until allergyArray) {
            //ダイアログ内のリストにワクチン一覧をセット
            items[i] = allergy[i]!!.allergy_name
        }
        // タイトル設定
        dialogBuilder.setTitle("アレルギーを選択してください")
        // リスト項目を設定 & クリック時の処理を設定
        dialogBuilder.setItems(
            items
        ) { _, which ->
            // whichには選択したリスト項目の順番が入っているので、それを使用して値を取得
            val selectedVal = items[which]
            allergyNameTexts.add(selectedVal.toString())
            textAllergyName.text = selectedVal
            layoutAllergy.addView(
                textAllergyName,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            allergyTextArray += 1
        }
        // dialogBalderを返す
        return dialogBuilder.create()
    }

    private fun childChang(){

        val vaccinationCount = intent.getStringExtra("vaccinationCount").toInt()
        val allergyCount = intent.getStringExtra("allergyCount").toInt()

        val aCounter = allergyCount + allergyNameTexts.size

        val account: Account? = realm.where<Account>().findFirst()
        val token = account!!.Rtoken
        val childId = intent.getStringExtra("user_id")
        val params = hashMapOf(
            "token" to token,
            "child_id" to childId
        )

        if (vaccineNameTexts.size == 0  || allergyNameTexts.size == 0){
            Toast.makeText(applicationContext, "どちらかは入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        for (i in vaccinationCount until vaccineNameTexts.size+vaccinationCount){
            params["vaccination_new[$i][vaccine_name]"] = vaccineNameTexts[i-vaccinationCount]
        }

        for (i in vaccinationCount until vaccineDateTexts.size+vaccinationCount){
            params["vaccination_new[$i][visit_date]"] = vaccineDateTexts[i-vaccinationCount]
        }

        for (i in allergyCount until aCounter){
            params["allergy_new[$i]"] = allergyNameTexts[i-allergyCount]
        }

        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else{
                when(it.getString("status")){
                    "200" -> {
                        Toast.makeText(applicationContext, "登録しました", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNKNOWN_GROUP -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_USER_ID -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_USER_NAME -> {
                                    ApiError.showEditTextError(child_name,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_BIRTHDAY -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_AGE -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_GENDER -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_BLOOD_TYPE -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_BODY_HEIGHT -> {
                                    ApiError.showEditTextError(child_height,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_BODY_WEIGHT -> {
                                    ApiError.showEditTextError(child_weight,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_CLOTHES_SIZE -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_SHOES_SIZE -> {
                                    ApiError.showEditTextError(child_shoes,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_VACCINATION -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                    Toast.makeText(applicationContext, "ワクチン情報登録エラー", Toast.LENGTH_SHORT).show()
                                }
                                ApiError.VALIDATION_ALLERGY -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                    Toast.makeText(applicationContext, "アレルギー登録エラー", Toast.LENGTH_SHORT).show()
                                }
                                ApiError.ALLOW_EXTENSION -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.UPLOAD_FAILED -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "child/edit"  ,params
            )
        )

    }



}
