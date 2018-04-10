/*
 * Created on Dec 20, 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * 
 * Copyright @2010-2011 the original author or authors.
 */
package org.assertj.core.api.floatarray;

import static org.assertj.core.test.TestData.someIndex;

import org.assertj.core.api.FloatArrayAssert;
import org.assertj.core.api.FloatArrayAssertBaseTest;
import org.assertj.core.data.Index;

import static org.mockito.Mockito.verify;


/**
 * Tests for <code>{@link FloatArrayAssert#contains(float, Index)}</code>.
 * 
 * @author Alex Ruiz
 */
public class FloatArrayAssert_contains_at_Index_Test extends FloatArrayAssertBaseTest {

  private final Index index = someIndex();

  @Override
  protected FloatArrayAssert invoke_api_method() {
    return assertions.contains(8f, index);
  }

  @Override
  protected void verify_internal_effects() {
    verify(arrays).assertContains(getInfo(assertions), getActual(assertions), 8f, index);
  }
}
