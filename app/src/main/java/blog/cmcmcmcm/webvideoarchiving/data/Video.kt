package blog.cmcmcmcm.webvideoarchiving.data

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class Video : RealmModel {

    var id: String = UUID.randomUUID().toString()

    var url: String? = null
    var seeingPoint:Long=0L //책갈피
    var addedDate: Date = Date()

    var tags: RealmList<Tag> = RealmList()
}