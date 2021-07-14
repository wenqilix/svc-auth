package auth.model.corppass.auth

data class ResultSet(
    var results: MutableList<Result> = mutableListOf<Result>()
)

data class Result(
    var auths: MutableList<Auth>? = null,
    var clients: MutableList<ThirdPartyClient>? = null
)
