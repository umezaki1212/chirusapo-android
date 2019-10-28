package com.vxx0.aso.ore_no_yome

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import jp.ac.asojuku.st.chirusapo.R
import jp.ac.asojuku.st.chirusapo.SignInActivity
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.content_main_post_add.*
import java.io.IOException

class MainPostAddActivity : AppCompatActivity() {
    private lateinit var userToken: String
    private lateinit var group_id: String
    private lateinit var charaSelect: String
    private val resultRequestPickImage01 = 1001
    private val resultRequestPickImage02 = 1002
    private var resultPickImage01: Bitmap? = null
    private var resultPickImage02: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        setSupportActionBar(toolbar)

        val pref = getSharedPreferences("data", MODE_PRIVATE)
        userToken = "Uel1KebSmQRH2I6yqoro1JgVLkskDi"
        group_id = "raigekka"

        loading_background.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == RESULT_OK) {
            if (resultData != null) {
                when (requestCode) {
                    resultRequestPickImage01 -> {
                        val uri = resultData.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            resultPickImage01 = bitmap
                            button_image_select_1.setImageBitmap(bitmap)
                            button_image_select_1.scaleType = ImageView.ScaleType.FIT_CENTER
                            button_image_select_2.visibility = View.VISIBLE
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    resultRequestPickImage02 -> {
                        val uri = resultData.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            resultPickImage02 = bitmap
                            button_image_select_2.setImageBitmap(bitmap)
                            button_image_select_2.scaleType = ImageView.ScaleType.FIT_CENTER
                        } catch (e: IOException) {
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

    override fun onResume() {
        super.onResume()

        button_image_select_1.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, resultRequestPickImage01)
        }

        button_image_select_2.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, resultRequestPickImage02)
        }


        button_post_add.setOnClickListener { view ->
            if (text_input_post_content.text.isNullOrEmpty() || charaSelect.isNotEmpty() || userToken.isNotEmpty()) {
                loading_background.visibility = View.VISIBLE
                text_input_post_content.visibility = View.INVISIBLE
                image_area.visibility = View.INVISIBLE
                button_post_add.visibility = View.INVISIBLE

                ApiPostTask {
                    if (it == null) {
                        Snackbar.make(view, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
                    } else {
                        when (it.getString("status")) {
                            "200" -> {
                                finish()
                            }
                            "400" -> {
                                val msgArray = it.getJSONArray("msg")
                                for (i in 0 until msgArray.length()) {
                                    when (msgArray.getString(i)) {
                                        "REQUIRED_PARAM" -> Snackbar.make(
                                            view,
                                            "必要な値が見つかりませんでした",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        "UNKNOWN_TOKEN" -> {
                                            Toast.makeText(this, "ログイントークンが不明です", Toast.LENGTH_SHORT).show()
                                            Intent(
                                                this, SignInActivity::class.java
                                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        "POST_CONTENT_LENGTH_OVER" -> Snackbar.make(
                                            view,
                                            "投稿できる最大文字数を超えています",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        else -> Snackbar.make(view, "不明なエラーが発生しました", Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            else -> Snackbar.make(view, "不明なエラーが発生しました", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    loading_background.visibility = View.GONE
                    text_input_post_content.visibility = View.VISIBLE
                    image_area.visibility = View.VISIBLE
                    button_post_add.visibility = View.VISIBLE
                }.execute(
                    ApiParam(
                        "post/add-post",
                        hashMapOf(
                            "token" to userToken,
                            "text" to text_input_post_content.text.toString(),
                            "user_id" to group_id
                        )
                    )
                )
            }
        }
    }

}