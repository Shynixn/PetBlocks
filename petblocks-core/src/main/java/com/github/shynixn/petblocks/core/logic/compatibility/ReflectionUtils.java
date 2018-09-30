package com.github.shynixn.petblocks.core.logic.compatibility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2016
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public final class ReflectionUtils {
    /**
     * Initializes a new instance of reflectionUtils
     */
    private ReflectionUtils() {
        super();
    }

    /**
     * Creates a new instance of the given clazz, paramTypes, params
     *
     * @param clazz      clazz
     * @param paramTypes paramTypes
     * @param params     params
     * @return instance
     * @throws IllegalAccessException    exception
     * @throws InvocationTargetException exception
     * @throws InstantiationException    exception
     * @throws NoSuchMethodException     exception
     */
    public static Object invokeConstructor(Class<?> clazz, Class<?>[] paramTypes, Object[] params) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        if (clazz == null)
            throw new IllegalArgumentException("Class cannot be null!");
        if (paramTypes == null)
            throw new IllegalArgumentException("ParamTypes cannot be null");
        if (params == null)
            throw new IllegalArgumentException("Params cannot be null!");
        final Constructor constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(params);
    }

    /**
     * Invokes the static method of the given clazz, name, paramTypes, params
     *
     * @param clazz      clazz
     * @param name       name
     * @param paramTypes paramTypes
     * @param params     params
     * @param <T>        returnType
     * @return returnValue
     * @throws NoSuchMethodException     exception
     * @throws InvocationTargetException exception
     * @throws IllegalAccessException    exception
     */
    public static <T> T invokeMethodByClass(Class<?> clazz, String name, Class<?>[] paramTypes, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (clazz == null)
            throw new IllegalArgumentException("Class cannot be null!");
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null!");
        if (paramTypes == null)
            throw new IllegalArgumentException("ParamTypes cannot be null");
        if (params == null)
            throw new IllegalArgumentException("Params cannot be null!");
        final Method method = clazz.getDeclaredMethod(name, paramTypes);
        method.setAccessible(true);
        return (T) method.invoke(null, params);
    }
}