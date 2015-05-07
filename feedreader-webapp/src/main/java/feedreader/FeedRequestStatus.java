package feedreader;

/**
 * Represents the status of the feed request.
 * @author jared.pearson
 * @see FeedRequest#getStatus()
 */
public enum FeedRequestStatus {
	/**
	 * If the request has not been started
	 */
	NOT_STARTED("N"),
	
	/**
	 * If the request was finished
	 */
	FINISHED("F"),
	
	/**
	 * If there was an error processing the request
	 */
	ERROR("E");
	
	private final String dbValue;
	
	private FeedRequestStatus(final String dbValue) {
		assert dbValue != null;
		this.dbValue = dbValue;
	}
	
	/**
	 * Gets the value to be stored in the database.
	 */
	public String getDbValue() {
		return dbValue;
	}
	
	/**
	 * Gets the status corresponding to the specified database value
	 */
	public static FeedRequestStatus fromDbValue(final String dbValue) {
		assert dbValue != null;
		for(FeedRequestStatus status : FeedRequestStatus.values()) {
			if(status.getDbValue().equals(dbValue)) {
				return status;
			}
		}
		throw new IllegalArgumentException("No status value corresponding to the dbValue specified: " + dbValue);
	}
}
