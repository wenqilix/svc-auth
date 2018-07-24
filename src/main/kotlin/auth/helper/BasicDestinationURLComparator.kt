package auth.helper

import net.shibboleth.utilities.java.support.net.BasicURLComparator

class BasicDestinationURLComparator(val supposedEndpoint: String?): BasicURLComparator() {
    override fun compare(uri1: String?, uri2:String?): Boolean {
        return super.compare(uri1, this.supposedEndpoint ?: uri2)
    }
}
