package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.ux.ArFragment
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Func
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_tryon.*
import java.io.BufferedInputStream
import java.io.FileInputStream

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TryonActivity : AppCompatActivity() {

    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String
    private var fragment: ArFragment? = null
    private var childPhotoArray:ArrayList<String> = arrayListOf()

    companion object {
        const val READ_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tryon)

        realm = Realm.getDefaultInstance()
        val account = realm.where(Account::class.java).findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()
        if (account == null || group == null) {
        } else {
            userToken = account.Rtoken
            groupId = group.Rgroup_id
        }

        onModelGet()
        onClothesBottomList()
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?


        button_child_model.setOnClickListener {
            selectPhoto()
        }

        button_camera.setOnClickListener {
            //スクリーンショット処理
        }

    }

    private fun onClothesBottomList() {
        val gallery: LinearLayout = findViewById(R.id.gallery_layout)
        val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val files = path?.resolve("clothes")?.listFiles()
        if(files != null){
            val clothesFiles = path!!.resolve("clothes").listFiles()
            for(i in clothesFiles.indices) {
                val imageView = ImageView(this)
                imageView.id = i
                val stream = FileInputStream(clothesFiles[i].toString())
                val bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))
                imageView.setImageBitmap(bitmap)
                gallery.addView(imageView)
//                imageView.setOnClickListener { view -> addPhoto(clothesFiles[i].toString())}
            }
        }
    }

    private fun onModelDownload(url: String, fileName: String) {
        val fetchConfiguration = FetchConfiguration.Builder(this).apply {
            setDownloadConcurrentLimit(1)
        }.build()
        val fetch = Fetch.Impl.getInstance(fetchConfiguration)
        val request = Request(url, fileName).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

        fetch.enqueue(request,
            Func { Toast.makeText(this, "ダウンロードしました", Toast.LENGTH_SHORT).show() },
            Func { Toast.makeText(this, "ダウンロードに失敗しました", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun onModelGet() {
        ApiGetTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showToast(this, ApiError.UNKNOWN_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val jsonData = jsonObject.getJSONObject("data")
                        val jsonModelChild = jsonData.getJSONArray("model_child")
                        val jsonModelClothes = jsonData.getJSONArray("model_clothes")
                        val path =
                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        val childFiles = path!!.resolve("child")
                            .listFiles()
                        val clothesFiles = path.resolve("clothes")
                            .listFiles()
                        val childString = childFiles?.toString()
                        val clothesString = clothesFiles?.toString()
                        for(i in 0 until jsonModelChild.length()){
                            val fileName = jsonModelChild.getString(i).split("/").last()
                            if(listOf(childString).contains("$path/child/$fileName")){
                            }else{
                                childPhotoArray.add("file://$path/child/$fileName")
                                onModelDownload(jsonModelChild.getString(i), "$path/child/$fileName")
                            }
                        }
                        for(j in 0 until jsonModelClothes.length()){
                            val fileName= jsonModelClothes.getString(j).split("/").last()
                            if(listOf(clothesString).contains("$path/clothes/$fileName")){
                            }else{
                                onModelDownload(jsonModelClothes.getString(j), "$path/clothes/$fileName")
                            }
                        }
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_TOKEN,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                else -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "model/get",
                hashMapOf("token" to userToken, "group_id" to groupId)
            )
        )
    }

    private fun selectPhoto(){
        val intent = Intent(this,TryonSelectChildActivity::class.java)
        intent.putStringArrayListExtra("MODEL_LIST",childPhotoArray)
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {

            READ_REQUEST_CODE -> {
                try {

                    var imageView = findViewById<ImageView>(R.id.imageChildModel)
                    val stream = FileInputStream(resultData!!.getStringExtra("MODEL_STRING"))
                    val bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))
                        imageView.setImageBitmap(bitmap)

                } catch (e: Exception) {
                    Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

}
