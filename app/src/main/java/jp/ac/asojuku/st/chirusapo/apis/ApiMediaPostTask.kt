package jp.ac.asojuku.st.chirusapo.apis

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.AsyncTask
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*
import okhttp3.RequestBody
import okhttp3.MultipartBody
import java.io.File

class ApiMediaPostTask(var callback: (JSONObject?) -> Unit) :
    AsyncTask<ApiParam, Unit, JSONObject>() {

    override fun doInBackground(vararg apiParams: ApiParam): JSONObject? {
        try {
            val params = apiParams[0]
            val api = params.api

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            params.params.forEach { (name, value) -> multipartBody.addFormDataPart(name, value) }

            if (params.image != null) {
                params.image.forEach {
                    val byteArray = bitmapToByteArray(it.content, it.fileName)

                    multipartBody.addFormDataPart(
                        it.serverArg,
                        it.fileName,
                        RequestBody.create(MediaType.parse(it.fileType), byteArray)
                    )
                }
            }

            if (params.movie != null) {
                val movieFile = File(params.movie.filePath)

                multipartBody.addFormDataPart(
                    params.movie.serverArg,
                    params.movie.fileName,
                    RequestBody.create(MediaType.parse(params.movie.fileType), movieFile)
                )
            }

            val requestBody = multipartBody.build()

            val request = Request.Builder().url(api).post(requestBody).build()
            val okHttpClient = OkHttpClient()
            val call = okHttpClient.newCall(request)
            val response = call.execute()
            val body = response.body()
            val responseBody = body!!.string()

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

    private fun bitmapToByteArray(bitmap: Bitmap, fileName: String): ByteArray {
        val stream = ByteArrayOutputStream()
        when (getExtension(fileName)) {
            "jpeg" -> {
                bitmap.compress(CompressFormat.JPEG, 100, stream)
            }
            "jpg" -> {
                bitmap.compress(CompressFormat.JPEG, 100, stream)
            }
            "png" -> {
                bitmap.compress(CompressFormat.PNG, 100, stream)
            }
            else -> {
                bitmap.compress(CompressFormat.JPEG, 100, stream)
            }
        }
        return stream.toByteArray()
    }

    private fun getExtension(fileName: String): String {
        val point = fileName.lastIndexOf(".")
        return if (point != -1) {
            fileName.substring(point + 1).toLowerCase(Locale.JAPAN)
        } else fileName.toLowerCase(Locale.JAPAN)
    }
}