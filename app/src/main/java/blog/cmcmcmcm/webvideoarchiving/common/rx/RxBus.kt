package blog.cmcmcmcm.webvideoarchiving.common.rx

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RxBus {

    private val subject = BehaviorSubject.create<Any>()

    companion object {
        private val bus = RxBus()

        fun getInstance(): RxBus {
            return bus
        }
    }


    fun takeBus(task : Any ) {
        subject.onNext(task)
    }

    fun toObservable() : Observable<Any> {
        return subject
    }
}