package jp.ac.asojuku.st.chirusapo.apis

import android.os.AsyncTask
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ApiPostTask(var callback: (JSONObject?) -> Unit) : AsyncTask<ApiParam, Unit, JSONObject>() {

    override fun doInBackground(vararg apiParams: ApiParam): JSONObject? {
        try {
            val params = apiParams[0]
            val api = params.api
            val request: Request

            request = if (params.params.isNullOrEmpty()) {
                Request.Builder().url(api).build()
            } else {
                val formBuilder = FormBody.Builder()
                params.params.forEach { (name, value) -> formBuilder.add(name, value) }
                val body = formBuilder.build()
                Request.Builder().url(api).post(body).build()
            }

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