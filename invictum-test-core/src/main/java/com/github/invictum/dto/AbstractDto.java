package com.github.invictum.dto;

import com.github.invictum.dto.annotation.DtoAttribute;
import com.github.invictum.dto.annotation.KeyAttribute;
import com.github.invictum.dto.attribute.converter.ConverterUtil;
import com.github.invictum.utils.ResourceProvider;
import com.github.invictum.utils.properties.EnhancedSystemProperty;
import com.github.invictum.utils.properties.PropertiesUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AbstractDto {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractDto.class);

    public static final boolean FULL_DTO_VIEW = Boolean
            .valueOf(PropertiesUtil.getProperty(EnhancedSystemProperty.FullDtoView));

    @Override
    public String toString() {
        String stringView = StringUtils.EMPTY;
        List<Attribute> data = getData(this, FULL_DTO_VIEW);
        if (data.isEmpty()) {
            return "{null}";
        }
        for (Attribute attribute : data) {
            String value = attribute.getValue();
            stringView += String.format("%s: %s, ", attribute.getName(), value == null ? "null" : value);
        }
        return String.format("{%s}", stringView.substring(0, stringView.length() - 2));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        List<Attribute> expected = getData(obj, false);
        List<Attribute> actual = getData(this, false);
        return actual.containsAll(expected);
    }

    @Override
    public int hashCode() {
        return getData(this, true).hashCode();
    }

    public void fromFile(String resourcePath, boolean useVelocity) {
        String content = ResourceProvider.getFileContent(resourcePath, useVelocity);
        Yaml yaml = new Yaml(new Constructor(this.getClass()));
        setContent(yaml.load(content));
    }

    public void fromFile(String resourcePath) {
        fromFile(resourcePath, false);
    }

    private void setContent(Object data) {
        for (Field from : data.getClass().getDeclaredFields()) {
            for (Field to : this.getClass().getDeclaredFields()) {
                if (to.equals(from)) {
                    try {
                        to.setAccessible(true);
                        from.setAccessible(true);
                        to.set(this, from.get(data));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String extractData(Object object, Field attribute) {
        attribute.setAccessible(true);
        Object value = null;
        try {
            value = attribute.get(object);
        } catch (IllegalAccessException ex) {
            LOG.error("Failed to get data for {} attribute", attribute);
        }
        return ConverterUtil.convert(value);
    }

    private List<Attribute> getData(Object object, boolean includeNulls) {
        List<Attribute> data = new ArrayList<>();
        Field[] fields = getFields(object.getClass());
        for (Field field : fields) {
            Attribute attribute = new Attribute();
            if (field.isAnnotationPresent(DtoAttribute.class)) {
                attribute.setName(field.getName());
                attribute.setValue(this.extractData(object, field));
                if (attribute.getValue() != null || includeNulls) {
                    if (field.isAnnotationPresent(KeyAttribute.class)) {
                        data.add(0, attribute);
                    } else {
                        data.add(attribute);
                    }
                }
            }
        }
        return data;
    }

    private Field[] getFields(Class klass) {
        Field[] fields = klass.getDeclaredFields();
        return klass.getSuperclass() == AbstractDto.class ? fields : (Field[]) ArrayUtils
                .addAll(fields, getFields(klass.getSuperclass()));
    }

}
