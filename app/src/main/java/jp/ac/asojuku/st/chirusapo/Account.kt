package jp.ac.asojuku.st.chirusapo

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.*
import java.util.*

open class Account : RealmObject() {
    @PrimaryKey open var Ruser_id : String = ""
    @Required open var Ruser_name: String = ""
    open var Ruser_icon: String? = ""
    open var Rtoken: String = ""
}