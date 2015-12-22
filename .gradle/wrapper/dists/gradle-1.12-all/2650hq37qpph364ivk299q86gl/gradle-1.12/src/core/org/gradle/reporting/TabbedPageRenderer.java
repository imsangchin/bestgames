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
package org.gradle.reporting;

import org.gradle.api.internal.html.SimpleHtmlWriter;
import org.gradle.util.GradleVersion;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public abstract class TabbedPageRenderer<T> extends ReportRenderer<T, SimpleHtmlWriter> {
    private T model;

    protected T getModel() {
        return model;
    }

    protected abstract String getTitle();

    protected abstract ReportRenderer<T, SimpleHtmlWriter> getHeaderRenderer();

    protected abstract ReportRenderer<T, SimpleHtmlWriter> getContentRenderer();

    protected String getPageTitle() {
        return getTitle();
    }

    @Override
    public void render(final T model, SimpleHtmlWriter htmlWriter) throws IOException {
        this.model = model;
        htmlWriter.startElement("head")
            .startElement("meta").attribute("http-equiv", "Content-Type").attribute("content", "text/html; charset=utf-8").endElement()
            .startElement("title").characters(getPageTitle()).endElement()
            .startElement("link").attribute("href", "css/base-style.css").attribute("rel", "stylesheet").attribute("type", "text/css").endElement()
            .startElement("link").attribute("href", "css/style.css").attribute("rel", "stylesheet").attribute("type", "text/css").endElement()
            .startElement("script").attribute("src", "js/report.js").attribute("type", "text/javascript").characters("").endElement() //html does not like <a name="..."/>
        .endElement();

        htmlWriter.startElement("body")
            .startElement("div").attribute("id", "content")
            .startElement("h1").characters(getTitle()).endElement();
            getHeaderRenderer().render(model, htmlWriter);
            getContentRenderer().render(model, htmlWriter);
            htmlWriter.startElement("div").attribute("id", "footer")
                .startElement("p").characters("Generated by ")
                    .startElement("a").attribute("href", "http://www.gradle.org").characters(String.format("Gradle %s", GradleVersion.current().getVersion())).endElement()
                    .characters(String.format(" at %s", DateFormat.getDateTimeInstance().format(new Date())))
                .endElement()
            .endElement()
        .endElement();
    }
}