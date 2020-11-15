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

import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aggregate report of the partial reports of work units executed in a parallel flow.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class ParallelFlowReport implements WorkReport {

    private final List<WorkReport> reports;

    /**
     * Create a new {@link ParallelFlowReport}.
     */
    public ParallelFlowReport() {
        this(new ArrayList<>());
    }

    /**
     * Create a new {@link ParallelFlowReport}.
     * 
     * @param reports of works executed in parallel
     */
    public ParallelFlowReport(List<WorkReport> reports) {
        this.reports = reports;
    }

    /**
     * Get partial reports.
     *
     * @return partial reports
     */
    public List<WorkReport> getReports() {
        return reports;
    }

    void add(WorkReport workReport) {
        reports.add(workReport);
    }

    void addAll(List<WorkReport> workReports) {
        reports.addAll(workReports);
    }

    /**
     * Return the status of the parallel flow.
     *
     * The status of a parallel flow is defined as follows:
     *
     * <ul>
     *     <li>{@link org.jeasy.flows.work.WorkStatus#COMPLETED}: If all work units have successfully completed</li>
     *     <li>{@link org.jeasy.flows.work.WorkStatus#FAILED}: If one of the work units has failed</li>
     * </ul>
     * @return workflow status
     */
    @Override
    public WorkStatus getStatus() {
        for (WorkReport report : reports) {
            if (report.getStatus().equals(WorkStatus.FAILED)) {
                return WorkStatus.FAILED;
            }
        }
        return WorkStatus.COMPLETED;
    }

    /**
     * Return the first error of partial reports.
     *
     * @return the first error of partial reports.
     */
    @Override
    public Throwable getError() {
        for (WorkReport report : reports) {
            Throwable error = report.getError();
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    /**
     * The parallel flow context is the union of all partial contexts. In a parallel
     * flow, each work unit should have its own unique keys to avoid key overriding
     * when merging partial contexts.
     * 
     * @return the union of all partial contexts
     */
    @Override
    public WorkContext getWorkContext() {
        WorkContext workContext = new WorkContext();
        for (WorkReport report : reports) {
            WorkContext partialContext = report.getWorkContext();
            for (Map.Entry<String, Object> entry : partialContext.getEntrySet()) {
                workContext.put(entry.getKey(), entry.getValue());
            }
        }
        return workContext;
    }
}
