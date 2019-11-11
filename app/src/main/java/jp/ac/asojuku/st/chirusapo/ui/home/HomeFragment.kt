package jp.ac.asojuku.st.chirusapo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import jp.ac.asojuku.st.chirusapo.R
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListItem
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel.text.observe(this, Observer {
        })
        return root
    }

    override fun onResume() {
        super.onResume()

        setHomeList(root_view)
    }

    private fun setHomeList(view: View) {
        ApiGetTask {
            if (it == null) {
                Snackbar.make(view, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
            } else {
                when (it.getString("status")) {
                    "200" -> {
                        val timelineData = it.getJSONObject("data").getJSONArray("timeline_data")
                        val list = ArrayList<PostTimelineListItem>()
                        for (i in 0 until timelineData.length()) {
                            val postTimelineListItem = PostTimelineListItem()
                            val item = timelineData.getJSONObject(i)
                            postTimelineListItem.id = i.toLong()
                            postTimelineListItem.userId = item.getString("user_id")
                            postTimelineListItem.userName = item.getString("user_name")
                            postTimelineListItem.userIcon = if (item.isNull("user_icon")) null else item.getString("user_icon")
                            postTimelineListItem.contentType = item.getString("content_type")
                            when (postTimelineListItem.contentType) {
                                "text" -> {
                                    postTimelineListItem.text = item.getString("text")
                                }
                                "text_image" -> {
                                    postTimelineListItem.text = item.getString("text")
                                    postTimelineListItem.image01 = item.getString("image01")
                                    postTimelineListItem.image02 = if (item.isNull("image02")) null else item.getString("image02")
                                    postTimelineListItem.image03 = if (item.isNull("image03")) null else item.getString("image03")
                                    postTimelineListItem.image04 = if (item.isNull("image04")) null else item.getString("image04")
                                }
                                "text_movie" -> {
                                    postTimelineListItem.text = item.getString("text")
                                    postTimelineListItem.movie01Thumbnail = item.getString("movie01_thumbnail")
                                    postTimelineListItem.movie01Content = item.getString("movie01_content")
                                }
                                "image" -> {
                                    postTimelineListItem.image01 = item.getString("image01")
                                    postTimelineListItem.image02 = if (item.isNull("image02")) null else item.getString("image02")
                                    postTimelineListItem.image03 = if (item.isNull("image03")) null else item.getString("image03")
                                    postTimelineListItem.image04 = if (item.isNull("image04")) null else item.getString("image04")
                                }
                                "movie" -> {
                                    postTimelineListItem.movie01Thumbnail = item.getString("movie01_thumbnail")
                                    postTimelineListItem.movie01Content = item.getString("movie01_content")
                                }
                            }
                            postTimelineListItem.postTime = item.getString("post_time")

                            list.add(postTimelineListItem)
                        }
                        val listView = timeline
                        val postTimelineListAdapter = PostTimelineListAdapter(activity!!)
                        postTimelineListAdapter.setPostTimelineList(list)
                        postTimelineListAdapter.notifyDataSetChanged()
                        listView.adapter = postTimelineListAdapter
                        listView.setOnItemClickListener { adapterView, _, i, _ ->
                            val item = adapterView.getItemAtPosition(i) as PostTimelineListItem
                            /*
                            val intent = Intent(activity!!, MainPostDetailActivity::class.java).apply {
                                putExtra("post_id", item.postId)
                            }
                            startActivity(intent)
                            */
                        }
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
                                //
                            }
                        }
                    }

                    else -> Snackbar.make(view, "不明なエラーが発生しました", Snackbar.LENGTH_SHORT).show()
                }
            }
//            mSwipeRefreshLayout.isRefreshing = false
        }.execute(ApiParam(Api.SLIM + "timeline/get", hashMapOf("token" to "Uel1KebSmQRH2I6yqoro1JgVLkskDi", "group_id" to "raigekka")))
    }
}