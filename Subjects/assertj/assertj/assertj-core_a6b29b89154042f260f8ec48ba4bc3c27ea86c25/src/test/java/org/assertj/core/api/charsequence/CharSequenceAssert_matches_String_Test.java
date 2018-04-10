/*
 * Created on Dec 22, 2010
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
package org.assertj.core.api.charsequence;

import static org.assertj.core.test.TestData.matchAnything;
import static org.mockito.Mockito.verify;


import org.assertj.core.api.CharSequenceAssert;
import org.assertj.core.api.CharSequenceAssertBaseTest;
import org.junit.BeforeClass;

/**
 * Tests for <code>{@link CharSequenceAssert#matches(CharSequence)}</code>.
 * 
 * @author Alex Ruiz
 */
public class CharSequenceAssert_matches_String_Test extends CharSequenceAssertBaseTest {

  private static CharSequence regex;

  @BeforeClass
  public static void setUpOnce() {
    regex = matchAnything().pattern();
  }

  @Override
  protected CharSequenceAssert invoke_api_method() {
    return assertions.matches(regex);
  }

  @Override
  protected void verify_internal_effects() {
    verify(strings).assertMatches(getInfo(assertions), getActual(assertions), regex);
  }
}
