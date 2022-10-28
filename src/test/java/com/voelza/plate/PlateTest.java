package com.voelza.plate;

import com.voelza.plate.view.View;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class PlateTest {

    private static String loadResultFile(final String testDir) throws IOException {
        return Files.readString(Paths.get(testDir)).replaceAll("\\r|\\n|\\t|\\s ", "");
    }

    public record Person(String name) {
    }

    private void test(final String testDir, final Model model) throws IOException {
        final AtomicInteger leastSigBit = new AtomicInteger(0);
        try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
            mocked.when(UUID::randomUUID).thenAnswer((invocation) -> {
                return new UUID(1, leastSigBit.incrementAndGet());
            });
            final View view = new View(testDir + "templates/Test.html", Locale.ENGLISH);
            final String resultHTML = loadResultFile(testDir + "result/index.html");
            assertThat(view.render(model), is(resultHTML));
            assertThat(view.getCSS(), is(loadResultFile(testDir + "result/index.css")));
            assertThat(view.getJavaScript(), is(loadResultFile(testDir + "result/index.js")));

            leastSigBit.set(0);
            final PrintWriter printWriter = mock(PrintWriter.class);
            final ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
            doNothing().when(printWriter).print(htmlCaptor.capture());
            view.stream(printWriter, model);
            assertThat(String.join("", htmlCaptor.getAllValues()), is(resultHTML));
        }
    }

    @Test
    public void staticTest() throws IOException {
        test("src/test/resources/Test_Static/", new Model());
    }

    @Test
    public void templatedTest00() throws IOException {
        final Model model = new Model();
        model.add("headingColor", "green");
        model.add("heading", "EVERYTHING IS GOING GREAT!");
        model.add("content", "This is the content!");
        model.add("title", "Website Title");
        model.add("aNumber", 1);
        test("src/test/resources/Test00/", model);
    }

    @Test
    public void templatedTest01() throws IOException {
        final Model model = new Model();
        model.add("color", "green");
        test("src/test/resources/Test01/", model);
    }

    @Test
    public void templatedTest02() throws IOException {
        final Model model = new Model();
        model.add("title", "Website Title");
        model.add("peoples", List.of(new Person("Achim"), new Person("Joachim")));
        test("src/test/resources/Test02/", model);
    }

    @Test
    public void templatedTest03() throws IOException {
        final Model model = new Model();
        model.add("title", "Website Title");
        model.add("peoples", List.of(new Person("Achim"), new Person("Joachim")));
        model.add("status", 200);
        test("src/test/resources/Test03/", model);
    }

    @Test
    public void htmlUnsafeTest() throws IOException {
        final Model model = new Model();
        model.add("html", "<h1>This is unsafe HTML from the Model</h1><div><script>alert(\"HACKED\");</script></div>");
        test("src/test/resources/HTMLTest/", model);
    }

    @Test
    public void htmlSafeTest() throws IOException {
        final Model model = new Model();
        model.add("html", "<h1>This is unsafe HTML from the Model</h1><div><script>alert(\"HACKED\");</script></div>");
        test("src/test/resources/SafeHTMLTest/", model);
    }

    @Test
    public void i18nTest() throws IOException {
        I18nService.addTranslation(
                Locale.ENGLISH,
                Map.of(
                        "test.main.text", "Welcome to our i18n test!",
                        "test.css.color", "green",
                        "test.js.log", "hello from js"
                )
        );
        final Model model = new Model();
        test("src/test/resources/I18nTest/", model);
    }

    @Test
    public void justCSS() throws IOException {
        test("src/test/resources/JustCSS/", new Model());
    }

    @Test
    public void withMainResolvePath() throws IOException {
        Plate.setTemplatesPath("src/test/resources/TestWithMainPath");
        test("src/test/resources/TestWithMainPath/", new Model());
    }

    @Test
    public void stream() throws IOException {
        final Model model = new Model();
        model.add("title", "Website Title");
        model.add("peoples", List.of(new Person("Achim"), new Person("Joachim")));
        Supplier<Integer> statusSupplier = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 200;
        };
        model.add("status", statusSupplier);
        test("src/test/resources/Test04/", model);
    }

    @Test
    public void withSlots() throws IOException {
        test("src/test/resources/TestWithSlots/", new Model());
    }

    @Test
    public void withSlotsWithinSlots() throws IOException {
        final Model model = new Model();
        model.add("title", "My Website");
        test("src/test/resources/TestWithSlotsWithinSlots/", model);
    }

    @Test
    public void loopTest() throws IOException {
        final Model model = new Model();
        final List<List<Integer>> map = List.of(List.of(1, 2, 3), List.of(4, 5, 6), List.of(7, 8, 9));
        model.add("map", map);
        test("src/test/resources/Test05/", model);
    }
}
