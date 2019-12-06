package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import android.widget.LinearLayout

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
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
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences_setting, rootKey)

            val realm = Realm.getDefaultInstance()
            val userToken: String
            val view = view

            val account = realm.where(Account::class.java).findFirst()
            if (account == null) {
                val intent = Intent(activity, SignInActivity::class.java)
                startActivity(intent)
                return
            }
            userToken = account.Rtoken

            val accountEdit = findPreference<PreferenceScreen>("profile_edit")
            accountEdit!!.setOnPreferenceClickListener {
                val intent = Intent(activity, ChangeProfileActivity::class.java)
                startActivity(intent)
                return@setOnPreferenceClickListener true
            }

            val apiKeyAdd = findPreference<PreferenceScreen>("api_key_add")
            apiKeyAdd?.setOnPreferenceClickListener {
                val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst()
                if (apiKey == null) {
                    val editText = EditText(activity).apply {
                        /*
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(20, 10, 20, 10)
                        }
                        layoutParams = lp
                        */
                    }

                    AlertDialog.Builder(activity!!)
                        .setTitle("APIキー追加")
                        .setView(editText)
                        .setPositiveButton("保存") { _, _ ->
                            realm.executeTransaction {
                                if (editText.text.isNotEmpty()) {
                                    realm.createObject(RemoveBgApiKey::class.java, editText.text.toString())
                                    Snackbar.make(
                                        (activity as SettingsActivity).findViewById(R.id.root_view),
                                        "APIキーを保存しました",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .setNegativeButton("キャンセル", null)
                        .create()
                        .show()

                    return@setOnPreferenceClickListener true
                } else {
                    Snackbar.make(
                        (activity as SettingsActivity).findViewById(R.id.root_view),
                        "APIキーが既に保存されています",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    return@setOnPreferenceClickListener false
                }
            }

            val apiKeyEdit = findPreference<PreferenceScreen>("api_key_edit")
            apiKeyEdit?.setOnPreferenceClickListener {
                val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst()
                if (apiKey == null) {
                    Snackbar.make(
                        (activity as SettingsActivity).findViewById(R.id.root_view),
                        "APIキーが保存されていないため変更できません",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    return@setOnPreferenceClickListener false
                } else {
                    val editText = EditText(activity).apply {
                        setText(apiKey.apiKey)
                        /*
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(20, 10, 20, 10)
                        }
                        layoutParams = lp
                        */
                    }

                    AlertDialog.Builder(activity!!)
                        .setTitle("APIキー変更")
                        .setView(editText)
                        .setPositiveButton("変更") { _, _ ->
                            realm.executeTransaction {
                                if (editText.text.isNotEmpty()) {
                                    realm.where(RemoveBgApiKey::class.java).findAll().deleteAllFromRealm()
                                    realm.createObject(RemoveBgApiKey::class.java, editText.text.toString())
                                    Snackbar.make(
                                        (activity as SettingsActivity).findViewById(R.id.root_view),
                                        "APIキーを変更しました",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .setNegativeButton("キャンセル", null)
                        .create()
                        .show()

                    return@setOnPreferenceClickListener true
                }
            }

            val apiKeyDelete = findPreference<PreferenceScreen>("api_key_delete")
            apiKeyDelete?.setOnPreferenceClickListener {
                val apiKey = realm.where(RemoveBgApiKey::class.java).findFirst()
                if (apiKey == null) {
                    Snackbar.make(
                        (activity as SettingsActivity).findViewById(R.id.root_view),
                        "APIキーが見つからないため削除できません",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    return@setOnPreferenceClickListener false
                } else {
                    AlertDialog.Builder(activity!!)
                        .setTitle("APIキー削除")
                        .setMessage("APIキーを削除しますか？")
                        .setPositiveButton("削除") { _, _ ->
                            realm.executeTransaction {
                                realm.where(RemoveBgApiKey::class.java).findAll().deleteAllFromRealm()
                            }
                            Snackbar.make(
                                (activity as SettingsActivity).findViewById(R.id.root_view),
                                "APIキーを削除しました",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        .setNegativeButton("キャンセル", null)
                        .create()
                        .show()

                    return@setOnPreferenceClickListener true
                }
            }

            val accountResign = findPreference<PreferenceScreen>("account_resign")
            accountResign!!.setOnPreferenceClickListener {
                AlertDialog.Builder(activity!!)
                    .setTitle("アカウント削除")
                    .setMessage("アカウントを削除しますか？")
                    .setPositiveButton("削除") { _, _ ->
                        ApiPostTask {
                            if (it == null) {
                                ApiError.showToast(activity!!, ApiError.CONNECTION_ERROR, Toast.LENGTH_SHORT)
                            } else {
                                when(it.getString("status")) {
                                    "200" -> {
                                        realm.executeTransaction {realm ->
                                            realm.where(Account::class.java).findAll().deleteAllFromRealm()
                                            realm.where(JoinGroup::class.java).findAll().deleteAllFromRealm()
                                        }

                                        Toast.makeText(activity, "退会が完了しました", Toast.LENGTH_LONG).show()
                                        val intent = Intent(activity, SignInActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        startActivity(intent)
                                    }
                                    "400" -> {
                                        val errorArray = it.getJSONArray("message")
                                        for (i in 0 until errorArray.length()) {
                                            when(errorArray.getString(i)) {
                                                ApiError.REQUIRED_PARAM -> {
                                                    ApiError.showToast(activity!!, ApiError.UNKNOWN_ERROR, Toast.LENGTH_SHORT)
                                                }
                                                ApiError.UNKNOWN_TOKEN -> {
                                                    val intent = Intent(activity, SignInActivity::class.java).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    startActivity(intent)
                                                }
                                                else -> {
                                                    ApiError.showToast(activity!!, ApiError.UNKNOWN_ERROR, Toast.LENGTH_SHORT)
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        ApiError.showToast(activity!!, ApiError.UNKNOWN_ERROR, Toast.LENGTH_SHORT)
                                    }
                                }
                            }
                        }.execute(
                            ApiParam(
                                Api.SLIM + "account/resign",
                                hashMapOf("token" to userToken)
                            )
                        )
                    }
                    .setNegativeButton("キャンセル", null)
                    .create()
                    .show()

                return@setOnPreferenceClickListener true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}