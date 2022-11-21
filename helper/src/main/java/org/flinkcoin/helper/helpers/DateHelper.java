package org.flinkcoin.helper.helpers;

/*-
 * #%L
 * Flink - Helper
 * %%
 * Copyright (C) 2021 - 2022 Flink Foundation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.ZonedDateTime;
import static java.time.ZonedDateTime.now;
import java.util.Date;

public class DateHelper {

    public static Date dateNow() {
        return Date.from(now().toInstant());
    }

    public static Date toDate(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.toInstant());
    }

    public static long toMillis(ZonedDateTime dateTime) {
        return dateTime.toInstant().toEpochMilli();

    }
}
