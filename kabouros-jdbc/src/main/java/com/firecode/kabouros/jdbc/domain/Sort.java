package com.firecode.kabouros.jdbc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.firecode.kabouros.jdbc.domain.Sort.Order;

/**
 * @url https://github.com/spring-projects/spring-data-commons/blob/master/src/main/java/org/springframework/data/domain/Sort.java
 * 
 * Sort option for queries. You have to provide at least a list of properties to sort for that must not include
 * {@literal null} or empty strings. The direction defaults to {@link Sort#DEFAULT_DIRECTION}.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
public class Sort implements Iterable<Order>, Serializable {


	private static final long serialVersionUID = -1766445276374951153L;

	public static final Direction DEFAULT_DIRECTION = Direction.ASC;

	private final List<Order> orders;

	/**
	 * Creates a new {@link Sort} instance using the given {@link Order}s.
	 * 
	 * @param orders must not be {@literal null}.
	 */
	public Sort(Order... orders) {
		
		this(Arrays.asList(orders));
	}

	/**
	 * Creates a new {@link Sort} instance.
	 * 
	 * @param orders must not be {@literal null} or contain {@literal null}.
	 */
	public Sort(List<Order> orders) {
		Assert.notEmpty(orders, "The sort property is empty.");
		this.orders = orders;
	}

	/**
	 * Creates a new {@link Sort} instance. Order defaults to {@value Direction#ASC}.
	 * 
	 * @param properties must not be {@literal null} or contain {@literal null} or empty strings
	 */
	public Sort(String... properties) {
		
		this(DEFAULT_DIRECTION, properties);
	}

	
	/**
	 * Creates a new {@link Sort} instance.
	 * 
	 * @param direction defaults to {@linke Sort#DEFAULT_DIRECTION} (for {@literal null} cases, too)
	 * @param properties must not be {@literal null}, empty or contain {@literal null} or empty strings.
	 */
	public Sort(Direction direction, String... properties) {
		Assert.notEmpty(properties, "The sort property is empty.");
		this.orders = new ArrayList<Order>(properties.length);
		for (String property : properties) {
			this.orders.add(new Order(direction, property));
		}
	}
	
	/**
	 * Returns a new {@link Sort} consisting of the {@link Order}s of the current {@link Sort} combined with the given
	 * ones.
	 * 
	 * @param sort can be {@literal null}.
	 * @return
	 */
	public Sort and(Sort sort) {
		if (sort == null) {
			return this;
		}
		ArrayList<Order> these = new ArrayList<Order>(this.orders);

		for (Order order : sort) {
			these.add(order);
		}
		return new Sort(these);
	}

	/**
	 * Returns the order registered for the given property.
	 * 
	 * @param property
	 * @return
	 */
	public Order getOrderFor(String property) {
		for (Order order : this) {
			if (order.getProperty().equals(property)) {
				return order;
			}
		}
		return null;
	}

	public Iterator<Order> iterator() {
		return this.orders.iterator();
	}
	
	/**
	 * Enumeration for sort directions.
	 * 
	 * @author Oliver Gierke
	 */
	public static enum Direction {

		ASC, DESC;

		/**
		 * Returns whether the direction is ascending.
		 * 
		 * @return
		 * @since 1.13
		 */
		public boolean isAscending() {
			return this.equals(ASC);
		}

		/**
		 * Returns whether the direction is descending.
		 * 
		 * @return
		 * @since 1.13
		 */
		public boolean isDescending() {
			return this.equals(DESC);
		}

		/**
		 * Returns the {@link Direction} enum for the given {@link String} value.
		 * 
		 * @param value
		 * @throws IllegalArgumentException in case the given value cannot be parsed into an enum value.
		 * @return
		 */
		public static Direction fromString(String value) {
			try {
				return Direction.valueOf(value.toUpperCase(Locale.US));
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format("Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
			}
		}

		/**
		 * Returns the {@link Direction} enum for the given {@link String} or null if it cannot be parsed into an enum
		 * value.
		 * 
		 * @param value
		 * @return
		 */
		public static Direction fromStringOrNull(String value) {
			try {
				return fromString(value);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	/**
	 * PropertyPath implements the pairing of an {@link Direction} and a property. It is used to provide input for
	 * {@link Sort}
	 * 
	 * @author Oliver Gierke
	 * @author Kevin Raymond
	 */
	public static class Order implements Serializable {
		
		public static final Set<String> DIRECTION_KEYWORDS = new HashSet<String>(Arrays.asList("Asc", "Desc"));

		private static final long serialVersionUID = 1522511010900108987L;
		private static final boolean DEFAULT_IGNORE_CASE = false;

		private final Direction direction;
		private final String property;
		private final boolean ignoreCase;


		/**
		 * Creates a new {@link Order} instance. Takes a single property. Direction defaults to
		 * {@link Sort#DEFAULT_DIRECTION}.
		 * 
		 * @param property must not be {@literal null} or empty.
		 */
		public Order(String property) {
			this(DEFAULT_DIRECTION, property);
		}
		
		/**
		 * Creates a new {@link Order} instance. if order is {@literal null} then order defaults to
		 * {@link Sort#DEFAULT_DIRECTION}
		 * 
		 * @param direction can be {@literal null}, will default to {@link Sort#DEFAULT_DIRECTION}
		 * @param property must not be {@literal null} or empty.
		 * @author jiang
		 */
		public Order(Direction direction, String property) {
			this(direction, property, DEFAULT_IGNORE_CASE);
		}

		/**
		 * Creates a new {@link Order} instance. if order is {@literal null} then order defaults to
		 * {@link Sort#DEFAULT_DIRECTION}
		 * 
		 * @param direction can be {@literal null}, will default to {@link Sort#DEFAULT_DIRECTION}
		 * @param property must not be {@literal null} or empty.
		 * @param ignoreCase true if sorting should be case insensitive. false if sorting should be case sensitive.
		 * @since 1.7
		 * @author jiang
		 */
		private Order(Direction direction, String property, boolean ignoreCase) {

			if (!StringUtils.hasText(property)) {
				throw new IllegalArgumentException("Property must not null or empty!");
			}

			this.direction = direction == null ? DEFAULT_DIRECTION : direction;
			this.property = property;
			this.ignoreCase = ignoreCase;
		}

		/**
		 * Returns the order the property shall be sorted for.
		 * 
		 * @return
		 */
		public Direction getDirection() {
			return direction;
		}

		/**
		 * Returns the property to order for.
		 * 
		 * @return
		 */
		public String getProperty() {
			return property;
		}

		/**
		 * Returns whether sorting for this property shall be ascending.
		 * 
		 * @return
		 */
		public boolean isAscending() {
			return this.direction.isAscending();
		}

		/**
		 * Returns whether sorting for this property shall be descending.
		 * 
		 * @return
		 * @since 1.13
		 */
		public boolean isDescending() {
			return this.direction.isDescending();
		}

		/**
		 * Returns whether or not the sort will be case sensitive.
		 * 
		 * @return
		 */
		public boolean isIgnoreCase() {
			return ignoreCase;
		}

		/**
		 * Returns a new {@link Order}
		 * 
		 * @param properties must not be {@literal null} or empty.
		 * @return
		 * @since 1.13
		 * @author jiang
		 */
		public Sort withProperties(String... properties) {
			return new Sort(this.direction, properties);
		}
	}
}
