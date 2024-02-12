package com.yifan.seckill.util;

/**
 * Description: Twitter’s global self-increasing ID snowflake algorithm Snowflake
 *
 * @author yifan
 **/
public class SnowFlake {

    /**
     * starting timestamp
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * Number of bits occupied by each part
     */
    private final static long SEQUENCE_BIT = 12; //The number of digits occupied by the sequence number
    private final static long MACHINE_BIT = 5; //The number of bits occupied by the machine identification
    private final static long DATACENTER_BIT = 5;//The number of bits occupied by the data center
    /**
     * maximum value of each part
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * The displacement of each part to the left
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId; //data center
    private long machineId; //machine identification
    private long sequence = 0L; //sequence number
    private long lastStmp = -1L;//Last timestamp

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * Generate next ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //In the same millisecond, the sequence number increases automatically
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //The number of sequences in the same millisecond has reached the maximum
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            // Within different milliseconds, the sequence number is set to 0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //Time stamp part
                | datacenterId << DATACENTER_LEFT //Data center part
                | machineId << MACHINE_LEFT //Machine identification part
                | sequence; //serial number part
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 3);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            System.out.println(snowFlake.nextId());
        }

        System.out.println(System.currentTimeMillis() - start);


    }
}
