/**
 * All rights Reserved, Designed By www.didichuxing.com.
 * 
 * @Title: t.java <\br>
 * @Package org.jeasy.flows.workflow <\br>
 * @Description: TODO(用一句话描述该文件做什么)<\br>
 * @author: zyt <\br>
 * @date: 2018年12月13日 下午5:28:10 <\br>
 * @version V1.0 <\br>
 * @Copyright: 2018 www.didichuxing Inc. All rights reserved.<\br>
 */

package org.jeasy.flows.workflow;

import java.util.List;
import org.assertj.core.util.Maps;
import org.jeasy.flows.work.DefaultWorkReport;
import org.jeasy.flows.work.Work;
import org.jeasy.flows.work.WorkReport;
import org.jeasy.flows.work.WorkStatus;

/**
 * @ClassName: t .
 * @Description:TODO(这里用一句话描述这个类的作用) <\br>
 * @author: zyt <\br>
 * @date: 2018年12月13日 下午5:28:10 <\br>
 * 
 */
public class HelloWorldWork implements Work<String> {

  private String name;
  private WorkStatus status;
  private boolean executed;

  HelloWorldWork(String name, WorkStatus status) {
    this.name = name;
    this.status = status;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public WorkReport<String> call(List param) {
    executed = true;
    DefaultWorkReport<String> dwr = new DefaultWorkReport<String>(status);
    dwr.setCollectData(Maps.newHashMap(name, "hello"));
    return dwr;
  }

  public boolean isExecuted() {
    return executed;
  }
}
