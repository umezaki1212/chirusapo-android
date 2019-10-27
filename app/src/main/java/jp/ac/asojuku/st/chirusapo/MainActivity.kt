package jp.ac.asojuku.st.chirusapo

import android.accounts.Account
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Bundle
import io.realm.Realm

class MainActivity : AppCompatActivity() {
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        realm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //TODO 後でoverride fun onNavigationItemSelected(item: MenuItem): Booleanに修正
    fun onNavigationItemSelected(){

    }
    //ログアウト時Realmで保存したデータをすべて削除する
    private fun onRealmDelete(){
        realm.executeTransaction{
            user = realm.where<Account>().findAll
            group = realm.where<JoinGroup>().findAll
            vaccine = realm.where<Vaccine>().findAll
            allergy = realm.where<Allergy>().findAll
            user.deleteAllFromRealm()
            group.deleteAllFromRealm()
            vaccine.deleteAllFromRealm()
            allergy.deleteAllFromRealm()
        }
    }
}
