package test.customtag;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/***
 *
 * @author Jshu
 * @since 2021/3/12 10:33
 */
public class CompanyNamespaceHandler extends NamespaceHandlerSupport {

  public void init() {
    registerBeanDefinitionParser("company", new CompanyBeanDefinitionParser());
  }
}
