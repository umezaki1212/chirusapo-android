package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask

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
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val realm = Realm.getDefaultInstance()
            val userToken: String

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