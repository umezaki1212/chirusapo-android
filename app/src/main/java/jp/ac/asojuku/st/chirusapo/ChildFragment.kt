package jp.ac.asojuku.st.chirusapo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.*
import jp.ac.asojuku.st.chirusapo.apis.ApiError.Companion.showToast
import kotlinx.android.synthetic.main.fragment_child.*

class ChildFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_child, container, false)

        realm = Realm.getDefaultInstance()

        val account = realm.where<Account>().findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()

        if (account != null && group != null) {
            userToken = account.Rtoken
            groupId = group.Rgroup_id

            getChild()
        } else {
            Toast.makeText(activity, "ユーザー情報を取得できませんでした", Toast.LENGTH_SHORT).show()
        }

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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun getChild() {
        ApiGetTask {
            if (it == null) {
                Snackbar.make(view!!, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
            } else {
                when (it.getString("status")) {
                    "200" -> {
                        val childData = it.getJSONObject("data")
                        val childList = childData.getJSONArray("child_list")

                        view_pager_child.adapter = object :
                            FragmentPagerAdapter(
                                activity!!.supportFragmentManager,
                                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
                            ) {
                            override fun getItem(position: Int): Fragment {
                                return fragmentList[position]
                            }

                            override fun getCount(): Int {
                                return fragmentList.size
                            }

                            override fun getPageTitle(position: Int): CharSequence? {
                                return fragmentTitle[position]
                            }

                            val fragmentTitle = arrayListOf<String>().apply {
                                (0 until childList.length()).forEach { index ->
                                    this.add(childList.getJSONObject(index).getString("user_name"))
                                }
                            }

                            val fragmentList = arrayListOf<Fragment>().apply {
                                (0 until childList.length()).forEach { index ->
                                    this.add(ChildDataSetFragment.newInstance(childList.getJSONObject(index)))
                                }
                            }
                        }
                        tab_layout_child.setupWithViewPager(view_pager_child)

                        val tabs = tab_layout_child.getChildAt(0) as ViewGroup
                        for (i in 0 until tabs.childCount) {
                            val childInfo = childList.getJSONObject(i)

                            tabs.getChildAt(i).setOnLongClickListener {
                                val message =
                                            "子ども情報を削除しますか？\n" +
                                            childInfo.getString("user_name") + " [" + childInfo.getString("user_id") + "]\n" +
                                            "の情報が削除されます"
                                AlertDialog.Builder(activity).apply {
                                    setTitle("子ども情報削除")
                                    setMessage(message)
                                    setPositiveButton("削除"){ _, _ ->
                                        deleteChild(childInfo.getString("user_id"))
                                    }
                                    setNegativeButton("キャンセル", null)
                                    create()
                                    show()
                                }
                                return@setOnLongClickListener false
                            }
                        }
                    }
                    "400" -> {
                        // messageからエラー文を配列で取得し格納する
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent = Intent(activity, SignInActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }
                                else -> {
                                    showToast(activity!!, errorArray.getString(i), Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    }
                    else -> Snackbar.make(
                        view!!,
                        "不明なエラーが発生しました",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "/child/list",
                hashMapOf("token" to userToken, "group_id" to groupId)
            )
        )
    }

    private fun deleteChild(childId:String) {
        val param = hashMapOf(
            "token" to userToken,
            "child_id" to childId
        )
        ApiPostTask{jsonObject ->
            if (jsonObject == null) {
                showToast(activity!!, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        Toast.makeText(activity, "子ども情報を削除しました", Toast.LENGTH_SHORT).show()
                        getChild()
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent = Intent(activity, SignInActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }
                                else -> {
                                    showToast(activity!!, errorArray.getString(i), Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(ApiParam(Api.SLIM + "/child/delete", param))
    }
}