package jp.ac.asojuku.st.chirusapo

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Func
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.fragment_dress.*
import java.io.IOException

class DressFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dress, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onResume() {
        super.onResume()

        list_view.setOnItemClickListener { adapterView, _, i, _ ->
            when (adapterView.getItemAtPosition(i)) {
                resources.getString(R.string.dress_model_child_add) -> {

                }
                resources.getString(R.string.dress_model_clothes_add) -> {

                }
                resources.getString(R.string.dress_model_generate) -> {
                    onModelGenerateSelect()
                }
                resources.getString(R.string.dress_model_list) -> {

                }
                resources.getString(R.string.dress_try_on) -> {

                }
                resources.getString(R.string.dress_try_on_photo) -> {

                }
            }
        }
    }

    private fun onModelGenerateSelect() {
        val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst()
        if (apiKey == null) {
            Snackbar.make(
                root_view,
                "APIキーが見つからないため実行できません\n設定を開いてAPIキーを入力してください",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        AlertDialog.Builder(activity).apply {
            setTitle("背景透過モデル画像生成")
            setItems(
                arrayOf(
                    "カメラで撮影して生成",
                    "ファイルを選択して生成"
                )
            ) { _: DialogInterface?, i: Int ->
                when (i) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, MODEL_GENERATE_CAMERA_CODE)
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*"
                        }
                        startActivityForResult(intent, MODEL_GENERATE_SELECT_CODE)
                    }
                }
            }
            create()
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == RESULT_OK) {
            if (resultData != null) {
                when (requestCode) {
                    MODEL_GENERATE_CAMERA_CODE -> {
                        try {
                            val bitmap = resultData.extras?.get("data") as Bitmap
                            onModelGenerate(bitmap)
                        } catch (e: Exception) {
                            Toast.makeText(activity, "撮影画像を取得できませんでした", Toast.LENGTH_SHORT).show()
                        }
                    }
                    MODEL_GENERATE_SELECT_CODE -> {
                        val uri = resultData.data as Uri
                        try {
                            val bitmap: Bitmap = getBitmapFromUri(uri)
                            onModelGenerate(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = activity!!.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun onModelGenerate(bitmap: Bitmap) {
        val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst() ?: return

        ApiMediaPostTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showToast(activity!!, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val imagePath = jsonObject.getJSONObject("data").getString("remove_image")
                        // activity!!.getExternalFilesDir(DIRECTORY_DOWNLOADS).toString()
                        val fileName = imagePath.split("/").last()
                        val path =
                            Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
                        onModelDownload(imagePath, "$path/$fileName")
                    }
                    "400" -> {
                        ApiError.showToast(activity!!, "透過画像を生成できませんでした", Toast.LENGTH_SHORT)
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "/external-api/remove.bg/remove",
                hashMapOf("api_key" to apiKey.apiKey),
                arrayListOf(ApiParamImage("image/jpeg", "image.jpeg", "image", bitmap))
            )
        )
    }

    private fun onModelDownload(url: String, fileName: String) {
        val fetchConfiguration = FetchConfiguration.Builder(activity!!).apply {
            setDownloadConcurrentLimit(1)
        }.build()
        val fetch = Fetch.Impl.getInstance(fetchConfiguration)

        val request = Request(url, fileName).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

        fetch.enqueue(request,
            Func { Toast.makeText(activity, "ダウンロードしました", Toast.LENGTH_SHORT).show() },
            Func { Toast.makeText(activity, "ダウンロードに失敗しました", Toast.LENGTH_SHORT).show() }
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        const val MODEL_GENERATE_CAMERA_CODE = 1000
        const val MODEL_GENERATE_SELECT_CODE = 1001
    }
}
