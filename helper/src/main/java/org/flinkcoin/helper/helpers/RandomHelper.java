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
package org.flinkcoin.helper.helpers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class RandomHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RandomHelper.class);

    public static SecureRandom get() {
        SecureRandom instance = null;
        try {
            instance = SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("Stange error: {}!", ex);
        }
        return instance;
    }
}
