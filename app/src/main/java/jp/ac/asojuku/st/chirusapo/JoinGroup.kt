package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.jetbrains.annotations.NotNull

open class JoinGroup : RealmObject(){
    @PrimaryKey
    open var Rgroup_id : Int = 0
    @Required
    open var Rgroup_name : String = ""
    @NotNull
    open var Rgroup_flag : Int = 0
}