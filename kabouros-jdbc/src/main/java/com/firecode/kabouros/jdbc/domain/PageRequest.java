package com.firecode.kabouros.jdbc.domain;

import com.firecode.kabouros.jdbc.domain.Sort.Direction;
/**
 * @author jiang
 */
public class PageRequest {
	
	public static final String NAME = PageRequest.class.getName();
	public static final String START_PATH = "_start";
	public static final String OFFSET_PATH = "_offset";
	
	
	private int pageNumber;
	
	private int pageSize;
	
	private Sort sort;
	
	
	public PageRequest(int pageNumber, int pageSize){
		
		this(pageNumber,pageSize,null);
	}
	
	public PageRequest(int pageNumber, int pageSize, Direction direction, String... properties) {
		
		this(pageNumber, pageSize, new Sort(direction, properties));
	}
	
	public PageRequest(int pageNumber, int pageSize, Sort sort) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.sort = sort;
	}
	
	public int getPageNumber() {
		
		return pageNumber < 1 ? 1 : pageNumber;
	}

	public int getPageSize() {
		
		return pageSize;
	}
	
	public long getStart(){
		
		return (getPageNumber() - 1) * pageSize;
	}

	public int getOffset() {
		
		return getPageNumber() * pageSize;
	}

	public Sort getSort() {
		return sort;
	}
	
	public PageRequest previous() {
		
		return getPageNumber() == 0 ? this : new PageRequest(getPageNumber() - 1, getPageSize(), getSort());
	}

	public PageRequest next(){
		
		return new PageRequest(getPageNumber() + 1, getPageSize(), getSort());
	}

	public PageRequest previousOrFirst() {
		
		return hasPrevious() ? previous() : first();
	}

	public PageRequest first() {
		
		return new PageRequest(0, getPageSize(), getSort());
	}

	public boolean hasPrevious() {
		
		return pageNumber > 0;
	}
	
	public boolean isErrorPageNumber(double totalCounts){
		
		return pageSize == 0 || getPageNumber() > (long) Math.ceil(totalCounts / (double) pageSize);
	}
	
}
