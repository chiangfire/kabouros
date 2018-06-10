package com.firecode.kabouros.jdbc.query.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.firecode.kabouros.jdbc.domain.Sort.Direction;
import com.firecode.kabouros.jdbc.domain.Sort.Order;

/**
 * @see https://github.com/spring-projects/spring-data-commons/blob/master/src/main/java/org/springframework/data/repository/query/parser/PartTree.java
 * 
 * Class to parse a {@link String} into a tree or {@link OrPart}s consisting of simple {@link Part} instances in turn.
 * Takes a domain class as well to validate that each of the {@link Part}s are referring to a property of the domain
 * class. The {@link PartTree} can then be used to build queries based on its API instead of parsing the method name for
 * each query execution.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class PartTree {

	
	/*
	 * We look for a pattern of: keyword followed by
	 *
	 *  an upper-case letter that has a lower-case variant \p{Lu}
	 * OR
	 *  any other letter NOT in the BASIC_LATIN Uni-code Block \\P{InBASIC_LATIN} (like Chinese, Korean, Japanese, etc.).
	 *
	 * @see <a href="http://www.regular-expressions.info/unicode.html">http://www.regular-expressions.info/unicode.html</a>
	 * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#ubc">Pattern</a>
	 */
	private static final String KEYWORD_TEMPLATE = "(%s)(?=(\\p{Lu}|\\P{InBASIC_LATIN}))";
	private static final String QUERY_PATTERN = "find|read|get|query|stream";
	private static final String COUNT_PATTERN = "count";
	private static final String EXISTS_PATTERN = "exists";
	private static final String DELETE_PATTERN = "delete|remove";
	private static final Pattern PREFIX_TEMPLATE = Pattern.compile("^(" + QUERY_PATTERN + "|" + COUNT_PATTERN + "|"+ EXISTS_PATTERN + "|" + DELETE_PATTERN + ")((\\p{Lu}.*?))??By");
			

	/**
	 * The subject, for example "findDistinctUserByNameOrderByAge" would have the subject "DistinctUser".
	 */
	private final Subject subject;

	/**
	 * The subject, for example "findDistinctUserByNameOrderByAge" would have the predicate "NameOrderByAge".
	 */
	private final Predicate predicate;
	
	/**
	 * @author jiang
	 */
	private final String source;
	
	public PartTree(String source) {
		Assert.notNull(source, "Source must not be null");
		this.source = source;
		Matcher matcher = PREFIX_TEMPLATE.matcher(source);
		if (!matcher.find()) {
			this.subject = new Subject(null);
			this.predicate = new Predicate(source);
		} else {
			this.subject = new Subject(matcher.group(0));
			this.predicate = new Predicate(source.substring(matcher.group().length()));
		}
	}

	/**
	 * @author jiang
	 */
	public List<Order> getOrders() {

		return predicate.getOrders();
	}
	
	/**
	 * @author jiang
	 */
	public Keyword getSubjectKeyword(){
		
		return subject.getSubjectKeyword();
	}

	/**
	 * Return {@literal true} if the create {@link PartTree} is meant to be used for a query with limited maximal results.
	 * 
	 * @return
	 * @since 1.9
	 */
	public boolean isLimiting() {

		return getMaxResults() != null;
	}

	/**
	 * maximum number of results
	 * 
	 * @return
	 */
	public Integer getMaxResults() {

		return subject.getMaxResults();
	}

	public boolean isDistinct() {

		return subject.isDistinct();
	}

	/**
	 * @author jiang
	 */
	public List<OrPart> getOrParts() {

		return this.predicate.getNodes();
	}
	
	/**
	 * Returns whether a count projection shall be applied.
	 * 
	 * @return
	 */
	public boolean isCountProjection() {
		
		return subject.isCountProjection();
	}
	/**
	 * Returns whether an exists projection shall be applied.
	 *
	 * @return
	 * @since 1.13
	 */
	public boolean isExistsProjection() {
		
		return subject.isExistsProjection();
	}

	/**
	 * @author jiang
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Splits the given text at the given keywords. Expects camel-case style to only match concrete keywords and not
	 * derivatives of it.
	 * 
	 * @param text the text to split
	 * @param keyword the keyword to split around
	 * @return an array of split items
	 */
	private static String[] split(String text, String keyword) {

		Pattern pattern = Pattern.compile(String.format(KEYWORD_TEMPLATE, keyword));
		return pattern.split(text);
	}

	/**
	 * A part of the parsed source that results from splitting up the resource around {@literal Or} keywords. Consists of
	 * {@link Part}s that have to be concatenated by {@literal And}.
	 * @author jiang
	 */
	public static class OrPart {

		private final List<Part> children = new ArrayList<Part>();

		OrPart(String source, boolean alwaysIgnoreCase) {
			String[] split = split(source, "And");
			for (String part : split) {
				if (StringUtils.hasText(part)) {
					children.add(new Part(part, alwaysIgnoreCase));
				}
			}
		}

		public List<Part> getChildren() {

			return this.children;
		}

		@Override
		public String toString() {

			return StringUtils.collectionToDelimitedString(children, " and ");
		}
	}

	/**
	 * Represents the subject part of the query. E.g. {@code findDistinctUserByNameOrderByAge} would have the subject
	 * {@code DistinctUser}.
	 * 
	 * @author Phil Webb
	 * @author Oliver Gierke
	 * @author Christoph Strobl
	 * @author Thomas Darimont
	 */
	private static class Subject {

		private static final String DISTINCT = "Distinct";
		private static final Pattern COUNT_BY_TEMPLATE = Pattern.compile("^count(\\p{Lu}.*?)??By");
		private static final Pattern EXISTS_BY_TEMPLATE = Pattern.compile("^(" + EXISTS_PATTERN + ")(\\p{Lu}.*?)??By");
		private static final Pattern DELETE_BY_TEMPLATE = Pattern.compile("^(" + DELETE_PATTERN + ")(\\p{Lu}.*?)??By");
		private static final String LIMITING_QUERY_PATTERN = "(First|Top)(\\d*)?";
		private static final Pattern LIMITED_QUERY_TEMPLATE = Pattern.compile("^(" + QUERY_PATTERN + ")(" + DISTINCT + ")?" + LIMITING_QUERY_PATTERN + "(\\p{Lu}.*?)??By");
      
		private final Integer maxResults;
		private final boolean distinct;
		private final boolean count;
		private final boolean exists;
        /**
         *  @author jiang
         */
		private final Keyword keyword;

		public Subject(String subject) {
			if (matches(subject, DELETE_BY_TEMPLATE)) {
				this.keyword = Keyword.DELETE;
			} else {
				this.keyword = Keyword.SELECT;
			}
			this.maxResults = returnMaxResultsIfFirstKMotifOrNull(subject);
			this.count = matches(subject, COUNT_BY_TEMPLATE);
			this.exists = matches(subject, EXISTS_BY_TEMPLATE);
			this.distinct = subject == null ? false : subject.contains(DISTINCT);
		}

		private Integer returnMaxResultsIfFirstKMotifOrNull(String motif) {
			if (motif == null) {
				return null;
			}
			Matcher grp = LIMITED_QUERY_TEMPLATE.matcher(motif);

			if (!grp.find()) {
				return null;
			}
			return StringUtils.hasText(grp.group(4)) ? Integer.valueOf(grp.group(4)) : 1;
		}
		
		public Keyword getSubjectKeyword(){
			
			return keyword;
		}

		public Integer getMaxResults() {

			return maxResults;
		}

		public boolean isDistinct() {
			return distinct;
		}
		
		public boolean isCountProjection() {
			
			return count;
		}
		
		public boolean isExistsProjection() {
			
			return exists;
		}

		private final boolean matches(String motif, Pattern pattern) {

			return motif == null ? false : pattern.matcher(motif).find();
		}
	}

	/**
	 * @author jiang
	 */
	public static enum Keyword {
		SELECT,
		UPDATE,
		DELETE;
		
		public static Keyword fromString(String value) {
			try {
				return Keyword.valueOf(value.toUpperCase(Locale.US));
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format("Invalid value '%s' for orders given! Has to be  data base Keyword (case insensitive).", value), e);
			}
		}

		public static Keyword fromStringOrNull(String value) {
			try {
				return fromString(value);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

	}

	/**
	 * Represents the predicate part of the query.
	 * 
	 * @author Oliver Gierke
	 * @author Phil Webb
	 */
	private static class Predicate {

        /**
         *  @author jiang
         */
		private static final String BLOCK_SPLIT = "(?<=Asc|Desc)(?=\\p{Lu})";
		private static final Pattern DIRECTION_SPLIT = Pattern.compile("(.+?)(Asc|Desc)?$");
		private static final String INVALID_ORDER_SYNTAX = "Invalid order syntax for part %s!";
		private final List<Order> orders;
        //----------------------------------------------------------------------------------//
		private static final Pattern ALL_IGNORE_CASE = Pattern.compile("AllIgnor(ing|e)Case");
		private static final String ORDER_BY = "OrderBy";
		private final List<OrPart> nodes = new ArrayList<OrPart>();
		private boolean alwaysIgnoreCase;
        /**
         *  @author jiang
         */
		public Predicate(String predicate) {

			String[] parts = split(detectAndSetAllIgnoreCase(predicate), ORDER_BY);

			if (parts.length > 2) {
				throw new IllegalArgumentException("OrderBy must not be used more than once in a method name!");
			}
			buildTree(parts[0]);
			this.orders = parts.length == 2 ? this.buildOrders(parts[1]) : null;
		}

		private String detectAndSetAllIgnoreCase(String predicate) {
			Matcher matcher = ALL_IGNORE_CASE.matcher(predicate);
			if (matcher.find()) {
				alwaysIgnoreCase = true;
				predicate = predicate.substring(0, matcher.start())+ predicate.substring(matcher.end(), predicate.length());
			}
			return predicate;
		}

		private void buildTree(String source) {
			String[] split = split(source, "Or");
			for (String part : split) {
				nodes.add(new OrPart(part, alwaysIgnoreCase));
			}
		}

		private List<Order> buildOrders(String clause) {
			List<Order> orders = new ArrayList<Order>();
			for (String part : clause.split(BLOCK_SPLIT)) {
				Matcher matcher = DIRECTION_SPLIT.matcher(part);
				if (!matcher.find()) {
					throw new IllegalArgumentException(String.format(INVALID_ORDER_SYNTAX, part));
				}
				String propertyString = matcher.group(1);
				String directionString = matcher.group(2);

				if (Order.DIRECTION_KEYWORDS.contains(propertyString) && directionString == null) {
					throw new IllegalArgumentException(String.format(INVALID_ORDER_SYNTAX, part));
				}
				Direction direction = StringUtils.hasText(directionString) ? Direction.fromString(directionString)
						: null;
				orders.add(new Order(direction, StringUtils.uncapitalize(propertyString)));
			}
			return orders;
		}
        /**
         *  @author jiang
         */
		public List<Order> getOrders() {

			return this.orders;
		}
        /**
         *  @author jiang
         */
		public List<OrPart> getNodes() {
			return this.nodes;
		}
	}
	
}
