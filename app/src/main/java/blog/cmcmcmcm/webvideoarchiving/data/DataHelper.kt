package blog.cmcmcmcm.webvideoarchiving.data

import android.util.Log
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where


        //지금 비디오 객체의 아이디를 이용해 갖고있는 태그를 리턴.
        fun getTagsByVideoId(realm: Realm, videoId:String?) : OrderedRealmCollection<Tag>? {
            val video = realm.where<Video>()
                    .equalTo(Video::id.name, videoId).findFirst()

            return  video?.tags?.sort(Tag::addedDate.name, Sort.DESCENDING)
        }

        fun addVideoAsync(realm: Realm, url: String?) {
            realm.executeTransactionAsync { r ->
                val video = r.createObject<Video>()
                video.url = url
                Log.d("WebView", "add")
            }
        }

        fun addTagAsync(realm: Realm, point:Long, text:String, videoId:String? ) {
            realm.executeTransactionAsync { r ->
                val maxId = r.where<Tag>().max(Tag::id.name) ?: 1 //기본키 값 설정해줌.
                val id = maxId.toInt() + 1

                val tag = r.createObject<Tag>(id)
                tag.point = point
                tag.text = text

                //비디오에도 넣어줌
                videoId?.let {
                    val video = r.where<Video>().equalTo(Video::id.name, it).findFirst()
                    video?.tags?.add(tag)
                }
            }
        }


