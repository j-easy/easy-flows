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
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

class ParallelFlowExecutor {

    private static final Logger LOGGER = Logger.getLogger(ParallelFlowExecutor.class.getName());

    private ExecutorService workExecutor;

    ParallelFlowExecutor(ExecutorService workExecutor) {
        this.workExecutor = workExecutor;
    }

    List<WorkReport> executeInParallel(List<Work> works, WorkContext workContext) {
        // submit work units to be executed in parallel
        Map<Work, Future<WorkReport>> reportFutures = new HashMap<>();

        CountDownLatch latch = new CountDownLatch(works.size());

        for (Work work : works) {
            Future<WorkReport> reportFuture = workExecutor.submit(() -> {
                try {
                    return work.call(workContext);
                } finally {
                    latch.countDown();
                }
            });
            reportFutures.put(work, reportFuture);
        }

        try {
            // wait for the work completion
            latch.await();
        } catch(InterruptedException e) {
            LOGGER.log(Level.WARNING, "Unable to complete all work units", e);
        }

        // gather reports
        List<WorkReport> workReports = new ArrayList<>();
        for (Map.Entry<Work, Future<WorkReport>> entry : reportFutures.entrySet()) {
            try {
                // If the CountDownLatch was interrupted not all work may have completed.
                if(entry.getValue().isDone()) {
                    workReports.add(entry.getValue().get());
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.log(Level.WARNING, "Unable to get report of work unit ''{0}''", entry.getKey().getName());
            }
        }

        return workReports;
    }
}
