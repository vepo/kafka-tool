package io.vepo.kafka.tool.support.gherkin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.opentest4j.TestAbortedException;

import io.vepo.kafka.tool.settings.KafkaBroker;

public final class Feature {
    public static final class FeatureBuilder {
        private final String featureName;
        private String scenarioName;
        private boolean kafkaBrokerEnabled;
        private boolean schemaRegistryEnabled;
        private String bootstrapServers = DEFAULT_BOOTSTRAP;
        private String schemaRegistryUrl = DEFAULT_SCHEMA_REGISTRY;

        private FeatureBuilder(String featureName) {
            this.featureName = featureName;
        }

        public FeatureBuilder scenario(String scenarioName) {
            this.scenarioName = scenarioName;
            return this;
        }

        public ScenarioEnvironment start() {
            if (scenarioName == null || scenarioName.isBlank()) {
                throw new IllegalStateException("Call scenario(\"...\") before start().");
            }
            if (schemaRegistryEnabled && !kafkaBrokerEnabled) {
                throw new IllegalStateException("Schema Registry requires withKafkaBroker().");
            }

            KafkaBroker broker = null;
            if (kafkaBrokerEnabled) {
                broker = new KafkaBroker("test", bootstrapServers,
                                         schemaRegistryEnabled ? schemaRegistryUrl : "");
                KafkaEnvironment.requireReachable(broker);
            }

            return new ScenarioEnvironment(featureName, scenarioName, broker);
        }

        public FeatureBuilder withKafkaBroker() {
            this.kafkaBrokerEnabled = true;
            return this;
        }

        public FeatureBuilder withKafkaBroker(String bootstrapServers) {
            this.kafkaBrokerEnabled = true;
            this.bootstrapServers = bootstrapServers;
            return this;
        }

        public FeatureBuilder withSchemaRegistry() {
            this.schemaRegistryEnabled = true;
            return this;
        }

        public FeatureBuilder withSchemaRegistry(String schemaRegistryUrl) {
            this.schemaRegistryEnabled = true;
            this.schemaRegistryUrl = schemaRegistryUrl;
            return this;
        }
    }

    static final class KafkaEnvironment {
        static void requireReachable(KafkaBroker broker) {
            if (!Boolean.parseBoolean(System.getProperty("kafka.integration", "false"))) {
                throw new TestAbortedException(
                                               "Integration scenario skipped. Re-run with -Dkafka.integration=true and "
                                                       + "./scripts/setup-local-env.sh up");
            }
            // Connectivity is validated by the scenario; broker profile is ready for
            // clients.
        }

        private KafkaEnvironment() {}
    }

    public static final class ScenarioEnvironment implements AutoCloseable {
        private final String featureName;
        private final String scenarioName;
        private final KafkaBroker broker;
        private final List<String> steps = new ArrayList<>();

        private ScenarioEnvironment(String featureName, String scenarioName, KafkaBroker broker) {
            this.featureName = featureName;
            this.scenarioName = scenarioName;
            this.broker = broker;
        }

        public KafkaBroker broker() {
            if (broker == null) {
                throw new IllegalStateException("Kafka broker was not enabled. Call withKafkaBroker() before start().");
            }
            return broker;
        }

        @Override
        public void close() {
            // Reserved for future embedded or leased infrastructure teardown.
        }

        private String formatFailure() {
            return String.format("%nFeature: %s%nScenario: %s%n%s",
                                 featureName,
                                 scenarioName,
                                 String.join(System.lineSeparator(), steps));
        }

        public <T> T given(Fixture<T> fixture) {
            steps.add("Given " + fixture.description());
            return fixture.value();
        }

        public ScenarioEnvironment given(String description) {
            steps.add("Given " + description);
            return this;
        }

        public ScenarioEnvironment given(String description, Runnable setup) {
            steps.add("Given " + description);
            setup.run();
            return this;
        }

        public <T> T given(String description, Supplier<T> setup) {
            steps.add("Given " + description);
            return setup.get();
        }

        public <T> T given(String description, T value) {
            steps.add("Given " + description);
            return value;
        }

        private void runAssertion(Runnable assertion) {
            try {
                assertion.run();
            } catch (AssertionError e) {
                throw new AssertionError(formatFailure(), e);
            }
        }

        private void runStep(Runnable action) {
            try {
                action.run();
            } catch (AssertionError | RuntimeException e) {
                throw new AssertionError(formatFailure(), e);
            }
        }

        private <T> T runStep(Supplier<T> action) {
            try {
                return action.get();
            } catch (AssertionError | RuntimeException e) {
                throw new AssertionError(formatFailure(), e);
            }
        }

        public ScenarioEnvironment then(String description) {
            steps.add("Then " + description);
            return this;
        }

        public ScenarioEnvironment then(String description, Runnable assertion) {
            steps.add("Then " + description);
            runAssertion(assertion);
            return this;
        }

        public <T> ScenarioEnvironment then(String description, T value, Consumer<T> assertion) {
            steps.add("Then " + description);
            runAssertion(() -> assertion.accept(value));
            return this;
        }

        public ScenarioEnvironment when(String description) {
            steps.add("When " + description);
            return this;
        }

        public ScenarioEnvironment when(String description, Runnable action) {
            steps.add("When " + description);
            runStep(action);
            return this;
        }

        public <T> T when(String description, Supplier<T> action) {
            steps.add("When " + description);
            return runStep(action);
        }
    }

    private static final String DEFAULT_BOOTSTRAP = "localhost:29092";

    private static final String DEFAULT_SCHEMA_REGISTRY = "http://localhost:8081";

    public static FeatureBuilder feature(String featureName) {
        return new FeatureBuilder(featureName);
    }

    private Feature() {}
}
