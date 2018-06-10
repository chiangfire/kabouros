package com.firecode.kabouros.jdbc.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.firecode.kabouros.jdbc.EntityPropertyHelper;
import com.firecode.kabouros.jdbc.EntityPropertyHelper.EntityProperty;
import com.firecode.kabouros.jdbc.RepositoryProxyManager;
import com.firecode.kabouros.jdbc.annotation.Table;
import com.firecode.kabouros.jdbc.domain.Page;
import com.firecode.kabouros.jdbc.domain.PageRequest;
import com.firecode.kabouros.jdbc.domain.ProperHelper;
import com.firecode.kabouros.jdbc.domain.Sort.Order;
import com.firecode.kabouros.jdbc.query.parser.Part;
import com.firecode.kabouros.jdbc.query.parser.PartTree;
import com.firecode.kabouros.jdbc.query.parser.PartTree.Keyword;
import com.firecode.kabouros.jdbc.query.parser.PartTree.OrPart;
import com.firecode.kabouros.jdbc.vendor.Database;
import com.firecode.kabouros.jdbc.vendor.LimitHandler;
import com.firecode.kabouros.jdbc.xml.NamedQueryCandidateComponentProvider;
import static com.firecode.kabouros.common.util.ObjectUtil.objectToList;
import static com.firecode.kabouros.common.util.ClassUtil.findCertainGenericType;
import static com.firecode.kabouros.common.util.ClassUtil.findGenericReturnType;

/**
 * @author jiang
 */
public class RelationalRepositoryProxyManager implements RepositoryProxyManager{
	
	private static final Log LOG = LogFactory.getLog(RelationalRepositoryProxyManager.class);
	
	private final NamedParameterJdbcOperations namedParameterJdbcOperations;
	
	private final LimitHandler limitHandler;
	
	private final Map<String,String>  queryMap = new HashMap<>();
	
	private final Map<String, Method> resultSetMethodMap = new HashMap<String, Method>();
	
	{
		resultSetMethodMap.put(String.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getString", String.class));
		resultSetMethodMap.put(Integer.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getInt", String.class));
		resultSetMethodMap.put("INT", ReflectionUtils.findMethod(ResultSet.class, "getInt", String.class));
		resultSetMethodMap.put(Long.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getLong", String.class));
		resultSetMethodMap.put(Boolean.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getBoolean", String.class));
		resultSetMethodMap.put(Byte.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getByte", String.class));
		resultSetMethodMap.put(Short.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getShort", String.class));
		resultSetMethodMap.put(Float.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getFloat", String.class));
		resultSetMethodMap.put(Double.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getDouble", String.class));
		resultSetMethodMap.put(BigDecimal.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getBigDecimal", String.class));
		resultSetMethodMap.put(Byte[].class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getBytes", String.class));
		resultSetMethodMap.put(Date.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getTimestamp", String.class));
		resultSetMethodMap.put(Time.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getTime", String.class));
		resultSetMethodMap.put(Timestamp.class.getSimpleName().toUpperCase(), ReflectionUtils.findMethod(ResultSet.class, "getTimestamp", String.class));
	}
	
	public RelationalRepositoryProxyManager(DataSource dataSource){
		this(dataSource,null);
	}
	
	public RelationalRepositoryProxyManager(DataSource dataSource,Database database){
		this(dataSource,null,null);
	}
	
	public RelationalRepositoryProxyManager(DataSource dataSource,Database database,String namedQueryPath){
		this(dataSource,database,namedQueryPath,false);
	}
	
	public RelationalRepositoryProxyManager(DataSource dataSource,Database database,String namedQueryPath,boolean failFast){
		Assert.notNull(dataSource, "data source is empty.");
		this.namedParameterJdbcOperations = new NamedParameterJdbcTemplate(dataSource);
		queryMap.putAll(new NamedQueryCandidateComponentProvider().loadNamedQueryDocument(namedQueryPath, failFast));
		if(null == database) database = Database.MYSQL;
		this.limitHandler = database.getLimitHandler();
	}
	
	public <T> int replaceSave(T t){
		
		return insert(t, "REPLACE");
	}
	
	public <T> int save(T t) {
		
		return insert(t,"INSERT");
	}
	
	private <T> int insert(T t,String insertType){
		Assert.notNull(t, "save object that be empty.");
		List<?> datas = objectToList(t);
		Assert.notEmpty(datas, "save collection that be empty.");
		String tableName = getTableName(datas.get(0).getClass());
	    StringBuilder sql = new StringBuilder(insertType).append(" INTO ").append(tableName).append(" (");
	    StringBuilder placeholder = new StringBuilder();
	    List<EntityProperty> entityPropertys = EntityPropertyHelper.getEntityPropertys(datas.get(0).getClass(),true);
	    if(!entityPropertys.isEmpty()){
	    	int fieldSize = entityPropertys.size();
	    	int dataSize = datas.size();
	    	MapSqlParameterSource[] params = new MapSqlParameterSource[dataSize];
	    	for(int i=0;i<dataSize;i++){
	    		Object object = datas.get(i);
	    		if(null != object){
	    			MapSqlParameterSource param = new MapSqlParameterSource();
					for(int j=0; j < fieldSize; j++){
						EntityProperty entityProperty = entityPropertys.get(j);
						if(0 == i){
				    		sql.append(entityProperty.getColumnName());
				    		placeholder.append(":").append(entityProperty.getName());
				    		if(j < fieldSize - 1){
				    			 sql.append(",");
				    			 placeholder.append(",");
				    		}
						}
						//custom data generation strategy
						if(null != entityProperty.getGenerator()){
							Serializable value = entityProperty.getGenerator().generate();
							param.addValue(entityProperty.getName(),value);
							entityProperty.setPropertyValue(object, value);
							continue;
						}
						//property value
						param.addValue(entityProperty.getName(), entityProperty.getPropertyValue(object));
					}
					params[i] = param;
	    		}
	    	}
	    	String saveSql = sql.append(") ").append("VALUES (").append(placeholder).append(")").toString();
	    	LOG.info(saveSql);
	    	return superpositionInt(namedParameterJdbcOperations.batchUpdate(saveSql, params));
	    }
		return superpositionInt();
	}
	
	public <T> int update(T t){
		Assert.notNull(t, "update object that be empty.");
		ProperHelper proper = (t instanceof ProperHelper) ? (ProperHelper)t : null;
		List<?> lists = (null == proper) ? objectToList(t) : proper.getContent();
		if(null != lists && !lists.isEmpty()){
			Object object = lists.get(0);
			if(null != object){
				String tableName = getTableName(object.getClass());
				List<EntityProperty> entityPropertys = EntityPropertyHelper.getEntityPropertys(object.getClass(),true);
				if(!entityPropertys.isEmpty()){
					int paramLength = lists.size();
					StringBuilder sb = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
					Map<String,String> primarykey = new HashMap<>();
					MapSqlParameterSource[] params = new MapSqlParameterSource[paramLength];
					for(int i=0;i<paramLength;i++){
						Object entity = lists.get(i);
						params[i] = new MapSqlParameterSource();
						for(int j=0,size=entityPropertys.size();j<size;j++){
							EntityProperty entityProperty = entityPropertys.get(j);
							String name = entityProperty.getName();
							//filtering attributes
							if(null != proper){
								//valid and not the primary key  <0 == i representation of splicing SQL>
								if(proper.isEffective(name)&&!entityProperty.isPrimarykey()){
									params[i].addValue(name, entityProperty.getPropertyValue(entity));
									if(0 == i) sb.append(entityProperty.getColumnName()).append(" = :").append(name).append(",");
								}
								if(entityProperty.isPrimarykey()){
									params[i].addValue(name, entityProperty.getPropertyValue(entity));
									if(0 == i) primarykey.put(entityProperty.getColumnName(),name);
								}
								continue;
							}
							//normal
							params[i].addValue(name, entityProperty.getPropertyValue(entity));
							if(0 == i){
								if(!entityProperty.isPrimarykey()){
									sb.append(entityProperty.getColumnName()).append(" = :").append(name).append(",");
								}else{
									primarykey.put(entityProperty.getColumnName(),name);
								}
							}
						}
					}
					if(sb.lastIndexOf(",") != -1){
						StringBuilder sql = sb.deleteCharAt(sb.length() -1);
						sql.append(" WHERE 1 = 1 ");
						if(primarykey.isEmpty()) new IllegalArgumentException(String.join(" ", t.getClass().getName(),"no mapped primary key."));
						for(String key:primarykey.keySet()){
							sql.append(" AND ").append(key).append(" = :").append(primarykey.get(key));
						}
						String updateSql = sql.toString();
						LOG.info(updateSql);
						return superpositionInt(namedParameterJdbcOperations.batchUpdate(updateSql, params));
					}
				}
			}
		}
		return superpositionInt();
	}
	
	@Override
	public Object invokeProxyMethod(Method method, Object[] args) throws Throwable {
		if("save".equals(method.getName())) return save(args[0]);
		
		if("replaceSave".equals(method.getName())) return replaceSave(args[0]);
		
		if("update".equals(method.getName())) return update(args[0]);
		
		if("count".equals(method.getName())) return count(method, null, null);
		
		Class<?> declaringClass = method.getDeclaringClass();
		MapSqlParameterSource paramSource = createMapSqlParameterSource(method,args);
		//named query
		String sql = queryMap.get(String.join(".", declaringClass.getName(),method.getName()));
		if(!StringUtils.isEmpty(sql)){
			return processor(sql, method,paramSource);
		}
		//method name query
		Class<?> clazz = findCertainGenericType(method, Table.class,true);
		String tableName = getTableName(clazz);
		PartTree pt = new PartTree(method.getName());
		StringBuilder sb = new StringBuilder();
		if(pt.isCountProjection() || pt.isExistsProjection()){
			sql = buildCondition(processCountSql(null,tableName),method,pt).toString();
			LOG.info(sql);
			long count = namedParameterJdbcOperations.queryForObject(sql, paramSource, long.class);
			return pt.isCountProjection() ? count : ((0 != count) ? true : false);
		}
		
		if(Keyword.DELETE.equals(pt.getSubjectKeyword())){
			sb.append(Keyword.DELETE).append(" FROM ").append(tableName).append(" WHERE ");
			return processor(buildCondition(sb,method,pt).toString(),method,paramSource);
		}
		
		List<EntityProperty> entityPropertys = EntityPropertyHelper.getEntityPropertys(clazz,true);
		Assert.notEmpty(entityPropertys, String.join(" ", clazz.getName()," not property."));
		sb.append(pt.getSubjectKeyword()).append(" ");
		if(pt.isDistinct()) sb.append(" DISTINCT ");
		sb.append(fieldsToColumns(entityPropertys));
		sb.append(" FROM ").append(tableName).append(" WHERE ");
		if(pt.isLimiting()){
			
		}
		return processor(buildCondition(sb,method,pt).toString(),method,paramSource);
	}
	
	private Object processor(String sql,Method method,MapSqlParameterSource paramSource){
		LOG.info(sql);
		Keyword keyword = Keyword.fromString(sql.substring(0,6));
		Class<?> returnType = method.getReturnType();
		//select sentence
		if(Keyword.SELECT.equals(keyword)){
			//return list value
			if(List.class.isAssignableFrom(returnType)){
				
				return selectList(sql,method,paramSource);
			}
			//return page value
			if(Page.class.isAssignableFrom(returnType)){

				return selectPage(sql,method,paramSource);
			}
			//return map value
			if(Map.class.isAssignableFrom(returnType)){
				
			    return namedParameterJdbcOperations.queryForMap(sql, paramSource);
			}
			//return entity value
			if(!returnType.isPrimitive() && returnType.getSimpleName().lastIndexOf("[]") == -1){
				return namedParameterJdbcOperations.query(sql, paramSource, (ResultSet rs) -> {
					
					return resultSetToObject(rs,returnType);
				});
				/*return namedParameterJdbcOperations.queryForObject(sql,paramSource,(ResultSet rs, int rowNum) -> {
					
					return resultSetToObject(rs,rowNum,returnType);
				});*/
			}
			//return primitive value
			return namedParameterJdbcOperations.queryForObject(sql,paramSource, returnType);
		}
		//update or delete sentence
		if(Keyword.UPDATE.equals(keyword) || Keyword.DELETE.equals(keyword)){
			
			return namedParameterJdbcOperations.update(sql, paramSource);
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object selectPage(String sql,Method method,MapSqlParameterSource paramSource){
		Object paramPageRequest = paramSource.getValue(PageRequest.NAME);
		Assert.notNull(paramPageRequest,String.join(" ","paging queries must have ",PageRequest.NAME,"parameter"));
		long totalCounts = count(method, paramSource,sql);
		PageRequest pageRequest =(PageRequest)paramPageRequest;
		if(pageRequest.isErrorPageNumber(totalCounts)){
			
			return new Page(new ArrayList<>(0),pageRequest,totalCounts);
		}
		List<?> content = selectList(limitHandler.processSql(sql),method,paramSource);
		return new Page(content,pageRequest,totalCounts);
	}
	
	private List<?> selectList(String sql,Method method,MapSqlParameterSource paramSource){
		Class<?> genericType = findGenericReturnType(method,true);
		//return List<Map<String,Object>>
		if(genericType.isAssignableFrom(Map.class)){
			
			return namedParameterJdbcOperations.queryForList(sql, paramSource);
		}
		//return List<T>
		if(!isPrimitive(genericType)){
			return namedParameterJdbcOperations.query(sql,paramSource,(ResultSet rs, int rowNum) -> {
				
				return resultSetToObject(rs,rowNum,genericType);
			});
		}
		//return List<primitive>
		return namedParameterJdbcOperations.queryForList(sql, paramSource, genericType);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private MapSqlParameterSource createMapSqlParameterSource(Method method,Object[] args){
		MapSqlParameterSource params = null;
		if(null != args){
			params = new MapSqlParameterSource();
			Parameter[] parameters = method.getParameters();
			for(int i=0,length=args.length;i<length;i++){
				Object object = args[i];
				if(null == object){
					params.addValue(parameters[i].getName(), null);
					continue;
				}
				Class<?> paramType = object.getClass();
				//Primitive OR List
				if(isPrimitive(paramType) || object instanceof List ||paramType.isAssignableFrom(List.class)){
					params.addValue(parameters[i].getName(),object);
				//Array
				}else if(object instanceof Object[] || paramType.getSimpleName().lastIndexOf("[]") != -1){
					params.addValue(parameters[i].getName(), Arrays.asList(object));
			    //Map
				}else if(object instanceof Map || paramType.isAssignableFrom(Map.class)){
					params.addValues((Map)object);
				//Entity	
				}else{
					//page 
					if(object instanceof PageRequest){
						PageRequest pageRequest = (PageRequest)object;
						params.addValue(PageRequest.START_PATH, pageRequest.getStart());
						params.addValue(PageRequest.OFFSET_PATH, limitHandler.getOffset(pageRequest));
						params.addValue(PageRequest.NAME, object);
						continue;
					}
					BeanPropertySqlParameterSource beanParam = new BeanPropertySqlParameterSource(object);
					String[] readablePropertyNames = beanParam.getReadablePropertyNames();
					if(null != readablePropertyNames){
						for(int j=0,readLength=readablePropertyNames.length;j<readLength;j++){
							String property = readablePropertyNames[j];
							if(!"class".equals(property)){
								params.addValue(property, beanParam.getValue(property));
							}
						}
					}
				}
			}
		}
		return params;
	}
	
	
	
	private StringBuilder buildCondition(StringBuilder sb,Method method,PartTree pt){
		List<OrPart> or = pt.getOrParts();
		for(int i=0,size=or.size();i<size;i++){
			if(i > 0) sb.append(" OR ");
			List<Part> children = or.get(i).getChildren();
			int length = children.size();
			if(i > 0 && length > 1) sb.append("(");
			for(int j=0;j<length;j++){
				Part part = children.get(j);
				if(i == 0 && j == 0){
					if(Part.Type.SIMPLE_PROPERTY.equals(part.getType())&&pt.getSource().equals(part.getUse())){
						throw new IllegalArgumentException(String.join("", "unable to parse ",method.getDeclaringClass().getName(),".",method.getName()));
					}
				}
				if(j > 0) sb.append(" AND ");
				sb.append(part.getColumnName()).append(" ");
				switch (part.getType()) {
				    case SIMPLE_PROPERTY: sb.append("= ").append(":").append(part.getUse()); break;
				    case IS_NULL: sb.append(part.getType().name().replace("_", " ")); break;
				    case IS_NOT_NULL: sb.append(part.getType().name().replace("_", " ")); break;
				    case LESS_THAN: sb.append("< :").append(part.getUse()); break;
				    case LESS_THAN_EQUAL: sb.append("<= :").append(part.getUse()); break;
				    case GREATER_THAN: sb.append("> :").append(part.getUse()); break;
				    case GREATER_THAN_EQUAL: sb.append(">= :").append(part.getUse()); break;
				    case AFTER: sb.append("> :").append(part.getUse()); break;
				    case BEFORE: sb.append("< :").append(part.getUse()); break;
				    case LIKE: sb.append(part.getType().name()).append(" concat('%',:").append(part.getUse()).append(",'%')"); break;
				    case NOT_LIKE: sb.append(part.getType().name().replace("_", " ")).append(" concat('%',:").append(part.getUse()).append(",'%')"); break;
				    case STARTING_WITH: sb.append("LIKE concat('%',:").append(part.getUse()).append(")"); break;
				    case ENDING_WITH: sb.append("LIKE concat(:").append(part.getUse()).append(",'%')"); break;
				    case CONTAINING: sb.append("LIKE").append(" concat('%',:").append(part.getUse()).append(",'%')"); break;
				    case NOT_CONTAINING: sb.append(" NOT LIKE").append(" :").append(part.getUse()); break;
				    case NEGATING_SIMPLE_PROPERTY: sb.append("<> :").append(part.getUse()); break;
				    case IN: sb.append(part.getType().name()).append(" (:").append(part.getUse()).append(")"); break;
				    case NOT_IN: sb.append("NOT IN").append(" (:").append(part.getUse()).append(")"); break;
				    case TRUE: sb.append("= ").append(part.getType().name().toLowerCase()); break;
				    case FALSE: sb.append("= ").append(part.getType().name().toLowerCase()); break;
					default: throw new IllegalArgumentException(String.join("", "wrong type.<",part.getType().toString(),""));
			    }
			}
			if(i > 0 && length > 1) sb.append(")");
		}
		List<Order> orders = pt.getOrders();
		if(null != orders && !orders.isEmpty()){
			sb.append(" ORDER BY ");
			for(Order order: orders){
				sb.append(order.getProperty()).append(" ").append(order.getDirection());
			}
		}
		return sb;
	}
	
	private long count(Method method,MapSqlParameterSource paramSource,String source){
		String sql = processCountSql(method,source).toString();
		LOG.info(sql);
		return namedParameterJdbcOperations.queryForObject(sql, paramSource, long.class);
	}
	
	private <T> T resultSetToObject(ResultSet rs,Class<T> t){
		
		return resultSetToObject(rs,0,t);
	}
	
	private <T> T resultSetToObject(ResultSet rs, int rowNum,Class<T> t){
		try {
			if(null != rs && rs.next()){
				T entity = t.newInstance();
				List<EntityProperty> entityPropertys = EntityPropertyHelper.getEntityPropertys(t,false);
				for(int i=0,size=entityPropertys.size();i<size;i++){
					EntityProperty entityProperty = entityPropertys.get(i);
					Method method = resultSetMethodMap.get(entityProperty.getPropertyType().getSimpleName().toUpperCase());
					if(null != method){
						Object value = ReflectionUtils.invokeJdbcMethod(method,rs,entityProperty.getColumnName());
						entityProperty.setPropertyValue(entity, value);
					}
				}
				return entity;
			}
			return null;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("entity initialization failure.",e);
		} catch (SQLException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private boolean isPrimitive(Class<?> clazz){
		
		return clazz.isPrimitive() || resultSetMethodMap.containsKey(clazz.getSimpleName().toUpperCase());
	}
	
	private String fieldsToColumns(List<EntityProperty> entityPropertys){
		StringBuilder columns = new StringBuilder();
		for(int i=0,size=entityPropertys.size();i<size;i++){
			EntityProperty entityProperty = entityPropertys.get(i);
			columns.append(entityProperty.getColumnName());
			if(i < size -1){
				columns.append(",");
			}
		}
		return columns.toString();
	}
	
	private String getTableName(Class<?> clazz){
		Assert.notNull(clazz, "The Class mapping is be empty.");
		Table annotation = AnnotationUtils.findAnnotation(clazz, Table.class);
		Object tableName = AnnotationUtils.getValue(annotation, "name");
	    Assert.notNull(tableName,String.join(" ", clazz.getName(),"entity has no mapping table."));
		return tableName.toString();
	}
	
	private StringBuilder processCountSql(Method method,String source){
		StringBuilder sb = new StringBuilder(Keyword.SELECT.name());
		sb.append(" COUNT(0) ").append("FROM ");
		if(StringUtils.isEmpty(source)){
			String tableName = getTableName(findCertainGenericType(method, Table.class,true));
			sb.append(" ").append(tableName);
		}else{
			sb.append(" (").append(source).append(") AS A");
		}
		return sb;
	}
	
	private int superpositionInt(int... values){
		if(null != values){
			int count = 0;
			for(int i=0,length=values.length; i<length; i++){
				count += values[i];
			}
			return count;
		}
		return 0;
	}

}
