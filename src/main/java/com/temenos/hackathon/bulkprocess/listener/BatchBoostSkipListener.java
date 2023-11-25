package com.temenos.hackathon.bulkprocess.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import com.temenos.hackathon.bulkprocess.model.Hacker;

/**
 * TODO: Document me!
 *
 * @author mohammed.mehran
 *
 */
@Component("BatchBoostSkipListener")
public class BatchBoostSkipListener implements SkipListener<Hacker, Hacker>{

    public static Logger logger = LogManager.getLogger(BatchBoostSkipListener.class);
    
    @Override
    public void onSkipInRead(Throwable t) {
        
        logger.error("Exception occured while reading a record. ", t);
        
    }

    @Override
    public void onSkipInWrite(Hacker item, Throwable t) {
        
        logger.error("Exception occured while writing a record.", t);
        
    }

    @Override
    public void onSkipInProcess(Hacker item, Throwable t) {
        
        logger.error("Exception occured while processing a record.", t);
        
    }

}
