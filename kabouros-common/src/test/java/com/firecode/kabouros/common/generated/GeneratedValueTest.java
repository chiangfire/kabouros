package com.firecode.kabouros.common.generated;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import com.firecode.kabouros.common.BaseTest;
import com.firecode.kabouros.common.GenerationType;

public class GeneratedValueTest {
	
	@Test
	public void test2() throws InterruptedException{
		List<String> list1 = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		List<String> list3 = new ArrayList<>();
		List<String> list4 = new ArrayList<>();
		List<String> list5 = new ArrayList<>();
		List<String> list6 = new ArrayList<>();
		List<String> list7 = new ArrayList<>();
		List<String> list8 = new ArrayList<>();
		List<String> list9 = new ArrayList<>();
		List<String> list10 = new ArrayList<>();
		BaseTest.timeTest((args)->{
			CountDownLatch latch = new CountDownLatch(10);
			new Thread(() ->{
				for(int i=0;i<100000;i++){
					list1.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list2.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list3.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list4.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list5.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list6.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list7.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list8.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list9.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			new Thread(()->{
				for(int i=0;i<100000;i++){
					list10.add(GenerationType.SNOWFLAKE.generate().toString());
				}
				latch.countDown();
			}).start();
			try {
				latch.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			list1.addAll(list2);
			list1.addAll(list3);
			list1.addAll(list4);
		    list1.addAll(list5);
		    list1.addAll(list6);
		    list1.addAll(list7);
		    list1.addAll(list8);
		    list1.addAll(list9);
		    list1.addAll(list10);
		    Assert.assertEquals("数据有误。", 1000000, list1.size());
		    Assert.assertEquals("出现重复数据。", 1000000, list1.stream().distinct().count());
		});
	}
	
}
