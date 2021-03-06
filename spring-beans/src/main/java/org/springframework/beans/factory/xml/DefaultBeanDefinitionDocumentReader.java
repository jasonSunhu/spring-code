/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface. Reads bean
 * definitions according to the "spring-beans" DTD and XSD format (Spring's default XML bean
 * definition format).
 *
 * <p>The structure, elements and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary to produce this
 * format). {@code &lt;beans&gt;} doesn't need to be the root element of the XML document: This
 * class will parse all bean definition elements in the XML file, not regarding the actual root
 * element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

  public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

  public static final String NESTED_BEANS_ELEMENT = "beans";

  public static final String ALIAS_ELEMENT = "alias";

  public static final String NAME_ATTRIBUTE = "name";

  public static final String ALIAS_ATTRIBUTE = "alias";

  public static final String IMPORT_ELEMENT = "import";

  public static final String RESOURCE_ATTRIBUTE = "resource";

  public static final String PROFILE_ATTRIBUTE = "profile";


  protected final Log logger = LogFactory.getLog(getClass());

  private Environment environment;

  private XmlReaderContext readerContext;

  private BeanDefinitionParserDelegate delegate;


  @Deprecated
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * This implementation parses bean definitions according to the "spring-beans" XSD (or DTD,
   * historically).
   * <p>Opens a DOM Document; then initializes the default settings
   * specified at the {@code <beans/>} level; then parses the contained bean definitions.
   * 提取root,以便于再次将root作为参数继续BeanDefinition的注册
   */
  public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    this.readerContext = readerContext;
    logger.debug("Loading bean definitions");
    Element root = doc.getDocumentElement();
    //核心部分
    doRegisterBeanDefinitions(root);
  }

  /**
   * Return the descriptor for the XML resource that this parser works on.
   */
  protected final XmlReaderContext getReaderContext() {
    return this.readerContext;
  }

  /**
   * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor} to pull the source
   * metadata from the supplied {@link Element}.
   */
  protected Object extractSource(Element ele) {
    return getReaderContext().extractSource(ele);
  }

  private Environment getEnvironment() {
    return (this.environment != null ? this.environment
        : getReaderContext().getReader().getEnvironment());
  }


  /**
   * Register each bean definition within the given root {@code <beans/>} element.
   * 在给定的<beans/>标签中注册每个bean对象
   */
  protected void doRegisterBeanDefinitions(Element root) {
    //处理profile 即得到Environment中的profile对比<beans/>标签中的profile
    // 选择是否解析注册该<beans/>
    String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
    if (StringUtils.hasText(profileSpec)) {
      String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
          profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
      if (!getEnvironment().acceptsProfiles(specifiedProfiles)) {
        return;
      }
    }

    // Any nested <beans> elements will cause recursion in this method. In
    // order to propagate and preserve <beans> default-* attributes correctly,
    // keep track of the current (parent) delegate, which may be null. Create
    // the new (child) delegate with a reference to the parent for fallback purposes,
    // then ultimately reset this.delegate back to its original (parent) reference.
    // this behavior emulates a stack of delegates without actually necessitating one.
    // 专门处理解析的委托
    BeanDefinitionParserDelegate parent = this.delegate;
    this.delegate = createDelegate(this.readerContext, root, parent);

    //模板方法模式preProcessXml、postProcessXml交给子类实现
    preProcessXml(root);
    //核心逻辑 ->
    parseBeanDefinitions(root, this.delegate);

    postProcessXml(root);

    this.delegate = parent;
  }

  protected BeanDefinitionParserDelegate createDelegate(
      XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate) {

    BeanDefinitionParserDelegate delegate = createHelper(readerContext, root, parentDelegate);
    if (delegate == null) {
      delegate = new BeanDefinitionParserDelegate(readerContext, getEnvironment());
      delegate.initDefaults(root, parentDelegate);
    }
    return delegate;
  }

  @Deprecated
  protected BeanDefinitionParserDelegate createHelper(
      XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate) {

    return null;
  }

  /**
   * Parse the elements at the root level in the document: "import", "alias", "bean".
   *
   * @param root the DOM root element of the document 默认标签包括 {@link #IMPORT_ELEMENT}{@link
   *             #ALIAS_ELEMENT} {@link #BEAN_ELEMENT}{@link #NESTED_BEANS_ELEMENT}
   */
  protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    //判断是否为beans标签 然后对其处理
    if (delegate.isDefaultNamespace(root)) {
      NodeList nl = root.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        Node node = nl.item(i);
        if (node instanceof Element) {
          Element ele = (Element) node;
          //判断为beans标签下的默认标签并进行处理
          if (delegate.isDefaultNamespace(ele)) {
            // ->
            parseDefaultElement(ele, delegate);
          } else {
            //对beans标签下的非默认标签的解析 ->
            delegate.parseCustomElement(ele);
          }
        }
      }
    } else {
      //对非默认标签的解析
      delegate.parseCustomElement(root);
    }
  }

  private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    //3. import标签解析 ->
    if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
      importBeanDefinitionResource(ele);
    }
    //2. alias标签解析 ->
    else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
      processAliasRegistration(ele);
    }
    //1. bean标签解析 ->
    else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
      processBeanDefinition(ele, delegate);
    }
    //beans标签解析 ->
    else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
      // recurse 递归
      doRegisterBeanDefinitions(ele);
    }
  }

  /**
   * Parse an "import" element and load the bean definitions from the given resource into the bean
   * factory.
   */
  protected void importBeanDefinitionResource(Element ele) {
    //获取resource属性
    String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
    //不存在resource则抛异常
    if (!StringUtils.hasText(location)) {
      getReaderContext().error("Resource location must not be empty", ele);
      return;
    }

    // Resolve system properties: e.g. "${user.dir}"
    // 解析系统属性
    location = getEnvironment().resolveRequiredPlaceholders(location);

    Set<Resource> actualResources = new LinkedHashSet<Resource>(4);

    // Discover whether the location is an absolute or relative URI
    //判定location是绝对URI还是相对URI
    boolean absoluteLocation = false;
    try {
      absoluteLocation =
          ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
    } catch (URISyntaxException ex) {
      // cannot convert to an URI, considering the location relative
      // unless it is the well-known Spring prefix "classpath*:"
    }

    // Absolute or relative?
    // 如果是绝对URI则直接根据地址加载对应的配置文件
    if (absoluteLocation) {
      try {
        int importCount = getReaderContext().getReader()
            .loadBeanDefinitions(location, actualResources);
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Imported " + importCount + " bean definitions from URL location [" + location + "]");
        }
      } catch (BeanDefinitionStoreException ex) {
        getReaderContext().error(
            "Failed to import bean definitions from URL location [" + location + "]", ele, ex);
      }
    } else {
      // No URL -> considering resource location as relative to the current file.
      //如果是相对地址则根据相对地址计算出绝对地址
      try {
        int importCount;
        //Resource存在多个子实现类，每个createRelative方式的实现都不一样，所以这里先使用子类的方法尝试解析
        Resource relativeResource = getReaderContext().getResource().createRelative(location);
        if (relativeResource.exists()) {
          importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
          actualResources.add(relativeResource);
        } else {
          //如果解析不成功，则使用默认解析器ResourcePatternResolver进行解析
          String baseLocation = getReaderContext().getResource().getURL().toString();
          importCount = getReaderContext().getReader().loadBeanDefinitions(
              StringUtils.applyRelativePath(baseLocation, location), actualResources);
        }
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Imported " + importCount + " bean definitions from relative location [" + location
                  + "]");
        }
      } catch (IOException ex) {
        getReaderContext().error("Failed to resolve current resource location", ele, ex);
      } catch (BeanDefinitionStoreException ex) {
        getReaderContext()
            .error("Failed to import bean definitions from relative location [" + location + "]",
                ele, ex);
      }
    }
    Resource[] actResArray = actualResources.toArray(new Resource[actualResources.size()]);
    //解析后进行监听器激活处理
    getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
  }

  /**
   * Process the given alias element, registering the alias with the registry.
   */
  protected void processAliasRegistration(Element ele) {
    //获取beanName
    String name = ele.getAttribute(NAME_ATTRIBUTE);
    //获取alias
    String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
    boolean valid = true;
    if (!StringUtils.hasText(name)) {
      getReaderContext().error("Name must not be empty", ele);
      valid = false;
    }
    if (!StringUtils.hasText(alias)) {
      getReaderContext().error("Alias must not be empty", ele);
      valid = false;
    }
    if (valid) {
      try {
        //注册alias
        getReaderContext().getRegistry().registerAlias(name, alias);
      } catch (Exception ex) {
        getReaderContext().error("Failed to register alias '" + alias +
            "' for bean with name '" + name + "'", ele, ex);
      }
      //别名注册后通知监听器做相应处理
      getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
    }
  }

  /**
   * Process the given bean element, parsing the bean definition and registering it with the
   * registry. 1. 首先委托BeanDefinitionParserDelegate类的parseBeanDefinitionElement方法进行元素解析，
   * 返回BeanDefinitionHolder类型的实例bdHolder，经过这个方法后bdHolder实例中已经包含我们的配置文件中配置的
   * 各种属性了，如：class、name、id、alias之类的属性 2. 当返回的bdHolder不为空的情况下若存在默认标签的子节点下再有自定义属性，还需要再次对自定义标签进行解析 3.
   * 解析完成后，需要对解析后的bdHolder进行注册，同样，注册操作委托给了BeanDefinitionReaderUtils的 registerBeanDefinition方法。 4.
   * 最后发出响应事件，通知想关的监听器，这个bean已经加载完成了。
   */
  protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
    // 解析bean及其子默认标签属性 ->
    BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
    if (bdHolder != null) {
      // 解析bean其下的非默认标签 ->
      bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
      try {
        // Register the final decorated instance. ->
        // 注册解析的BeanDefinition ->
        BeanDefinitionReaderUtils
            .registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
      } catch (BeanDefinitionStoreException ex) {
        getReaderContext().error("Failed to register bean definition with name '" +
            bdHolder.getBeanName() + "'", ele, ex);
      }
      // Send registration event.
      // 这里的实现只是为了拓展，当程序开发人员需要对注册BeanDefinition事件进行监听时可以通过注册监听器的方式并将
      // 逻辑写入监听器，目前在spring中并没有对此事件做任何逻辑处理
      getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
    }
  }


  /**
   * Allow the XML to be extensible by processing any custom element types first, before we start to
   * process the bean definitions. This method is a natural extension point for any other custom
   * pre-processing of the XML.
   * <p>The default implementation is empty. Subclasses can override this method to
   * convert custom elements into standard Spring bean definitions, for example. Implementors have
   * access to the parser's bean definition reader and the underlying XML resource, through the
   * corresponding accessors.
   *
   * @see #getReaderContext()
   */
  protected void preProcessXml(Element root) {
  }

  /**
   * Allow the XML to be extensible by processing any custom element types last, after we finished
   * processing the bean definitions. This method is a natural extension point for any other custom
   * post-processing of the XML.
   * <p>The default implementation is empty. Subclasses can override this method to
   * convert custom elements into standard Spring bean definitions, for example. Implementors have
   * access to the parser's bean definition reader and the underlying XML resource, through the
   * corresponding accessors.
   *
   * @see #getReaderContext()
   */
  protected void postProcessXml(Element root) {
  }

}
