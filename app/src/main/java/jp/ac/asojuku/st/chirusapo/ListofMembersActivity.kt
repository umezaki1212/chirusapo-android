package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.GroupMemberListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.GroupMemberListItem
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_list_of_members.*

class ListOfMembersActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_members)

        supportActionBar?.let {
            title = "メンバー一覧"
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        realm = Realm.getDefaultInstance()

        getMemberList()
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getMemberList() {

        realm.executeTransaction {
            val account: Account? = realm.where<Account>().findFirst()
            val test = 1
            val group: JoinGroup? =
                realm.where<JoinGroup>().equalTo("Rgroup_flag", test).findFirst()

            if (account !== null && group !== null) {
                val token = account.Rtoken
                val groupId = group.Rgroup_id
                ApiGetTask {
                    if (it == null) {
                        ApiError.showToast(this, ApiError.CONNECTION_ERROR, Toast.LENGTH_LONG)
                    } else {
                        when (it.getString("status")) {
                            "200" -> {
                                val belongMember =
                                    it.getJSONObject("data").getJSONArray("belong_member")

                                val list = ArrayList<GroupMemberListItem>()
                                for (i in 0 until belongMember.length()) {
                                    val memberInfo = belongMember.getJSONObject(i)
                                    val memberId = memberInfo.getString("user_id")
                                    val memberName = memberInfo.getString("user_name")
                                    val memberIcon = if (memberInfo.isNull("user_icon")) {
                                        null
                                    } else {
                                        memberInfo.getString("user_icon")
                                    }

                                    val item = GroupMemberListItem()
                                    item.id = i.toLong()
                                    item.userId = memberId
                                    item.userName = memberName
                                    item.userIcon = memberIcon

                                    list.add(item)
                                }
                                val listView = listview
                                val adapter = GroupMemberListAdapter(this)
                                adapter.setSampleListItem(list)
                                adapter.notifyDataSetChanged()
                                listView.adapter = adapter
                                listView.setOnItemClickListener { adapterView, view, i, l ->
                                    val item = adapterView.getItemAtPosition(i) as GroupMemberListItem
                                    val intent =
                                        Intent(application, CheckProfileActivity::class.java)
                                    intent.putExtra("USER_ID", item.userId)
                                    startActivity(intent)
                                }
                            }
                            "400" -> {
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        // 値が不足している場合に表示
                                        ApiError.REQUIRED_PARAM -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        // バリデーションに失敗した場合に表示
                                        ApiError.VALIDATION_GROUP_ID -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        // トークン検証に失敗した場合に表示
                                        ApiError.UNKNOWN_TOKEN -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        // グループ情報が見つからない場合に表示
                                        ApiError.UNKNOWN_GROUP -> {
                                            ApiError.showToast(
                                                this,
                                                errorArray.getString(i),
                                                Toast.LENGTH_LONG
                                            )
                                        }
                                        // 取得しようとしたユーザーがグループに所属していない場合に表示
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
                        }
                    }
                }.execute(
                    ApiParam(
                        Api.SLIM + "group/belong-member",
                        hashMapOf("token" to token, "group_id" to groupId)
                    )
                )
            }
        }
    }
}
