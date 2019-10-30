package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_main_post_add.*
import kotlinx.android.synthetic.main.content_main_post_add.*
import java.io.IOException

class MainPostAddActivity : AppCompatActivity() {
    private lateinit var userToken: String
    private lateinit var group_id: String
    private val resultRequestPickImage01 = 1001
    private val resultRequestPickImage02 = 1002
    private val resultRequestPickImage03 = 1003
    private val resultRequestPickImage04 = 1004
    private var resultPickImage01: Bitmap? = null
    private var resultPickImage02: Bitmap? = null
    private var resultPickImage03: Bitmap? = null
    private var resultPickImage04: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_post_add)

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

        button_image_select_1.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setMessage("選択を解除しますか？")
                .setPositiveButton("解除") { _, _ ->
                    resultPickImage01 = null
                    button_image_select_1.setImageBitmap(null)
                }
                .setNegativeButton("キャンセル", null)
                .create()
                .show()
            return@setOnLongClickListener true
        }

        button_image_select_2.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, resultRequestPickImage02)
        }

        button_image_select_2.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setMessage("選択を解除しますか？")
                .setPositiveButton("解除") { _, _ ->
                    resultPickImage02 = null
                    button_image_select_2.setImageBitmap(null)
                    button_image_select_2.visibility = View.INVISIBLE
                }
                .setNegativeButton("キャンセル", null)
                .create()
                .show()
            return@setOnLongClickListener true
        }

        button_post_add.setOnClickListener { view ->
            if (text_input_post_content.text.isNullOrEmpty() || userToken.isNotEmpty()) {
                loading_background.visibility = View.VISIBLE
                text_input_post_content.visibility = View.INVISIBLE
                image_area.visibility = View.INVISIBLE
                button_post_add.visibility = View.INVISIBLE

                val param = hashMapOf(
                    "token" to userToken,
                    "text" to text_input_post_content.text.toString(),
                    "group_id" to group_id
                )
                val image = arrayListOf<ApiParamImage>()

                if (resultPickImage01 != null) {
                    image.add(ApiParamImage(
                        "image/jpg",
                        "image01.jpg",
                        "image01",
                        resultPickImage01!!
                    ))

                    if (resultPickImage02 != null) {
                        image.add(ApiParamImage(
                            "image/jpg",
                            "image02.jpg",
                            "image02",
                            resultPickImage02!!
                        ))

                        if (resultPickImage03 != null) {
                            image.add(ApiParamImage(
                                "image/jpg",
                                "image03.jpg",
                                "image03",
                                resultPickImage03!!
                            ))

                            if (resultPickImage04 != null) {
                                image.add(ApiParamImage(
                                    "image/jpg",
                                    "image04.jpg",
                                    "image04",
                                    resultPickImage04!!
                                ))
                            }
                        }
                    }
                }

                ApiMediaPostTask {
                    if (it == null) {
                        Snackbar.make(view, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
                    } else {
                        when (it.getString("status")) {
                            "200" -> {
                                finish()
                            }
                            "400" -> {
                                val msgArray = it.getJSONArray("message")
                                for (i in 0 until msgArray.length()) {
                                    when (msgArray.getString(i)) {
                                        "REQUIRED_PARAM" -> Snackbar.make(
                                            view,
                                            "必要な値が見つかりませんでした",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        "UNKNOWN_TOKEN" -> {
                                            Toast.makeText(
                                                this,
                                                "ログイントークンが不明です",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Intent(
                                                this, SignInActivity::class.java
                                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        "POST_CONTENT_LENGTH_OVER" -> Snackbar.make(
                                            view,
                                            "投稿できる最大文字数を超えています",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        else -> Snackbar.make(
                                            view,
                                            "不明なエラーが発生しました",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            else -> Snackbar.make(
                                view,
                                "不明なエラーが発生しました",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                    loading_background.visibility = View.GONE
                    text_input_post_content.visibility = View.VISIBLE
                    image_area.visibility = View.VISIBLE
                    button_post_add.visibility = View.VISIBLE
                }.execute(
                    ApiParam(
                        Api.SLIM + "timeline/post",
                        param,
                        image
                    )
                )
            }
        }
    }

}