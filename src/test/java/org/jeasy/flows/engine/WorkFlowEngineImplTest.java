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
package org.jeasy.flows.engine;

import static org.jeasy.flows.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static org.jeasy.flows.work.WorkReportPredicate.COMPLETED;
import static org.jeasy.flows.workflow.ConditionalFlow.Builder.aNewConditionalFlow;
import static org.jeasy.flows.workflow.ParallelFlow.Builder.aNewParallelFlow;
import static org.jeasy.flows.workflow.RepeatFlow.Builder.aNewRepeatFlow;
import static org.jeasy.flows.workflow.SequentialFlow.Builder.aNewSequentialFlow;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.util.Maps;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;
import org.jeasy.flows.workflow.ConditionalFlow;
import org.jeasy.flows.workflow.ParallelFlow;
import org.jeasy.flows.workflow.RepeatFlow;
import org.jeasy.flows.workflow.SequentialFlow;
import org.jeasy.flows.workflow.WorkFlow;
import org.junit.Test;
import org.mockito.Mockito;

public class WorkFlowEngineImplTest {

  public static AtomicInteger ai = new AtomicInteger(0);
  private WorkFlowEngine workFlowEngine = new WorkFlowEngineImpl();

  @Test
  public void run() throws Exception {
    // given
    WorkFlow workFlow = Mockito.mock(WorkFlow.class);

    // when
    workFlowEngine.run(workFlow, null);

    // then
    Mockito.verify(workFlow).call(null, null);
  }

  /**
   * The following tests are not really unit tests, but serve as examples of how to create a
   * workflow and execute it
   */

  @Test
  public void composeWorkFlowFromSeparateFlowsAndExecuteIt() throws Exception {

    PrintMessageWork work1 = new PrintMessageWork("foo");
    PrintMessageWork work2 = new PrintMessageWork("hello");
    PrintMessageWork work3 = new PrintMessageWork("world");
    PrintMessageWork work4 = new PrintMessageWork("done");
    PrintMessageWork work5 = new PrintMessageWork("beijing");

    RepeatFlow repeatFlow =
        aNewRepeatFlow().named("print foo 3 times").repeat(work1).times(3).build();

    ParallelFlow parallelFlow = aNewParallelFlow().named("print 'hello' and 'world' in parallel")
        .execute(work2, work3).build();

    SequentialFlow delayFlow = aNewSequentialFlow().execute(work5).then(work4).build();

    ConditionalFlow conditionalFlow =
        aNewConditionalFlow().execute(parallelFlow).when(COMPLETED).then(delayFlow).build();

    SequentialFlow sequentialFlow =
        aNewSequentialFlow().execute(repeatFlow).then(conditionalFlow).build();

    WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
    WorkReport workReport = workFlowEngine.run(sequentialFlow, null);
    System.out.println("workflow report = " + workReport);
  }

  // @Test
  // public void defineWorkFlowInlineAndExecuteIt() throws Exception {
  //
  // PrintMessageWork work1 = new PrintMessageWork("foo");
  // PrintMessageWork work2 = new PrintMessageWork("hello");
  // PrintMessageWork work3 = new PrintMessageWork("world");
  // PrintMessageWork work4 = new PrintMessageWork("done");
  //
  // WorkFlow workflow = aNewSequentialFlow()
  // .execute(aNewRepeatFlow().named("print foo 3 times").repeat(work1).times(3).build())
  // .then(aNewConditionalFlow().execute(aNewParallelFlow()
  // .named("print 'hello' and 'world' in parallel").execute(work2, work3).build())
  // .when(COMPLETED).then(work4).build())
  // .build();
  //
  // WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
  // WorkReport workReport = workFlowEngine.run(workflow);
  // System.out.println("workflow report = " + workReport);
  // }

  static class PrintMessageWork implements Work<String> {


    private String name;

    public PrintMessageWork(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public WorkReport call(List param, WorkContext context) {
      System.out.println("---------------new work---------------");
      if (param != null) {
        System.out.println("*********pre work data start **********");
        param.forEach(t -> {
          WorkReport wr = (WorkReport) t;
          System.out.println(wr.getCollectData());
        });
        System.out.println("*********pre work data end **********");

      }
      System.out.println(name);
      DefaultWorkReport dwr = new DefaultWorkReport(WorkStatus.COMPLETED);
      dwr.setCollectData(Maps.newHashMap(getName(), "haha data" + ai.addAndGet(1)));

      return dwr;
    }



  }
}
