package jp.ac.asojuku.st.chirusapo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.activity_child_graph.*

class ChildGraphActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var childId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_graph)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
            title = "成長グラフ"
        }

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        val userId = intent.getStringExtra("user_id")

        if (account != null && userId != null) {
            userToken = account.Rtoken
            childId = userId
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()

        ApiGetTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showSnackBar(root_view, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val historyData =
                            jsonObject.getJSONObject("data").getJSONObject("history_data")
                        val keys = historyData.keys()

                        if (historyData.length() == 0) {
                            Snackbar.make(root_view, "成長データが見つかりませんでした", Snackbar.LENGTH_SHORT).show()
                        }

                        var paramDate = ""
                        var paramBodyHeight = ""
                        var paramBodyWeight = ""
                        var paramClothesSize = ""
                        var paramShoesSize = ""

                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = historyData.getJSONObject(key)

                            if (keys.hasNext()) {
                                paramDate += "$key,"
                                paramBodyHeight += obj.getString("body_height") + ","
                                paramBodyWeight += obj.getString("body_weight") + ","
                                paramClothesSize += obj.getString("clothes_size") + ","
                                paramShoesSize += obj.getString("shoes_size") + ","
                            } else {
                                paramDate += key
                                paramBodyHeight += obj.getString("body_height")
                                paramBodyWeight += obj.getString("body_weight")
                                paramClothesSize += obj.getString("clothes_size")
                                paramShoesSize += obj.getString("shoes_size")
                            }
                        }

                        val graph01Url = Uri.parse("https://slim.chirusapo.vxx0.com/graph")
                            .buildUpon().apply {
                                appendQueryParameter("label1", "身長")
                                appendQueryParameter("label2", "体重")
                                appendQueryParameter("day", paramDate)
                                appendQueryParameter("data1", paramBodyHeight)
                                appendQueryParameter("data2", paramBodyWeight)
                            }.build()


                        val graph02Url = Uri.parse("https://slim.chirusapo.vxx0.com/graph")
                            .buildUpon().apply {
                                appendQueryParameter("label1", "服のサイズ")
                                appendQueryParameter("label2", "靴のサイズ")
                                appendQueryParameter("day", paramDate)
                                appendQueryParameter("data1", paramClothesSize)
                                appendQueryParameter("data2", paramShoesSize)
                            }.build()

                        graph01.webViewClient = WebViewClient()
                        graph01.loadUrl(graph01Url.toString())
                        graph01.settings.javaScriptEnabled = true


                        println(graph01Url.toString())

                        graph02.webViewClient = WebViewClient()
                        graph02.loadUrl(graph02Url.toString())
                        graph02.settings.javaScriptEnabled = true

                        println(graph02Url.toString())
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_CHILD,
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent = Intent(this, SignInActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                }
                                else -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Snackbar.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "/child/growth/history/get",
                hashMapOf("token" to userToken, "child_id" to childId)
            )
        )
    }
}