/*
 * Created on Mar 17, 2012
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
 * Copyright @2012 the original author or authors.
 */
package org.assertj.core.internal.objectarrays;

import static org.assertj.core.error.ElementsShouldNotBeAtLeast.elementsShouldNotBeAtLeast;
import static org.assertj.core.test.TestData.someInfo;
import static org.assertj.core.test.TestFailures.failBecauseExpectedAssertionErrorWasNotThrown;
import static org.assertj.core.util.Arrays.array;


import static org.mockito.Mockito.verify;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Condition;
import org.assertj.core.internal.ObjectArrays;
import org.assertj.core.internal.ObjectArraysWithConditionBaseTest;
import org.junit.Test;


/**
 * Tests for <code>{@link ObjectArrays#assertAreNotAtLeast(AssertionInfo, Object[], Condition, int)}</code> .
 * 
 * @author Nicolas François
 * @author Mikhail Mazursky
 */
public class ObjectArrays_assertAreNotAtLeast_Test extends ObjectArraysWithConditionBaseTest {

  @Test
  public void should_pass_if_not_satisfies_at_least_times_condition() {
    actual = array("Yoda", "Solo", "Leia");
    arrays.assertAreNotAtLeast(someInfo(), actual, 2, jedi);
    verify(conditions).assertIsNotNull(jedi);
  }

  @Test
  public void should_pass_if_never_satisfies_condition_() {
    actual = array("Leia", "Chewbacca", "Solo");
    arrays.assertAreNotAtLeast(someInfo(), actual, 2, jedi);
    verify(conditions).assertIsNotNull(jedi);
  }

  @Test
  public void should_throw_error_if_condition_is_null() {
    thrown.expectNullPointerException("The condition to evaluate should not be null");
    actual = array("Yoda", "Luke");
    arrays.assertAreNotAtLeast(someInfo(), actual, 2, null);
    verify(conditions).assertIsNotNull(null);
  }

  @Test
  public void should_fail_if_condition_is_not_met_enought() {
    testCondition.shouldMatch(false);
    AssertionInfo info = someInfo();
    try {
      actual = array("Yoda", "Luke", "Obiwan");
      arrays.assertAreNotAtLeast(someInfo(), actual, 2, jedi);
    } catch (AssertionError e) {
      verify(conditions).assertIsNotNull(jedi);
      verify(failures).failure(info, elementsShouldNotBeAtLeast(actual, 2, jedi));
      return;
    }
    failBecauseExpectedAssertionErrorWasNotThrown();
  }

}
