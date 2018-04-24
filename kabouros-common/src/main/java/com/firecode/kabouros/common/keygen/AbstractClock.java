package com.firecode.kabouros.common.keygen;

public abstract class AbstractClock {
	
    public static AbstractClock systemClock() {
    	
        return new SystemClock();
    }
    
    public abstract long millis();
    
    private static final class SystemClock extends AbstractClock {
    
        @Override
        public long millis() {
        	
            return System.currentTimeMillis();
        }
    }
}
