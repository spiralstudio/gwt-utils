//
// $Id$
//
// OOO GWT Utils - utilities for creating GWT applications
// Copyright (C) 2009-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/ooo-gwt-utils/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.gwt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * An tool task to automatically generate <code>FooMessages.java</code> classes from
 * <code>FooMessages.properties</code> classes for GWT's i18n.
 */
public class I18nSync
{
    /**
     * Converts a single i18n properties file to its corresponding source file.
     *
     * @param sourceDir the root of the source directory. Used to infer the package for the
     * generated source given the path to the properties file and the root of the source directory.
     * @param propsFile the properties file from which to generate a source file. Name must be of
     * the form <code>X.properties</code> for any X.
     */
    public static void processFile (File sourceDir, File propsFile)
        throws IOException
    {
        String fileName = propsFile.getName();
        if (!fileName.endsWith(".properties")) {
            System.err.println("Ignoring non-properties file: " + propsFile);
            return;
        }
        fileName = fileName.substring(0, fileName.length()-".properties".length());
        File javaFile = new File(propsFile.getParent(), fileName + ".java");
        if (propsFile.lastModified() <= javaFile.lastModified()) {
            return;
        }

        String sourcePath = sourceDir.getAbsolutePath();
        String javaPath = javaFile.getAbsolutePath();
        if (!javaPath.startsWith(sourcePath)) {
            System.err.println("Ignoring properties file outside 'srcdir': " + propsFile);
            return;
        }

        String pkg = javaPath.substring(sourcePath.length());
        if (pkg.startsWith(File.separator)) {
            pkg = pkg.substring(1);
        }
        pkg = pkg.substring(0, pkg.indexOf(javaFile.getName()));
        if (pkg.endsWith(File.separator)) {
            pkg = pkg.substring(0, pkg.length()-1);
        }
        pkg = pkg.replace(File.separatorChar, '.');

        String clazz = javaFile.getName().substring(0, javaFile.getName().indexOf(".java"));

        System.out.println("Generating " + pkg + "." + clazz + "...");

        StringBuilder buf = new StringBuilder();
        buf.append("//\n");
        buf.append("// Generated by I18nSyncTask on ").append(new Date()).append("\n\n");
        buf.append("package ").append(pkg).append(";\n\n");
        buf.append("public interface ").append(clazz);
        buf.append(" extends com.google.gwt.i18n.client.Messages\n");
        buf.append("{\n");

        Properties props = new Properties();
        props.load(new FileInputStream(propsFile));
        Enumeration<?> names = props.propertyNames();
        for (int method = 0; names.hasMoreElements(); method++) {
            if (method > 0) {
                buf.append("\n");
            }
            String key = String.valueOf(names.nextElement());
            buf.append("    @Key(\"").append(key).append("\")\n");
            buf.append("    String ").append(keyToMethod(key)).append(" (");
            String value = props.getProperty(key);
            int idx = 0;
            while (value.indexOf("{" + idx) != -1) {
                if (idx > 0) {
                    buf.append(", ");
                }
                // TODO: make {N,number,integer} into an int argument, {N,number,percent}
                // into a float, etc.
                // http://docs.oracle.com/javase/1.5.0/docs/api/java/text/MessageFormat.html
                buf.append("String arg").append(idx);
                idx++;
            }
            buf.append(");\n");
        }
        buf.append("}\n");

        PrintWriter out = new PrintWriter(javaFile, "UTF-8");
        out.print(buf.toString());
        out.close();
    }

    /**
     * Entry point for command line tool.
     */
    public static void main (String[] args)
    {
        if (args.length <= 1) {
            System.err.println("Usage: I18nSyncTask rootDir rootDir/com/mypackage/Foo.properties " +
                               "[.../Bar.properties ...]");
            System.exit(255);
        }

        File rootDir = new File(args[0]);
        if (!rootDir.isDirectory()) {
            System.err.println("Invalid root directory: " + rootDir);
            System.exit(255);
        }

        boolean errors = false;
        for (int ii = 1; ii < args.length; ii++) {
            try {
                processFile(rootDir, new File(args[ii]));
            } catch (IOException ioe) {
                System.err.println("Error processing '" + args[ii] + "': " + ioe);
                errors = true;
            }
        }
        System.exit(errors ? 255 : 0);
    }

    protected static String keyToMethod (String key)
    {
        return key.replace('.', '_');
    }
}
