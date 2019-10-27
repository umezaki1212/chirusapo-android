package jp.ac.asojuku.st.chirusapo

import android.app.Application
import io.realm.Realm

class InitRealm : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}