package com.knowledge.knowledgeagentplatform.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.Arrays;

/**
 * 自定义的工具类
 */
public class BeanCopyUtil {

    public static void copyProperty(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    public static String[] getNullPropertyNames(Object source) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        return Arrays.stream(beanWrapper.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(name -> beanWrapper.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

}
