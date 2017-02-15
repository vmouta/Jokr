package com.jokrapp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

@Path("/joke")
/**
 * 
 */
public class JokeRestService {

	private static final Logger log = Logger.getLogger(JokeRestService.class
			.getName());

	public JokeRestService() {
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Joke get(@QueryParam("jokeId") String jokeId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		Joke joke = null;
		if (jokeId != null) {

			log.fine("Joke id: " + joke);

			try {
				// check if joke exists
				joke = db.find(Joke.class, jokeId);
			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"Joke doesn't exist.");
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: jokeId.");
		}

		return joke;
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Joke downloadJoke(@QueryParam("jokeId") String jokeId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		Joke joke = null;
		if (jokeId != null) {

			log.fine("Joke id: " + joke);

			try {
				// check if joke exists
				joke = db.find(Joke.class, jokeId);
			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"Joke doesn't exist.");
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: jokeId.");
		}

		return joke;
	}
	
	@GET
	@Path("/getAllJokesOfUser")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Joke> getAll(@QueryParam("userId") String userId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		List<Joke> jokes = null;
		if (userId != null) {

			log.fine("Fetching jokes of user: " + userId);

			try {
				// check if jokes exists
				jokes = db.search("query/getAllJokesOfUser").includeDocs(true)
						.query("userId:" + userId, Joke.class);
			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.NO_CONTENT,
						"There are no jokes for this user.");
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: jokeId.");
		}

		return jokes;
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Joke joke) throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (joke != null && joke.get_id() != null) {

			log.fine("Joke id: " + joke);

			try {
				// check if joke exists
				Joke storedJoke = db.find(Joke.class, joke.get_id());
				storedJoke.setTags(joke.getTags());
				storedJoke.setFramingBegin(joke.getFramingBegin());
				storedJoke.setFramingEnd(joke.getFramingEnd());
				storedJoke.setTitle(joke.getTitle());
				storedJoke.setLanguage(joke.getLanguage());
				storedJoke.setIsPublic(joke.isPublic());
				db.update(storedJoke);

			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"Joke doesn't exist.");
			}
			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Invalid request. Please supply joke as JSON with _id attribute.");
		}

	}

	@POST
	@Path("/delete")
	public Response delete(@QueryParam("jokeId") String jokeId) throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (jokeId != null ) {

			log.fine("Deleting Joke: " + jokeId);

			try {
				// check if joke exists
				Joke storedJoke = db.find(Joke.class, jokeId);
				db.remove(storedJoke);
				return Response.ok().build();
			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"Joke doesn't exist.");
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Invalid request. Please supply joke as JSON with _id attribute.");
		}
		
	}

	@POST
	@Path("/like")
	public Response like(@QueryParam("userId") String userId,
			@QueryParam("jokeId") String jokeId) throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (userId != null && jokeId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> likes = storedUser.getLikes();

			if (likes == null) {
				likes = new ArrayList<String>();
			}

			likes.add(jokeId);

			storedUser.setLikes(likes);

			db.update(storedUser);

			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Wrong params...");
		}
	}

	@POST
	@Path("/dislike")
	public Response dislike(@QueryParam("userId") String userId,
			@QueryParam("jokeId") String jokeId) throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (userId != null && jokeId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> dislike = storedUser.getDislikes();

			if (dislike == null) {
				dislike = new ArrayList<String>();
			}

			dislike.add(jokeId);

			storedUser.setDislikes(dislike);

			db.update(storedUser);

			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Wrong params...");
		}
	}

	private Database getDB() {
		return CloudantClientMgr.getDB();
	}

}
