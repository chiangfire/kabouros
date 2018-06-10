package com.firecode.kabouros.jdbc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

/**
 * @author jiang
 *
 * @param <T>
 */
public class Page<T> implements Iterable<T>,Serializable{
	
	private static final long serialVersionUID = 4831811669388154375L;
	
	private final List<T> content = new ArrayList<T>();
	private final PageRequest pageRequest;
	private final long totalCounts;
	private final long totalPages;
	
	public Page(List<T> content) {
		
		this(content, null, null == content ? 0 : content.size());
	}

	public Page(List<T> content, PageRequest pageRequest,long total) {
		Assert.notNull(content, "Content must not be null!");
		this.content.addAll(content);
		this.pageRequest = pageRequest;
		this.totalCounts = total;
		this.totalPages = getPageSize() == 0 ? 1 : (long) Math.ceil((double) totalCounts / (double) getPageSize());
	}
	
	public long getTotalPages() {
		
		return totalPages;
	}
	
	public long getTotalCounts() {
		
		return totalCounts;
	}
	
	public int getPageSize() {
		
		return pageRequest == null ? 0 : pageRequest.getPageSize();
	}
	
	public int getPageNumber() {
		
		return pageRequest == null ? 0 : pageRequest.getPageNumber();
	}
	
	public boolean hasNext() {
		
		return getPageNumber() + 1 < getTotalPages();
	}
	
	public int getNumberOfElements() {
		
		return content.size();
	}
	
	public boolean hasPrevious() {
		
		return getPageNumber() > 0;
	}
	
	public PageRequest nextPageable() {
		return hasNext() ? pageRequest.next() : null;
	}

	public PageRequest previousPageable() {
		if (hasPrevious()) {
			return pageRequest.previousOrFirst();
		}
		return null;
	}
	
	public List<T> getContent() {
		
		return Collections.unmodifiableList(content);
	}
	
	public Sort getSort() {
		
		return pageRequest == null ? null : pageRequest.getSort();
	}

	@Override
	public Iterator<T> iterator() {
		
		return content.iterator();
	}
	
	public <S> Page<S> map(Converter<? super T, ? extends S> converter) {
		Assert.notNull(converter, "Converter must not be null!");
		List<S> result = new ArrayList<S>(content.size());
		for (T element : this) {
			result.add(converter.convert(element));
		}
		return new Page<S>(result, pageRequest, totalCounts);
	}

	@Override
	public String toString() {
		return "Page [content=" + content + ", pageRequest=" + pageRequest + ", totalCounts=" + totalCounts
				+ ", totalPages=" + totalPages + ", pageSize="+this.getPageSize() +", pageNumber="+ this.getPageNumber()+"]";
	}
	
}
