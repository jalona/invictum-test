package com.github.invictum.unified.data.provider.parsers;

import com.github.invictum.unified.data.provider.UnifiedDataProvider;
import com.github.invictum.utils.ResourceProvider;
import com.github.invictum.utils.properties.EnhancedSystemProperty;
import com.github.invictum.utils.properties.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;

public class YamlParser implements Parser {

    private String locatorDirectory = PropertiesUtil.getProperty(EnhancedSystemProperty.LocatorsDirectory);
    private final static Logger LOG = LoggerFactory.getLogger(YamlParser.class);
    private final static String EXTENSION = "yml";

    @Override
    public UnifiedDataProvider load(final String fileName) {
        try {
            File yamlFile = ResourceProvider.getFile(locatorDirectory, String.format("%s.%s", fileName, EXTENSION));
            Yaml yaml = new Yaml(new Constructor(UnifiedDataProvider.class));
            return (UnifiedDataProvider) yaml.load(new FileInputStream(yamlFile));
        } catch (Exception e) {
            LOG.error("Yaml file not found for {}", fileName);
            return new UnifiedDataProvider();
        }
    }
}
