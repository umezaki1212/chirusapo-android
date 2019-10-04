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
            return apiServer + apiReplace.toList().fold(apiName) { acc, (search, target) ->
                acc.replace(search, target)
            }
        }
    }
}