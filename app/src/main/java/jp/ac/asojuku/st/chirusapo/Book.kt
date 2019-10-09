package jp.ac.asojuku.st.chirusapo

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.*
import java.util.*

open class account_user : RealmObject() {
    @PrimaryKey open var user_id : Int = 0
    @Required open var user_name: String = ""
    open var icon_file_name: String? = ""
    open var token: String = ""
}

open class child_allergy : RealmObject(){
    @PrimaryKey open var child_id : Int = 0
    open var child_name : String = ""
    open var allergy_name: String? = ""
    open var vaccine_name: String? = ""
    open var icon_file_nanme: String = ""
}