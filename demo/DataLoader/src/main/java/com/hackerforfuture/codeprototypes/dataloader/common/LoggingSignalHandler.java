/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wallace on 2018/8/29.
 */
public class LoggingSignalHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingSignalHandler.class);

    private final Constructor<?> signalConstructor;
    private final Class<?> signalHandlerClass;
    private final Class<?> signalClass;
    private final Method signalHandleMethod;
    private final Method signalGetNameMethod;
    private final Method signalHandlerHandleMethod;

    /**
     * Create an instance of this class.
     *
     * @throws ReflectiveOperationException if the underlying API has changed in an incompatible manner.
     */
    public LoggingSignalHandler() throws ReflectiveOperationException {
        signalClass = Class.forName("sun.misc.Signal");
        signalConstructor = signalClass.getConstructor(String.class);
        signalHandlerClass = Class.forName("sun.misc.SignalHandler");
        signalHandlerHandleMethod = signalHandlerClass.getMethod("handle", signalClass);
        signalHandleMethod = signalClass.getMethod("handle", signalClass, signalHandlerClass);
        signalGetNameMethod = signalClass.getMethod("getName");
    }

    private static ClassLoader getContextOrKafkaClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null)
            return getKafkaClassLoader();
        else
            return cl;
    }

    private static ClassLoader getKafkaClassLoader() {
        return LoggingSignalHandler.class.getClassLoader();
    }

    /**
     * Register signal handler to log termination due to SIGTERM, SIGHUP and SIGINT (control-c). This method
     * does not currently work on Windows.
     *
     * @implNote sun.misc.Signal and sun.misc.SignalHandler are described as "not encapsulated" in
     * http://openjdk.java.net/jeps/260. However, they are not available in the compile classpath if the `--release`
     * flag is used. As a workaround, we rely on reflection.
     */
    public void register() throws ReflectiveOperationException {
        Map<String, Object> jvmSignalHandlers = new ConcurrentHashMap<>();
        register("TERM", jvmSignalHandlers);
        register("INT", jvmSignalHandlers);
        register("HUP", jvmSignalHandlers);
    }

    private Object createSignalHandler(final Map<String, Object> jvmSignalHandlers) {
        InvocationHandler invocationHandler = new InvocationHandler() {

            private String getName(Object signal) throws ReflectiveOperationException {
                return (String) signalGetNameMethod.invoke(signal);
            }

            private void handle(Object signalHandler, Object signal) throws ReflectiveOperationException {
                signalHandlerHandleMethod.invoke(signalHandler, signal);
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object signal = args[0];
                log.info("Terminating process due to signal {}", signal);
                Object handler = jvmSignalHandlers.get(getName(signal));
                if (handler != null)
                    handle(handler, signal);
                return null;
            }
        };
        return Proxy.newProxyInstance(getContextOrKafkaClassLoader(), new Class[]{signalHandlerClass},
                invocationHandler);
    }

    private void register(String signalName, final Map<String, Object> jvmSignalHandlers) throws ReflectiveOperationException {
        Object signal = signalConstructor.newInstance(signalName);
        Object signalHandler = createSignalHandler(jvmSignalHandlers);
        Object oldHandler = signalHandleMethod.invoke(null, signal, signalHandler);
        if (oldHandler != null)
            jvmSignalHandlers.put(signalName, oldHandler);
    }
}
