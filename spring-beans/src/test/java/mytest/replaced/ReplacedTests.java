package mytest.replaced;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/***
 *
 * @author Jshu
 * @since 2021/3/10 21:15
 */
public class ReplacedTests {

  @Test
  public void test() {
    XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("replacedTest.xml"));
    TestChangeMethod test = (TestChangeMethod) beanFactory.getBean("testChangeMethod");
    test.changeMe();
  }
}
