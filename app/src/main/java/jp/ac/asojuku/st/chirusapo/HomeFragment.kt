package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.PostTimelineListItem
import jp.ac.asojuku.st.chirusapo.apis.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.regex.Pattern

class HomeFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var userToken: String

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

        val account = realm.where<Account>().findFirst()
        if (account == null) {
            Toast.makeText(activity, "アカウント情報が取得できません", Toast.LENGTH_SHORT).show()
        } else {
            userToken = account.Rtoken
        }

        time_line_group_create.setOnClickListener {
            groupCreate()
        }

        time_line_group_participation.setOnClickListener {
            groupJoin()
        }

        setHomeList(root_view)

        button_post_add.setOnClickListener {
            val intent = Intent(activity,MainPostAddActivity::class.java)
            startActivity(intent)
        }
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

                root_view.visibility = View.INVISIBLE
                button_post_add.visibility = View.INVISIBLE
            } else {
                time_line_start.visibility = View.INVISIBLE
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

    private fun judgeGroupCreate(layoutGroupId: TextInputLayout, layoutGroupName: TextInputLayout):Boolean {
        return judgeGroupId(layoutGroupId) && judgeGroupName(layoutGroupName)
    }

    private fun judgeGroupJoin(layoutGroupId: TextInputLayout, layoutGroupPin: TextInputLayout):Boolean {
        return judgeGroupId(layoutGroupId) && judgeGroupPin(layoutGroupPin)
    }

    private fun judgeGroupId(layoutGroupId: TextInputLayout):Boolean {
        val inputGroupId = layoutGroupId.editText?.text.toString().trim()

        return if (inputGroupId.length < 5) {
            layoutGroupId.error = "5文字以上で入力してください"
            false
        } else if (inputGroupId.length > 30) {
            layoutGroupId.error = "30文字以下で入力してください"
            false
        } else if (!Pattern.compile("^[a-zA-Z0-9-_]{1,30}\$").matcher(inputGroupId).find()) {
            layoutGroupId.error = "使用できない文字が含まれています"
            false
        } else {
            layoutGroupId.error = null
            true
        }
    }

    private fun judgeGroupName(layoutGroupName: TextInputLayout):Boolean {
        val inputGroupName = layoutGroupName.editText?.text.toString().trim()

        return if (inputGroupName.isEmpty()) {
            layoutGroupName.error = "1文字以上で入力してください"
            false
        } else if (inputGroupName.length > 30) {
            layoutGroupName.error = "30文字以下で入力してください"
            false
        } else if (!Pattern.compile("^.{1,30}\$").matcher(inputGroupName).find()) {
            layoutGroupName.error = "使用できない文字が含まれています"
            false
        } else {
            layoutGroupName.error = null
            true
        }
    }

    private fun judgeGroupPin(layoutGroupPin: TextInputLayout):Boolean {
        val inputGroupPin = layoutGroupPin.editText?.text.toString().trim()

        return if(!Pattern.compile("^[0-9]{4}$").matcher(inputGroupPin).find()){
            layoutGroupPin.error = "4文字の数字で入力してください"
            false
        }else{
            layoutGroupPin.error = null
            true
        }
    }

    // グループ作成
    private fun groupCreate(){
        val inputView = View.inflate(activity, R.layout.layout_group_create, null)
        // 関連付け
        val layoutGroupId = inputView.findViewById(R.id.group_id) as TextInputLayout
        val layoutGroupName = inputView.findViewById(R.id.group_name) as TextInputLayout

        // Dialog生成
        val dialog = AlertDialog.Builder(activity!!)
            .setTitle("グループ作成")
            .setView(inputView)
            .setPositiveButton(
                "作成"
            ) { _, _ ->
                val groupId = layoutGroupId.editText?.text.toString()
                val groupName = layoutGroupName.editText?.text.toString()

                // APIとの通信を行う
                ApiPostTask{
                    // 処理した結果が帰ってくる
                    if (it == null) {
                        ApiError.showToast(activity!!, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                    }
                    // なにかしら返答があった場合
                    else {
                        //statusを取得する
                        when (it.getString("status")) {
                            "200" -> {
                                val num1 = 1
                                val num2 = 0
                                realm.executeTransaction{
                                    val group = realm.where<JoinGroup>().equalTo("Rgroup_flag",num1).findAll()
                                    if(group != null){
                                        for(x in group){
                                            x.Rgroup_flag = num2
                                        }
                                    }
                                }
                                // 参加・作成したグループ情報の取得
                                val belongGroup = it.getJSONObject("data").getJSONArray("belong_group")
                                realm.executeTransaction{
                                    for (i in 0 until belongGroup.length()) {
                                        val groupInfo = belongGroup.getJSONObject(i)
                                        val groupInfoGroupId = groupInfo.getString("group_id")
                                        val groupInfoGroupName = groupInfo.getString("group_name")
                                        if(realm.where<JoinGroup>().equalTo("Rgroup_id",groupInfoGroupId).findFirst() == null) {
                                            // realmに保存する
                                            realm.createObject(JoinGroup::class.java, groupInfoGroupId).apply {
                                                Rgroup_name = groupInfoGroupName
                                                // 現在見ているグループに設定するためフラグを(1)にする
                                                Rgroup_flag = num1
                                            }
                                        }
                                    }
                                }

                                // メッセージ表示
                                AlertDialog.Builder(activity!!)
                                    .setMessage("グループを作成しました")
                                    .setNegativeButton("閉じる", null)
                                    .setOnDismissListener {
                                        val intent = Intent(activity, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .create()
                                    .show()
                            }
                            "400" -> {
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        ApiError.VALIDATION_GROUP_ID -> {
                                            // 指定した値が正しくない
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.VALIDATION_GROUP_NAME -> {
                                            // 指定して値が正しくない
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.UNKNOWN_TOKEN -> {
                                            // ログイントークンの検証失敗
                                            val intent = Intent(activity, SignInActivity::class.java).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            startActivity(intent)
                                        }
                                        ApiError.ALREADY_CREATE_GROUP -> {
                                            // 既に同じグループIDが登録している場合
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )

                                        }
                                        ApiError.REQUIRED_PARAM -> {
                                            // 必要な値が見つかりませんでした表示
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.execute(ApiParam(
                    Api.SLIM + "group/create" ,
                    hashMapOf("token" to userToken,"group_id" to groupId,"group_name" to groupName)
                ))
            }
            .setNegativeButton("キャンセル", null)
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        layoutGroupId.editText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    judgeGroupCreate(layoutGroupId, layoutGroupName)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        layoutGroupName.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    judgeGroupCreate(layoutGroupId, layoutGroupName)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })
    }

    // グループ参加
    private fun groupJoin(){
        val inputView = View.inflate(activity, R.layout.layout_group_join, null)

        // 関連付け
        val layoutGroupId = inputView.findViewById(R.id.group_id) as TextInputLayout
        val layoutGroupPin = inputView.findViewById(R.id.pin_code) as TextInputLayout

        // Dialog生成
        val dialog = AlertDialog.Builder(activity!!)
            .setTitle("グループ参加")
            .setView(inputView)
            .setPositiveButton(
                "参加"
            ) { _, _ ->
                val groupId = layoutGroupId.editText?.text.toString()
                val groupPin = layoutGroupPin.editText?.text.toString()

                // APIとの通信を行う
                ApiPostTask{
                    // 処理した結果が帰ってくる
                    if (it == null) {
                        ApiError.showToast(activity!!, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                    }
                    // なにかしら返答があった場合
                    else {
                        //statusを取得する
                        when (it.getString("status")) {
                            "200" -> {
                                val num1 = 1
                                val num2 = 0
                                realm.executeTransaction{
                                    val group = realm.where<JoinGroup>().equalTo("Rgroup_flag", num1).findAll()
                                    if(group != null){
                                        for(x in group) {
                                            x.Rgroup_flag = num2
                                        }
                                    }
                                }
                                //参加・作成したグループ情報の取得
                                val belongGroup = it.getJSONObject("data").getJSONArray("belong_group")
                                realm.executeTransaction{
                                    for (i in 0 until belongGroup.length()) {
                                        val groupInfo = belongGroup.getJSONObject(i)
                                        val groupInfoGroupId = groupInfo.getString("group_id")
                                        val groupInfoGroupName = groupInfo.getString("group_name")
                                        val groupInfoPinCode = groupInfo.getString("pin_code")

                                        if(realm.where<JoinGroup>().equalTo("Rgroup_id",groupInfoGroupId).findFirst() == null){
                                            // realmに保存する
                                            realm.createObject<JoinGroup>().apply{
                                                Rgroup_id = groupInfoGroupId
                                                Rgroup_name = groupInfoGroupName
                                                Rpin_code = groupInfoPinCode
                                                //現在見ているグループに設定するためフラグを(1)にする
                                                Rgroup_flag = num1
                                            }
                                        }
                                    }
                                }

                                AlertDialog.Builder(activity!!)
                                    .setMessage("グループに参加しました")
                                    .setNegativeButton("閉じる", null)
                                    .setOnDismissListener {
                                        val intent = Intent(activity, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .create()
                                    .show()
                            }
                            "400" -> {
                                val errorArray = it.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        ApiError.VALIDATION_GROUP_ID -> {
                                            // 指定した値が正しくない
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.UNKNOWN_TOKEN -> {
                                            // ログイントークンの検証失敗
                                            val intent = Intent(activity, SignInActivity::class.java).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            startActivity(intent)
                                        }
                                        ApiError.VALIDATION_PIN_CODE -> {
                                            // 指定した値が正しくない
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.UNKNOWN_GROUP -> {
                                            // グループが見つかりませんでした表示
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.VERIFY_PIN_CODE -> {
                                            // PINコードの検証に失敗しました表示
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.ALREADY_BELONG_GROUP -> {
                                            // 既にグループに所属しています表示
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.REQUIRED_PARAM -> {
                                            // 必要な値が見つかりませんでした表示
                                            ApiError.showToast(
                                                activity!!,
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
                        Api.SLIM + "group/join" ,
                        hashMapOf("token" to userToken,"group_id" to groupId,"pin_code" to groupPin)
                    )
                )
            }
            .setNegativeButton("キャンセル", null)
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        layoutGroupId.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    judgeGroupJoin(layoutGroupId, layoutGroupPin)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        layoutGroupPin.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    judgeGroupJoin(layoutGroupId, layoutGroupPin)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })
    }
}