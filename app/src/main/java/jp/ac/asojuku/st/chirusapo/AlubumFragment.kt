package jp.ac.asojuku.st.chirusapo

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.fragment_album.*
import java.io.IOException

class AlbumFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mRootView: FrameLayout
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String

    companion object {
        const val PHOTO_SELECT_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_album, container, false)

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        mRootView = view.findViewById(R.id.root_view)

        return view
    }

    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        getAlbum()
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()

        if (account != null && group != null) {
            userToken = account.Rtoken
            groupId = group.Rgroup_id
        }

        button_upload.setOnClickListener {
            onUploadPhotoPicker()
        }

        button_upload_fab.setOnClickListener {
            onUploadPhotoPicker()
        }
    }


    override fun onRefresh() {
        getAlbum()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
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
        val parcelFileDescriptor = activity!!.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (resultCode == RESULT_OK) {
            if (resultData != null) {
                when (requestCode) {
                    PHOTO_SELECT_CODE -> {
                        try {
                            val uri = resultData.data as Uri
                            val bitmap = getBitmapFromUri(uri)
                            onPhotoUpload(bitmap)
                        } catch (e: Exception) {
                            Snackbar
                                .make(mRootView, "選択された画像を取得できませんでした", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun onPhotoUpload(bitmap: Bitmap) {
        Snackbar.make(mRootView, "画像をアップロードしています…", Snackbar.LENGTH_SHORT).show()

        ApiMediaPostTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showSnackBar(mRootView, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val albumJsonArray =
                            jsonObject.getJSONObject("data").getJSONArray("album_data")

                        val layoutNotFindImage =
                            activity!!.findViewById<LinearLayout>(R.id.not_find_image)
                        val layoutGridView =
                            activity!!.findViewById<GridView>(R.id.grid_view)

                        if (albumJsonArray.length() == 0) {
                            layoutNotFindImage.visibility = View.VISIBLE
                            layoutGridView.visibility = View.INVISIBLE
                        } else {
                            layoutNotFindImage.visibility = View.INVISIBLE
                            layoutGridView.visibility = View.VISIBLE

                            val albumArrayList = arrayListOf<String>().apply {
                                (0 until albumJsonArray.length()).forEach {
                                    this.add(albumJsonArray.getString(it))
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
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP,
                                ApiError.UPLOAD_FAILED,
                                ApiError.ALLOW_EXTENSION -> {
                                    ApiError.showSnackBar(
                                        mRootView,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent =
                                        Intent(activity, SignInActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "album/upload",
                hashMapOf("token" to userToken, "group_id" to groupId),
                arrayListOf(
                    ApiParamImage("image/jpeg", "image.jpeg", "photo", bitmap)
                )
            )
        )
    }

    private fun getAlbum() {
        ApiGetTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showSnackBar(mRootView, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val albumJsonArray =
                            jsonObject.getJSONObject("data").getJSONArray("album_data")

                        val layoutNotFindImage =
                            activity!!.findViewById<LinearLayout>(R.id.not_find_image)
                        val layoutGridView =
                            activity!!.findViewById<GridView>(R.id.grid_view)

                        if (albumJsonArray.length() == 0) {
                            layoutNotFindImage.visibility = View.VISIBLE
                            layoutGridView.visibility = View.INVISIBLE
                        } else {
                            layoutNotFindImage.visibility = View.INVISIBLE
                            layoutGridView.visibility = View.VISIBLE

                            val albumArrayList = arrayListOf<String>().apply {
                                (0 until albumJsonArray.length()).forEach {
                                    this.add(albumJsonArray.getString(it))
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
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showSnackBar(
                                        mRootView,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent =
                                        Intent(activity, SignInActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "album/get",
                hashMapOf("token" to userToken, "group_id" to groupId)
            )
        )
    }

    private fun setAlbum(list: ArrayList<String>) {
        class GridAdapter(private val context: Context) : BaseAdapter() {

            override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
                val imageView = ImageView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(500, 500)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(10, 10, 10, 10)
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

        val gridView = activity!!.findViewById<GridView>(R.id.grid_view)
        gridView.adapter = GridAdapter(activity!!)
    }
}
