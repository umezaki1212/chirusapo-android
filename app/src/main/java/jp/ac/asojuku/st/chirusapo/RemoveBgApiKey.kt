package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RemoveBgApiKey : RealmObject() {
    @PrimaryKey
    open var apiKey: String = ""
}