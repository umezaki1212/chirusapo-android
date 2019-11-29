package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListItem
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
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

        setHomeList(root_view)
    }

    lateinit var realm: Realm

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



            } else {
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
//            mSwipeRefreshLayout.isRefreshing = false
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