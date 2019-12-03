package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_change_profile.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var userToken: String
    // text
    private lateinit var inputUserName: TextInputLayout
    private lateinit var inputLineId: TextInputLayout
    private lateinit var inputIntroduction: TextInputLayout
    private var oldUserName: String = ""
    private var oldLineId: String = ""
    private var oldIntroduction: String = ""
    // image
    private var userIcon:Bitmap? = null
    private val userIconRequestCode = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_profile)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
            title = "プロフィール変更"
        }

        inputUserName = input_user_name
        inputLineId = input_line_id
        inputIntroduction = input_introduction

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        if (account != null) {
            userToken = account.Rtoken

            if (!account.Ruser_icon.isNullOrEmpty()) {
                Picasso.get().load(account.Ruser_icon).into(button_user_icon)
            }
        }

        getMyProfile()

        button_user_icon.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, userIconRequestCode)
        }

        button_user_icon.setOnLongClickListener {
            if (userIcon != null) {
                AlertDialog.Builder(this)
                    .setMessage("選択を解除しますか？")
                    .setPositiveButton("解除") { _, _ ->
                        userIcon = null
                        button_user_icon.setImageBitmap(null)
                    }
                    .setNegativeButton("キャンセル", null)
                    .create()
                    .show()
            }
            return@setOnLongClickListener true
        }

        button_edit_profile.setOnClickListener {view ->
            editMyProfile(view)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (data != null) {
                when (requestCode) {
                    userIconRequestCode -> {
                        val uri = data.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            userIcon = bitmap
                            button_user_icon.apply {
                                setImageBitmap(bitmap)
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                        } catch (e: IOException) {
                            Toast.makeText(this, "画像を取得できませんでした", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun validationUserName(): Boolean{
        val userName = inputUserName.editText?.text.toString()
        return when {
            userName.isEmpty() -> {
                inputUserName.error = null
                true
            }
            !Pattern.compile("^.{2,30}$").matcher(userName).find() -> {
                inputUserName.error = "2文字以上30文字以下で入力してください"
                false
            }
            else -> {
                inputUserName.error = null
                true
            }
        }
    }

    private fun validationLineId():Boolean {
        val lineId = inputLineId.editText?.text.toString()
        return when {
            lineId.isEmpty() -> {
                input_line_id.error = null
                true
            }
            !(Pattern.compile("^[a-zA-Z0-9-_]{4,20}$").matcher(lineId).find()) -> {
                input_line_id.error = "半角英数字で4文字以上20文字以下で入力してください"
                false
            }
            else -> {
                input_line_id.error = null
                true
            }
        }
    }

    private fun validationIntroduction(): Boolean {
        val introduction = inputIntroduction.editText?.text.toString()
        return when {
            introduction.isEmpty() -> {
                inputIntroduction.error = null
                true
            }
            introduction.count() > 250 -> {
                inputIntroduction.error = "250文字以下で入力してください"
                false
            }
            else -> {
                inputIntroduction.error = null
                true
            }
        }
    }

    private fun getMyProfile() {
        root_view.visibility = View.INVISIBLE
        ApiPostTask {
            if (it == null) {
                ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (it.getString("status")) {
                    "200" -> {
                        val dataJson = it.getJSONObject("data")
                        val userInfoJson = dataJson.getJSONObject("user_info")
                        val userName = userInfoJson.getString("user_name")
                        val lineId = if (userInfoJson.isNull("line_id")) {
                            ""
                        } else {
                            userInfoJson.getString("line_id")
                        }
                        val introduction = if (userInfoJson.isNull("introduction")) {
                            ""
                        } else {
                            userInfoJson.getString("introduction")
                        }
                        if (!userInfoJson.isNull("user_icon")) {
                            Picasso.get().load(userInfoJson.getString("user_icon")).into(button_user_icon)
                        }

                        inputUserName.editText?.setText(userName)
                        oldUserName = userName
                        inputLineId.editText?.setText(lineId)
                        oldLineId = lineId
                        inputIntroduction.editText?.setText(introduction)
                        oldIntroduction = introduction
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(
                                        this,
                                        ApiError.UNKNOWN_ERROR,
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this, ApiError.UNKNOWN_TOKEN, Toast.LENGTH_SHORT)
                                    val intent = Intent(this, SignInActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }
                                else -> {
                                    ApiError.showToast(
                                        this,
                                        ApiError.UNKNOWN_ERROR,
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        ApiError.showToast(this, ApiError.UNKNOWN_ERROR, Toast.LENGTH_SHORT)
                    }
                }
            }
            root_view.visibility = View.VISIBLE
        }.execute(ApiParam(Api.SLIM + "account/edit", hashMapOf("token" to userToken)))
    }

    private fun editMyProfile(view: View) {
        var check = true
        if (!validationUserName()) check = false
        if (!validationLineId()) check = false
        if (!validationIntroduction()) check = false

        Log.d("TEST", "name -> " + (validationUserName()).toString())
        Log.d("TEST", "line -> " + (validationLineId()).toString())
        Log.d("TEST", "into -> " + (validationIntroduction()).toString())

        if (!check) return

        val newUserName = inputUserName.editText?.text.toString()
        val newLineId = inputLineId.editText?.text.toString()
        val newIntroduction = inputIntroduction.editText?.text.toString()

        var flgUserName = false
        var flgLineId = false
        var flgIntroduction = false
        var flgUserIcon = false

        if (!Objects.equals(newUserName, oldUserName)) {
            flgUserName = true
        }
        if (!Objects.equals(newLineId, oldLineId)) {
            flgLineId = true
        }
        if (!Objects.equals(newIntroduction, oldIntroduction)) {
            flgIntroduction = true
        }
        if (userIcon != null) {
            flgUserIcon = true
        }

        if (flgUserName || flgLineId || flgIntroduction || flgUserIcon) {
            val param = hashMapOf("token" to userToken)
            val paramImage = arrayListOf<ApiParamImage>()

            if (flgUserName) {
                param["user_name"] = newUserName
            }
            if (flgLineId) {
                param["line_id"] = newLineId
            }
            if (flgIntroduction) {
                param["introduction"] = newIntroduction
            }
            if (flgUserIcon) {
                val paramItem = ApiParamImage("image/jpg", "image01.jpg", "user_icon", userIcon!!)
                paramImage.add(paramItem)
            }

            ApiMediaPostTask {
                if (it == null) {
                    ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                } else {
                    when (it.getString("status")) {
                        "200" -> {
                            ApiError.showSnackBar(view, "プロフィールを更新しました", Snackbar.LENGTH_SHORT)

                            val dataJson = it.getJSONObject("data")
                            val userInfoJson = dataJson.getJSONObject("user_info")
                            val userName = userInfoJson.getString("user_name")
                            val lineId = if (userInfoJson.isNull("line_id")) {
                                ""
                            } else {
                                userInfoJson.getString("line_id")
                            }
                            val introduction = if (userInfoJson.isNull("introduction")) {
                                ""
                            } else {
                                userInfoJson.getString("introduction")
                            }
                            val userIcon = if (userInfoJson.isNull("user_icon")) {
                                null
                            } else {
                                Picasso.get().load(userInfoJson.getString("user_icon")).into(button_user_icon)
                                userInfoJson.getString("user_icon")
                            }

                            val account = realm.where(Account::class.java).findFirst()
                            if (account != null) {
                                realm.executeTransaction {
                                    account.Ruser_name = userName
                                    account.Ruser_icon = userIcon
                                }
                            }

                            inputUserName.editText?.setText(userName)
                            oldUserName = userName
                            inputUserName.error = null
                            inputLineId.editText?.setText(lineId)
                            oldLineId = lineId
                            inputLineId.error = null
                            inputIntroduction.editText?.setText(introduction)
                            oldIntroduction = introduction
                            inputIntroduction.error = null
                            this.userIcon = null
                        }
                        "400" -> {
                            val errorArray = it.getJSONArray("message")
                            for (i in 0 until errorArray.length()) {
                                when (errorArray.getString(i)) {
                                    ApiError.REQUIRED_PARAM -> {
                                        ApiError.showToast(
                                            this,
                                            ApiError.UNKNOWN_ERROR,
                                            Toast.LENGTH_SHORT
                                        )
                                    }
                                    ApiError.UNKNOWN_TOKEN -> {
                                        ApiError.showToast(this, ApiError.UNKNOWN_TOKEN, Toast.LENGTH_SHORT)
                                        val intent = Intent(this, SignInActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        startActivity(intent)
                                    }
                                    ApiError.VALIDATION_USER_NAME -> {
                                        ApiError.showEditTextError(inputUserName, ApiError.VALIDATION_USER_NAME)
                                    }
                                    ApiError.VALIDATION_LINE_ID -> {
                                        ApiError.showEditTextError(inputLineId, ApiError.VALIDATION_LINE_ID)
                                    }
                                    ApiError.VALIDATION_INTRODUCTION -> {
                                        ApiError.showEditTextError(inputIntroduction, ApiError.VALIDATION_INTRODUCTION)
                                    }
                                    ApiError.UPLOAD_FAILED -> {
                                        ApiError.showToast(this, ApiError.UPLOAD_FAILED, Toast.LENGTH_SHORT)
                                    }
                                    ApiError.ALLOW_EXTENSION -> {
                                        ApiError.showToast(this, ApiError.ALLOW_EXTENSION, Toast.LENGTH_SHORT)
                                    }
                                    else -> {
                                        ApiError.showToast(
                                            this,
                                            ApiError.UNKNOWN_ERROR,
                                            Toast.LENGTH_SHORT
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            ApiError.showToast(this, ApiError.UNKNOWN_ERROR, Toast.LENGTH_SHORT)
                        }
                    }
                }
            }.execute(ApiParam(Api.SLIM + "account/edit", param, paramImage))
        }
    }
}
