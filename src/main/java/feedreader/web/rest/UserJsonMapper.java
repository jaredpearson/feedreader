package feedreader.web.rest;

import java.io.IOException;

import common.json.JsonMapper;
import common.json.JsonWriter;
import feedreader.User;

class UserJsonMapper implements JsonMapper<User> {
	private static final UserJsonMapper INSTANCE = new UserJsonMapper();
	
	public void write(JsonWriter out, User user) throws IOException {
		out.startObject();
		out.name("id").value(user.getId());
		out.name("email").value(user.getEmail());
		out.endObject();
	}
	
	public static UserJsonMapper getDefaultInstance() {
		return INSTANCE;
	}
}