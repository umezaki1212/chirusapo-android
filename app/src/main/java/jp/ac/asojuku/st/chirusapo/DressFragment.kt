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
    private lateinit var userToken: String
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        val account = realm.where(Account::class.java).findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()
        if (account != null && group != null) {
            userToken = account.Rtoken
            groupId = group.Rgroup_id
        }
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
                    onChildModelGenerateSelect()
                }
                resources.getString(R.string.dress_model_clothes_add) -> {
                    onClothesGenerateSelect()
                }
                resources.getString(R.string.dress_model_list) -> {
                    val intent = Intent(activity, DressModelViewer::class.java)
                    startActivity(intent)
                }
                resources.getString(R.string.dress_try_on) -> {
                    val intent = Intent(activity, TryonActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun onChildModelGenerateSelect() {
        val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst()
        if (apiKey == null) {
            Snackbar.make(
                root_view,
                "APIキーが見つからないため実行できません\n設定を開いてAPIキーを入力してください",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val dialogItems = resources.getStringArray(R.array.dress_fragment_model_dialog)
        AlertDialog.Builder(activity).apply {
            setItems(dialogItems) { _: DialogInterface?, i: Int ->
                when (dialogItems[i]) {
                    resources.getString(R.string.dress_model_dialog_camera) -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, MODEL_CHILD_CAMERA_CODE)
                    }
                    resources.getString(R.string.dress_model_dialog_file) -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*"
                        }
                        startActivityForResult(intent, MODEL_CHILD_SELECT_CODE)
                    }
                }
            }
            create()
            show()
        }
    }

    private fun onClothesGenerateSelect() {
        val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst()
        if (apiKey == null) {
            Snackbar.make(
                root_view,
                "APIキーが見つからないため実行できません\n設定を開いてAPIキーを入力してください",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val dialogItems = resources.getStringArray(R.array.dress_fragment_model_dialog)
        AlertDialog.Builder(activity).apply {
            setItems(dialogItems) { _: DialogInterface?, i: Int ->
                when (dialogItems[i]) {
                    resources.getString(R.string.dress_model_dialog_camera) -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, MODEL_CLOTHES_CAMERA_CODE)
                    }
                    resources.getString(R.string.dress_model_dialog_file) -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*"
                        }
                        startActivityForResult(intent, MODEL_CLOTHES_SELECT_CODE)
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
                    MODEL_CHILD_CAMERA_CODE -> {
                        try {
                            val bitmap = resultData.extras?.get("data") as Bitmap
                            onModelChildAdd(bitmap)
                        } catch (e: Exception) {
                            Snackbar
                                .make(root_view, "撮影画像を取得できませんでした", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                    MODEL_CHILD_SELECT_CODE -> {
                        try {
                            val uri = resultData.data as Uri
                            val bitmap = getBitmapFromUri(uri)
                            onModelChildAdd(bitmap)
                        } catch (e: IOException) {
                            Snackbar
                                .make(root_view, "選択された画像を取得できませんでした", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                    MODEL_CLOTHES_CAMERA_CODE -> {
                        try {
                            val bitmap = resultData.extras?.get("data") as Bitmap
                            onModelClothesAdd(bitmap)
                        } catch (e: Exception) {
                            Snackbar
                                .make(root_view, "撮影画像を取得できませんでした", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                    MODEL_CLOTHES_SELECT_CODE -> {
                        try {
                            val uri = resultData.data as Uri
                            val bitmap = getBitmapFromUri(uri)
                            onModelClothesAdd(bitmap)
                        } catch (e: Exception) {
                            Snackbar
                                .make(root_view, "選択された画像を取得できませんでした", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun onModelChildAdd(bitmap: Bitmap) {
        Snackbar.make(root_view, "処理しています…", Snackbar.LENGTH_LONG).show()
        val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst() ?: return
        ApiMediaPostTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showToast(activity!!, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val imagePath = jsonObject.getJSONObject("data").getString("model_path")
                        val path = activity!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
                        val fileName = imagePath.split("/").last()
                        onModelDownload(imagePath, "$path/child/$groupId/$fileName")
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP,
                                ApiError.ALLOW_EXTENSION,
                                ApiError.UPLOAD_FAILED,
                                "GENERATE_MODEL_FAILED" -> {
                                    ApiError.showSnackBar(root_view, errorArray.getString(i), Toast.LENGTH_SHORT)
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent = Intent(activity, SignInActivity::class.java)
                                    startActivity(intent)
                                }
                                else -> {
                                    ApiError.showSnackBar(root_view, errorArray.getString(i), Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "/model/add/child/remove",
                hashMapOf(
                    "token" to userToken,
                    "group_id" to groupId,
                    "api_key" to apiKey.apiKey
                ),
                arrayListOf(ApiParamImage("image/jpeg", "image.jpeg", "image", bitmap))
            )
        )
    }

    private fun onModelClothesAdd(bitmap: Bitmap) {
        Snackbar.make(root_view, "処理しています…", Snackbar.LENGTH_LONG).show()
        val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst() ?: return
        ApiMediaPostTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showToast(activity!!, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val imagePath = jsonObject.getJSONObject("data").getString("model_path")
                        val path = activity!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
                        val fileName = imagePath.split("/").last()
                        onModelDownload(imagePath, "$path/clothes/$groupId/$fileName")
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP,
                                ApiError.ALLOW_EXTENSION,
                                ApiError.UPLOAD_FAILED,
                                "GENERATE_MODEL_FAILED" -> {
                                    ApiError.showSnackBar(root_view, errorArray.getString(i), Toast.LENGTH_SHORT)
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent = Intent(activity, SignInActivity::class.java)
                                    startActivity(intent)
                                }
                                else -> {
                                    ApiError.showSnackBar(root_view, errorArray.getString(i), Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "/model/add/clothes/remove",
                hashMapOf(
                    "token" to userToken,
                    "group_id" to groupId,
                    "api_key" to apiKey.apiKey
                ),
                arrayListOf(ApiParamImage("image/jpeg", "image.jpeg", "image", bitmap))
            )
        )
    }

    @Throws(IOException::class)
    fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = activity!!.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun onModelDownload(url: String, fileName: String) {
        val fetchConfiguration = FetchConfiguration.Builder(activity!!).apply {
            setDownloadConcurrentLimit(3)
        }.build()
        val fetch = Fetch.Impl.getInstance(fetchConfiguration)

        val request = Request(url, fileName).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

        fetch.enqueue(request,
            Func { Snackbar.make(root_view, "モデル画像を保存しました", Snackbar.LENGTH_SHORT).show() },
            Func { Snackbar.make(root_view, "ダウンロードに失敗しました", Snackbar.LENGTH_SHORT).show() }
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
        const val MODEL_CHILD_CAMERA_CODE = 1000
        const val MODEL_CHILD_SELECT_CODE = 1001
        const val MODEL_CLOTHES_CAMERA_CODE = 1002
        const val MODEL_CLOTHES_SELECT_CODE = 1003
    }
}