package feedreader;

import java.util.Calendar;
import java.util.Date;

import common.DateUtils;

public class UserSession {
	private Integer id;
	private Integer userId;
	private long created = -1;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}
	
	public boolean isExpired() {
		if(created == -1) {
			return true;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		return created < calendar.getTimeInMillis();
	}
}
