public class ImperativeBookLoader implements BookLoader {
    private final Metrics metrics;
    private final BookLoader oldLoader;
    private final BookLoader newLoader;

    public ImperativeBookLoader(final Metrics metrics, final BookLoader oldLoader, final BookLoader newLoader) {
        this.metrics = metrics;
        this.oldLoader = oldLoader;
        this.newLoader = newLoader;
    }

    public Book load() throws Exception {
        final Tuple<Book, Exception> oldLoaderResult = tryLoadFromLoader(oldLoader);
        final Tuple<Book, Exception> newLoaderResult = tryLoadFromLoader(newLoader);

        final Exception oldLoadError = oldLoaderResult.right;
        final Exception newLoadError = newLoaderResult.right;

        final boolean oldLoaderSuccessful = (oldLoadError == null);
        final boolean newLoaderSuccessful = (newLoadError == null);

        if (oldLoaderSuccessful && newLoaderSuccessful) {
            compareBooks(oldLoaderResult.left, newLoaderResult.left);

            return oldLoaderResult.left;
        }

        // one or more failures

        if (oldLoaderSuccessful) {
            metrics.incrementCompareFailedMetric();
            return oldLoaderResult.left;
        }

        throw oldLoadError;
    }

    private Tuple<Book, Exception> tryLoadFromLoader(final BookLoader loader) {
        try {
            return new Tuple<>(loader.load(), null);
        } catch (Exception e) {
            return new Tuple<>(null, e);
        }
    }

    private void compareBooks(final Book oldBook, final Book newBook) {
        if (oldBook.compareTo(newBook) != 0) {
            metrics.incrementCompareFailedMetric();
        }
    }
}