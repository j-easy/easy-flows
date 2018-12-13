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

import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;
import org.junit.Test;

public class ParallelFlowExecutorTest {

  @Test
  public void call() throws Exception {

    // given
    HelloWorldWork work1 = new HelloWorldWork("work1", WorkStatus.COMPLETED);
    HelloWorldWork work2 = new HelloWorldWork("work2", WorkStatus.FAILED);
    ParallelFlowExecutor parallelFlowExecutor = new ParallelFlowExecutor();

    // when
    List<WorkReport> workReports =
        parallelFlowExecutor.executeInParallel(Arrays.asList(work1, work2));

    // then
    Assertions.assertThat(workReports).hasSize(2);
    Assertions.assertThat(work1.isExecuted()).isTrue();
    Assertions.assertThat(work2.isExecuted()).isTrue();
  }



}
