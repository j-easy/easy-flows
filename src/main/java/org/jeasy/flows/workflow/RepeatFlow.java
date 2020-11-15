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

import org.jeasy.flows.work.NoOpWork;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkReportPredicate;
import org.jeasy.flows.work.WorkReport;

import java.util.UUID;

/**
 * A repeat flow executes a work repeatedly until its report satisfies a given predicate.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class RepeatFlow extends AbstractWorkFlow {

    private final Work work;
    private final WorkReportPredicate predicate;

    RepeatFlow(String name, Work work, WorkReportPredicate predicate) {
        super(name);
        this.work = work;
        this.predicate = predicate;
    }

    /**
     * {@inheritDoc}
     */
    public WorkReport execute(WorkContext workContext) {
        WorkReport workReport;
        do {
            workReport = work.execute(workContext);
        } while (predicate.apply(workReport));
        return workReport;
    }

    public static class Builder {

        private Builder() {
            // force usage of static method aNewRepeatFlow
        }

        public static NameStep aNewRepeatFlow() {
            return new BuildSteps();
        }

        public interface NameStep extends RepeatStep {
            RepeatStep named(String name);
        }

        public interface RepeatStep {
            UntilStep repeat(Work work);
        }

        public interface UntilStep {
            BuildStep until(WorkReportPredicate predicate);
            BuildStep times(int times);
        }

        public interface BuildStep {
            RepeatFlow build();
        }

        private static class BuildSteps implements NameStep, RepeatStep, UntilStep, BuildStep {

            private String name;
            private Work work;
            private WorkReportPredicate predicate;

            BuildSteps() {
                this.name = UUID.randomUUID().toString();
                this.work = new NoOpWork();
                this.predicate = WorkReportPredicate.ALWAYS_FALSE;
            }
            
            @Override
            public RepeatStep named(String name) {
                this.name = name;
                return this;
            }

            @Override
            public UntilStep repeat(Work work) {
                this.work = work;
                return this;
            }

            @Override
            public BuildStep until(WorkReportPredicate predicate) {
                this.predicate = predicate;
                return this;
            }

            @Override
            public BuildStep times(int times) {
                until(WorkReportPredicate.TimesPredicate.times(times));
                return this;
            }

            @Override
            public RepeatFlow build() {
                return new RepeatFlow(name, work, predicate);
            }
        }

    }
}
