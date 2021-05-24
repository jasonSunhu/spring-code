package test.mytest.jdkproxy.cjlib;

import org.mockito.cglib.proxy.Enhancer;

/***
 * TODO
 * @author Jshu
 * @since 2021/5/24 14:06
 */
public class EnhancerDemo {

  public static void main(String[] args) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(EnhancerDemo.class);
    enhancer.setCallback(new MethodInterceptorImpl());

    EnhancerDemo demo = (EnhancerDemo) enhancer.create();
    demo.test();
    System.out.println(demo);
  }

  public void test() {
    System.out.println("EnhancerDemo test()");
  }
}
