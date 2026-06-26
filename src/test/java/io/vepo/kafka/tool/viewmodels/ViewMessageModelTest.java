package io.vepo.kafka.tool.viewmodels;

import static io.vepo.kafka.tool.support.gherkin.Feature.feature;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ViewMessageModelTest {

    @Test
    void clearResetsState() throws Throwable {
        try (var env = feature("View message model").scenario("Clear message").start()) {
            var model = new ViewMessageModel("warn", ViewMessageType.WARNING);
            model.clear();
            env.then("type is none", () -> assertEquals(ViewMessageType.NONE, model.typeProperty().get()));
            env.then("text is empty", () -> assertEquals("", model.textProperty().get()));
        }
    }

    @Test
    void showErrorSetsTypeAndText() throws Throwable {
        try (var env = feature("View message model").scenario("Show error message").start()) {
            var model = new ViewMessageModel();
            model.showError("failed");
            env.then("type is error", () -> assertEquals(ViewMessageType.ERROR, model.typeProperty().get()));
            env.then("text is set", () -> assertEquals("failed", model.textProperty().get()));
        }
    }

}
