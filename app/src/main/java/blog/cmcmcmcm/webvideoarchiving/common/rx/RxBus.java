package blog.cmcmcmcm.webvideoarchiving.common.rx;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class RxBus{

    private static RxBus bus;
    private BehaviorSubject<Object> subject = BehaviorSubject.create();

    private RxBus() {
    }

    public static RxBus getInstance() {
        if (bus == null) {
            bus = new RxBus();
        }
        return bus;
    }

    public void takeBus(Object passenger) {
        subject.onNext(passenger);
    }

    public Observable<Object> toObservable() {
        return subject;
    }
}
