package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListItem
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(),SwipeRefreshLayout.OnRefreshListener {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var userToken: String
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var realm: Realm

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        mSwipeRefreshLayout = view!!.findViewById(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        return view
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

    interface OnFragmentInteractionListener

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()


        val account = realm.where<Account>().findFirst()
        if (account == null) {
            Toast.makeText(activity, "アカウント情報が取得できません", Toast.LENGTH_SHORT).show()
        } else {
            userToken = account.Rtoken
        }

        val mainActivity = activity
        if (mainActivity is MainActivity) {
            time_line_group_create.setOnClickListener {
                mainActivity.groupCreate()
            }

            time_line_group_participation.setOnClickListener {
                mainActivity.groupJoin()
            }
        }

        setHomeList(root_view)

        button_post_add.setOnClickListener {
            val intent = Intent(activity,MainPostAddActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onRefresh() {
        setHomeList(root_view)
    }

    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        setHomeList(root_view)
    }


    private fun setHomeList(view: View) {
        //Tokenの取得
        val account: Account? = realm.where<Account>().findFirst()
        //Tokenが存在するか？
        if (account == null) {
            // 新規登録orログインが行われていないのでSignInActivityに遷移
            val intent = Intent(activity!!,SignInActivity::class.java)
            startActivity(intent)
        }else {
            val token = account.Rtoken
            //現在見ているグループIDの取得
            val test = 1
            val group:JoinGroup? =
                realm.where<JoinGroup>().equalTo("Rgroup_flag", test).findFirst()
            //存在しなかった(グループに参加を促すようにする
            if (group == null) {
                Toast.makeText(activity, "グループ情報が取得できません", Toast.LENGTH_SHORT).show()
                root_view.visibility = View.INVISIBLE
                button_post_add.visibility = View.INVISIBLE
                no_coment.visibility = View.INVISIBLE
            } else {
                time_line_start.visibility = View.INVISIBLE
                no_coment.visibility = View.INVISIBLE
                val groupId = group.Rgroup_id
                ApiGetTask {
                    if (it == null) {
                        Snackbar.make(view, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
                    } else {
                        when (it.getString("status")) {
                            "200" -> {
                                val timelineData =
                                    it.getJSONObject("data").getJSONArray("timeline_data")
                                val list = ArrayList<PostTimelineListItem>()
                                if (timelineData.length() == 0){
                                    no_coment.visibility = View.VISIBLE
                                }
                                for (i in 0 until timelineData.length()) {
                                    val postTimelineListItem = PostTimelineListItem()
                                    val item = timelineData.getJSONObject(i)
                                    postTimelineListItem.id = i.toLong()
                                    postTimelineListItem.userId = item.getString("user_id")
                                    postTimelineListItem.userName = item.getString("user_name")
                                    postTimelineListItem.userIcon =
                                        if (item.isNull("user_icon")) null else item.getString("user_icon")
                                    postTimelineListItem.contentType =
                                        item.getString("content_type")
                                    when (postTimelineListItem.contentType) {
                                        "text" -> {
                                            postTimelineListItem.text = item.getString("text")
                                        }
                                        "text_image" -> {
                                            postTimelineListItem.text = item.getString("text")
                                            postTimelineListItem.image01 = item.getString("image01")
                                            postTimelineListItem.image02 =
                                                if (item.isNull("image02")) null else item.getString(
                                                    "image02"
                                                )
                                            postTimelineListItem.image03 =
                                                if (item.isNull("image03")) null else item.getString(
                                                    "image03"
                                                )
                                            postTimelineListItem.image04 =
                                                if (item.isNull("image04")) null else item.getString(
                                                    "image04"
                                                )
                                        }
                                        "text_movie" -> {
                                            postTimelineListItem.text = item.getString("text")
                                            postTimelineListItem.movie01Thumbnail =
                                                item.getString("movie01_thumbnail")
                                            postTimelineListItem.movie01Content =
                                                item.getString("movie01_content")
                                        }
                                        "image" -> {
                                            postTimelineListItem.image01 = item.getString("image01")
                                            postTimelineListItem.image02 =
                                                if (item.isNull("image02")) null else item.getString(
                                                    "image02"
                                                )
                                            postTimelineListItem.image03 =
                                                if (item.isNull("image03")) null else item.getString(
                                                    "image03"
                                                )
                                            postTimelineListItem.image04 =
                                                if (item.isNull("image04")) null else item.getString(
                                                    "image04"
                                                )
                                        }
                                        "movie" -> {
                                            postTimelineListItem.movie01Thumbnail =
                                                item.getString("movie01_thumbnail")
                                            postTimelineListItem.movie01Content =
                                                item.getString("movie01_content")
                                        }
                                    }
                                    val nen = item.getString("post_time").substring(0, 4)
                                    val man = item.getString("post_time").substring(5, 7)
                                    val day = item.getString("post_time").substring(8, 10)
                                    val hourEg = item.getString("post_time").substring(11, 13).toInt() + 7
                                    var hourJp = hourEg.toString()
                                    if (hourEg <10){
                                        hourJp = "0$hourJp"
                                    }
                                    val second = item.getString("post_time").substring(14, 16)
                                    postTimelineListItem.postTime = "$nen/$man/$day/ $hourJp:$second"

                                    list.add(postTimelineListItem)
                                }
                                val listView = timeline
                                val postTimelineListAdapter = PostTimelineListAdapter(activity!!)
                                postTimelineListAdapter.setPostTimelineList(list)
                                postTimelineListAdapter.notifyDataSetChanged()
                                listView.adapter = postTimelineListAdapter
                                /*
                                listView.setOnItemClickListener { adapterView, _, i, _ ->
                               val item =
                                   adapterView.getItemAtPosition(i) as PostTimelineListItem
                                    Toast.makeText(activity, item.text, Toast.LENGTH_SHORT).show()
                                }
                                */
                            }
                            "400" -> {
                                //messageからエラー文を配列で取得し格納する
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        //グループ情報なし
                                        ApiError.UNKNOWN_GROUP -> {
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //値が不足している場合
                                        ApiError.REQUIRED_PARAM -> {
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //トークンの検証失敗
                                        ApiError.UNKNOWN_TOKEN -> {
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //所属グループなし
                                        ApiError.UNREADY_BELONG_GROUP -> {
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
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
                    mSwipeRefreshLayout.isRefreshing = false
                }.execute(
                    ApiParam(
                        Api.SLIM + "timeline/get",
                        hashMapOf("token" to token, "group_id" to groupId)
                    )
                )
            }
        }
    }
}