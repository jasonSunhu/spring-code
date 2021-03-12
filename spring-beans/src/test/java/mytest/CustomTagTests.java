package mytest;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import test.customtag.Company;

/***
 *
 * @author Jshu
 * @since 2021/3/12 10:44
 */
public class CustomTagTests {

  @Test
  public void testBean() {
    XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("beanFactory.xml"));
    Company company = beanFactory.getBean("company", Company.class);
    System.out.println(company);
  }
}
