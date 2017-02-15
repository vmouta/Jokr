package com.jokrapp.server.task;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ws.rs.core.Response;

import org.lightcouch.NoDocumentException;

import com.cloudant.client.api.Database;
import com.google.gson.internal.LinkedTreeMap;
import com.jokrapp.server.CloudantClientMgr;
import com.jokrapp.server.RestException;
import com.jokrapp.server.data.model.User;

/* This service updates the likes cnt of the jokes... */
@Singleton
public class FollowersCntUpdateTask {

	private static final Logger log = Logger
			.getLogger(FollowersCntUpdateTask.class.getName());

	// every minute.. TODO put this to every hour... to decrease server time...
	@Schedule(second = "0", minute = "0", hour = "*", persistent = false)
	public void execute() {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
					"Problem with database.");
		}

		// TODO potential source for errors.... memory... add proper paging if
		// possible
		ArrayList<LinkedTreeMap<String, Object>> view = (ArrayList) db
				.view("query/followingCnt").reduce(true).group(true)
				.limit(100000000).query(Object.class);

		for (LinkedTreeMap<String, Object> ht : view) {

			String userId = (String) ht.get("key");
			Double cnt = (Double) ht.get("value");
			User user;
			try { // check if joke exists
				user = db.find(User.class, userId);
				if (user.getFollowersCnt() != cnt.intValue()) {
					user.setFollowersCnt(cnt.intValue());
					db.update(user);
				}
			} catch (NoDocumentException e) {
				// ignore...
			}
		}

	}

	private Database getDB() {
		return CloudantClientMgr.getDB();
	}
}
