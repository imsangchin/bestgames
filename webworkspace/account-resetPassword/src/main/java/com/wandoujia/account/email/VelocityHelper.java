package com.wandoujia.account.email;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class VelocityHelper {
    private VelocityEngine initVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        ve.setProperty(Velocity.ENCODING_DEFAULT, "utf8");
        ve.setProperty(Velocity.INPUT_ENCODING, "utf8");
        ve.setProperty(Velocity.OUTPUT_ENCODING, "utf8");

        ve.init();
        return ve;
    }

    public String getContent(String vm, Map<String, Object> arguments) {
        VelocityEngine ve = initVelocityEngine();

        VelocityContext context = new VelocityContext();

        Iterator<Entry<String, Object>> iter = arguments.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iter
                    .next();
            context.put(entry.getKey(), entry.getValue());
        }

        Template template = null;

        template = ve.getTemplate(vm);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);

        String content = sw.toString();
        
        return content;
    }

}
