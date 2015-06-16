package org.jenkins.ci.plugins.pluginstats;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utils class.
 * @author Victor Martinez
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    public static String findPathJar(Class<?> context) throws IllegalStateException {
        URL location = context.getResource('/' + context.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        String findPath = "";
        if (jarPath != null && !jarPath.equals("")) {
            try {
                findPath = jarPath.substring("file:".length(), jarPath.lastIndexOf("!")).replaceAll("/WEB-INF.*", "");
            } catch (java.lang.StringIndexOutOfBoundsException e) {
                // it is based on class
                LOG.log(Level.WARNING, "Cannot be converted '" + context.getName() + "' (" + jarPath + ")");
                findPath = jarPath.substring("file:".length(), jarPath.lastIndexOf("/WEB-INF"));
            } finally {
                LOG.log(Level.FINEST, "findPathJar '" + context.getName() + "' (" + jarPath + ")" + "[" + findPath + "]");
                return findPath;
            }
        } else {
            return findPath;
        }
    }
}
