package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataAdapter
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListItem
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataListSub
import jp.ac.asojuku.st.chirusapo.adapters.ChildDataSubAdapter
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.layout_child_item.*

class ChildFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    lateinit var realm: Realm
    private lateinit var userToken: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_child, container, false)
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
            }else{
                val groupId = group.Rgroup_id
                ApiGetTask {
                    if (it == null) {
                        Snackbar.make(view!!, "APIとの通信に失敗しました", Snackbar.LENGTH_SHORT).show()
                    } else when (it.getString("status")) {
                        "200" -> {
                            val childData =
                                it.getJSONObject("data").getJSONArray("child_list")
                            val list = ArrayList<ChildDataListItem>()
                            var customListView = ChildDataListItem()
                            //書き換え
                            val i = 4
                            var item = childData.getJSONObject(i)

                            customListView.dataTitle = "名前"
                            customListView.dataMain = item.getString("user_name")
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            item = childData.getJSONObject(i)
                            customListView.dataTitle = "誕生日"
                            val nen = item.getString("birthday").substring(0,4)
                            val man = item.getString("birthday").substring(5,7)
                            val day = item.getString("birthday").substring(8,10)
                            customListView.dataMain = nen + "年" + man + "月" + day + "日"
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            customListView.dataTitle = "年齢"
                            customListView.dataMain = item.getString("age")  +"歳"
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            item = childData.getJSONObject(i)
                            customListView.dataTitle = "性別"
                            val sex :String
                            sex = when(item.getString("gender")) {
                                "1" -> "男の子"
                                "2" -> "女の子"
                                else -> "未記入"
                            }
                            customListView.dataMain = sex
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            item = childData.getJSONObject(i)
                            customListView.dataTitle = "血液型"
                            val blood :String
                            blood = when(item.getString("blood_type")){
                                "1" -> "A型"
                                "2" -> "B型"
                                "3" -> "O型"
                                "4" -> "AB型"
                                else -> "未記入"
                            }
                            customListView.dataMain = blood
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            item = childData.getJSONObject(i)
                            customListView.dataTitle = "身長"
                            customListView.dataMain = item.getString("body_height") + "cm"
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            item = childData.getJSONObject(i)
                            customListView.dataTitle = "体重"
                            customListView.dataMain = item.getString("body_weight") + "kg"
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            item = childData.getJSONObject(i)
                            customListView.dataTitle = "服のサイズ"
                            customListView.dataMain = item.getString("clothes_size") + "cm"
                            list.add(customListView)

                            customListView = ChildDataListItem()
                            item = childData.getJSONObject(i)
                            customListView.dataTitle = "靴のサイズ"
                            customListView.dataMain = item.getString("shoes_size") + "cm"
                            list.add(customListView)

                            val listView = child_main_data_list
                            val childDataAdapter = ChildDataAdapter(activity!!)
                            childDataAdapter.setChildDataAdapter(list)
                            childDataAdapter.notifyDataSetChanged()
                            listView.adapter = childDataAdapter

                            setListViewHeightBasedOnChildren(child_main_data_list)

                            val listSub = ArrayList<ChildDataListSub>()
                            arrayListOf<String>().apply {
                                (0 until item.getJSONArray("vaccination").length()).forEach {i ->
                                    val childDataListSub = ChildDataListSub()
                                    val vaccinationItem = item.getJSONArray("vaccination").getJSONObject(i)
                                    childDataListSub.id = i.toLong()
                                    childDataListSub.dataTitle = vaccinationItem.getString("vaccine_name")
                                        // vaccinationItem.getString("vaccine_name")
                                    listSub.add(childDataListSub)
                                }
                                if (item.getJSONArray("vaccination").length() == 0){
                                    val childDataListSub = ChildDataListSub()
                                    childDataListSub.id = i.toLong()
                                    childDataListSub.dataTitle = "登録なし"
                                    listSub.add(childDataListSub)
                                }
                            }
                            val listSubViewAllergy = child_vaccination_list
                            val childDataAllergyAdapter = ChildDataSubAdapter(activity!!)
                            childDataAllergyAdapter.setChildDataSubAdapter(listSub)
                            listSubViewAllergy.adapter = childDataAllergyAdapter

                            setListViewHeightBasedOnChildren(child_vaccination_list)

                            val listAllergy = ArrayList<ChildDataListSub>()
                            arrayListOf<String>().apply {
                                (0 until item.getJSONArray("allergy").length()).forEach {i ->
                                    val childDataListAllergy = ChildDataListSub()
                                    val vaccinationItem = item.getJSONArray("allergy").getJSONObject(i)
                                    childDataListAllergy.id = i.toLong()
                                    childDataListAllergy.dataTitle = vaccinationItem.getString("allergy_name")
                                    // vaccinationItem.getString("vaccine_name")
                                    listAllergy.add(childDataListAllergy)
                                }
                                if (item.getJSONArray("allergy").length() == 0){
                                    val childDataListAllergy = ChildDataListSub()
                                    childDataListAllergy.id = i.toLong()
                                    childDataListAllergy.dataTitle = "登録なし"
                                    listAllergy.add(childDataListAllergy)
                                }
                            }
                            val listSubView = child_list_allergy
                            val childDataSubAdapter = ChildDataSubAdapter(activity!!)
                            childDataSubAdapter.setChildDataSubAdapter(listAllergy)
                            listSubView.adapter = childDataSubAdapter

                            setListViewHeightBasedOnChildren(child_list_allergy)


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
                            view!!,
                            "不明なエラーが発生しました",
                            Snackbar.LENGTH_SHORT
                        ).show()
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