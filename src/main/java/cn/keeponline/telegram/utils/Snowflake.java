package cn.keeponline.telegram.utils;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wyk
 * @version 1.0.0
 * @Description
 * @createTime 2021/6/2 4:19 下午
 */
public class Snowflake implements Serializable {

    private static final long serialVersionUID = 1L;
    private final long twepoch;
    private final long workerIdBits;
    private final long dataCenterIdBits;
    private final long maxWorkerId;
    private final long maxDataCenterId;
    private final long sequenceBits;
    private final long workerIdShift;
    private final long dataCenterIdShift;
    private final long timestampLeftShift;
    private final long sequenceMask;
    private final long workerId;
    private final long dataCenterId;
    private final boolean useSystemClock;
    private long sequence;
    private long lastTimestamp;

    public Snowflake(long workerId, long dataCenterId) {
        this(workerId, dataCenterId, false);
    }

    public Snowflake(long workerId, long dataCenterId, boolean isUseSystemClock) {
        this((Date)null, workerId, dataCenterId, isUseSystemClock);
    }

    public Snowflake(Date epochDate, long workerId, long dataCenterId, boolean isUseSystemClock) {
        this.workerIdBits = 5L;
        this.dataCenterIdBits = 5L;
        this.maxWorkerId = 31L;
        this.maxDataCenterId = 31L;
        this.sequenceBits = 12L;
        this.workerIdShift = 12L;
        this.dataCenterIdShift = 17L;
        this.timestampLeftShift = 22L;
        this.sequenceMask = 4095L;
        this.sequence = 0L;
        this.lastTimestamp = -1L;
        if (null != epochDate) {
            this.twepoch = epochDate.getTime();
        } else {
            this.twepoch = 1288834974657L;
        }

        if (workerId <= 31L && workerId >= 0L) {
            if (dataCenterId <= 31L && dataCenterId >= 0L) {
                this.workerId = workerId;
                this.dataCenterId = dataCenterId;
                this.useSystemClock = isUseSystemClock;
            } else {
                throw new IllegalArgumentException(StrUtil.format("datacenter Id can't be greater than {} or less than 0", new Object[]{31L}));
            }
        } else {
            throw new IllegalArgumentException(StrUtil.format("worker Id can't be greater than {} or less than 0", new Object[]{31L}));
        }
    }

    public long getWorkerId(long id) {
        return id >> 12 & 31L;
    }

    public long getDataCenterId(long id) {
        return id >> 17 & 31L;
    }

    public long getGenerateDateTime(long id) {
        return (id >> 22 & 2199023255551L) + this.twepoch;
    }

    public synchronized long nextId(boolean ifEvenNum) {
        long timestamp = this.genTime();
        if (timestamp < this.lastTimestamp) {
            throw new IllegalStateException(StrUtil.format("Clock moved backwards. Refusing to generate id for {}ms", new Object[]{this.lastTimestamp - timestamp}));
        }
        /**
         * 时间不连续出来全是偶数
         */
        if(ifEvenNum) {
            if (timestamp == this.lastTimestamp) {
                // 相同毫秒内，序列号自增
                this.sequence = (this.sequence + 1L) & 4095L;
                // 同一毫秒的序列数已经达到最大
                if (this.sequence == 0L) {
                    timestamp = this.tilNextMillis(this.lastTimestamp);
                }
            } else {
                // 不同毫秒内，序列号置为0
                this.sequence = 0L;
            }
        } else {
            // 相同毫秒内，序列号自增
            this.sequence = (this.sequence + 1L) & 4095L;
        }
        this.lastTimestamp = timestamp;
        return timestamp - this.twepoch << 22 | this.dataCenterId << 17 | this.workerId << 12 | this.sequence;
    }

    public String nextIdStr() {
        return Long.toString(this.nextId(false));
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp;
        for(timestamp = this.genTime(); timestamp == lastTimestamp; timestamp = this.genTime()) {
        }

        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(StrUtil.format("Clock moved backwards. Refusing to generate id for {}ms", new Object[]{lastTimestamp - timestamp}));
        } else {
            return timestamp;
        }
    }

    private long genTime() {
        return this.useSystemClock ? SystemClock.now() : System.currentTimeMillis();
    }


    public static void main(String[] args) throws InterruptedException {
        Snowflake snowFlake = new Snowflake(5, 5);
        for (int i = 0; i < 10; i++) {
            long snowFlakeId = snowFlake.nextId(false);
            System.out.println(snowFlakeId);
            Thread.sleep(1000);
        }
    }
}