package org.mskcc.portal.tool.bundle;

import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class BundleFactory {
    private static HashMap <String, Class> bundleMap = new HashMap<String, Class>();

    //  Register the BRCA Bundle
    static {
        BundleFactory.register("BRCA", BrcaBundle.class);
    }

    public static void register (String id, Class bundle) {
        System.out.println ("Registering Bundle:  " + id);
        bundleMap.put(id, bundle);
    }

    public static Bundle createBundle(String id) throws IllegalAccessException,
            InvocationTargetException, InstantiationException, NoSuchMethodException {
        Class bundleClass = bundleMap.get(id);
        if (bundleClass != null) {
            Constructor bundleConstuctor = bundleClass.getDeclaredConstructor();
            return (Bundle) bundleConstuctor.newInstance();
        } else {
            return null;
        }
    }
}
