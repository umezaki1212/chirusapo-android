package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialView
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiError.Companion.showToast
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.fragment_child.*

class ChildFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    lateinit var realm: Realm
    private lateinit var userToken: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_child, container, false)

//        val speedDialView = view.findViewById<SpeedDialView>(R.id.speedDialChild)
//        speedDialView.inflate(R.menu.menu_speed_dial)
//
//        speedDialView.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
//            when (actionItem.id) {
//                R.id.action_add_body -> {
//                    val intent = Intent(activity!!, RegistrationWeightHeightActivity::class.java)
//                    startActivity(intent)
//                    speedDialView.close() // To close the Speed Dial with animation
//                    return@OnActionSelectedListener true // false will close it without animation
//                }
//                R.id.action_add_image -> {
//                    val intent = Intent(activity!!, ChildTimeLinePostAdd::class.java)
//                    startActivity(intent)
//                    speedDialView.close() // To close the Speed Dial with animation
//                    return@OnActionSelectedListener true // false will close it without animation
//                }
//                R.id.action_update_body -> {
//                    showToast(activity!!,"No label action clicked!\nClosing with animation",Toast.LENGTH_SHORT)
//                    speedDialView.close() // To close the Speed Dial with animation
//                    return@OnActionSelectedListener true // false will close it without animation
//                }
//                R.id.action_add_user -> {
//                    showToast(activity!!,"No label action clicked!\nClosing with animation",Toast.LENGTH_SHORT)
//                    speedDialView.close() // To close the Speed Dial with animation
//                    return@OnActionSelectedListener true // false will close it without animation
//                }
//            }
//            false
//        })

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

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()

        val account = realm.where<Account>().findFirst()

        //Tokenが存在するか？
        if (account == null) {
            // 新規登録orログインが行われていないのでSignInActivityに遷移
            val intent = Intent(activity!!, SignInActivity::class.java)
            startActivity(intent)
        }else {
            userToken = account.Rtoken
            //現在見ているグループIDの取得
            val test = 1
            val group:JoinGroup? =
                realm.where<JoinGroup>().equalTo("Rgroup_flag", test).findFirst()
            //存在しなかった(グループに参加を促すようにする
            if(group == null){
                Toast.makeText(activity, "グループ情報が取得できません", Toast.LENGTH_SHORT).show()
            }
            else{
                val groupId = group.Rgroup_id
                ApiGetTask {
                    Log.d("TEST", it.toString())
                    if (it == null) {
                        Snackbar.make(view!!, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
                    } else {
                        when (it.getString("status")) {
                            "200" -> {

                                val childData = it.getJSONObject("data")
//                                val array = arrayListOf<String>()
//
//                                for (i in 0 until childData.getJSONArray("child_list").length()) {
//                                    array.add(
//                                        childData.getJSONArray("child_list").getJSONObject(i).getString(
//                                            "user_id"
//                                        )
//                                    )
//                                }

//                                Log.d("TEST", array.toString())

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
                                        // array[position]
                                    }

//                                    val fragmentTitle = arrayListOf<String>("test", "test", "test", "test", "test")
//                                    val fragmentList = arrayListOf<Fragment>(
//                                        ChildDataSetFragment.newInstance("test"),
//                                        ChildDataSetFragment.newInstance("test"),
//                                        ChildDataSetFragment.newInstance("test"),
//                                        ChildDataSetFragment.newInstance("test"),
//                                        ChildDataSetFragment.newInstance("test")
//                                    )

//                                        arrayListOf<Fragment>().apply {
//                                        (0 until childData.length()).forEach { index ->
//                                            this.add(ChildDataSetFragment.newInstance(childData.getJSONArray("child_list").getJSONObject(index).getString("user_id")))
//                                        }
//                                    }


                                    val fragmentTitle = arrayListOf<String>().apply {
                                        (0 until childData.getJSONArray("child_list").length()).forEach {index ->
                                            this.add(childData.getJSONArray("child_list").getJSONObject(index).getString("user_name"))
                                        }
                                    }

//                                    private fun test(){
//                                        for (i in 0 until array.size){
//                                            fragmentList.add(ChildDataSetFragment.newInstance(""))
//                                        }
//                                    }

                                    val fragmentList = arrayListOf<Fragment>().apply {
                                        (0 until childData.getJSONArray("child_list").length()).forEach { index ->
                                            this.add(ChildDataSetFragment.newInstance(childData.getJSONArray("child_list").getJSONObject(index).getString("user_id")))
                                        }
                                    }
//                                    (
//                                        ChildDataSetFragment.newInstance("")
//                                    )
                                }
                                tab_layout_child.setupWithViewPager(view_pager_child)
                            }
                            "400" -> {
                                //messageからエラー文を配列で取得し格納する
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        //グループ情報なし
                                        ApiError.UNKNOWN_GROUP -> {
                                            showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //値が不足している場合
                                        ApiError.REQUIRED_PARAM -> {
                                            showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //トークンの検証失敗
                                        ApiError.UNKNOWN_TOKEN -> {
                                            showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        //所属グループなし
                                        ApiError.UNREADY_BELONG_GROUP -> {
                                            showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
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
        }
    }


}