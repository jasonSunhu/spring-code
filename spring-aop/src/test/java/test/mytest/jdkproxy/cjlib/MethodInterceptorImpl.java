package test.mytest.jdkproxy.cjlib;

import java.lang.reflect.Method;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.cglib.proxy.MethodProxy;

/***
 *
 * @author Jshu
 * @since 2021/5/24 14:04
 */
public class MethodInterceptorImpl implements MethodInterceptor {

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
    System.out.println("Before invoke "+method);
    Object result = proxy.invokeSuper(obj, args);
    System.out.println("After invoke "+method);
    return result;
  }
}
