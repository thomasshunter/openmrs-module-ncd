package org.openmrs.module.ncd.database.filter;

public class SearchFilterMessageCountSummary  extends SearchFilterBase {

	private static final long serialVersionUID = -4534627019164536936L;

	/* The number of buckets to be returned.  First bucket is for all time, with 2nd...nth buckets beginning 
	 * with the bucket containing the current date, and working backwards in time for the specified 
	 * total number of buckets. */
	private int numberOfBuckets;

	public int getNumberOfBuckets() {
		if (numberOfBuckets == 0) {
			// default number of buckets: 6 (one for all time), plus the past 5 days worth of message counts
			return 6;
		}
		return numberOfBuckets;
	}

	public void setNumberOfBuckets(int numberOfBuckets) {
		this.numberOfBuckets = numberOfBuckets;
	}
}
