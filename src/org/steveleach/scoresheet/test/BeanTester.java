package org.steveleach.scoresheet.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Random;

/**
 * Created by steve on 12/03/16.
 */
public class BeanTester {

    public class BeanValidationFailure extends RuntimeException {
        public BeanValidationFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private Random random = new Random();

    public void testBean(Class<?> beanClass) {
        Object bean = instantiate(beanClass);
        validateToString(bean);
        validateProperties(beanClass, bean);
    }

    private void validateProperties(Class<?> beanClass, Object bean) {
        for (Method method : beanClass.getMethods()) {
            if (method.getName().startsWith("set")) {
                if (method.getParameterTypes().length == 1) {
                    Method setter = method;
                    Class<?> propertyType = setter.getParameterTypes()[0];
                    Method getter = findGetter(beanClass, setter, propertyType);
                    if ((getter != null) && getter.getReturnType().equals(propertyType)) {
                        Object value = createValue(propertyType);
                        if (value != null) {
                            setterGetterRoundTrip(beanClass, bean, setter, getter, value);
                        }
                    }
                }
            }
        }
    }

    private void validateToString(Object bean) {
        if (bean.toString() == null) {
            throw new BeanValidationFailure("toString returned null",null);
        }
    }

    private void setterGetterRoundTrip(Class<?> beanClass, Object bean, Method setter, Method getter, Object value) {
        String propertyName = beanClass.getCanonicalName() + "." + setter.getName().substring(4);
        try {
            setAccessible(setter, getter);
            setter.invoke(bean,value);
            Object result = getter.invoke(bean);
            if (!result.equals(value)) {
                throw new BeanValidationFailure("Getter did not return value set with setter: "+propertyName,null);
            }
        } catch (IllegalAccessException e) {
            throw new BeanValidationFailure("Cannot access " + propertyName, null);
        } catch (InvocationTargetException e) {
            throw new BeanValidationFailure("Cannot access " + propertyName, null);
        }
    }

    private void setAccessible(Method setter, Method getter) {
        setter.setAccessible(true);
        getter.setAccessible(true);
    }

    private Method findGetter(Class<?> beanClass, Method setter, Class<?> propertyType) {
        String getterName = findGetterName(setter, propertyType);
        try {
            return beanClass.getMethod(getterName,new Class[] {});
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private String findGetterName(Method setter, Class<?> propertyType) {
        if (propertyType.equals(Boolean.TYPE)) {
            return setter.getName().replaceFirst("set", "is");
        } else {
            return setter.getName().replaceFirst("set", "get");
        }
    }

    private Object createValue(Class<?> propertyType) {
        Object value = null;
        if (propertyType.equals(String.class)) {
            value = "ABC"+random.nextLong();
        } else if (propertyType.equals(Integer.TYPE)) {
            value = random.nextInt();
        } else if (propertyType.equals(Boolean.TYPE)) {
            value = true;
        } else if (propertyType.equals(Date.class)) {
            value = new Date(random.nextLong());
        }
        return value;
    }

    private Object instantiate(Class<?> beanClass) {
        try {
            return beanClass.newInstance();
        } catch (Exception e) {
            throw new BeanValidationFailure("Error instantiating " + beanClass.getCanonicalName(), e);
        }
    }
}