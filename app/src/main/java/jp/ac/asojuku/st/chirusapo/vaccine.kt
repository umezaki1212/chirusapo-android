package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class vaccine : RealmObject(){
    @PrimaryKey
    open var vaccine_name: String? = ""
}