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

import com.cloudant.client.api.Database;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.jokrapp.server.data.model.Joke;
import com.jokrapp.server.data.model.User;

@Path("/user")
/**
 * Rest CRUD service of user.
 */
public class UserRestService {

	private static final Logger log = Logger.getLogger(UserRestService.class
			.getName());

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User post(User user) throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (user.getDeviceId() == null) {
			throw new RestException(Response.Status.BAD_REQUEST,
					"No deviceId provided.");
		}

		log.info("UserId: " + user.get_id());

		List<User> users;

		// check if user exists
		users = db.search("query/allUsers").includeDocs(true).limit(1)
				.query("deviceId: " + user.getDeviceId(), User.class);

		if (users.size() == 0) {
			log.info("Creating user with deviceId : " + user.getDeviceId());
			user.setCreationTimestamp(System.currentTimeMillis());
			user.setListeningTimestamp(0l);
			if (user.getUserName() == null) {
				user.setUserName("");
			}
			user.setLastActiveTimestamp(System.currentTimeMillis());
			com.cloudant.client.api.model.Response resp = db.save(user);
			log.info("User created with id: " + resp.getId());
			user.set_id(resp.getId());
			return user;
		}

		User storedUser = users.get(0);

		storedUser.setDeviceOS(user.getDeviceOS());
		storedUser.setLanguages(user.getLanguages());

		if (storedUser.getUserName() != null && user.getUserName() != null
				&& !storedUser.getUserName().equals(user.getUserName())) {
			// TODO update username of all jokes of user
		}

		storedUser.setUserName(user.getUserName());
		storedUser.setYearOfBirth(user.getYearOfBirth());
		db.update(storedUser);

		return storedUser;

	}

	@POST
	@Path("/addFavorite")
	public Response addFavorite(@QueryParam("userId") String userId,
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

			ArrayList<String> favorites = storedUser.getFavoriteJokes();

			if (favorites == null) {
				favorites = new ArrayList<String>();
			}

			favorites.add(jokeId);

			storedUser.setFavoriteJokes(favorites);

			db.update(storedUser);

			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"No userId provided.");
		}

	}

	@POST
	@Path("/removeFavorite")
	public Response removeFavorite(@QueryParam("userId") String userId,
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

			ArrayList<String> favorites = storedUser.getFavoriteJokes();

			if (favorites != null) {
				favorites.remove(jokeId);
				storedUser.setFavoriteJokes(favorites);
				db.update(storedUser);
			}
			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"No userId provided.");
		}

	}

	@GET
	@Path("/getAllFavoritesOfUser")
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

			User storedUser = db.find(User.class, userId);

			ArrayList<String> favorites = storedUser.getFavoriteJokes();

			if (favorites == null) {
				jokes = new ArrayList<Joke>();
			} else {
				jokes = db.view("query/allJokes").limit(9999).includeDocs(true)
						.keys(favorites).query(Joke.class);
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: jokeId.");
		}

		return jokes;
	}

	@POST
	@Path("/follow")
	public Response follow(@QueryParam("userId") String userId,
			@QueryParam("followUserId") String followUserId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (userId != null && followUserId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> followers = storedUser.getFollowingUsers();

			if (followers == null) {
				followers = new ArrayList<String>();
			}

			followers.add(followUserId);

			storedUser.setFollowingUsers(followers);

			db.update(storedUser);

			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Wrong params...");
		}

	}

	@POST
	@Path("/unfollow")
	public Response unfollow(@QueryParam("userId") String userId,
			@QueryParam("followUserId") String followUserId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (userId != null && followUserId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> followers = storedUser.getFollowingUsers();

			if (followers != null) {

				followers.remove(followUserId);

				storedUser.setFollowingUsers(followers);

				db.update(storedUser);
			}
			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Wrong params...");
		}

	}

	@GET
	@Path("/getFollowers")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getFollowers(@QueryParam("userId") String userId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		List<User> users = null;
		if (userId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> following = storedUser.getFollowingUsers();

			users = new ArrayList<User>();
			if (following != null) {

				ArrayList<LinkedTreeMap<String, Object>> view = (ArrayList) db
						.view("query/allUsersMinimal").limit(9999)
						.keys(following).query(Object.class);

				Gson gson = new Gson();

				for (LinkedTreeMap<String, Object> ht : view) {

					String key = (String) ht.get("key");
					LinkedTreeMap value = (LinkedTreeMap) ht.get("value");
					JsonObject jsonObject = (new JsonParser()).parse(
							gson.toJson(value)).getAsJsonObject();

					User u = new User();
					u.set_id(key);
					if (jsonObject.get("userName") != null
							&& !jsonObject.get("userName").isJsonNull()) {
						u.setUserName(jsonObject.get("userName").getAsString());
					}
					u.setCreationTimestamp(jsonObject.get("creationTimestamp")
							.getAsLong());
					if (jsonObject.get("followersCnt") != null) {
						u.setFollowersCnt(jsonObject.get("followersCnt")
								.getAsInt());
					}
					if (jsonObject.get("jokesCnt") != null
							&& !jsonObject.get("jokesCnt").isJsonNull()) {
						u.setJokesCnt(jsonObject.get("jokesCnt").getAsInt());
					}
					users.add(u);
				}
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: userId.");
		}

		return users;
	}

	@POST
	@Path("/blockUser")
	public Response blockUser(@QueryParam("userId") String userId,
			@QueryParam("userToBlockId") String userToBlockId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (userId != null && userToBlockId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> blockings = storedUser.getBlockedUsers();

			if (blockings == null) {
				blockings = new ArrayList<String>();
			}

			blockings.add(userToBlockId);

			storedUser.setBlockedUsers(blockings);

			db.update(storedUser);

			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Wrong params...");
		}
	}

	@POST
	@Path("/unblockUser")
	public Response unblockUser(@QueryParam("userId") String userId,
			@QueryParam("userToBlockId") String userToBlockId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		if (userId != null && userToBlockId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> blockings = storedUser.getBlockedUsers();

			if (blockings != null) {

				blockings.remove(userToBlockId);

				storedUser.setBlockedUsers(blockings);

				db.update(storedUser);
			}
			return Response.ok().build();
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Wrong params...");
		}
	}

	@GET
	@Path("/getBlockedUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getBlockedUsers(@QueryParam("userId") String userId)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new WebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		List<User> users = null;
		if (userId != null) {

			User storedUser = db.find(User.class, userId);

			ArrayList<String> blockings = storedUser.getBlockedUsers();

			users = new ArrayList<User>();
			if (blockings != null) {

				ArrayList<LinkedTreeMap<String, Object>> view = (ArrayList) db
						.view("query/allUsersMinimal").limit(9999)
						.keys(blockings).query(Object.class);

				Gson gson = new Gson();

				for (LinkedTreeMap<String, Object> ht : view) {

					String key = (String) ht.get("key");
					LinkedTreeMap value = (LinkedTreeMap) ht.get("value");
					JsonObject jsonObject = (new JsonParser()).parse(
							gson.toJson(value)).getAsJsonObject();

					User u = new User();
					u.set_id(key);
					if (jsonObject.get("userName") != null
							&& !jsonObject.get("userName").isJsonNull()) {
						u.setUserName(jsonObject.get("userName").getAsString());
					}
					u.setCreationTimestamp(jsonObject.get("creationTimestamp")
							.getAsLong());
					if (jsonObject.get("followersCnt") != null) {
						u.setFollowersCnt(jsonObject.get("followersCnt")
								.getAsInt());
					}
					if (jsonObject.get("jokesCnt") != null
							&& !jsonObject.get("jokesCnt").isJsonNull()) {
						u.setJokesCnt(jsonObject.get("jokesCnt").getAsInt());
					}
					users.add(u);
				}
			}

		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: userId.");
		}

		return users;
	}

	private Database getDB() {
		return CloudantClientMgr.getDB();
	}

}
