package org.jenkins.ci.plugins.pluginstats;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by victor.martinez on 13/06/15.
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    public static String findPathJar(Class<?> context) throws IllegalStateException {
        URL location = context.getResource('/' + context.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        if (jarPath != null && !jarPath.equals("")) {
            try {
                return jarPath.substring("file:".length(), jarPath.lastIndexOf("!")).replaceAll("/WEB-INF.*", "");
            } catch (java.lang.StringIndexOutOfBoundsException e) {
                // it is based on class
                LOG.log(Level.WARNING, "Cannot be converted '" + context.getName() + "' (" + jarPath + ")");
                return jarPath.substring("file:".length(), jarPath.lastIndexOf("/WEB-INF"));
            }
        } else {
            return "";
        }
    }
}
