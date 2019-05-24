/*
 * Copyright (c) 2019 JCav <825195983@qq.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.jcav.bilibilidanmakubackup.cidtokenizer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * If a new CidTokenizer subclass is added to this package,
 * and it contains a final static string called URL_PATTERN,
 * this CidTokenizer will be added into the Guilder.
 * The guilder will select a proper CidTokenizer by given
 * url which make url.match(CidTokenizer.URL_PATTERN) is true.
 */
public final class TokenizerGuilder {
    public static TokenizerGuilder ins = new TokenizerGuilder();
    private final Map<String, Constructor<?>> constructorMap;
    private final ClassLoader classLoader;

    private TokenizerGuilder(){
        constructorMap = new HashMap<>();
        classLoader = this.getClass().getClassLoader();

        loadCidTokenizer();
    }

    /**
     * @param url
     * @return null if there isn't any proper CidTokenizer
     */
    public CidTokenizer getCidTokenizerByURL(String url, int timeout){
        CidTokenizer tokenizer = null;
        Constructor<?> con = constructorMap
                .entrySet()
                .stream()
                .filter(e -> url.matches(e.getKey()))
                .findAny()
                .orElse(new NullEntry<>())
                .getValue();
        if(con == null) return null;
        try {
            tokenizer = (CidTokenizer) con.newInstance(url, timeout);
        } catch (Exception e) {
            System.out.println("Init CidTokenizer error: " + e.getMessage());
        }
        return tokenizer;
    }

    private void loadCidTokenizer() {
        Set<Class<?>> classSet = new HashSet<>();

        // get all classes from cur package.
        URL curURL = this.getClass().getResource("");
        String pro = curURL.getProtocol();
        if(pro.equals("file")){
            try {
                String filePath = URLDecoder.decode(curURL.getFile(), "utf-8");
                loadClassFromFile(filePath, classSet);
            } catch (UnsupportedEncodingException e) {}
        }else if(pro.equals("jar")){
            JarFile jar = null;
            try {
                jar = ((JarURLConnection)curURL.openConnection()).getJarFile();
            } catch (IOException e) {
                System.out.println("Cannot load jar file: " + curURL);
                System.out.println("Error msg: " + e.getMessage());
            }

            Enumeration<JarEntry> jaritr = jar.entries();
            loadClassFromJar(jaritr, classSet);
        }

        // add valid class
        for(Class<?> c : classSet){
            // is subclass of AbCidTokenizer
            if(c.getSuperclass() != AbCidTokenizer.class) continue;

            // has field URL_PATTERN
            Field field = null;
            String pat = null;
            try {
                field = c.getDeclaredField("URL_PATTERN");
                pat = (String) field.get("");
            } catch (Exception e) {
                continue;
            }

            // has Constructor(String, int)
            Constructor<?> con = null;
            try {
                 con = c.getConstructor(String.class, int.class);
            } catch (NoSuchMethodException e) {
                continue;
            }

            constructorMap.put(pat, con);
            System.out.println("Register CidTokenizer: " + c.getSimpleName());
        }
    }

    private void loadClassFromJar(Enumeration<JarEntry> jaritr, Set<Class<?>> set){
        String packPath = TokenizerGuilder.class.getPackage().getName();
        packPath = packPath.replace('.', '/');

        while(jaritr.hasMoreElements()){
            JarEntry je = jaritr.nextElement();
            String entryPath = je.getName();

            if(entryPath.startsWith("/")) entryPath = entryPath.substring(1);
            if(entryPath.startsWith(packPath)
                    && entryPath.toLowerCase().endsWith(".class")
                    && !je.isDirectory()
            ){
                entryPath = entryPath.replace('/', '.');
                entryPath = entryPath.substring(0, entryPath.length() - 6);
                try {
                    Class<?> c = classLoader.loadClass(entryPath);
                    set.add(c);
                    //System.out.println("Load class: " + c);
                } catch (ClassNotFoundException e) {
                    System.out.println("Can't load class: " + entryPath);
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadClassFromFile(String filePath, Set<Class<?>> set){
        String packPath = TokenizerGuilder.class.getPackage().getName();

        File dir = new File(filePath);
        File[] list = dir.listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().toLowerCase().endsWith(".class");
                    }
                }
        );
        if(list == null) return;
        for(File classFile : list){
            String name = classFile.getName();
            String className = name.substring(0, name.length() - 6);
            try{
                Class<?> c = classLoader.loadClass(packPath + "." + className);
                set.add(c);
                //System.out.println("Load class: " + c.getName());
            }catch(Exception e){
                System.out.println("Can't load class: " + classFile.toString());
            }
        }
    }

    private static class NullEntry<K, V> implements Map.Entry<K, V> {

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V value) {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}










