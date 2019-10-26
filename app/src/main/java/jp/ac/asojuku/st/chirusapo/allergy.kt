package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class allergy : RealmObject(){
    @PrimaryKey
    open var allergy_name: String? = ""
}