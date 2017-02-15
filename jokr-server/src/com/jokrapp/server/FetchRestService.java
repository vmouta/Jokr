package com.jokrapp.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lightcouch.NoDocumentException;

import com.cloudant.client.api.Database;
import com.jokrapp.server.data.model.Joke;
import com.jokrapp.server.data.model.User;

@Path("/fetch")
/**
 * 
 */
public class FetchRestService {

	private static final Logger log = Logger.getLogger(FetchRestService.class
			.getName());

	public FetchRestService() {
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Joke> get(@QueryParam("userId") String userId,
			@QueryParam("demo") Boolean demo) throws WebApplicationException {

		if (demo == null) {
			demo = false;
		}

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
					"Problem with database.");
		}

		// TODO switch log level, remove logs....
		log.fine("Fetching request...");

		Long lastListeningTimestamp = 0l;
		User user = null;
		if (userId != null) {

			log.info("User id: " + userId);

			try {
				// check if user exists
				user = db.find(User.class, userId);
			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"User doesn't exist.");
			}

			lastListeningTimestamp = user.getListeningTimestamp();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"UserId not provided");
		}

		List<Joke> tracks;
		if (!demo) {

			lastListeningTimestamp = lastListeningTimestamp + 1l;
			log.fine("timeStamp: " + lastListeningTimestamp.longValue());

			String[] langs = user.getLanguages().trim().split("\\s+");
			String langsQuery = "";

			if (langs.length > 0) {
				langsQuery = "language:(";
				langsQuery = langsQuery + langs[0].trim();
				for (int i = 1; i < langs.length; i++) {
					langsQuery = langsQuery + " OR " + langs[i].trim();
				}
				langsQuery = langsQuery + ") AND ";
			}
			/*
			 * tracks = db.view("query/jokesByTimestamp").limit(10)
			 * .startKey(lastListeningTimestamp.longValue())
			 * .includeDocs(true).query(Joke.class);
			 */
			tracks = db
					.search("query/allJokes")
					.includeDocs(true)
					.limit(3)
					.sort("\"uploadTimestamp<number>\"")
					.query(langsQuery + "uploadTimestamp: ["
							+ lastListeningTimestamp + " TO Infinity]",
							Joke.class);

		} else {

			tracks = db.view("query/jokesByTimestamp").limit(5)
					.includeDocs(true).query(Joke.class);
		}

		if (!tracks.isEmpty()) {

			ArrayList<Joke> jokes = new ArrayList<Joke>();

			if (user.getBlockedUsers() != null) {
				// remove blocked users jokes
				HashSet<String> blockedUsers = new HashSet<String>(
						user.getBlockedUsers());
				for (Joke joke : tracks) {
					if (!blockedUsers.contains(joke.getUserId())) {
						jokes.add(joke);
					}
				}
			} else {
				for (Joke joke : tracks) {
					jokes.add(joke);
				}
			}

			if (user != null) {
				// setting time of last received joke
				user.setListeningTimestamp(tracks.get(tracks.size() - 1)
						.getUploadTimestamp());
				db.update(user);
			}

			return jokes;
		} else {
			// no more new jokes for this user.... sending 204
			throw new RestException(Response.Status.NO_CONTENT,
					"No new jokes for this user.");

		}

	}

	@GET
	@Path("/recommended")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Joke> getRecommended(@QueryParam("userId") String userId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
					"Problem with database.");
		}


		User user = null;
		if (userId != null) {

			log.info("User id: " + userId);

			try {
				// check if user exists
				user = db.find(User.class, userId);
			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"User doesn't exist.");
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"UserId not provided");
		}

		List<Joke> tracks;

		String[] langs = user.getLanguages().trim().split("\\s+");
		String langsQuery = "";

		if (langs.length > 0) {
			langsQuery = "language:(";
			langsQuery = langsQuery + langs[0].trim();
			for (int i = 1; i < langs.length; i++) {
				langsQuery = langsQuery + " OR " + langs[i].trim();
			}
			langsQuery = langsQuery + ")";
		}

		tracks = db
				.search("query/allJokes")
				.includeDocs(true)
				.limit(10)
				.sort("\"uploadTimestamp<number>\"")
				.query(langsQuery, Joke.class);

		if (!tracks.isEmpty()) {

			ArrayList<Joke> jokes = new ArrayList<Joke>();

			if (user.getBlockedUsers() != null) {
				// remove blocked users jokes
				HashSet<String> blockedUsers = new HashSet<String>(
						user.getBlockedUsers());
				for (Joke joke : tracks) {
					if (!blockedUsers.contains(joke.getUserId())) {
						jokes.add(joke);
					}
				}
			} else {
				for (Joke joke : tracks) {
					jokes.add(joke);
				}
			}

			if (user != null) {
				// setting time of last received joke
				user.setListeningTimestamp(tracks.get(tracks.size() - 1)
						.getUploadTimestamp());
				db.update(user);
			}

			return jokes;
		} else {
			// no recommended jokes for this user.... sending 204
			throw new RestException(Response.Status.NO_CONTENT,
					"No recommended jokes for this user.");

		}

	}

	private Database getDB() {
		return CloudantClientMgr.getDB();
	}

}
