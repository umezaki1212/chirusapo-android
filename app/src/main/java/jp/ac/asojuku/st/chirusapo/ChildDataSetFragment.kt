package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialView
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataAdapter
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListItem
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListSub
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataSubAdapter
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiError.Companion.showToast
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.fragment_child_data_set.*
import org.json.JSONObject

class ChildDataSetFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var childId : String
    private var counter : Int = 0
    private lateinit var childData: JSONObject
    private lateinit var bodyHeight:String
    private lateinit var bodyWeight:String
    private lateinit var clothesSize:String
    private lateinit var shoesSize:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            childId= it.getString("childData")!!
        }
        childId = childData.getString("user_id")
        bodyHeight =  childData.getString("body_height")
        bodyWeight =  childData.getString("body_weight")
        clothesSize =  childData.getString("clothes_size")
        shoesSize =  childData.getString("shoes_size")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_child_data_set, container, false)

        val speedDialView = view!!.findViewById<SpeedDialView>(R.id.speedDialChild)
        speedDialView.inflate(R.menu.menu_speed_dial)

        speedDialView.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                //子供情報更新画面
                R.id.action_add_body -> {
                    val intent = Intent(activity!!, RegistrationWeightHeightActivity::class.java)
                    intent.putExtra("user_id", childId)
                    intent.putExtra("bodyWeight", bodyWeight)
                    intent.putExtra("bodyHeight", bodyHeight)
                    intent.putExtra("clothesSize", clothesSize)
                    intent.putExtra("shoesSize", shoesSize)
                    startActivity(intent)
                    speedDialView.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
                //子供成長投稿画面
                R.id.action_add_image -> {
                    val intent = Intent(activity!!, ChildTimeLinePostAdd::class.java)
                    intent.putExtra("user_id", childId)
                    startActivity(intent)
                    speedDialView.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
                //子成情報変更画面
                R.id.action_update_body -> {
                    showToast(activity!!,"No label action clicked!\nClosing with animation",Toast.LENGTH_SHORT)
                    speedDialView.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
                //子供情報新規登録
                R.id.action_add_user -> {
//                    showToast(activity!!,"No label action clicked!\nClosing with animation",Toast.LENGTH_SHORT)
                    val intent = Intent(activity!!, RegistrationChildActivity::class.java)
                    startActivity(intent)
                    speedDialView.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
            }
            false
        })

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
                    if (it == null) {
                        Snackbar.make(view, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
                    } else {
                        when (it.getString("status")) {
                            "200" -> {

                                val childData =
                                    it.getJSONObject("data").getJSONArray("child_list")
                                val list = arrayListOf<ChildDataListItem>()
                                var customListView = ChildDataListItem()

                                for (y in 0 until childData.length()){
                                    if (childData.getJSONObject(y).getString("user_id") == childId){
                                        counter = y
                                        break
                                    }
                                }

                                var item = childData.getJSONObject(counter)

                                childId = item.getString("user_id")

                                customListView.dataTitle = "名前"
                                customListView.dataMain = item.getString("user_name")
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                item = childData.getJSONObject(counter)
                                customListView.dataTitle = "誕生日"
                                val nen = item.getString("birthday").substring(0, 4)
                                val man = item.getString("birthday").substring(5, 7)
                                val day = item.getString("birthday").substring(8, 10)
                                customListView.dataMain = nen + "年" + man + "月" + day + "日"
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                customListView.dataTitle = "年齢"
                                customListView.dataMain = item.getString("age") + "歳"
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                item = childData.getJSONObject(counter)
                                customListView.dataTitle = "性別"
                                val sex: String
                                sex = when (item.getString("gender")) {
                                    "1" -> "男の子"
                                    "2" -> "女の子"
                                    else -> "未記入"
                                }
                                customListView.dataMain = sex
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                item = childData.getJSONObject(counter)
                                customListView.dataTitle = "血液型"
                                val blood: String
                                blood = when (item.getString("blood_type")) {
                                    "1" -> "A型"
                                    "2" -> "B型"
                                    "3" -> "O型"
                                    "4" -> "AB型"
                                    else -> "未記入"
                                }
                                customListView.dataMain = blood
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                item = childData.getJSONObject(counter)
                                customListView.dataTitle = "身長"
                                customListView.dataMain = item.getString("body_height") + "cm"
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                item = childData.getJSONObject(counter)
                                customListView.dataTitle = "体重"
                                customListView.dataMain = item.getString("body_weight") + "kg"
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                item = childData.getJSONObject(counter)
                                customListView.dataTitle = "服のサイズ"
                                customListView.dataMain = item.getString("clothes_size") + "cm"
                                list.add(customListView)

                                customListView = ChildDataListItem()
                                item = childData.getJSONObject(counter)
                                customListView.dataTitle = "靴のサイズ"
                                customListView.dataMain = item.getString("shoes_size") + "cm"
                                list.add(customListView)

                                val listView = child_main_data_list
                                val childDataAdapter = ChildDataAdapter(activity!!)
                                childDataAdapter.setChildDataAdapter(list)
                                childDataAdapter.notifyDataSetChanged()
                                listView.adapter = childDataAdapter

                                setListViewHeightBasedOnChildren(child_main_data_list)

                                val listSub = ArrayList<ChildDataListItem>()
                                arrayListOf<String>().apply {
                                    (0 until item.getJSONArray("vaccination").length()).forEach { i ->
                                        val childDataListSub = ChildDataListItem()
                                        val vaccinationItem =
                                            item.getJSONArray("vaccination").getJSONObject(i)
                                        childDataListSub.id = i.toLong()
                                        childDataListSub.dataTitle =
                                            vaccinationItem.getString("vaccine_name")
                                        val visitData = vaccinationItem.getString("visit_date")
                                        val visitNen = visitData.substring(0, 4)
                                        val visitMan = visitData.substring(5, 7)
                                        val visitDay = visitData.substring(8, 10)
                                        childDataListSub.dataMain =
                                            visitNen + "年" + visitMan + "月" + visitDay + "日"
                                        listSub.add(childDataListSub)
                                    }
                                    if (item.getJSONArray("vaccination").length() == 0) {
                                        val childDataListSub = ChildDataListItem()
                                        childDataListSub.id = counter.toLong()
                                        childDataListSub.dataTitle = "登録なし"
                                        listSub.add(childDataListSub)
                                    }
                                }
                                val listSubViewAllergy = child_vaccination_list
                                val childDataAllergyAdapter = ChildDataAdapter(activity!!)
                                childDataAllergyAdapter.setChildDataAdapter(listSub)
                                listSubViewAllergy.adapter = childDataAllergyAdapter

                                setListViewHeightBasedOnChildren(child_vaccination_list)

                                val listAllergy = ArrayList<ChildDataListSub>()
                                arrayListOf<String>().apply {
                                    (0 until item.getJSONArray("allergy").length()).forEach { i ->
                                        val childDataListAllergy = ChildDataListSub()
                                        val vaccinationItem =
                                            item.getJSONArray("allergy").getJSONObject(i)
                                        childDataListAllergy.id = i.toLong()
                                        childDataListAllergy.dataTitle =
                                            vaccinationItem.getString("allergy_name")
                                        // vaccinationItem.getString("vaccine_name")
                                        listAllergy.add(childDataListAllergy)
                                    }
                                    if (item.getJSONArray("allergy").length() == 0) {
                                        val childDataListAllergy = ChildDataListSub()
                                        childDataListAllergy.id = counter.toLong()
                                        childDataListAllergy.dataTitle = "登録なし"
                                        listAllergy.add(childDataListAllergy)
                                    }
                                }

                                val listSubView = child_list_allergy
                                val childDataSubAdapter = ChildDataSubAdapter(activity!!)
                                childDataSubAdapter.setChildDataSubAdapter(listAllergy)
                                listSubView.adapter = childDataSubAdapter

                                setListViewHeightBasedOnChildren(child_list_allergy)

                                val listViewRecord =
                                    activity!!.findViewById<ListView>(R.id.child_list_record)
                                val dataArray = arrayOf("今までの成長", "グラフの表示", "友達リスト")
                                val adapter = ArrayAdapter(
                                    activity!!,
                                    android.R.layout.simple_list_item_1,
                                    dataArray
                                )
                                listViewRecord.adapter = adapter

                                listViewRecord.setOnItemClickListener { adapterView, _, position, _ ->
                                    when (adapterView.getItemAtPosition(position) as String) {
                                        "今までの成長" -> {
                                            val intent =
                                                Intent(activity!!, CheckGrowthActivity::class.java)
                                            intent.putExtra("user_id",childId)
                                            Log.d("TEST", childId)
                                            startActivity(intent)
                                        }
                                        "グラフの表示" -> {
                                            val intent =
                                                Intent(activity!!, ChildGraphActivity::class.java)
                                            intent.putExtra("user_id",childId)
                                            startActivity(intent)
                                        }
                                        "友達リスト" -> {
                                            val intent =
                                                Intent(activity!!, ListofFriendActivity::class.java)
                                            intent.putExtra("user_id",childId)
                                            Log.d("TEST", childId)
                                            startActivity(intent)
                                        }
                                    }
                                }

                                setListViewHeightBasedOnChildren(child_list_record)
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
                                view,
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

        private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return
        var totalHeight = listView.paddingTop + listView.paddingBottom
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            if (listItem is ViewGroup) {
                listItem.setLayoutParams(
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
        listView.layoutParams = params
    }

    companion object {
        @JvmStatic
        fun newInstance(obj: JSONObject) =
            ChildDataSetFragment().apply {
                childData = obj
            }
        /*
        fun newInstance(list: String) =
            ChildDataSetFragment().apply {
                arguments = Bundle().apply {
                    putString("childData", list)
                }
            }

         */
    }

}
