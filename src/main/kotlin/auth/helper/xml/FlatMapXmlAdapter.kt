package auth.helper.xml

import javax.xml.bind.annotation.adapters.XmlAdapter
import auth.corppass.model.auth.ResultSet
import auth.corppass.model.auth.Result
import auth.corppass.model.auth.Auth
import auth.corppass.model.auth.ThirdPartyClient

class FlatMapXmlAdapter<T> : XmlAdapter<ResultSet, MutableList<T>>() {
    override fun unmarshal(v: ResultSet): MutableList<T> {
        return v.results.fold(mutableListOf<T>()) {
            acc, result -> acc.union((result.auths ?: result.clients) as MutableList<T>).toMutableList()
        }
    }

    override fun marshal(v: MutableList<T>): ResultSet {
        return ResultSet(mutableListOf(Result(v as? MutableList<Auth>, v as? MutableList<ThirdPartyClient>)))
    }
}
