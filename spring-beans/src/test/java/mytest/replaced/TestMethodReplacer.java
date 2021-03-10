package mytest.replaced;

import java.lang.reflect.Method;
import org.springframework.beans.factory.support.MethodReplacer;

/***
 * TODO
 * @author Jshu
 * @since 2021/3/10 21:39
 */
public class TestMethodReplacer implements MethodReplacer {

  @Override
  public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
    System.out.println("i replaced");
    return null;
  }
}
