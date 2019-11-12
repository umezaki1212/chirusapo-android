package jp.ac.asojuku.st.chirusapo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm

class CheckGrowthActivity : AppCompatActivity() {
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_growth)
        realm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}
