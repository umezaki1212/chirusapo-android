package jp.ac.asojuku.st.chirusapo

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_tryon.*
import android.provider.MediaStore.Images
import java.io.*
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.view.MotionEvent
import androidx.core.view.children
import com.google.ar.sceneform.ux.ArFragment

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
class TryonActivity : AppCompatActivity(){

    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String
    private var fragment: ArFragment? = null
    private val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    private var targetLocalX: Int = 0
    private var targetLocalY: Int = 0

    private var screenX: Int = 0
    private var screenY: Int = 0

    private var idCounter:Int = 100;

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

        onClothesBottomList()
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?


        button_child_model.setOnClickListener {
            //モデル画像選択処理
            selectPhoto(onChildGetPicture())
        }

        button_camera.setOnClickListener {
            //スクリーンショット処理
            permisson()
        }


    }


    private fun onClothesBottomList() {
        val gallery: LinearLayout = findViewById(R.id.gallery_layout)
        val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val files = path!!.resolve("clothes/$groupId")?.listFiles()
        if(files != null){
            for(i in files.indices) {
                val imageView = ImageView(this)
                imageView.id = i
                val stream = FileInputStream(files[i].toString())
                val bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))
                imageView.setImageBitmap(bitmap)
                gallery.addView(imageView)
                imageView.setOnClickListener { view -> onAddPhoto(files[i].toString())}
            }
        }
    }

    private fun onChildGetPicture(): ArrayList<String> {
        val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val filesDirectory = path!!.resolve("child/$groupId")?.listFiles()
        var files:File
        val childPhotoArray = arrayListOf<String>()
        if(filesDirectory != null){
            for(i in filesDirectory.indices) {
                files = filesDirectory[i]
                childPhotoArray.add("file://$files")
            }
        }
        return childPhotoArray
    }

    private fun selectPhoto(childPhotoArray:ArrayList<String>){
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

    @SuppressLint("ClickableViewAccessibility")
    private fun onAddPhoto(fileName: String){
        val image = ImageView(this)

        image.id = ++idCounter;
        image.translationX = 400F
        image.translationY = 400F

        Log.d("Debug", image.id.toString())
        Log.d("Debug", image.x.toString())
        Log.d("Debug", image.y.toString())
        Log.d("Debug", fileName);

        val stream = FileInputStream(fileName)
        val bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))
        image.setImageBitmap(bitmap)
        screen_shot_area.addView(image)

        for( childView in screen_shot_area.children ){

            childView.translationX= childView.x
            childView.translationY = childView.y
        }

        image.setOnTouchListener { v, event ->

            val x = event.rawX.toInt()
            val y = event.rawY.toInt()


            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    targetLocalX = v.left
                    targetLocalY = v.top

                    screenX = x
                    screenY = y
                }

                MotionEvent.ACTION_MOVE -> {

                    val diffX = screenX - x
                    val diffY = screenY - y

                    targetLocalX -= diffX
                    targetLocalY -= diffY

                    v.layout(
                        targetLocalX,
                        targetLocalY,
                        targetLocalX + v.width,
                        targetLocalY + v.height
                    )

                    screenX = x
                    screenY = y
                }

                MotionEvent.ACTION_UP -> {

                    Log.d("Debug", v.id.toString())
                    Log.d("Debug", v.x.toString())
                    Log.d("Debug", v.y.toString())

                    if (v.x < 0 && v.y< 0) {
                        screen_shot_area.removeView(v)
                        for( childView in screen_shot_area.children ){

                            childView.translationX= childView.x
                            childView.translationY = childView.y
                        }
                    }
                }

            }
            true
        }

    }

    private fun generateScreenShot(){

        val path = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
        val filename =System.currentTimeMillis().toString() + ".jpg"
        val file = File("$path/$filename")
        try {

            val output = FileOutputStream(file)
            screen_shot_area.isDrawingCacheEnabled = true
            val saveBitmap = Bitmap.createBitmap(screen_shot_area.drawingCache)  // Bitmap生成
            saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
            output.close()

            screen_shot_area.isDrawingCacheEnabled = false

        }catch (e: IOException){
            Log.d("スクリーンショットでエラーが起きました",e.toString())
        }

        val values = ContentValues()
        val contentResolver = contentResolver
        values.put(Images.Media.MIME_TYPE, "image/jpeg")
        values.put(Images.Media.TITLE, filename)
        values.put("_data", "$path/$filename")
        contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values)

        Toast.makeText(this, "写真を保存しました", Toast.LENGTH_SHORT).show()
    }

    private fun permisson() {
        if (ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            generateScreenShot()
        }
    }

}

