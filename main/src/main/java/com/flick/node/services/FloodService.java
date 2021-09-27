package com.flick.node.services;

import com.flick.data.proto.communication.Message;
import com.flick.node.communication.CommonProcessor;
import com.flick.node.configuration.ProcessorBase;
import com.google.inject.Singleton;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FloodService extends ProcessorBase<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FloodService.class);

    private final Provider<CommonProcessor> commonHandler;

    @Inject
    public FloodService(Provider<CommonProcessor> commonHandler) {
        super(PublishProcessor.create());
        this.commonHandler = commonHandler;
        this.publishProcessor
                .onBackpressureBuffer(1000, () -> {
                }, BackpressureOverflowStrategy.DROP_LATEST)
                .observeOn(Schedulers.single())
                .subscribe(this);
    }

    public void newMessage(Message message) {
        publishProcessor.onNext(message);
    }

    @Override
    public void process(Message message) {
        commonHandler.get().flood(message);
    }

}
