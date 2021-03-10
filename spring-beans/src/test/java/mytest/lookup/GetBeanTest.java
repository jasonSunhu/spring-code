package mytest.lookup;

/***
 *
 * @author Jshu
 * @since 2021/3/10 21:16
 */
public abstract class GetBeanTest {

  public void showMe() {
    this.getBean().showMe();
  }

  public abstract User getBean();
}
