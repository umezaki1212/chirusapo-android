package jp.ac.asojuku.st.chirusapo.apis

class Api {
    companion object {
        const val SLIM = "https://slim.chirusapo.vxx0.com/"
        const val FLASK = "https://flask.chirusapo.vxx0.com/"

        fun urlBuilder(
            apiServer: String,
            apiName: String,
            apiReplace: Map<String, String>
        ): String {
            var temp = apiName
            apiReplace.forEach { (search, target) ->
                temp = temp.replace(search, target)
            }

            return apiServer + temp
        }
    }
}