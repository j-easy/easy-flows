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
package org.jeasy.flows.work;

/**
 * Default implementation of {@link WorkReport}.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class DefaultWorkReport implements WorkReport {

    private final WorkStatus status;
    private final WorkContext workContext;
    private Throwable error;

    /**
     * Create a new {@link DefaultWorkReport}.
     *
     * @param status of work
     */
    public DefaultWorkReport(WorkStatus status, WorkContext workContext) {
        this.status = status;
        this.workContext = workContext;
    }

    /**
     * Create a new {@link DefaultWorkReport}.
     *
     * @param status of work
     * @param error if any
     */
    public DefaultWorkReport(WorkStatus status, WorkContext workContext, Throwable error) {
        this(status, workContext);
        this.error = error;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public WorkContext getWorkContext() {
        return workContext;
    }

    @Override
    public String toString() {
        return "DefaultWorkReport {" +
                "status=" + status +
                ", context=" + workContext +
                ", error=" + (error == null ? "''" : error) +
                '}';
    }
}
