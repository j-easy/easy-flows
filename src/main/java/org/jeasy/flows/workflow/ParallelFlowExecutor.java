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

import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

class ParallelFlowExecutor {

    private static final Logger LOGGER = Logger.getLogger(ParallelFlowExecutor.class.getName());

    /*
     * TODO Making the executor configurable requires to answer the following questions first:
     *
     * 1. If the user provides a custom executor, when should it be shutdown? -> Could be documented so the user shuts it down himself
     * 2. If the user provides a custom executor which is shared by multiple parallel flow, shutting it down here (as currently done) may impact other flows
     * 3. If it is decided to shut down the executor at the end of the parallel flow, the parallel flow could not be re-run (in a repeat flow for example) since the executor will be in an illegal state
     */
    private ExecutorService workExecutor;

    ParallelFlowExecutor() {
        this.workExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());;
    }

    List<WorkReport> executeInParallel(List<Work> works) {
        // re-init in case it has been shut down in a previous run (See question 3)
        if(workExecutor.isShutdown()) {
            workExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        }

        // submit works to be executed in parallel
        Map<Work, Future<WorkReport>> reportFutures = new HashMap<>();
        for (Work work : works) {
            Future<WorkReport> reportFuture = workExecutor.submit(work);
            reportFutures.put(work, reportFuture);
        }

        // poll for work completion
        int finishedWorks = works.size();
        // FIXME polling futures for completion, not sure this is the best way to run callables in parallel and wait them for completion (use CompletionService??)
        while (finishedWorks > 0) {
            for (Future<WorkReport> future : reportFutures.values()) {
                if (future != null && future.isDone()) {
                        finishedWorks--;
                }
            }
        }

        // gather reports
        List<WorkReport> workReports = new ArrayList<>();
        for (Map.Entry<Work, Future<WorkReport>> entry : reportFutures.entrySet()) {
            try {
                workReports.add(entry.getValue().get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.log(Level.WARNING, "Unable to get work report of work ''{0}''", entry.getKey().getName());
            }
        }

        workExecutor.shutdown(); // because if not, the workflow engine may run forever.. (See question 2).
        return workReports;
    }
}
