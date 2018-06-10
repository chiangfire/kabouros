package com.firecode.kabouros.jdbc.vendor;

import com.firecode.kabouros.jdbc.domain.PageRequest;

/**
 * @author jiang
 */
public interface LimitHandler {
	
	public String processSql(String sql);
	
	public int getOffset(PageRequest pr);
	
	public String supportLimitOne(String sql);

}
