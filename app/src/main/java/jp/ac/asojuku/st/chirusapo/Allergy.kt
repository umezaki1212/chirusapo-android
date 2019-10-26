package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Allergy : RealmObject(){
    @PrimaryKey
    open var allergy_name: String? = ""
}