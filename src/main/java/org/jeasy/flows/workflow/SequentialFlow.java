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
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jeasy.flows.work.WorkStatus.FAILED;

/**
 * A sequential flow executes a set of work units in sequence.
 *
 * If a unit of work fails, next work units in the pipeline will be skipped.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class SequentialFlow extends AbstractWorkFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequentialFlow.class.getName());

    private final List<Work> workUnits = new ArrayList<>();

    SequentialFlow(String name, List<Work> workUnits) {
        super(name);
        this.workUnits.addAll(workUnits);
    }

    /**
     * {@inheritDoc}
     */
    public WorkReport execute(WorkContext workContext) {
        WorkReport workReport = null;
        for (Work work : workUnits) {
            workReport = work.execute(workContext);
            if (workReport != null && FAILED.equals(workReport.getStatus())) {
                LOGGER.info("Work unit ''{}'' has failed, skipping subsequent work units", work.getName());
                break;
            }
        }
        return workReport;
    }

    public static class Builder {

        private Builder() {
            // force usage of static method aNewSequentialFlow
        }

        public static NameStep aNewSequentialFlow() {
            return new BuildSteps();
        }

        public interface NameStep extends ExecuteStep {
            ExecuteStep named(String name);
        }

        public interface ExecuteStep {
            ThenStep execute(Work initialWork);
            ThenStep execute(List<Work> initialWorkUnits);
        }

        public interface ThenStep {
            ThenStep then(Work nextWork);
            ThenStep then(List<Work> nextWorkUnits);
            SequentialFlow build();
        }

        private static class BuildSteps implements NameStep, ExecuteStep, ThenStep {

            private String name;
            private final List<Work> works;
            
            BuildSteps() {
                this.name = UUID.randomUUID().toString();
                this.works = new ArrayList<>();
            }
            
            public ExecuteStep named(String name) {
                this.name = name;
                return this;
            }

            @Override
            public ThenStep execute(Work initialWork) {
                this.works.add(initialWork);
                return this;
            }

            @Override
            public ThenStep execute(List<Work> initialWorkUnits) {
                this.works.addAll(initialWorkUnits);
                return this;
            }

            @Override
            public ThenStep then(Work nextWork) {
                this.works.add(nextWork);
                return this;
            }

            @Override
            public ThenStep then(List<Work> nextWorkUnits) {
                this.works.addAll(nextWorkUnits);
                return this;
            }

            @Override
            public SequentialFlow build() {
                return new SequentialFlow(this.name, this.works);
            }
        }
    }
}
