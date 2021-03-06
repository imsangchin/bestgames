/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativebinaries.language.internal;

import org.gradle.nativebinaries.internal.AbstractBinaryToolSpec;
import org.gradle.nativebinaries.toolchain.internal.NativeCompileSpec;

import java.io.File;
import java.util.*;

public abstract class AbstractNativeCompileSpec extends AbstractBinaryToolSpec implements NativeCompileSpec {

    private List<File> includeRoots = new ArrayList<File>();
    private List<File> sourceFiles = new ArrayList<File>();
    private Map<String, String> macros = new LinkedHashMap<String, String>();
    private File objectFileDir;
    private boolean positionIndependentCode;

    public List<File> getIncludeRoots() {
        return includeRoots;
    }

    public void include(File... includeRoots) {
        Collections.addAll(this.includeRoots, includeRoots);
    }

    public void include(Iterable<File> includeRoots) {
        addAll(this.includeRoots, includeRoots);
    }

    public List<File> getSourceFiles() {
        return sourceFiles;
    }

    public void source(Iterable<File> sources) {
        addAll(sourceFiles, sources);
    }

    public File getObjectFileDir() {
        return objectFileDir;
    }

    public void setObjectFileDir(File objectFileDir) {
        this.objectFileDir = objectFileDir;
    }

    public Map<String, String> getMacros() {
        return macros;
    }

    public void setMacros(Map<String, String> macros) {
        this.macros = macros;
    }

    public boolean isPositionIndependentCode() {
        return positionIndependentCode;
    }

    public void setPositionIndependentCode(boolean positionIndependentCode) {
        this.positionIndependentCode = positionIndependentCode;
    }

    private void addAll(List<File> list, Iterable<File> iterable) {
        for (File file : iterable) {
            list.add(file);
        }
    }
}
