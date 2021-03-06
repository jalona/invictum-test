package com.github.invictum.fixtures;

import com.github.invictum.utils.ResourceProvider;
import com.github.invictum.utils.properties.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.github.invictum.utils.properties.EnhancedSystemProperty.FixturesPackageName;

public class FixtureProcessor {

    public static final String FIXTURES_PACKAGE = PropertiesUtil.getProperty(FixturesPackageName);
    private final static Logger LOG = LoggerFactory.getLogger(FixtureProcessor.class);

    private static Set<Class<? extends AbstractFixture>> availableFixtureClasses = new HashSet<>();
    private static ThreadLocal<Queue<Fixture>> registeredFixtures = new ThreadLocal<Queue<Fixture>>() {
        @Override
        protected Queue<Fixture> initialValue() {
            return new ArrayDeque<>();
        }
    };

    static {
        if (StringUtils.equals(FIXTURES_PACKAGE, FixturesPackageName.defaultValue())) {
            LOG.info("Project root is used as fixtures package. You may redefine it with '{}' property",
                    FixturesPackageName);
        } else if (!ResourceProvider.isPackagePresent(FIXTURES_PACKAGE)) {
            LOG.error("Configure fixtures package with '{}' property", FixturesPackageName);
        }
        Reflections reflections = new Reflections(ClasspathHelper.forPackage(FIXTURES_PACKAGE));
        availableFixtureClasses = reflections.getSubTypesOf(AbstractFixture.class);
        LOG.debug("Found {} available fixtures", availableFixtureClasses.size());
    }

    public static void apply(Map<String, String> annotations) {
        for (Class<? extends Fixture> fixtureClass : availableFixtureClasses) {
            for (Map.Entry<String, String> annotation : annotations.entrySet()) {
                if (annotation.getKey().equalsIgnoreCase(fixtureClass.getSimpleName())) {
                    applyFixture(fixtureClass, prepareParams(annotation.getValue()));
                }
            }
        }
    }

    public static void put(Class<? extends Fixture> fixtureClass, String... arguments) {
        applyFixture(fixtureClass, arguments);
    }

    public static void rollback() {
        while (registeredFixtures.get().size() > 0) {
            Fixture fixture = registeredFixtures.get().poll();
            LOG.info("Rollback for {} fixture", fixture);
            fixture.rollbackCondition();
        }
    }

    public static Queue<Fixture> getRegisteredFixtures() {
        return registeredFixtures.get();
    }

    private static void applyFixture(Class<? extends Fixture> fixtureClass, String... arguments) {
        try {
            Fixture fixture = fixtureClass.newInstance();
            fixture.setParams(arguments);
            LOG.info("Applying {} fixture", fixture);
            fixture.prepareCondition();
            registeredFixtures.get().add(fixture);
        } catch (ReflectiveOperationException e) {
            LOG.error("Failed to apply {} fixture", fixtureClass);
        }
    }

    private static String[] prepareParams(String paramsString) {
        String[] params = paramsString.split(",");
        int index = 0;
        for (String paramItem : params) {
            params[index] = paramItem.trim();
            index++;
        }
        return params;
    }
}
