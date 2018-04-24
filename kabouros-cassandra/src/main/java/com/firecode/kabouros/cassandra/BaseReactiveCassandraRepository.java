package com.firecode.kabouros.cassandra;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Publisher;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleReactiveCassandraRepository;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.firecode.kabouros.common.annotation.GeneratedValue;
import com.firecode.kabouros.common.domain.PrimaryKeyMethodWrap;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository base implementation for Cassandra.
 * 
 * ReactiveCassandraTemplate 简单使用：
 * Mono person = cassandraTemplate.selectOneById(query(where("age").is(33)), Person.class);
 * Maybe maybe = Flowable.fromPublisher(person).firstElement();
 * 
 * @author JIANG
 */
public class BaseReactiveCassandraRepository<T, ID> extends SimpleReactiveCassandraRepository<T, ID> {
	
	//primaryKey getter setter 
	private final PrimaryKeyMethodWrap[] primaryKeyMethodWraps;

	public BaseReactiveCassandraRepository(CassandraEntityInformation<T, ID> metadata,
			ReactiveCassandraOperations operations) {
		super(metadata, operations);
		this.primaryKeyMethodWraps = primaryKeyMethos(metadata.getJavaType());
	}
	
	@Override
	public <S extends T> Mono<S> save(S entity) {
		
		return super.save(this.generatePrimaryKey(entity));
	}


	@Override
	public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
		
		return Flux.from(entityStream).flatMap(entity -> save(entity));
	}
	
	@Override
	public <S extends T> Mono<S> insert(S entity) {
		
		return super.insert(this.generatePrimaryKey(entity));
	}

	@Override
	public <S extends T> Flux<S> insert(Iterable<S> entitys) {
		
		return Flux.fromIterable(entitys).flatMap(this::insert);
	}

	@Override
	public <S extends T> Flux<S> insert(Publisher<S> entityStream) {
		
		return Flux.from(entityStream).flatMap(this::insert);
	}

	/**
	 * primaryKey gett sett
	 * @param clazz
	 * @return
	 */
	private PrimaryKeyMethodWrap[] primaryKeyMethos(Class<?> clazz){
		List<PrimaryKeyMethodWrap> wraps = new ArrayList<>();
		ReflectionUtils.doWithFields(clazz, field -> {
			try {
				GeneratedValue gener = AnnotationUtils.findAnnotation(field, GeneratedValue.class);
				if (gener != null) {
					PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
					wraps.add(new PrimaryKeyMethodWrap(pd.getReadMethod(),pd.getWriteMethod(),gener.strategy()));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		if(!wraps.isEmpty()){
			return wraps.toArray(new PrimaryKeyMethodWrap[wraps.size()]);
		}
		return null;
	}

	/**
	 * 生成主键
	 * @param entity
	 * @return
	 */
	private <S extends T> S generatePrimaryKey(S entity) {
		Assert.notNull(entity, "Entity must not be null");
		if(null != primaryKeyMethodWraps){
			for(int i=0,length=primaryKeyMethodWraps.length;i<length;i++){
				PrimaryKeyMethodWrap primaryKeyMethodWrap = primaryKeyMethodWraps[i];
				Object value = ReflectionUtils.invokeMethod(primaryKeyMethodWrap.getReadMethod(), entity);
				if(value == null){
					ReflectionUtils.invokeMethod(primaryKeyMethodWrap.getWriteMethod(), entity,primaryKeyMethodWrap.getStrategy().generate());
				}
			}
		}
		return entity;
	}

}
