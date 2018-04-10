/*
 * Created on Oct 18, 2010
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
package org.assertj.core.error;

/**
 * Creates an error message indicating that an assertion that verifies that a {@link Throwable} have certain message failed.
 * @author Joel Costigliola
 */
public class ShouldHaveMessage extends BasicErrorMessageFactory {

  /**
   * Creates a new </code>{@link ShouldHaveMessage}</code>.
   * @param actual the actual {@link Throwable} in the failed assertion.
   * @param expectedMessage the expected message of actual {@link Throwable}.
   * @return the created {@code ErrorMessageFactory}.
   */
  public static ErrorMessageFactory shouldHaveMessage(Throwable actual, String expectedMessage) {
    return new ShouldHaveMessage(actual, expectedMessage);
  }

  private ShouldHaveMessage(Throwable actual, String expectedMessage) {
    super("expected message:\n<%s>\n but was:\n<%s>", expectedMessage, actual.getMessage());
  }
}
