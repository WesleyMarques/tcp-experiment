/*
 * Created on Dec 26, 2010
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
package org.assertj.core.internal.strings;

import static org.assertj.core.error.ShouldEndWith.shouldEndWith;
import static org.assertj.core.test.TestData.someInfo;
import static org.assertj.core.test.TestFailures.failBecauseExpectedAssertionErrorWasNotThrown;
import static org.assertj.core.util.FailureMessages.actualIsNull;


import static org.mockito.Mockito.verify;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.internal.Strings;
import org.assertj.core.internal.StringsBaseTest;
import org.junit.Test;


/**
 * Tests for <code>{@link Strings#assertEndsWith(AssertionInfo, String, String)}</code>.
 * 
 * @author Alex Ruiz
 * @author Joel Costigliola
 */
public class Strings_assertEndsWith_Test extends StringsBaseTest {

  @Test
  public void should_fail_if_actual_does_not_end_with_suffix() {
    AssertionInfo info = someInfo();
    try {
      strings.assertEndsWith(info, "Yoda", "Luke");
    } catch (AssertionError e) {
      verify(failures).failure(info, shouldEndWith("Yoda", "Luke"));
      return;
    }
    failBecauseExpectedAssertionErrorWasNotThrown();
  }

  @Test
  public void should_throw_error_if_suffix_is_null() {
    thrown.expectNullPointerException("The given suffix should not be null");
    strings.assertEndsWith(someInfo(), "Yoda", null);
  }

  @Test
  public void should_fail_if_actual_is_null() {
    thrown.expectAssertionError(actualIsNull());
    strings.assertEndsWith(someInfo(), null, "Yoda");
  }

  @Test
  public void should_pass_if_actual_ends_with_suffix() {
    strings.assertEndsWith(someInfo(), "Yoda", "oda");
  }

  @Test
  public void should_pass_if_actual_ends_with_suffix_according_to_custom_comparison_strategy() {
    stringsWithCaseInsensitiveComparisonStrategy.assertEndsWith(someInfo(), "Yoda", "oda");
    stringsWithCaseInsensitiveComparisonStrategy.assertEndsWith(someInfo(), "Yoda", "da");
    stringsWithCaseInsensitiveComparisonStrategy.assertEndsWith(someInfo(), "Yoda", "a");
    stringsWithCaseInsensitiveComparisonStrategy.assertEndsWith(someInfo(), "Yoda", "Oda");
    stringsWithCaseInsensitiveComparisonStrategy.assertEndsWith(someInfo(), "Yoda", "ODA");
  }

  @Test
  public void should_fail_if_actual_does_not_end_with_suffix_according_to_custom_comparison_strategy() {
    AssertionInfo info = someInfo();
    try {
      stringsWithCaseInsensitiveComparisonStrategy.assertEndsWith(info, "Yoda", "Luke");
    } catch (AssertionError e) {
      verify(failures).failure(info, shouldEndWith("Yoda", "Luke", comparisonStrategy));
      return;
    }
    failBecauseExpectedAssertionErrorWasNotThrown();
  }

}
