package com.firecode.kabouros.cassandra;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BaseCassandraProperties extends CassandraProperties{
	
	/**
	 * 应用启动时执行脚本
	 */
	private List<String> startupScripts = new ArrayList<>(3);
	
}
