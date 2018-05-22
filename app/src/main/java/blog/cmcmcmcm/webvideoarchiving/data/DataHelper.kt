package blog.cmcmcmcm.webvideoarchiving.data

import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where

/**
 *  비디오 아이디로 태그 목록 가져오기
 */
fun getTagsByVideoId(realm: Realm, videoId: String?): OrderedRealmCollection<Tag>? {
    val video = realm.where<Video>()
            .equalTo(Video::id.name, videoId).findFirst()

    return video?.tags?.sort(Tag::addedDate.name, Sort.DESCENDING)
}

/**
 * 비디오 데이터 저장
 */
fun addVideoAsync(realm: Realm, url: String?) {
    realm.executeTransactionAsync { r ->
        val video = r.createObject<Video>()
        video.url = url
    }
}

/**
 * 태그 데이터 저장
 */
fun addTagAsync(realm: Realm, point: Long, text: String, videoId: String?) {
    realm.executeTransactionAsync { r ->
        val maxId = r.where<Tag>().max(Tag::id.name) ?: 1 //기본키 값 설정해줌.
        val id = maxId.toInt() + 1

        val tag = r.createObject<Tag>(id)
        tag.point = point
        tag.text = text

        //비디오의 tag list 에도 추가해주기
        videoId?.let {
            val video = r.where<Video>().equalTo(Video::id.name, it).findFirst()
            video?.tags?.add(tag)
        }
    }
}


