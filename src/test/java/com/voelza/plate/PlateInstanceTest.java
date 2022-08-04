package com.voelza.plate;

import com.voelza.plate.view.View;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.voelza.plate.Plate.VIEWS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlateInstanceTest {

    @Test
    public void getCssJsFromView() {

        final String css = "css";
        final String js = "js";
        final View view = mock(View.class);
        when(view.getCSS()).thenReturn(css);
        when(view.getJavaScript()).thenReturn(js);

        final String viewKey = ViewKeyCreator.create("thing/thing/thing.html");
        VIEWS.put(viewKey + Locale.ENGLISH, view);

        final String cssUrl = viewKey + "-DEV.css";
        final String jsUrl = viewKey + "-DEV.js";
        assertThat(Plate.getCSS(cssUrl, Locale.ENGLISH).get(), is(css));
        assertThat(Plate.getJavaScript(jsUrl, Locale.ENGLISH).get(), is(js));
    }

}