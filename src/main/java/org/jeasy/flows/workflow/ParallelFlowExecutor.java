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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class ParallelFlowExecutor {

    private final ExecutorService workExecutor;

    ParallelFlowExecutor(ExecutorService workExecutor) {
        this.workExecutor = workExecutor;
    }

    List<WorkReport> executeInParallel(List<Work> workUnits, WorkContext workContext) {
        // prepare tasks for parallel submission
        List<Callable<WorkReport>> tasks = new ArrayList<>(workUnits.size());
        workUnits.forEach(work -> tasks.add(() -> work.execute(workContext)));

        // submit work units and wait for results
        List<Future<WorkReport>> futures;
        try {
            futures = this.workExecutor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException("The parallel flow was interrupted while executing work units", e);
        }
        Map<Work, Future<WorkReport>> workToReportFuturesMap = new HashMap<>();
        for (int index = 0; index < workUnits.size(); index++) {
            workToReportFuturesMap.put(workUnits.get(index), futures.get(index));
        }

        // gather reports
        List<WorkReport> workReports = new ArrayList<>();
        for (Map.Entry<Work, Future<WorkReport>> entry : workToReportFuturesMap.entrySet()) {
            try {
                workReports.add(entry.getValue().get());
            } catch (InterruptedException e) {
                String message = String.format("The parallel flow was interrupted while waiting for the result of work unit '%s'", entry.getKey().getName());
                throw new RuntimeException(message, e);
            } catch (ExecutionException e) {
                String message = String.format("Unable to execute work unit '%s'", entry.getKey().getName());
                throw new RuntimeException(message, e);
            }
        }

        return workReports;
    }
}
