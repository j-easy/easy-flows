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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;
import org.jeasy.flows.workflow.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jeasy.flows.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static org.jeasy.flows.work.WorkReportPredicate.COMPLETED;
import static org.jeasy.flows.workflow.ConditionalFlow.Builder.aNewConditionalFlow;
import static org.jeasy.flows.workflow.ParallelFlow.Builder.aNewParallelFlow;
import static org.jeasy.flows.workflow.RepeatFlow.Builder.aNewRepeatFlow;
import static org.jeasy.flows.workflow.SequentialFlow.Builder.aNewSequentialFlow;

public class WorkFlowEngineImplTest {

    private final WorkFlowEngine workFlowEngine = new WorkFlowEngineImpl();

    @Test
    public void run() {
        // given
        WorkFlow workFlow = Mockito.mock(WorkFlow.class);
        WorkContext workContext = Mockito.mock(WorkContext.class);

        // when
        workFlowEngine.run(workFlow,workContext);

        // then
        Mockito.verify(workFlow).call(workContext);
    }

    /**
     * The following tests are not really unit tests, but serve as examples of how to create a workflow and execute it
     */

    @Test
    public void composeWorkFlowFromSeparateFlowsAndExecuteIt() {

        PrintMessageWork work1 = new PrintMessageWork("foo");
        PrintMessageWork work2 = new PrintMessageWork("hello");
        PrintMessageWork work3 = new PrintMessageWork("world");
        PrintMessageWork work4 = new PrintMessageWork("done");

        RepeatFlow repeatFlow = aNewRepeatFlow()
                .named("print foo 3 times")
                .repeat(work1)
                .times(3)
                .build();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ParallelFlow parallelFlow = aNewParallelFlow()
                .named("print 'hello' and 'world' in parallel")
                .execute(work2, work3)
                .with(executorService)
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
        WorkContext workContext = new WorkContext();
        WorkReport workReport = workFlowEngine.run(sequentialFlow, workContext);
        executorService.shutdown();
        assertThat(workReport.getStatus()).isEqualTo(WorkStatus.COMPLETED);
        System.out.println("workflow report = " + workReport);
    }

    @Test
    public void defineWorkFlowInlineAndExecuteIt() {

        PrintMessageWork work1 = new PrintMessageWork("foo");
        PrintMessageWork work2 = new PrintMessageWork("hello");
        PrintMessageWork work3 = new PrintMessageWork("world");
        PrintMessageWork work4 = new PrintMessageWork("done");

        ExecutorService executorService = Executors.newFixedThreadPool(2);
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
                                    .with(executorService)
                                    .build())
                        .when(COMPLETED)
                        .then(work4)
                        .build())
                .build();

        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        WorkContext workContext = new WorkContext();
        WorkReport workReport = workFlowEngine.run(workflow, workContext);
        executorService.shutdown();
        assertThat(workReport.getStatus()).isEqualTo(WorkStatus.COMPLETED);
        System.out.println("workflow report = " + workReport);
    }

    @Test
    public void useWorkContextToPassInitialParametersAndShareDataBetweenWorkUnits() {
        WordCountWork work1 = new WordCountWork(1);
        WordCountWork work2 = new WordCountWork(2);
        AggregateWordCountsWork work3 = new AggregateWordCountsWork();
        PrintWordCount work4 = new PrintWordCount();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        WorkFlow workflow = aNewSequentialFlow()
                .execute(aNewParallelFlow()
                            .execute(work1, work2)
                            .with(executorService)
                            .build())
                .then(work3)
                .then(work4)
                .build();

        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        WorkContext workContext = new WorkContext();
        workContext.put("partition1", "hello foo");
        workContext.put("partition2", "hello bar");
        WorkReport workReport = workFlowEngine.run(workflow, workContext);
        executorService.shutdown();
        assertThat(workReport.getStatus()).isEqualTo(WorkStatus.COMPLETED);
    }

    static class PrintMessageWork implements Work {

        private String message;

        public PrintMessageWork(String message) {
            this.message = message;
        }

        public String getName() {
            return "print message work";
        }

        public WorkReport call(WorkContext workContext) {
            System.out.println(message);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        }

    }
    
    static class WordCountWork implements Work {

        private int partition;

        public WordCountWork(int partition) {
            this.partition = partition;
        }

        @Override
        public String getName() {
            return "count words in a given string";
        }

        @Override
        public WorkReport call(WorkContext workContext) {
            String input = (String) workContext.get("partition" + partition);
            workContext.put("wordCountInPartition" + partition, input.split(" ").length);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        }
    }
    
    static class AggregateWordCountsWork implements Work {

        @Override
        public String getName() {
            return "aggregate word counts from partitions";
        }

        @Override
        public WorkReport call(WorkContext workContext) {
            Set<Map.Entry<String, Object>> entrySet = workContext.getEntrySet();
            int sum = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                if (entry.getKey().contains("InPartition")) {
                    sum += (int) entry.getValue();
                }
            }
            workContext.put("totalCount", sum);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        }
    }

    static class PrintWordCount implements Work {

        @Override
        public String getName() {
            return "print total word count";
        }

        @Override
        public WorkReport call(WorkContext workContext) {
            int totalCount = (int) workContext.get("totalCount");
            System.out.println(totalCount);
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        }
    }
}
