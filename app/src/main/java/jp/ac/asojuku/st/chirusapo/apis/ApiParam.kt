package jp.ac.asojuku.st.chirusapo.apis

class ApiParam(
    val api: String,
    val params: HashMap<String, String> = hashMapOf(),
    val image: Array<ApiParamImage>? = null,
    val movie: ApiParamMovie? = null
)