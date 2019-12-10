package jp.ac.asojuku.st.chirusapo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.adapters.GroupWithdrawalListAdapter
import jp.ac.asojuku.st.chirusapo.adapters.GroupWithdrawalListItem
import jp.ac.asojuku.st.chirusapo.apis.*
import java.util.regex.Pattern

class GroupSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_setting)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var realm: Realm
        private lateinit var userToken: String
        private lateinit var groupId: String
        private lateinit var clipboardManager: ClipboardManager

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_group_setting, rootKey)

            realm = Realm.getDefaultInstance()
            clipboardManager = activity!!.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

            val account = realm.where(Account::class.java).findFirst()
            val group =
                realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()
            if (account == null || group == null) {
                val intent = Intent(activity, SignInActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                return
            }
            userToken = account.Rtoken
            groupId = group.Rgroup_id

            val viewGroupId = findPreference<PreferenceScreen>("view_group_id")
            viewGroupId?.summary = group.Rgroup_id
            viewGroupId?.setOnPreferenceClickListener {
                AlertDialog.Builder(activity!!).apply {
                    setTitle("グループID")
                    setMessage(group.Rgroup_id)
                    setPositiveButton("クリップボードにコピー") { _, _ ->
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText("label", group.Rgroup_id)
                        )
                        Toast.makeText(activity, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton("閉じる", null)
                    create()
                    show()
                }

                return@setOnPreferenceClickListener true
            }

            val viewGroupName = findPreference<PreferenceScreen>("view_group_name")
            viewGroupName?.summary = group.Rgroup_name
            viewGroupName?.setOnPreferenceClickListener {
                AlertDialog.Builder(activity!!).apply {
                    setTitle("グループ名")
                    setMessage(group.Rgroup_name)
                    setPositiveButton("クリップボードにコピー") { _, _ ->
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText("label", group.Rgroup_name)
                        )
                        Toast.makeText(activity, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton("閉じる", null)
                    create()
                    show()
                }

                return@setOnPreferenceClickListener true
            }

            val viewPinCode = findPreference<PreferenceScreen>("view_pin_code")
            viewPinCode?.summary = group.Rpin_code
            viewPinCode?.setOnPreferenceClickListener {
                AlertDialog.Builder(activity!!).apply {
                    setTitle("PINコード")
                    setMessage(group.Rpin_code)
                    setPositiveButton("クリップボードにコピー") { _, _ ->
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText("label", group.Rpin_code)
                        )
                        Toast.makeText(activity, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton("閉じる", null)
                    create()
                    show()
                }

                return@setOnPreferenceClickListener true
            }

            val editGroupInfo = findPreference<PreferenceScreen>("edit_group_info")
            editGroupInfo?.setOnPreferenceClickListener {
                val inputView = View.inflate(activity, R.layout.layout_group_edit, null)
                val layoutGroupName = inputView.findViewById(R.id.group_name) as TextInputLayout
                layoutGroupName.editText?.setText(group.Rgroup_name)
                val layoutPinCode = inputView.findViewById(R.id.pin_code) as TextInputLayout
                layoutPinCode.editText?.setText(group.Rpin_code)

                val dialog = AlertDialog.Builder(activity!!).apply {

                    setTitle("グループ情報変更")
                    setView(inputView)
                    setPositiveButton("変更") { _, _ ->
                        val groupName = layoutGroupName.editText?.text.toString()
                        val pinCode = layoutPinCode.editText?.text.toString()

                        ApiPostTask { jsonObject ->
                            if (jsonObject == null) {
                                ApiError.showToast(
                                    activity!!,
                                    ApiError.CONNECTION_ERROR,
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                when (jsonObject.getString("status")) {
                                    "200" -> {
                                        val groupInfo = jsonObject.getJSONObject("data")
                                            .getJSONObject("group_info")

                                        val newGroupName = groupInfo.getString("group_name")
                                        val newPinCode = groupInfo.getString("pin_code")

                                        viewGroupName?.summary = newGroupName
                                        viewPinCode?.summary = newPinCode

                                        realm.executeTransaction {
                                            val targetGroup = realm.where(JoinGroup::class.java)
                                                .equalTo("Rgroup_id", groupId).findFirst()

                                            if (targetGroup != null) {
                                                targetGroup.Rgroup_name = newGroupName
                                                targetGroup.Rpin_code = newPinCode
                                            }
                                        }

                                        Snackbar.make(
                                            activity!!.findViewById(R.id.root_view),
                                            "グループ情報を更新しました",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                    "400" -> {
                                        val errorArray = jsonObject.getJSONArray("message")
                                        for (i in 0 until errorArray.length()) {
                                            when (errorArray.getString(i)) {
                                                ApiError.REQUIRED_PARAM,
                                                ApiError.VALIDATION_GROUP_NAME,
                                                ApiError.VALIDATION_PIN_CODE -> {
                                                    ApiError.showToast(
                                                        activity!!,
                                                        errorArray.getString(i),
                                                        Toast.LENGTH_SHORT
                                                    )
                                                }
                                                ApiError.UNKNOWN_TOKEN -> {
                                                    val intent = Intent(
                                                        activity,
                                                        SignInActivity::class.java
                                                    ).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    startActivity(intent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }.execute(
                            ApiParam(
                                Api.SLIM + "group/edit",
                                hashMapOf(
                                    "token" to userToken,
                                    "group_id" to groupId,
                                    "group_name" to groupName,
                                    "pin_code" to pinCode
                                )
                            )
                        )
                    }
                    setNegativeButton("キャンセル", null)
                }.create()
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

                layoutGroupName.editText?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                            judgeGroupEdit(layoutGroupName, layoutPinCode)
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                })

                layoutPinCode.editText?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                            judgeGroupEdit(layoutGroupName, layoutPinCode)
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                })

                return@setOnPreferenceClickListener true
            }

            val groupDelete = findPreference<PreferenceScreen>("group_delete")
            groupDelete?.setOnPreferenceClickListener {
                AlertDialog.Builder(activity!!).apply {
                    setTitle("グループ削除")
                    setMessage("グループを削除しますか？")
                    setPositiveButton("削除") { _, _ ->
                        ApiPostTask { jsonObject ->
                            if (jsonObject == null) {
                                ApiError.showToast(
                                    activity!!,
                                    ApiError.CONNECTION_ERROR,
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                when (jsonObject.getString("status")) {
                                    "200" -> {
                                        realm.executeTransaction { realm ->
                                            realm.where(JoinGroup::class.java)
                                                .equalTo("Rgroup_id", groupId)
                                                .findAll().deleteAllFromRealm()

                                            val groupArray =
                                                realm.where(JoinGroup::class.java).findAll()
                                            if (groupArray != null) {
                                                for (i in 0 until groupArray.size) {
                                                    if (groupArray[i] != null) {
                                                        groupArray[i]!!.Rgroup_flag = 0
                                                    }
                                                }
                                            }

                                            val groupFirst =
                                                realm.where(JoinGroup::class.java).findFirst()
                                            if (groupFirst != null) {
                                                groupFirst.Rgroup_flag = 1
                                            }

                                            val intent =
                                                Intent(activity, MainActivity::class.java).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            startActivity(intent)

                                            Toast.makeText(
                                                activity,
                                                "グループ情報を削除しました",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    "400" -> {
                                        val errorArray = jsonObject.getJSONArray("message")
                                        for (i in 0 until errorArray.length()) {
                                            when (errorArray.getString(i)) {
                                                ApiError.REQUIRED_PARAM,
                                                ApiError.UNKNOWN_GROUP,
                                                ApiError.UNREADY_BELONG_GROUP -> {
                                                    ApiError.showToast(
                                                        activity!!,
                                                        errorArray.getString(i),
                                                        Toast.LENGTH_SHORT
                                                    )
                                                }
                                                ApiError.UNKNOWN_TOKEN -> {
                                                    val intent = Intent(
                                                        activity,
                                                        SignInActivity::class.java
                                                    ).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    startActivity(intent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }.execute(
                            ApiParam(
                                Api.SLIM + "group/delete",
                                hashMapOf("token" to userToken, "group_id" to groupId)
                            )
                        )
                    }
                    setNegativeButton("キャンセル", null)
                    create()
                    show()
                }
                return@setOnPreferenceClickListener true
            }

            val groupWithdrawalForce = findPreference<PreferenceScreen>("group_withdrawal_force")
            groupWithdrawalForce?.setOnPreferenceClickListener {
                ApiGetTask { jsonObject ->
                    if (jsonObject == null) {
                        ApiError.showToast(
                            activity!!,
                            ApiError.CONNECTION_ERROR,
                            Toast.LENGTH_SHORT
                        )
                    } else {
                        when (jsonObject.getString("status")) {
                            "200" -> {
                                val belongMember =
                                    jsonObject.getJSONObject("data").getJSONArray("belong_member")
                                val list = ArrayList<GroupWithdrawalListItem>()
                                for (i in 0 until belongMember.length()) {
                                    val member = belongMember.getJSONObject(i)
                                    if (member.getString("user_id") != account.Ruser_id) {
                                        val item = GroupWithdrawalListItem().apply {
                                            id = i.toLong()
                                            userId = member.getString("user_id")
                                            userName = member.getString("user_name")
                                        }
                                        list.add(item)
                                    }
                                }
                                if (list.size == 0) {
                                    Snackbar.make(
                                        activity!!.findViewById<LinearLayout>(R.id.root_view),
                                        "退会させることができるユーザーが見つかりませんでした",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                } else {
//                                    val listView = ListView(activity)
//                                    val adapter = GroupWithdrawalListAdapter(activity!!).apply {
//                                        setItem(list)
//                                        notifyDataSetChanged()
//                                    }
//                                    listView.adapter = adapter
//
//                                    AlertDialog.Builder(activity!!).apply {
//                                        setTitle("ユーザー退会")
//                                        setView(listView)
//                                        create()
//                                        show()
//                                    }
                                }
                            }
                            "400" -> {
                                val errorArray = jsonObject.getJSONArray("message")
                                for (i in 0 until errorArray.length()) {
                                    when (errorArray.getString(i)) {
                                        ApiError.REQUIRED_PARAM,
                                        ApiError.UNKNOWN_GROUP,
                                        ApiError.UNREADY_BELONG_GROUP -> {
                                            ApiError.showToast(
                                                activity!!,
                                                errorArray.getString(i),
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                        ApiError.UNKNOWN_TOKEN -> {
                                            val intent = Intent(
                                                activity,
                                                SignInActivity::class.java
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.execute(
                    ApiParam(
                        Api.SLIM + "group/belong-member",
                        hashMapOf("token" to userToken, "group_id" to groupId)
                    )
                )

                return@setOnPreferenceClickListener true
            }

            val groupWithdrawal = findPreference<PreferenceScreen>("group_withdrawal")
            groupWithdrawal?.setOnPreferenceClickListener {
                AlertDialog.Builder(activity!!).apply {
                    setTitle("グループ脱退")
                    setMessage("グループを脱退しますか？")
                    setPositiveButton("削除") { _, _ ->
                        ApiPostTask { jsonObject ->
                            if (jsonObject == null) {
                                ApiError.showToast(
                                    activity!!,
                                    ApiError.CONNECTION_ERROR,
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                when (jsonObject.getString("status")) {
                                    "200" -> {
                                        realm.executeTransaction { realm ->
                                            realm.where(JoinGroup::class.java)
                                                .equalTo("Rgroup_id", groupId).findAll()
                                                .deleteAllFromRealm()

                                            val groupArray =
                                                realm.where(JoinGroup::class.java).findAll()
                                            if (groupArray != null) {
                                                for (i in 0 until groupArray.size) {
                                                    if (groupArray[i] != null) {
                                                        groupArray[i]!!.Rgroup_flag = 0
                                                    }
                                                }
                                            }

                                            val groupFirst =
                                                realm.where(JoinGroup::class.java).findFirst()
                                            if (groupFirst != null) {
                                                groupFirst.Rgroup_flag = 1
                                            }

                                            val intent =
                                                Intent(activity, MainActivity::class.java).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            startActivity(intent)

                                            Toast.makeText(
                                                activity,
                                                "グループから脱退しました",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    "400" -> {
                                        val errorArray = jsonObject.getJSONArray("message")
                                        for (i in 0 until errorArray.length()) {
                                            when (errorArray.getString(i)) {
                                                ApiError.REQUIRED_PARAM,
                                                ApiError.UNKNOWN_GROUP,
                                                ApiError.UNREADY_BELONG_GROUP -> {
                                                    ApiError.showToast(
                                                        activity!!,
                                                        errorArray.getString(i),
                                                        Toast.LENGTH_SHORT
                                                    )
                                                }
                                                ApiError.UNKNOWN_TOKEN -> {
                                                    val intent = Intent(
                                                        activity,
                                                        SignInActivity::class.java
                                                    ).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    startActivity(intent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }.execute(
                            ApiParam(
                                Api.SLIM + "group/withdrawal",
                                hashMapOf("token" to userToken, "group_id" to groupId)
                            )
                        )
                    }
                    setNegativeButton("キャンセル", null)
                    create()
                    show()
                }
                return@setOnPreferenceClickListener true
            }
        }

        private fun judgeGroupName(layoutGroupName: TextInputLayout): Boolean {
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

        private fun judgeGroupPin(layoutGroupPin: TextInputLayout): Boolean {
            val inputGroupPin = layoutGroupPin.editText?.text.toString().trim()

            return if (!Pattern.compile("^[0-9]{4}$").matcher(inputGroupPin).find()) {
                layoutGroupPin.error = "4文字の数字で入力してください"
                false
            } else {
                layoutGroupPin.error = null
                true
            }
        }

        private fun judgeGroupEdit(
            layoutGroupName: TextInputLayout,
            layoutGroupPin: TextInputLayout
        ): Boolean {
            return judgeGroupName(layoutGroupName) && judgeGroupPin(layoutGroupPin)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}