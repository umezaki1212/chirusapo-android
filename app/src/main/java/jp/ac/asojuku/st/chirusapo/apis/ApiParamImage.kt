package jp.ac.asojuku.st.chirusapo.apis

import android.graphics.Bitmap

class ApiParamImage(
    val fileType: String,
    val fileName: String,
    val serverArg: String,
    val content: Bitmap
)