package org.mskcc.cbio.portal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;

public class TransactionalScripts implements Runnable {
    public List<String[]> scripts = new ArrayList<String[]>();

    /**
     * Primary constructor, used to specify scripts through dependency injection.
     * @param scripts
     */
    public TransactionalScripts(List<String[]> scripts) {
        this.scripts = scripts;
    }

    public List<String[]> getScripts() {
        return scripts;
    }

    public void setScripts(List<String[]> scripts) {
        this.scripts = scripts;
    }

    @Override
    public void run() {
        for (String[] command : getScripts()) {
            // The first element in the array will be a class name. We should
            // locate the class and the call the static main method with the
            // rest of the arguments. Oh, and there needs to be a whole bunch
            // of error handling.

            String className = command[0];
            String args[] = (String[]) ArrayUtils.remove(command, 0);

            // Now let's look for the class
            Class<?> scriptClass;
            try {
                scriptClass = getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new NestableRuntimeException(
                    "Failed to load script class: " + className,
                    e
                );
            }

            try {
                Constructor<?>[] ctors = scriptClass.getDeclaredConstructors();
                Constructor<?> ctor = null;
                for (int i = 0; i < ctors.length; i++) {
                    ctor = ctors[i];
                    if (ctor.getGenericParameterTypes().length == 1) break;
                }

                Runnable newInstance = (Runnable) ctor.newInstance(
                    new Object[] { args }
                );
                newInstance.run();
            } catch (InstantiationException e) {
                e.printStackTrace(System.err);
                throw new NestableRuntimeException(
                    "Can't find instantiate runner for: " + className,
                    e
                );
            } catch (SecurityException e) {
                e.printStackTrace(System.err);
                throw new NestableRuntimeException(
                    "Can't access run() method in: " + className,
                    e
                );
            } catch (IllegalAccessException e) {
                e.printStackTrace(System.err);
                throw new NestableRuntimeException(
                    "Invalid access to run() method in: " + className,
                    e
                );
            } catch (IllegalArgumentException e) {
                e.printStackTrace(System.err);
                throw new NestableRuntimeException(
                    "Invalid arguments for run() method in: " + className,
                    e
                );
            } catch (InvocationTargetException e) {
                e.printStackTrace(System.err);
                throw new NestableRuntimeException(
                    "Can't call run() method in: " + className,
                    e
                );
            }
        }
    }
}
