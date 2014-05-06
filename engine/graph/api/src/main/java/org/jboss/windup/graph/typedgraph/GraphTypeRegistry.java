package org.jboss.windup.graph.typedgraph;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.Module;

public class GraphTypeRegistry
{
    private static final Logger LOG = LoggerFactory.getLogger(GraphTypeRegistry.class);

    @Inject
    private FurnaceGraphModelScanner scanner;
    
    @Inject
    private GraphTypeManager graphTypeManager;

    public void addTypeToElement(Class<? extends VertexFrame> kind, Element element)
    {
        graphTypeManager.addTypeToElement(kind, element);
    }
    
    @PostConstruct
    public void init()
    {
        List<Class<?>> classNames = scanner.scan();
        for (Class<?> clazz : classNames)
        {
            LOG.info("Found class: " + clazz);
            if (WindupVertexFrame.class.isAssignableFrom(clazz))
            {
                @SuppressWarnings("unchecked")
                Class<? extends WindupVertexFrame> wvf = (Class<? extends WindupVertexFrame>) clazz;
                graphTypeManager.addTypeToRegistry(wvf);
            }
            else
            {
                LOG.debug("Not adding to GraphTypeRegistry, not a subclass of WindupVertexFrame: "
                            + clazz.getCanonicalName());
            }
        }
    }

    public Module build()
    {
        return new AbstractModule()
        {
            @Override
            public void doConfigure(FramedGraphConfiguration config)
            {
                config.addTypeResolver(graphTypeManager);
                config.addFrameInitializer(graphTypeManager);
            }
        };
    }
}
