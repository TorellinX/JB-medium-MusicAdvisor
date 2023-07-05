package advisor.view;

import java.util.List;

public class View {

  public void printList(List<String> items, int page, int fromPages) {
    for (String item : items) {
      System.out.println(item);
    }
    System.out.printf("---PAGE %d OF %d---%n", page, fromPages);
  }

  public void printMessage(String msg) {
    System.out.println(msg);
  }

  public void printErrorMessage(String msg) {
    System.out.println(msg);
  }

}
