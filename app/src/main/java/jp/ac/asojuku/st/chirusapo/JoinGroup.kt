package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class JoinGroup : RealmObject(){
    @PrimaryKey
    open var Rgroup_id : Int = 0
    @Required
    open var Rgroup_name : String = ""
}