package mytest.lookup;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/***
 *
 * @author Jshu
 * @since 2021/3/10 21:15
 */
public class LookupTests {

  @Test
  public void test() {
    XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("lookupTest.xml"));
    GetBeanTest test = (GetBeanTest) beanFactory.getBean("getBeanTest");
    test.showMe();
  }
}
