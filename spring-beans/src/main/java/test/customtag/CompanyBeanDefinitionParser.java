package test.customtag;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/***
 *
 * @author Jshu
 * @since 2021/3/12 10:23
 */
public class CompanyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  //Element对应的类
  @Override
  protected Class getBeanClass(Element element) {
    return Company.class;
  }

  //从element中解析并提取对应的元素
  @Override
  protected void doParse(Element element, BeanDefinitionBuilder builder) {
    String companyName = element.getAttribute("companyName");
    String address = element.getAttribute("address");
    //将提取到的数据放入BeanDefinitionBuilder中，待完成所有的bean解析后统一注册到beanFactory中
    if (StringUtils.hasText(companyName)) {
      builder.addPropertyValue("companyName", companyName);
    }
    if (StringUtils.hasText(address)) {
      builder.addPropertyValue("address", address);
    }
  }
}
