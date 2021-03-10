package mytest;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/***
 *
 * @author Jshu
 * @since 2021/2/20 14:06
 */
public final class BeanFactoryTests {
  @Test
  public void testBean() {
    XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("beanFactory.xml"));
    MyTestBean myTestBean = beanFactory.getBean("myTestBean", MyTestBean.class);
    System.out.println(myTestBean.getTestStr());
  }
}
