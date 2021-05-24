package test.mytest.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/***
 *
 * @author Jshu
 * @since 2021/5/12 21:53
 */
public class MyInvocationHandler implements InvocationHandler {

  private Object target;

  public MyInvocationHandler(Object target) {
    super();
    this.target = target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println("=======befoer");
    Object result = method.invoke(target, args);
    System.out.println("===========after");
    return result;
  }

  public Object getProxy() {
    return Proxy
        .newProxyInstance(Thread.currentThread().getContextClassLoader(), target.getClass().getInterfaces(), this);
  }

  public static void main(String[] args) {
    MyInvocationHandler myInvocationHandler = new MyInvocationHandler(new UserServiceImpl());
    UserService proxy = (UserService) myInvocationHandler.getProxy();
    proxy.add();
  }
}
