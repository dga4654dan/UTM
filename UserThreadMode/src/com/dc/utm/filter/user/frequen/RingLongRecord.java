package com.dc.utm.filter.user.frequen;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author Daemon
 *
 * Long类型的环形记录类 (出于性能考虑，有些方法是弱并发性的)
 * 有个记录当前位置的指针,每插入一个数，往后移动一格，如果超过了size，则移到数组头部
 *
 */
public class RingLongRecord {
	
	private final AtomicInteger index = new AtomicInteger(0);
	private final AtomicLongArray ring;

	public RingLongRecord(int recordSize) {

		ring = new AtomicLongArray(recordSize);
	}

	/**
	 * 将setter记录到下一个记录的位置，如果comparer大于原来的记录则返回true
	 * (重新写入失败也将返回false：即有两个人尝试改动这个位置的数据
	 * （即下标指针饶了一圈后有回到这个位置：至少有recordSize+1个并发在修改这个类）) 该方法为弱并发(性能考虑)：
	 * eg:数组长度为3 原数据： 2,3,4 （下标分别为0 1 2） 并发插入数据：3,4,5,6 （recordSize+1个并发）
	 * 并发对比数据：2,3,4,5 则如果6先写入0号位置，那么将会返回true，而3的写入会返回false
	 * （理论上如果是强并发的话，3和6的插入都会返回false）
	 * 
	 * @param target
	 *            比较的数值
	 * @return 如果大于第一个记录则返回true
	 */
	public final boolean setFirstRecordNCompare(long comparer, long setter) {

		int i = 0;
		for (;;) {

			int current = index.get();
			int next = current + 1;
			if (next == ring.length())
				next = 0;

			if (index.compareAndSet(current, next))
				i = current;
			break;
		}

		long old = ring.get(i);

		if (!ring.compareAndSet(i, old, setter))
			return false;
		else if (comparer > old)
			return true;
		else
			return false;

	}
}
