package jp.ac.asojuku.st.chirusapo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.isseiaoki.simplecropview.CropImageView
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_child_face_trimming.*
import java.io.IOException

class ChildFaceTrimmingActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var childId:String

    companion object {
        const val PHOTO_SELECT_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_face_trimming)

        onUploadPhotoPicker()

    }

    private fun onUploadPhotoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, PHOTO_SELECT_CODE)
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = this.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun trimming(bitmap: Bitmap) {
        val cropImageView = findViewById<CropImageView>(R.id.cropImageView)

        val croppedImageView = findViewById<View>(R.id.croppedImageView) as ImageView

    // トリミングしたい画像をセット
        cropImageView.imageBitmap = bitmap
            // BitmapFactory.decodeResource(resources, bitmap)

        val cropButton = findViewById<View>(R.id.crop_button) as Button
        cropButton.setOnClickListener {
            // フレームに合わせてトリミング
            croppedImageView.setImageBitmap(cropImageView.croppedBitmap)
            val afterImage = (croppedImageView.drawable as BitmapDrawable).bitmap
            onPhotoUpload(afterImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                when (requestCode) {
                    ChildFaceActivity.PHOTO_SELECT_CODE -> {
                        try {
                            val uri = resultData.data as Uri
                            val bitmap = getBitmapFromUri(uri)
                            trimming(bitmap)
                        } catch (e: Exception) {
                            Snackbar
                                .make(root_layout, "選択された画像を取得できませんでした", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun onPhotoUpload(bitmap: Bitmap) {
        Snackbar.make(root_layout, "画像をアップロードしています…", Snackbar.LENGTH_SHORT).show()

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        childId = intent.getStringExtra("childId")!!

        if (account != null) {
            userToken = account.Rtoken
        }

        ApiMediaPostTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showSnackBar(root_layout, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val albumJsonArray =
                            jsonObject.getJSONObject("data").getJSONObject("face_info")

                        if (albumJsonArray.length() == 0) {
                        } else {

                            arrayListOf<String>().apply {
                                (0 until albumJsonArray.length()).forEach { _ ->
                                    this.add(albumJsonArray.getString("file_name"))
                                }
                            }

                            finish()
                        }
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent =
                                        Intent(this, SignInActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    startActivity(intent)
                                }
                                else -> {
                                    ApiError.showSnackBar(root_layout, errorArray.getString(i), Snackbar.LENGTH_SHORT)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "/child/face/add",
                hashMapOf("token" to userToken, "child_id" to childId),
                arrayListOf(
                    ApiParamImage("image/jpeg", "image.jpeg", "face_image", bitmap)
                )
            )
        )
    }


}
