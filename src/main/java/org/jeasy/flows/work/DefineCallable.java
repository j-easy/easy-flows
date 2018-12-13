/**
 * All rights Reserved
 * 
 * @Title: Callable.java <\br>
 * @Package org.jeasy.flows.work <\br>
 * @Description: TODO(用一句话描述该文件做什么)<\br>
 * @author: zyt <\br>
 * @date: 2018年12月13日 下午7:04:36 <\br>
 * @version V1.0 <\br>
 * @Copyright: 2018 <\br>
 */

package org.jeasy.flows.work;

import java.util.List;

/**
 * @ClassName: Callable .
 * @Description:TODO(这里用一句话描述这个类的作用) <\br>
 * @author: zyt <\br>
 * @date: 2018年12月13日 下午7:04:36 <\br>
 * 
 */
public interface DefineCallable<T> {

  T call(List<T> param);
}
