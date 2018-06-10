package com.firecode.kabouros.jdbc.vendor;

import com.firecode.kabouros.jdbc.domain.PageRequest;

/**
 * @author jiang
 *
 */
public enum Database{

	MYSQL,

	ORACLE;
	
	public LimitHandler getLimitHandler(){
		switch (this) {
			case ORACLE: return new OracleLimitHandler();
			
			default: return new MySqlLimitHandler();
		}
	}
	
	
	private class MySqlLimitHandler implements LimitHandler{
		@Override
		public String processSql(String sql) {
			
			return String.join("", sql," LIMIT :",PageRequest.START_PATH,",:",PageRequest.OFFSET_PATH);
		}

		@Override
		public int getOffset(PageRequest pr) {
			
			return pr.getPageSize();
		}

		@Override
		public String supportLimitOne(String sql) {
			
			return String.join(" ", sql,"LIMIT 1");
		}
	}
	
	private class OracleLimitHandler implements LimitHandler{
		@Override
		public String processSql(String sql) {
			StringBuilder sb = new StringBuilder("SELECT A.* FROM (");
			sb.append("SELECT B.*,ROWNUM ROW_NUM FROM (").append(sql);
			sb.append(") B WHERE ROWNUM <= :").append(PageRequest.OFFSET_PATH);
			sb.append(") A WHERE A.ROW_NUM > :").append(PageRequest.START_PATH);
			return sb.toString();
		}

		@Override
		public int getOffset(PageRequest pr) {
			
			return pr.getOffset();
		}

		@Override
		public String supportLimitOne(String sql) {
			
			return sql;
		}
	}

}
