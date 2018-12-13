/**
 * The MIT License
 *
 * Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.jeasy.flows.workflow;

import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReportPredicate;
import org.junit.Test;
import org.mockito.Mockito;

public class ConditionalFlowTest {

  @Test
  public void callOnPredicateSuccess() {
    // given
    Work toExecute = Mockito.mock(Work.class);
    Work nextOnPredicateSuccess = Mockito.mock(Work.class);
    Work nextOnPredicateFailure = Mockito.mock(Work.class);
    WorkReportPredicate predicate = WorkReportPredicate.ALWAYS_TRUE;
    ConditionalFlow conditionalFlow =
        ConditionalFlow.Builder.aNewConditionalFlow().execute(toExecute).when(predicate)
            .then(nextOnPredicateSuccess).otherwise(nextOnPredicateFailure).build();

    // when
    conditionalFlow.call(null);

    // then
    Mockito.verify(toExecute, Mockito.times(1)).call(null);
    Mockito.verify(nextOnPredicateSuccess, Mockito.times(1)).call(null);
    Mockito.verify(nextOnPredicateFailure, Mockito.never()).call(null);
  }

  @Test
  public void callOnPredicateFailure() {
    // given
    Work toExecute = Mockito.mock(Work.class);
    Work nextOnPredicateSuccess = Mockito.mock(Work.class);
    Work nextOnPredicateFailure = Mockito.mock(Work.class);
    WorkReportPredicate predicate = WorkReportPredicate.ALWAYS_FALSE;
    ConditionalFlow conditionalFlow =
        ConditionalFlow.Builder.aNewConditionalFlow().execute(toExecute).when(predicate)
            .then(nextOnPredicateSuccess).otherwise(nextOnPredicateFailure).build();

    // when
    conditionalFlow.call(null);

    // then
    Mockito.verify(toExecute, Mockito.times(1)).call(null);
    Mockito.verify(nextOnPredicateFailure, Mockito.times(1)).call(null);
    Mockito.verify(nextOnPredicateSuccess, Mockito.never()).call(null);
  }

}
