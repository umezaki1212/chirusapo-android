package jp.ac.asojuku.st.chirusapo.apis

class Api {
    companion object {
        const val SLIM3 = "https://slim3.chirusapo.vxx0.com/"
        const val FLASK = "https://flask.chirusapo.vxx0.com/"

        fun urlBuilder(
            apiServer: String,
            apiName: String,
            apiReplace: Map<String, String>
        ): String {
            var apiNameTemp = apiName
            apiReplace.forEach { (search, target) ->
                apiNameTemp = apiNameTemp.replace(search, target)
            }

            return apiServer + apiNameTemp
        }
    }
}