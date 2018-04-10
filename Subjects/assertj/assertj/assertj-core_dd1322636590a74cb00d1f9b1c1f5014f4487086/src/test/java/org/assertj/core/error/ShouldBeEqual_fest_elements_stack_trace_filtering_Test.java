/*
 * Created on Sep 16, 2007
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
 * Copyright @2007-2011 the original author or authors.
 */
package org.assertj.core.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.StackTraceUtils.hasStackTraceElementRelatedToFest;


import org.assertj.core.api.Fail;
import org.assertj.core.error.ShouldBeEqual;
import org.junit.Test;


/**
 * Tests for <code>{@link ShouldBeEqual}</code> related to AssertionError stack trace filtering.
 * 
 * @author Joel Costigliola
 */
public class ShouldBeEqual_fest_elements_stack_trace_filtering_Test {

  @Test
  public void fest_elements_should_be_removed_from_assertion_error_stack_trace() {
    Fail.setRemoveAssertJRelatedElementsFromStackTrace(true);
    try {
      assertThat("Xavi").isEqualTo("Xabi");
    } catch (AssertionError assertionError) {
      assertThat(hasStackTraceElementRelatedToFest(assertionError)).isFalse();
    }
  }

  @Test
  public void fest_elements_should_be_kept_in_assertion_error_stack_trace() {
    Fail.setRemoveAssertJRelatedElementsFromStackTrace(false);
    try {
      assertThat("Messi").isEqualTo("Ronaldo");
    } catch (AssertionError assertionError) {
      assertThat(hasStackTraceElementRelatedToFest(assertionError)).isTrue();
    }
  }

}
