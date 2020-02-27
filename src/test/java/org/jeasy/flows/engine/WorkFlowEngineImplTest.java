/*
 * The MIT License
 *
 *  Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.jeasy.flows.engine;

import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;
import org.jeasy.flows.workflow.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.jeasy.flows.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static org.jeasy.flows.work.WorkReportPredicate.COMPLETED;
import static org.jeasy.flows.workflow.ConditionalFlow.Builder.aNewConditionalFlow;
import static org.jeasy.flows.workflow.ParallelFlow.Builder.aNewParallelFlow;
import static org.jeasy.flows.workflow.RepeatFlow.Builder.aNewRepeatFlow;
import static org.jeasy.flows.workflow.SequentialFlow.Builder.aNewSequentialFlow;

public class WorkFlowEngineImplTest {

    private WorkFlowEngine workFlowEngine = new WorkFlowEngineImpl();

    @Test
    public void run() {
        // given
        WorkFlow workFlow = Mockito.mock(WorkFlow.class);

        // when
        workFlowEngine.run(workFlow);

        // then
        Mockito.verify(workFlow).call();
    }

    /**
     * The following tests are not really unit tests, but serve as examples of how to create a workflow and execute it
     */

    @Test
    public void composeWorkFlowFromSeparateFlowsAndExecuteIt() throws Exception {

        PrintMessageWork work1 = new PrintMessageWork("foo");
        PrintMessageWork work2 = new PrintMessageWork("hello");
        PrintMessageWork work3 = new PrintMessageWork("world");
        PrintMessageWork work4 = new PrintMessageWork("done");

        RepeatFlow repeatFlow = aNewRepeatFlow()
                .named("print foo 3 times")
                .repeat(work1)
                .times(3)
                .build();

        ParallelFlow parallelFlow = aNewParallelFlow()
                .named("print 'hello' and 'world' in parallel")
                .execute(work2, work3)
                .build();

        ConditionalFlow conditionalFlow = aNewConditionalFlow()
                .execute(parallelFlow)
                .when(COMPLETED)
                .then(work4)
                .build();

        SequentialFlow sequentialFlow = aNewSequentialFlow()
                .execute(repeatFlow)
                .then(conditionalFlow)
                .build();

        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        WorkReport workReport = workFlowEngine.run(sequentialFlow);
        System.out.println("workflow report = " + workReport);
    }

    @Test
    public void defineWorkFlowInlineAndExecuteIt() {

        PrintMessageWork work1 = new PrintMessageWork("foo");
        PrintMessageWork work2 = new PrintMessageWork("hello");
        PrintMessageWork work3 = new PrintMessageWork("world");
        PrintMessageWork work4 = new PrintMessageWork("done");

        WorkFlow workflow = aNewSequentialFlow()
                .execute(aNewRepeatFlow()
                            .named("print foo 3 times")
                            .repeat(work1)
                            .times(3)
                            .build())
                .then(aNewConditionalFlow()
                        .execute(aNewParallelFlow()
                                    .named("print 'hello' and 'world' in parallel")
                                    .execute(work2, work3)
                                    .build())
                        .when(COMPLETED)
                        .then(work4)
                        .build())
                .build();

        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        WorkReport workReport = workFlowEngine.run(workflow);
        System.out.println("workflow report = " + workReport);
    }

    static class PrintMessageWork implements Work {

        private String message;

        public PrintMessageWork(String message) {
            this.message = message;
        }

        public String getName() {
            return "print message work";
        }

        public WorkReport call() {
            System.out.println(message);
            return new DefaultWorkReport(WorkStatus.COMPLETED);
        }

    }
}
