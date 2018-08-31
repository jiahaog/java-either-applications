public class Book implements Comparable<Book>{
    private final String title;

    public Book(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int compareTo(final Book o) {
        return title.compareTo(o.getTitle());
    }
}