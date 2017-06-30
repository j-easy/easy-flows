package org.jeasy.flows.workflow;

import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ParallelFlowExecutor {

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
        List<Future<WorkReport>> reportFutures = new ArrayList<>();
        for (Work work : works) {
            Future<WorkReport> reportFuture = workExecutor.submit(work);
            reportFutures.add(reportFuture);
        }

        // poll for work completion
        int finishedWorks = works.size();
        // FIXME polling futures for completion, not sure this is the best way to run callables in parallel and wait them for completion (use CompletionService??)
        while (finishedWorks > 0) {
            for (Future<WorkReport> future : reportFutures) {
                if (future != null && future.isDone()) {
                        finishedWorks--;
                }
            }
        }

        // gather reports
        List<WorkReport> workReports = new ArrayList<>();
        for (Future<WorkReport> reportFuture : reportFutures) {
            try {
                workReports.add(reportFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(); // TODO handle exception
            }
        }

        workExecutor.shutdown(); // because if not, the workflow engine may run forever.. (See question 2).
        return workReports;
    }
}
