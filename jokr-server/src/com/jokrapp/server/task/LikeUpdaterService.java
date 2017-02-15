package com.jokrapp.server.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ws.rs.core.Response;

import org.lightcouch.NoDocumentException;

import com.cloudant.client.api.Database;
import com.google.gson.internal.LinkedTreeMap;
import com.jokrapp.server.CloudantClientMgr;
import com.jokrapp.server.RestException;
import com.jokrapp.server.data.model.Joke;

/* This service updates the likes cnt of the jokes... */
@Singleton
public class LikeUpdaterService {

	private static final Logger log = Logger.getLogger(LikeUpdaterService.class
			.getName());

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
				.view("query/likes").reduce(true).group(true).limit(100000000)
				.query(Object.class);

		log.fine("all elements..." + view.toString());

		ArrayList<LinkedTreeMap<String, Object>> dlikes = (ArrayList) db
				.view("query/dislikes").reduce(true).group(true)
				.limit(100000000).query(Object.class);

		HashMap<String, Double> dislikes = new HashMap<String, Double>();
		for (LinkedTreeMap<String, Object> ht : dlikes) {
			String jokeId = (String) ht.get("key");
			Double cnt = (Double) ht.get("value");
			dislikes.put(jokeId, cnt);
		}

		for (LinkedTreeMap<String, Object> ht : view) {

			String jokeId = (String) ht.get("key");
			Double cnt = (Double) ht.get("value");
			Joke joke;
			try { // check if joke exists
				joke = db.find(Joke.class, jokeId);
				log.fine(joke.toString());

				if (cnt != null && dislikes.get(jokeId) != null) {
					cnt = cnt - dislikes.get(jokeId);

					if (joke.getLikes() != cnt) {
						joke.setLikes(cnt.intValue());
						db.update(joke);
					}
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
