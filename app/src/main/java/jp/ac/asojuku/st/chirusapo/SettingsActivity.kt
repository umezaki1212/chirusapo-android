package jp.ac.asojuku.st.chirusapo

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

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

            val accountResign = findPreference<PreferenceScreen>("account_resign")
            accountResign!!.setOnPreferenceClickListener {
                AlertDialog.Builder(activity!!)
                    .setTitle("アカウント削除")
                    .setMessage("アカウントを削除しますか？")
                    .setPositiveButton("削除") { _, _ ->
                        // 退会処理
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