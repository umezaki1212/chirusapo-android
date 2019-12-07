package jp.ac.asojuku.st.chirusapo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_dress_model_viewer.*

class DressModelViewer : AppCompatActivity(),
    DressModelViewerFragment.OnFragmentInteractionListener {
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dress_model_viewer)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
            title = resources.getString(R.string.dress_model_list)
        }

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()
        if (account == null || group == null) {

        } else {
            userToken = account.Rtoken
            groupId = group.Rgroup_id
        }
    }

    override fun onResume() {
        super.onResume()

        ApiGetTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showToast(this, ApiError.UNKNOWN_ERROR, Toast.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val jsonData = jsonObject.getJSONObject("data")
                        val jsonModelChild = jsonData.getJSONArray("model_child")
                        val jsonModelClothes = jsonData.getJSONArray("model_clothes")

                        val modelChild = arrayListOf<String>().apply {
                            (0 until jsonModelChild.length()).forEach {
                                this.add(jsonModelChild.getString(it))
                            }
                        }
                        val modelClothes = arrayListOf<String>().apply {
                            (0 until jsonModelClothes.length()).forEach {
                                this.add(jsonModelClothes.getString(it))
                            }
                        }

                        view_pager.adapter = object :
                            FragmentPagerAdapter(
                                supportFragmentManager,
                                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
                            ) {
                            override fun getItem(position: Int): Fragment {
                                return fragmentList[position]
                            }

                            override fun getCount(): Int {
                                return fragmentList.size
                            }

                            override fun getPageTitle(position: Int): CharSequence? {
                                return titleList[position]
                            }

                            val titleList = listOf(
                                "子どもモデル",
                                "洋服モデル"
                            )

                            val fragmentList = listOf(
                                DressModelViewerFragment.newInstance(modelChild),
                                DressModelViewerFragment.newInstance(modelClothes)
                            )
                        }
                        tab_layout.setupWithViewPager(view_pager)
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_TOKEN,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                else -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "model/get",
                hashMapOf("token" to userToken, "group_id" to groupId)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
