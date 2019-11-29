package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class JoinGroup : RealmObject() {
    @PrimaryKey
    open var Rgroup_id: String = ""
    @Required
    open var Rgroup_name: String = ""
    @Required
    open var Rpin_code: String = ""
    open var Rgroup_flag: Int = 0
}