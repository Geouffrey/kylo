/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.common;

import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_NAME;

import java.util.List;

import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

/**
 * An abstract processor that can be configured with the feed canteory and name and
 * which will look up the feed's ID.
 * 
 * @author Sean Felten
 */
public abstract class FeedProcessor extends BaseProcessor {
    private static final Logger log = LoggerFactory.getLogger(FeedProcessor.class);

    private transient MetadataProviderService providerService;
    
    @OnScheduled
    public void scheduled(ProcessContext context) {
        this.providerService = context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class);
    }
    
    public FlowFile initialize(ProcessContext context, ProcessSession session, FlowFile flowFile) {
        return ensureFeedId(context, session, flowFile);
    }
    
    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(CommonProperties.METADATA_SERVICE);
        list.add(CommonProperties.FEED_CATEGORY);
        list.add(CommonProperties.FEED_NAME);
    }
    
    protected MetadataProvider getMetadataProvider() {
        return this.providerService.getProvider();
    }
    
    protected MetadataRecorder getMetadataRecorder() {
        return this.providerService.getRecorder();
    }
    
    protected String getFeedId(ProcessContext context, FlowFile flowFile) {
        String feedId = flowFile.getAttribute("feedId");
        
        if (feedId == null) {
            final String category = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
            final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
            
            log.info("Resolving category {} and feed {}", category, feedName);
            
            feedId = getMetadataProvider().getFeedId(category, feedName);

            log.info("Resolving id {} for category {} and feed {}", feedId, category, feedName);
            
            return feedId;
        } else {
            return feedId;
        }
    }
    
    protected FlowFile ensureFeedId(ProcessContext context, ProcessSession session, FlowFile flowFile) {
        String feedId = flowFile.getAttribute("feedId");
        
        if (feedId == null) {
            final String category = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
            final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
            
            log.info("Resolving category {} and feed {}", category, feedName);
            
            feedId = getMetadataProvider().getFeedId(category, feedName);

            log.info("Resolving id {} for category {} and feed {}", feedId, category, feedName);
            
            return session.putAttribute(flowFile, "feedId", feedId);
        } else {
            return flowFile;
        }
    }

}