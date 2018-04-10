/*
 * Created on Jan 29, 2011
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
 * Copyright @2011 the original author or authors.
 */
package org.assertj.core.internal.files;

import static org.assertj.core.error.ShouldBeDirectory.shouldBeDirectory;
import static org.assertj.core.test.TestData.someInfo;
import static org.assertj.core.test.TestFailures.failBecauseExpectedAssertionErrorWasNotThrown;
import static org.assertj.core.util.FailureMessages.actualIsNull;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.internal.Files;
import org.assertj.core.internal.FilesBaseTest;
import org.junit.Test;


/**
 * Tests for <code>{@link Files#assertIsDirectory(AssertionInfo, File)}</code>.
 * 
 * @author Yvonne Wang
 * @author Joel Costigliola
 */
public class Files_assertIsDirectory_Test extends FilesBaseTest {

  @Test
  public void should_fail_if_actual_is_null() {
    thrown.expectAssertionError(actualIsNull());
    files.assertIsDirectory(someInfo(), null);
  }

  @Test
  public void should_fail_if_actual_is_not_directory() {
    when(actual.isDirectory()).thenReturn(false);
    AssertionInfo info = someInfo();
    try {
      files.assertIsDirectory(info, actual);
    } catch (AssertionError e) {
      verify(failures).failure(info, shouldBeDirectory(actual));
      return;
    }
    failBecauseExpectedAssertionErrorWasNotThrown();
  }

  @Test
  public void should_pass_if_actual_is_directory() {
    when(actual.isDirectory()).thenReturn(true);
    files.assertIsDirectory(someInfo(), actual);
  }
}
