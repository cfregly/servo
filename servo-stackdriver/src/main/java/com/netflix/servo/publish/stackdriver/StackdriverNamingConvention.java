/**
 * Copyright 2013 Stackdriver
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
package com.netflix.servo.publish.stackdriver;

import com.netflix.servo.Metric;

/**
 * Allows the developer to override the default Stackdriver naming convention to massage the objects into
 * the right shape for the Stackdriver backend.
 * 
 * @author cfregly@stackdriver.com
 */
public interface StackdriverNamingConvention {
    String getName(Metric metric);
}
