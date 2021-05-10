package mytest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/***
 *
 * @author Jshu
 * @since 2021/3/25 16:04
 */
public class ApplicationContextTests {

  public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("beanFactory.xml");
    TestBean testBean = (TestBean) context.getBean("testBean");
    System.out.println(testBean.getS());
  }

}
