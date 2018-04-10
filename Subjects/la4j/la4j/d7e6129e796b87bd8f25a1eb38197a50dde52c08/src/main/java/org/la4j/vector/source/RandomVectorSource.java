/*
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 * 
 */

package org.la4j.vector.source;

import java.util.Random;

public class RandomVectorSource implements VectorSource {

    private int length;
    private Random random;

    public RandomVectorSource(int length) {
        this.length = length;
        this.random = new Random();
    }

    @Override
    public double get(int i) {
        return random.nextDouble();
    }

    @Override
    public int length() {
        return length;
    }
}
