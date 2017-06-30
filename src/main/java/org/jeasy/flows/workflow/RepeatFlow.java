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

import org.jeasy.flows.work.NoOpWork;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.work.WorkReport;

import java.util.UUID;

/**
 * A repeat flow executes a work repeatedly until its report is satisfied by a given predicate.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class RepeatFlow extends AbstractWorkFlow {

    private Work work;
    private WorkReportPredicate predicate;

    RepeatFlow(String name, Work work, WorkReportPredicate predicate) {
        super(name);
        this.work = work;
        this.predicate = predicate;
    }

    /**
     * {@inheritDoc}
     */
    public WorkReport call() {
        WorkReport workReport;
        do {
            workReport = work.call();
        } while (predicate.apply(workReport));
        return workReport;
    }

    public static class Builder {

        private String name;
        private Work work;
        private WorkReportPredicate predicate;

        private Builder() {
            this.name = UUID.randomUUID().toString();
            this.work = new NoOpWork();
            this.predicate = WorkReportPredicate.ALWAYS_FALSE;
        }

        public static RepeatFlow.Builder aNewRepeatFlow() {
            return new Builder();
        }

        public RepeatFlow.Builder named(String name) {
            this.name = name;
            return this;
        }

        public RepeatFlow.Builder repeat(Work work) {
            this.work = work;
            return this;
        }

        public RepeatFlow.Builder times(int times) {
            return until(WorkReportPredicate.TimesPredicate.times(times));
        }

        public RepeatFlow.Builder until(WorkReportPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public RepeatFlow build() {
            return new RepeatFlow(name, work, predicate);
        }
    }
}
