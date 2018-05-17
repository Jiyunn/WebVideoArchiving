package blog.cmcmcmcm.webvideoarchiving.data

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class Tag : RealmModel {

    @PrimaryKey
    var id: Int = 0

    var point: Long = 0L //태그 포인트 지점
    var text: String = "" //태그와 함께 적은 글자
    var addedDate: Date = Date()


}