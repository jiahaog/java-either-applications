import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(value = Parameterized.class)
public class BookLoaderTest {
    private final Class loaderClass;

    private BookLoader loader;
    private Metrics metrics;
    private BookLoader oldLoader;
    private BookLoader newLoader;
    private Book testBook;


    public BookLoaderTest(final Class klass) {
        loaderClass = klass;
    }

    @Parameterized.Parameters
    public static Collection parameters() {
        return Arrays.asList(ImperativeBookLoader.class, DeclarativeBookLoader.class);
    }

    @Before
    public void setUp() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        metrics = mock(Metrics.class);
        oldLoader = mock(BookLoader.class);
        newLoader = mock(BookLoader.class);

        Class[] classArg = new Class[]{
                Metrics.class,
                BookLoader.class,
                BookLoader.class,
        };

        loader = (BookLoader) loaderClass.getDeclaredConstructor(classArg).newInstance(metrics, oldLoader, newLoader);

        testBook = new Book("test book");
    }

    @Test
    public void loadBothSuccessful() throws Exception {
        when(oldLoader.load()).thenReturn(testBook);
        when(newLoader.load()).thenReturn(testBook);

        Book result = loader.load();
        assertEquals(testBook, result);
    }

    @Test
    public void loadBothSuccessfulDifferentBook() throws Exception {
        when(oldLoader.load()).thenReturn(testBook);
        when(newLoader.load()).thenReturn(new Book("Different book"));

        Book result = loader.load();
        assertEquals(testBook, result);

        verify(metrics).incrementCompareFailedMetric();
    }

    @Test
    public void loadOldLoaderSuccessfulNewLoaderFail() throws Exception {
        when(oldLoader.load()).thenReturn(testBook);
        when(newLoader.load()).thenThrow(new Exception());

        Book result = loader.load();
        assertEquals(testBook, result);

        verify(metrics).incrementCompareFailedMetric();
    }

    @Test(expected = Exception.class)
    public void loadOldLoaderFailNewLoaderSuccessful() throws Exception {
        when(oldLoader.load()).thenThrow(new Exception());
        when(newLoader.load()).thenReturn(testBook);

        loader.load();
    }

    @Test(expected = Exception.class)
    public void loadOldLoaderFailNewLoaderFail() throws Exception {
        when(oldLoader.load()).thenThrow(new Exception());
        when(newLoader.load()).thenThrow(new Exception());

        loader.load();
    }
}