/*
 * Copyright © 2021 Flink Foundation (info@flinkcoin.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flinkcoin.node.services;

import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.node.communication.CommonProcessor;
import org.flinkcoin.node.configuration.ProcessorBase;
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
