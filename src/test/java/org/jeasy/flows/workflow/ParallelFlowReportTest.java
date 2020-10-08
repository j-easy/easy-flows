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

import org.assertj.core.api.Assertions;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.WorkContext;
import org.jeasy.flows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;

public class ParallelFlowReportTest {

	private Exception exception;
	private ParallelFlowReport parallelFlowReport;

	@Before
	public void setUp() {
		exception = new Exception("test exception");
		WorkContext workContext = new WorkContext();
		parallelFlowReport = new ParallelFlowReport();
		parallelFlowReport.add(new DefaultWorkReport(WorkStatus.FAILED, workContext, exception));
		parallelFlowReport.add(new DefaultWorkReport(WorkStatus.COMPLETED, workContext));
	}

	@Test
	public void testGetStatus() {
		Assertions.assertThat(parallelFlowReport.getStatus()).isEqualTo(WorkStatus.FAILED);
	}

	@Test
	public void testGetError() {
		Assertions.assertThat(parallelFlowReport.getError()).isEqualTo(exception);
	}

	@Test
	public void testGetReports() {
		Assertions.assertThat(parallelFlowReport.getReports()).hasSize(2);
	}
}
