package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.activity_child_face.*

class ChildFaceActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener{

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var childId:String

    companion object {
        const val PHOTO_SELECT_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_face)

        mSwipeRefreshLayout = this.findViewById(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        supportActionBar?.let {
            title = "子供顔情報"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        getAlbum()
    }

    override fun onRefresh() {
        getAlbum()
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        childId = intent.getStringExtra("user_id")!!

        if (account != null) {
            userToken = account.Rtoken
        }

        button_add_face.setOnClickListener {
            val intent = Intent(this, ChildFaceTrimmingActivity::class.java).apply {
                putExtra("childId", childId)
            }
            startActivity(intent)
        }

        button_add_face_fab.setOnClickListener {
            val intent = Intent(this, ChildFaceTrimmingActivity::class.java).apply {
                putExtra("childId", childId)
            }
            startActivity(intent)
        }

        getAlbum()
    }

    private fun getAlbum() {
        ApiGetTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showSnackBar(root_layout, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val albumJsonArray =
                            jsonObject.getJSONObject("data").getJSONArray("face_list")

                        val layoutNotFindImage =
                            this.findViewById<LinearLayout>(R.id.not_find_image)
                        val layoutGridView =
                            this.findViewById<GridView>(R.id.grid_view)

                        if (albumJsonArray.length() == 0) {
                            layoutNotFindImage.visibility = View.VISIBLE
                            layoutGridView.visibility = View.INVISIBLE
                        } else {
                            layoutNotFindImage.visibility = View.INVISIBLE
                            layoutGridView.visibility = View.VISIBLE

                            val albumArrayList = arrayListOf<String>().apply {
                                (0 until albumJsonArray.length()).forEach {
                                    this.add(albumJsonArray.getJSONObject(it).getString("file_name"))
                                }
                            }

                            setAlbum(albumArrayList)
                        }

                        mSwipeRefreshLayout.isRefreshing = false
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
                                    ApiError.showSnackBar(
                                        root_layout,
                                        errorArray.getString(i),
                                        Snackbar.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "child/face/get",
                hashMapOf("token" to userToken, "child_id" to childId)
            )
        )
    }

    private fun setAlbum(list: ArrayList<String>) {
        class GridAdapter : BaseAdapter() {

            override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
                val imageView = ImageView(this@ChildFaceActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(500, 500)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(10, 10, 10, 10)
                    // ImageViewer
                    setOnClickListener {
                        StfalconImageViewer.Builder(this@ChildFaceActivity, mutableListOf(list[i])) { view, image ->
                            Picasso.get().load(image).into(view)
                        }.apply {
                            withHiddenStatusBar(false)
                            show()
                        }
                    }
                    setOnLongClickListener {
                        val message =
                            "この画像を削除しますか？"
                        android.app.AlertDialog.Builder(this@ChildFaceActivity).apply {
                            setTitle("画像削除")
                            setMessage(message)
                            setPositiveButton("削除"){ _, _ ->
                                deleteImage(list[i])
                            }
                            setNegativeButton("キャンセル", null)
                            create()
                            show()
                        }
                        return@setOnLongClickListener true
                    }
                }

                Picasso.get().load(list[i]).into(imageView)

                return imageView
            }

            override fun getItem(i: Int): Any {
                return list[i]
            }

            override fun getItemId(i: Int): Long {
                return i.toLong()
            }

            override fun getCount(): Int {
                return list.size
            }

        }

        this.findViewById<GridView>(R.id.grid_view).apply {
            adapter = GridAdapter()
        }
    }

    private fun deleteImage(file:String) {
        val item = file.split("/")
        val count = item.size -1
        val param = hashMapOf(
            "token" to userToken,
            "file_name" to item[count]
        )
        ApiPostTask{jsonObject ->
            if (jsonObject == null) {
                ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        Toast.makeText(this, "画像を削除しました", Toast.LENGTH_SHORT).show()
                        getAlbum()
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent = Intent(this, SignInActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }
                                else -> {
                                    ApiError.showToast(
                                        this,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.execute(ApiParam(Api.SLIM + "child/face/delete", param))
    }

}
