package com.firecode.kabouros.common.keygen;

import java.util.Calendar;

import org.springframework.util.StringUtils;

import com.firecode.kabouros.common.util.StringUtil;

/**
 * Use snowflake algorithm. Length is 64 bit
 * 1bit   sign bit
 * 41bits time offset
 * 10bits work process Id
 * 12bits auto increment offset in one mills
 * 
 * @author gaohongtao
 */
public abstract class CommonIdGenerator{
	
	private final long SJDBC_EPOCH;

	private static final long SEQUENCE_BITS = 12L;

	private static final long WORKER_ID_BITS = 10L;

	private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;

	private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;

	private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;

	private static final long WORKER_ID_MAX_VALUE = 1L << WORKER_ID_BITS;

	private AbstractClock clock = AbstractClock.systemClock();

	private long workerId;
	
	private long sequence;

	private long lastTime;

	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2017, Calendar.NOVEMBER, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		SJDBC_EPOCH = calendar.getTimeInMillis();
		initWorkerId();
	}

	void initWorkerId() {
		String workerId = System.getProperty("snowflake.id.generator.worker.id");
		if (!StringUtils.isEmpty(workerId)) {
			setWorkerId(Long.valueOf(workerId));
			return;
		}
		workerId = System.getenv("SNOWFLAKE_ID_GENERATOR_WORKER_ID");
		if (StringUtils.isEmpty(workerId)) {
			return;
		}
		setWorkerId(Long.valueOf(workerId));
	}

	/**
	 * @param workerId  工作ID
	 */
	protected void setWorkerId(final Long workerId) {
		if(workerId < 0L || workerId > WORKER_ID_MAX_VALUE){
			throw new IllegalArgumentException(workerId < 0L ? "WorkerId < 0" : String.join("> ", "WorkerId ",String.valueOf(WORKER_ID_MAX_VALUE)));
		}
		this.workerId = workerId;
	}

	protected long getWorkerIdLength() {
		return WORKER_ID_BITS;
	}

	protected synchronized Number createId() {
		long time = this.clock.millis();
		if(lastTime > time){
			throw new IllegalStateException(StringUtil.format("Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds",lastTime,time));
		}
		if (lastTime == time) {
			if (0L == (++sequence & SEQUENCE_MASK)) {
				time = waitUntilNextTime(time);
			}
		} else {
			sequence = 0;
		}
		lastTime = time;
		
		return ((time - SJDBC_EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | (this.workerId << WORKER_ID_LEFT_SHIFT_BITS) | sequence;
	}

	private long waitUntilNextTime(final long lastTime) {
		long time = this.clock.millis();
		while (time <= lastTime) {
			time = this.clock.millis();
		}
		return time;
	}

	protected long getWorkerId() {
		return this.workerId;
	}
	protected void setClock(AbstractClock clock) {
		this.clock = clock;
	}

}
