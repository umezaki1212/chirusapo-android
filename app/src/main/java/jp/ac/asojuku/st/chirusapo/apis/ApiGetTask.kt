package jp.ac.asojuku.st.chirusapo.apis

import android.os.AsyncTask
import okhttp3.HttpUrl.Builder
import okhttp3.HttpUrl.parse
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ApiGetTask(var callback: (JSONObject?) -> Unit) : AsyncTask<ApiParam, Unit, JSONObject>() {

    override fun doInBackground(vararg apiParams: ApiParam): JSONObject? {
        try {
            val params = apiParams[0]
            val api = params.api
            val urlBuilder: Builder
            urlBuilder = parse(api)!!.newBuilder()

            if (!params.params.isNullOrEmpty()) {
                params.params.forEach { (name, value) -> urlBuilder.addQueryParameter(name, value) }
            }

            val request = Request.Builder().url(urlBuilder.toString()).build()
            val okHttpClient = OkHttpClient.Builder().build()
            val call = okHttpClient.newCall(request)
            val response = call.execute()
            val responseBody = response.body()!!.string()

            return JSONObject(responseBody)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: JSONObject?) {
        super.onPostExecute(result)

        callback(result)
    }

}