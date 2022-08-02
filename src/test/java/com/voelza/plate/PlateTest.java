package com.voelza.plate;

import com.voelza.plate.view.View;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mockStatic;

public class PlateTest {

    private static String loadResultFile(final String testDir) throws IOException {
        return Files.readString(Paths.get(testDir)).replaceAll("\\r|\\n|\\t|\\s ", "");
    }

    private void test(final String testDir, final Model model) throws IOException {
        final AtomicInteger leastSigBit = new AtomicInteger(0);
        try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
            mocked.when(UUID::randomUUID).thenAnswer((invocation) -> {
                return new UUID(1, leastSigBit.incrementAndGet());
            });
            final View view = new View(testDir + "templates/Test.html", Locale.ENGLISH);
            assertThat(view.render(model), is(loadResultFile(testDir + "result/index.html")));
            assertThat(view.getCSS(), is(loadResultFile(testDir + "result/index.css")));
            assertThat(view.getJavaScript(), is(loadResultFile(testDir + "result/index.js")));
        }
    }

    @Test
    public void staticTest() throws IOException {
        final Model model = new Model();
        test("src/test/resources/Test_Static/", model);
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
        test("src/test/resources/Test03/", model);
    }

    public static record Person(String name) {
    }
}
