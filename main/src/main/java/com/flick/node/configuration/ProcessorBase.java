package com.flick.node.configuration;

import io.reactivex.rxjava3.processors.PublishProcessor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessorBase<T> implements Subscriber<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorBase.class);

    protected final PublishProcessor<T> publishProcessor;
    private Subscription subscription;

    public ProcessorBase(PublishProcessor<T> publishProcessor) {
        this.publishProcessor = publishProcessor;
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        subscription.request(1);
        LOGGER.debug("On subscribe!");
    }

    public abstract void process(T data);

    @Override
    public void onNext(T data) {
        try {
            process(data);
        } catch (Exception ex) {
            LOGGER.error("Something wrong: ", ex);
        } finally {
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable thrwbl) {
        LOGGER.debug("On error!", thrwbl);
    }

    @Override
    public void onComplete() {
        LOGGER.debug("On complete!");
    }
}
