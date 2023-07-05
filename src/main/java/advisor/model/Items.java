package advisor.model;

import java.util.List;

public class Items {

  private static Items instance;
  private List<String> items;
  private int currentPage;
  private int lastPageIndex;

  private Items() {
  }

  public static Items getInstance() {
    if (instance == null) {
      instance = new Items();
    }
    return instance;
  }

  public List<String> getItems(int pageLimit, int page) {
    if (page > getLastPageIndex() || page < 1) {
      throw new IllegalArgumentException(String.format("Page index %d out of bounds [%d, %d]",
          page, 0, getLastPageIndex()));
    }
    currentPage = page;
    int fromIndex = (page - 1) * pageLimit;
    int toIndex = Math.min(fromIndex + pageLimit, items.size());
    return items.subList(fromIndex, toIndex);
  }

  public void setItems(List<String> items, int pageLimit) {
    this.items = items;
    currentPage = 1;
    lastPageIndex = (int) Math.ceil((double) items.size() / pageLimit);
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public int getLastPageIndex() {
    return lastPageIndex;
  }

}
