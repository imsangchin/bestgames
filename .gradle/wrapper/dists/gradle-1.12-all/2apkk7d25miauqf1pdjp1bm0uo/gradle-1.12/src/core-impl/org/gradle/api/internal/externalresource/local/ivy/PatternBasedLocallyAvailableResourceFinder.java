/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.externalresource.local.ivy;

import org.gradle.api.Transformer;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.artifacts.metadata.ModuleVersionArtifactMetaData;
import org.gradle.api.internal.artifacts.repositories.resolver.ResourcePattern;
import org.gradle.api.internal.externalresource.local.AbstractLocallyAvailableResourceFinder;
import org.gradle.api.internal.file.collections.MinimalFileTree;
import org.gradle.api.internal.file.collections.SingleIncludePatternFileTree;
import org.gradle.internal.Factory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class PatternBasedLocallyAvailableResourceFinder extends AbstractLocallyAvailableResourceFinder<ModuleVersionArtifactMetaData> {

    public PatternBasedLocallyAvailableResourceFinder(File baseDir, ResourcePattern pattern) {
        super(createProducer(baseDir, pattern));
    }

    private static Transformer<Factory<List<File>>, ModuleVersionArtifactMetaData> createProducer(final File baseDir, final ResourcePattern pattern) {
        return new Transformer<Factory<List<File>>, ModuleVersionArtifactMetaData>() {
            public Factory<List<File>> transform(final ModuleVersionArtifactMetaData artifact) {
                return new Factory<List<File>>() {
                    public List<File> create() {
                        final List<File> files = new LinkedList<File>();
                        if (artifact != null) {
                            getMatchingFiles(artifact).visit(new EmptyFileVisitor() {
                                public void visitFile(FileVisitDetails fileDetails) {
                                    files.add(fileDetails.getFile());
                                }
                            });
                        }
                        return files;
                    }
                };
            }

            private MinimalFileTree getMatchingFiles(ModuleVersionArtifactMetaData artifact) {
                String patternString = pattern.toPath(artifact);
                return new SingleIncludePatternFileTree(baseDir, patternString);
            }

        };
    }
}
