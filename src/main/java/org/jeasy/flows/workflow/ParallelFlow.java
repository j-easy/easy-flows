/**
 * The MIT License
 *
 *  Copyright (c) 2017, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
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

import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A parallel flow executes a set of works in parallel.
 *
 * The status of a parallel flow is set to:
 *
 * <ul>
 *     <li>{@link org.jeasy.flows.work.WorkStatus#COMPLETED}: If all works have successfully completed</li>
 *     <li>{@link org.jeasy.flows.work.WorkStatus#FAILED}: If one of the works has failed</li>
 * </ul>
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class ParallelFlow extends AbstractWorkFlow {

    private List<Work> works = new ArrayList<>();
    private ExecutorService workExecutor;

    ParallelFlow(String name, List<Work> works) {
        super(name);
        this.works.addAll(works);
        this.workExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * <strong>Warning: If you use a custom executor service, it will be shutdown at the end of this parallel flow.</strong>
     * @param workExecutor to use to execute works in parallel
     */
    public void setWorkExecutor(ExecutorService workExecutor) {
        this.workExecutor = workExecutor;
    }

    /**
     * {@inheritDoc}
     */
    public ParallelFlowReport call() {
        ParallelFlowReport workFlowReport = new ParallelFlowReport();
        List<Future<WorkReport>> reportFutures = new ArrayList<Future<WorkReport>>();
        for (Work work : works) {
            Future<WorkReport> reportFuture = workExecutor.submit(work);
            reportFutures.add(reportFuture);
        }

        // poll for work completion
        int finishedWorks = works.size();
        // FIXME polling futures for completion, not sure this is the best way to run callables in parallel and wait them for completion (use CompletionService??)
        // TODO extract in a separate class that, given a list of works, run them in parallel and return their reports
        while (finishedWorks > 0) {
            for (Future<WorkReport> future : reportFutures) {
                if (future != null && future.isDone()) {
                    WorkReport workReport;
                    try {
                        workReport = future.get();
                        workFlowReport.add(workReport);
                        finishedWorks--;
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to run work", e); // TODO error message should mention which work has failed
                    }
                }
            }
        }
        // end poll for work completion

        workExecutor.shutdown();
        return workFlowReport;
    }

    public static class Builder {

        private String name;
        private List<Work> works;
        private ExecutorService workExecutor;

        private Builder() {
            this.name = UUID.randomUUID().toString();
            this.works = new ArrayList<>();
            this.workExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        }

        public static ParallelFlow.Builder aNewParallelFlow() {
            return new ParallelFlow.Builder();
        }

        public ParallelFlow.Builder named(String name) {
            this.name = name;
            return this;
        }

        public ParallelFlow.Builder execute(Work... works) {
            this.works.addAll(Arrays.asList(works));
            return this;
        }

        public ParallelFlow.Builder using(ExecutorService workExecutor) {
            this.workExecutor = workExecutor;
            return this;
        }

        public ParallelFlow build() {
            ParallelFlow parallelFlow = new ParallelFlow(name, works);
            parallelFlow.setWorkExecutor(this.workExecutor);
            return parallelFlow;
        }
    }
}
