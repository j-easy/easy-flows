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
package org.jeasy.flows.workflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jeasy.flows.engine.WorkFlowEngine;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.jeasy.flows.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static org.jeasy.flows.workflow.SequentialFlow.Builder.aNewSequentialFlow;

public class SequentialFlowTest {

    @Test
    public void call() {
        // given
        Work work1 = Mockito.mock(Work.class);
        Work work2 = Mockito.mock(Work.class);
        Work work3 = Mockito.mock(Work.class);
        WorkContext workContext = Mockito.mock(WorkContext.class);
        SequentialFlow sequentialFlow = SequentialFlow.Builder.aNewSequentialFlow()
                .named("testFlow")
                .execute(work1)
                .then(work2)
                .then(work3)
                .build();

        // when
        sequentialFlow.call(workContext);

        // then
        InOrder inOrder = Mockito.inOrder(work1, work2, work3);
        inOrder.verify(work1, Mockito.times(1)).call(workContext);
        inOrder.verify(work2, Mockito.times(1)).call(workContext);
        inOrder.verify(work3, Mockito.times(1)).call(workContext);
    }

    @Test
    public void sequentialFlowInterruptionTest() throws InterruptedException {
        Work work1 = new FileCountingWork(Paths.get("/Users/mbenhassine/projects"));
        Work work2 = new FileCountingWork(Paths.get("/Users/mbenhassine/tools"));
        Work work3 = new FileCountingWork(Paths.get("/Users/mbenhassine/documents"));
        WorkFlow workflow = aNewSequentialFlow()
                .execute(work1)
                .then(work2)
                .then(work3)
                .build();

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine()
                .executorService(executorService)
                .build();
        WorkContext workContext = new WorkContext();
        Future<WorkReport> workReportFuture = workFlowEngine.submit(workflow, workContext);
        Thread.sleep(500); // prevent stopping the flow before even starting it
        workReportFuture.cancel(true); // I'm expecting the sequential flow to be interrupted after work1 or work2
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        
        // Another issue:
        // we can't get WorkReport after cancellation because workReportFuture.get
        // throws java.util.concurrent.CancellationException since it is cancelled..
        // So how to get the report to check its status and work context in this case?
    }
    
    static class FileCountingWork implements Work {

        private Path path;

        public FileCountingWork(Path path) {
            this.path = path;
        }

        @Override
        public WorkReport call(WorkContext workContext) {
            long count;
            try {
                count = Files.walk(path).count();
                System.out.println(Thread.currentThread().getName() +
                        ": There are " + count + " files in " + path.toAbsolutePath().toString());
                // can put count in workContext to get access to it from the report
            } catch (IOException e) {
                new DefaultWorkReport(WorkStatus.FAILED, workContext);
            }
            return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
        }
        
    }
}
