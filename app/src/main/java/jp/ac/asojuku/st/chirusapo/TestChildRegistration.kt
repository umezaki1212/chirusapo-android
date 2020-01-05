package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_registration_child.*
import kotlinx.android.synthetic.main.activity_test_child_registration.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class TestChildRegistration : AppCompatActivity() {
    lateinit var realm: Realm

    //誕生日取得のためのデータ取得
    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)

    //血液型のデータ設定
    private var bloodType = 0

    //性別
    private var gender = 0

    //服のサイズ
    private var clothes = 0

    private var vaccineArray = 0
    private var allergyArray = 0

    private var vaccineNameTexts = ArrayList<String>(vaccineArray)
    private var vaccineDateTexts = ArrayList<String>(vaccineArray)
    private val allergyNameTexts = ArrayList<String>(allergyArray)

    private var vaccineTextArray = 0
    private var allergyTextArray = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_child_registration)
        realm = Realm.getDefaultInstance()
        child_birthday.setOnClickListener { onBirthdaySetting() }

        val vaccine = realm.where<Vaccine>().findAll()
        val allergy = realm.where<Allergy>().findAll()
        vaccineArray = vaccine!!.size
        allergyArray = allergy.size

        supportActionBar?.let {
            it.title = "子供情報登録"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        // idがdialogButtonのButtonを取得
        val vaccineAddBtn = findViewById<View>(R.id.VaccineData_Add) as Button
        val allergyAddBtn = findViewById<View>(R.id.AllergyData_Add) as Button
        // clickイベント追加
        vaccineAddBtn.setOnClickListener {
            // ダイアログクラスをインスタンス化
            val vaccineDialog = VaccineName(vaccine)

            // 表示  getFragmentManager()は固定、sampleは識別タグ
            vaccineDialog.show()
        }
        allergyAddBtn.setOnClickListener {
            val allergyDialog = Allergy(allergy)
            allergyDialog.show()
        }

        childAdd_Button.setOnClickListener { addChild() }
    }

    override fun onResume() {
        super.onResume()
        child_birthday.isFocusable = false

        //血液型選択
        child_blood.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.child_blood)
                when (spinner.selectedItem.toString()) {
                    "未回答" -> bloodType = 0
                    "A型" -> bloodType = 1
                    "B型" -> bloodType = 2
                    "O型" -> bloodType = 3
                    "AB型" -> bloodType = 4
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                bloodType = 0
            }
        }

        //性別選択
        child_gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.child_gender)
                when (spinner.selectedItem.toString()) {
                    "未回答" -> gender = 0
                    "男性" -> gender = 1
                    "女性" -> gender = 2
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                gender = 0
            }
        }

        //服のサイズ選択
        child_clothes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = findViewById<Spinner>(R.id.child_gender)
                when (spinner.selectedItem.toString()) {
                    "50cm" -> clothes = 50
                    "60cm" -> clothes = 60
                    "70cm" -> clothes = 70
                    "80cm" -> clothes = 80
                    "90cm" -> clothes = 90
                    "100cm" -> clothes = 100
                    "110cm" -> clothes = 110
                    "120cm" -> clothes = 120
                    "130cm" -> clothes = 130
                    "140cm" -> clothes = 140
                    "150cm" -> clothes = 150
                    "160cm" -> clothes = 160
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                gender = 0
            }
        }

    }

    private fun userNameCheck():Boolean{
        val childName= child_name.editText?.text.toString().trim()

        return when {
            childName.isEmpty() -> {
                child_name.error = "ユーザー名が入力されていません"
                false
            }
            childName.count() < 2 -> {
                child_name.error = "ユーザー名の文字数が不正です"
                false
            }
            childName.count() > 30 -> {
                child_name.error = "ユーザー名の文字数が不正です"
                false
            }
            else -> {
                child_name.error = null
                true
            }
        }
    }

    private fun onBirthdaySetting(){
        val birthday = findViewById<EditText>(R.id.child_birthday)
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
            birthday.setText("%s-%s-%s".format(year, month, day))
        }, year,month,day
        ).show()
    }

    private fun onChildHeightCheck(): Boolean {
        val childHeight = child_height.editText?.text.toString().trim()

        return when{
            childHeight.isEmpty() -> {
                child_height.error = "身長が入力されていません"
                false
            }
            childHeight.toDouble() < 10 -> {
                child_height.error = "身長は10～200までの間で入力してください"
                false
            }
            childHeight.toDouble() > 200 -> {
                child_height.error = "身長は10～200までの間で入力してください"
                false
            }
            !Pattern.compile("^[0-9-.]*\$").matcher(childHeight).find()-> {
                child_height.error = "使用できない文字が含まれています"
                false
            }
            else -> {
                child_height.error = null
                true
            }
        }
    }

    private fun onChildWeightCheck(): Boolean {
        val childWeight = child_weight.editText?.text.toString().trim()

        return when{
            childWeight.isEmpty() -> {
                child_weight.error = "体重が入力されていません"
                false
            }
            childWeight.toDouble() < 1 -> {
                child_weight.error = "体重は1～150までの間で入力してください"
                false
            }
            childWeight.toDouble() > 150 -> {
                child_weight.error = "体重は1～150までの間で入力してください"
                false
            }
            !Pattern.compile("^[0-9-.]*\$").matcher(childWeight).find()-> {
                child_weight.error = "使用できない文字が含まれています"
                false
            }
            else -> {
                child_weight.error = null
                true
            }
        }
    }

    private fun onChildShoesSizeCheck(): Boolean{
        val childShoes = child_shoes.editText?.text.toString().trim()

        return when{
            childShoes.isEmpty() -> {
                child_shoes.error = "靴のサイズが入力されていません"
                false
            }
            childShoes.toDouble() < 5 -> {
                child_shoes.error = "靴のサイズは5～30までの間で入力してください"
                false
            }
            childShoes.toDouble() > 30 -> {
                child_shoes.error = "靴のサイズは5～30までの間で入力してください"
                false
            }
            !Pattern.compile("^[0-9-.]*\$").matcher(childShoes).find()-> {
                child_weight.error = "使用できない文字が含まれています"
                false
            }
            else -> {
                child_shoes.error = null
                true
            }
        }
    }

    private fun childBirthdayCheck():Boolean {
        val childBirthday = child_birthday.text.toString().trim()
        return when {
            childBirthday.isEmpty() -> {
                child_birthday.error = ""
                child_error.text = "誕生日が未入力です"
                false
            }
            else -> {
                child_birthday.error = null
                child_error.text = null
                true
            }
        }
    }

    private fun VaccineName(vaccine: RealmResults<Vaccine>): Dialog {
        val layoutName: LinearLayout = findViewById(R.id.vaccine_name_array)
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
            layoutName.addView(textVaccineName,LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        // dialogBalderを返す
        return dialogBuilder.create()
    }


    private fun VaccineDate(which: Int) {
        val layoutDate: LinearLayout = findViewById(R.id.vaccine_date_array)
        val textVaccineDate = TextView(this)
        DatePickerDialog(this,DatePickerDialog.OnDateSetListener{ _, y, m, d ->
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
            layoutDate.addView(textVaccineDate,LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT))

            vaccineTextArray += 1
        }, year,month,day
        ).show()
    }
    private fun Allergy(allergy: RealmResults<Allergy>):Dialog{
        val layoutAllergy: LinearLayout = findViewById(R.id.AllergyList)
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
            layoutAllergy.addView(textAllergyName,LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT))
            allergyTextArray += 1
        }
        // dialogBalderを返す
        return dialogBuilder.create()
    }
    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun addChild(){

        var check = true
        if(!userNameCheck())check = false
        if(!childBirthdayCheck())check = false
        if(!onChildHeightCheck())check = false
        if(!onChildWeightCheck())check = false
        if(!onChildShoesSizeCheck())check = false

        if(!check)return

//        val paramImage = arrayListOf<ApiParamImage>()
//        if(userIcon != null){
//            val paramItem = ApiParamImage("image/jpg","Child01.jpg","user_icon",userIcon!!)
//            paramImage.add(paramItem)
//        }

        val nen = child_birthday.text.toString().substring(0, 4).toInt()
        val mon = child_birthday.text.toString().substring(5, 7).toInt()
        val bday = child_birthday.text.toString().substring(8, 10).toInt()
        var age = year - nen
        if (mon > month){
            age--
        }else if (mon == month){
            if (bday > day){
                age--
            }
        }

        val account: Account? = realm.where<Account>().findFirst()
        val joinGroup: JoinGroup? = realm.where<JoinGroup>().findFirst()
        val groupId = joinGroup!!.Rgroup_id
        val token = account!!.Rtoken

        val params = hashMapOf(
            "token" to token,
            "group_id" to groupId,
            "user_name" to child_name.editText?.text.toString(),
            "birthday" to Child_Birthday.text.toString().trim(),
            "age" to age,
            "gender" to gender.toString(),
            "blood_type" to bloodType.toString(),
            "body_height" to child_height.editText?.text.toString(),
            "body_weight" to child_weight.editText?.text.toString(),
            "clothes_size" to child_clothes.toString(),
            "shoes_size" to child_shoes.editText?.text.toString()
        )

        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else{
                when(it.getString("status")){
                    "200" -> {
                        Toast.makeText(applicationContext, "登録しました", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(this, CheckGrowthActivity::class.java).apply {
//                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        }
//                        startActivity(intent)
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
                                    ApiError.showEditTextError(Child_Name,errorArray.getString(i))
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
                                    ApiError.showEditTextError(Child_Height,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_BODY_WEIGHT -> {
                                    ApiError.showEditTextError(Child_Weight,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_CLOTHES_SIZE -> {
                                    ApiError.showToast(this,errorArray.getString(i), Toast.LENGTH_LONG)
                                }
                                ApiError.VALIDATION_SHOES_SIZE -> {
                                    ApiError.showEditTextError(child_shoesSize,errorArray.getString(i))
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
                                ApiError.ALREADY_USER_ID -> {
                                    ApiError.showEditTextError(Child_Id,errorArray.getString(i))
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "child/add"
//                params,paramImage
            )
        )

    }

}
