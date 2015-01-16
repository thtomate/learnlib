/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.acex.analyzers;

import de.learnlib.acex.AbstractCounterexample;
import de.learnlib.acex.impl.BaseAbstractCounterexample;

/**
 * Abstract counterexample analysis algorithms.
 * <p>
 * All of the algorithms contained in this class takes as arguments:
 * <ul>
 * <li>an {@link BaseAbstractCounterexample} <code>acex</code>,</li>
 * <li>the lower bound of the search range <code>low</code>, and</li>
 * <li>the upper bound of the search range <code>high</code>.
 * </ul>
 * For a valid input, all of the methods in this class will return an
 * index <code>i</code> such that <code>acex.testEffect(i) != acex.testEffect(i+1)</code>.
 * The input is valid iff <code>high &gt; low</code>, <code>acex.testEffect(low) == 0</code>,
 * and <code>acex.testEffect(high) == 1</code>.
 * 
 * @author Malte Isberner
 *
 */
public class AcexAnalysisAlgorithms {
	
	/**
	 * Scan linearly through the counterexample in ascending order.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static int linearSearchFwd(AbstractCounterexample acex, int low, int high) {
		assert acex.test(low) == 0 && acex.test(high) == 1;
		
		int cur;
		for(cur = low + 1; cur < high; cur++) {
			if(acex.test(cur) == 1) {
				break;
			}
		}
		return (cur-1);
	}
	
	/**
	 * Scan linearly through the counterexample in descending order.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static int linearSearchBwd(AbstractCounterexample acex, int low, int high) {
		assert acex.test(low) == 0 && acex.test(high) == 1;
		
		int cur;
		for(cur = high-1; cur > low; cur--) {
			if(acex.test(cur) == 0) {
				break;
			}
		}
		return cur;
	}
	
	/**
	 * Search for a suffix index using an exponential search.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static int exponentialSearchBwd(AbstractCounterexample acex, int low, int high) {
		assert acex.test(low) == 0 && acex.test(high) == 1;
		
		int ofs = 1;
		
		while(high - ofs > low) {
			if(acex.test(high - ofs) == 0) {
				low = high - ofs;
				break;
			}
			high -= ofs;
			ofs *= 2;
		}
		
		return binarySearch(acex, low, high);
	}
	
	public static int exponentialSearchFwd(AbstractCounterexample acex, int low, int high) {
		assert acex.test(low) == 0 && acex.test(high) == 1;
		
		int ofs = 1;
		
		while(low + ofs < high) {
			if(acex.test(low + ofs) == 1) {
				high = low + ofs;
				break;
			}
			low += ofs;
			ofs *= 2;
		}
		
		return binarySearch(acex, low, high);
	}
	
	/**
	 * Search for a suffix index using a binary search.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static int binarySearch(AbstractCounterexample acex, int low, int high) {
		assert acex.test(low) == 0 && acex.test(high) == 1;
		
		while(high - low > 1) {
			int mid = low + (high - low)/2;
			if(acex.test(mid) == 0) {
				low = mid;
			}
			else {
				high = mid;
			}
		}
		
		return low;
	}
	
	/**
	 *  Search for a suffix index using a partition search
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static int partitionSearchBwd(AbstractCounterexample acex, int low, int high) {
		assert acex.test(low) == 0 && acex.test(high) == 1;
		
		int span = high - low + 1;
		double logSpan = Math.log(span)/Math.log(2);
		
		int step = (int)(span/logSpan);
		
		while(high - step > low) {
			if(acex.test(high - step) == 0) {
				low = high - step;
				break;
			}
			high -= step;
		}
		
		return binarySearch(acex, low, high);
	}
	
	public static int partitionSearchFwd(AbstractCounterexample acex, int low, int high) {
		assert acex.test(low) == 0 && acex.test(high) == 1;
		
		int span = high - low + 1;
		double logSpan = Math.log(span)/Math.log(2);
		
		int step = (int)(span/logSpan);
		
		while(low + step < high) {
			if(acex.test(low + step) == 1) {
				high = low + step;
				break;
			}
			low += step;
		}
		
		return binarySearch(acex, low, high);
	}
}
