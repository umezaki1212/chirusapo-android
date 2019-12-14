package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListItem
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_check_growth.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.button_post_add
import kotlinx.android.synthetic.main.fragment_home.no_coment
import kotlinx.android.synthetic.main.fragment_home.root_view
import kotlinx.android.synthetic.main.fragment_home.time_line_start
import kotlinx.android.synthetic.main.fragment_home.timeline
import jp.ac.asojuku.st.chirusapo.MainActivity as MainActivity1

class CheckGrowthActivity : AppCompatActivity() {
    private lateinit var userToken: String
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_growth)

        mSwipeRefreshLayout = this.findViewById(R.id.swipe_refresh_layout_child)
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        val childId = intent.getStringExtra("list")


        val account = realm.where<Account>().findFirst()
        if (account == null) {
            Toast.makeText(this, "アカウント情報が取得できません", Toast.LENGTH_SHORT).show()
        } else {
            userToken = account.Rtoken
        }

        setHomeList(root_view_child)
    }

    fun onRefresh() {
        setHomeList(root_view_child)
    }

    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        setHomeList(root_view_child)
    }


    private fun setHomeList(view: View) {
        //Tokenの取得
        val account: Account? = realm.where<Account>().findFirst()
        //Tokenが存在するか？
        if (account == null) {
            // 新規登録orログインが行われていないのでSignInActivityに遷移
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
        }else {
            val token = account.Rtoken
            //現在見ているグループIDの取得
            val test = 1
            val group:JoinGroup? =
                realm.where<JoinGroup>().equalTo("Rgroup_flag", test).findFirst()
            //存在しなかった(グループに参加を促すようにする
            if (group == null) {
                Toast.makeText(this, "グループ情報が取得できません", Toast.LENGTH_SHORT).show()
            } else {
                no_coment_child.visibility = View.INVISIBLE
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
                                    no_coment_child.visibility = View.VISIBLE
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
                                    postTimelineListItem.postTime = item.getString("post_time")

                                    list.add(postTimelineListItem)
                                }
                                val listView = timeline_child
                                val postTimelineListAdapter = PostTimelineListAdapter(this)
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
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //値が不足している場合
                                        ApiError.REQUIRED_PARAM -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //トークンの検証失敗
                                        ApiError.UNKNOWN_TOKEN -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //所属グループなし
                                        ApiError.UNREADY_BELONG_GROUP -> {
                                            ApiError.showToast(
                                                this,
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
