package com.soul;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2023/03/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RxJavaLearn {

    public void text() {
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("a");
                        subscriber.onNext("b");
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String integer) {
                        System.out.println(integer);
                    }
                });
    }


    /**
     * .doOnSubscribe()-2
     * => main.doOnSubscribe()-1
     * => doOnSubscribe1之后的subscribeOnOnSubscribe.call()
     * => create之后的subscribeOn.onNext()
     * => create之后的subscribeOnRxJava-onNext
     */
    public void test1() {
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        threadInfo("OnSubscribe.call()");
                        subscriber.onNext("RxJava");
                    }
                })
                .subscribeOn(getNamedScheduler("create之后的subscribeOn"))
                .doOnSubscribe(() -> threadInfo(".doOnSubscribe()-1"))
                .subscribeOn(getNamedScheduler("doOnSubscribe1之后的subscribeOn"))
                .doOnSubscribe(() -> threadInfo(".doOnSubscribe()-2"))
                .subscribe(s -> {
                    threadInfo(".onNext()");
                    System.out.println(s + "-onNext");
                });
    }

    private void threadInfo(String content) {
        System.out.println(content);
    }

    private Scheduler getNamedScheduler(String content) {
        System.out.println(content);
        return Schedulers.io();
    }
}
