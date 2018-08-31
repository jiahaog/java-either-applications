import io.vavr.control.Either;

public class DeclarativeBookLoader implements BookLoader {
    private final Metrics metrics;
    private final BookLoader oldLoader;
    private final BookLoader newLoader;

    public DeclarativeBookLoader(final Metrics metrics, final BookLoader oldLoader, final BookLoader newLoader) {
        this.metrics = metrics;
        this.oldLoader = oldLoader;
        this.newLoader = newLoader;
    }

    public Book load() throws Exception {
        final Either<Book, Exception> oldLoaderResult = tryLoadFromLoader(oldLoader);
        final Either<Book, Exception> newLoaderResult = tryLoadFromLoader(newLoader);

        final Either<Book, Exception> result = oldLoaderResult.fold(
                (oldBook) -> newLoaderResult.fold(
                        (newBook) -> {
                            compareBooks(oldBook, newBook);
                            return Either.left(oldBook);
                        },
                        (newLoaderError) -> {
                            metrics.incrementCompareFailedMetric();
                            return Either.left(oldBook);
                        }),
                (oldLoaderError) -> newLoaderResult.fold(
                        (newBook) -> Either.right(oldLoaderError),
                        (newLoaderError) -> Either.right(oldLoaderError)
                )
        );

        return result.left().getOrElseThrow((exception) -> exception);
    }

    private Either<Book, Exception> tryLoadFromLoader(final BookLoader loader) {
        try {
            return Either.left(loader.load());
        } catch (Exception e) {
            return Either.right(e);
        }
    }

    private void compareBooks(final Book oldBook, final Book newBook) {
        if (oldBook.compareTo(newBook) != 0) {
            metrics.incrementCompareFailedMetric();
        }
    }
}