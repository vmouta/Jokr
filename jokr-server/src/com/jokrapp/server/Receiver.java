package com.jokrapp.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.lightcouch.NoDocumentException;

import com.cloudant.client.api.Database;
import com.jokrapp.server.audio.ConcatMp3;
import com.jokrapp.server.audio.ConvertPcmToMp3;
import com.jokrapp.server.data.model.Joke;
import com.jokrapp.server.data.model.User;

@WebServlet("/receiver")
@MultipartConfig()
public class Receiver extends HttpServlet {

	private static final Logger log = Logger
			.getLogger(Receiver.class.getName());

	private static final long serialVersionUID = 1L;

	private ServletContext context;
	private static byte[] laugh = null;
	private static byte[] clapping = null;

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Part part = request.getPart("file");

		String _id = request.getParameter("_id");
		String userId = request.getParameter("userId");
		String title = request.getParameter("title");
		String language = request.getParameter("language");
		String tags = request.getParameter("tags");
		String framingBegin = request.getParameter("framingBegin");
		String framingEnd = request.getParameter("framingEnd");
		String creationTimestamp = request.getParameter("creationTimestamp");
		String isPublicString = request.getParameter("isPublic");

		boolean isPublic = true;
		
		if(isPublicString != null){
			isPublic = Boolean.valueOf(isPublicString);
		}
		
		if (userId == null || _id == null || title == null || language == null
				|| tags == null || framingBegin == null || framingEnd == null
				|| creationTimestamp == null) {
			log.warning("id: " + _id + ", userId: " + userId + ", title: "
					+ title + ", language: " + language + ", tags: " + tags
					+ ", framingBegin: " + framingBegin + ", framingEnd:"
					+ framingEnd + ", creationTimestamp: " + creationTimestamp);
			throw new ServletException("Incorrect parameters... :-(");
		}

		title = title.trim();
		tags = tags.trim();
		log.info("Receiving file... from: " + userId+", title: "+title);

		context = request.getServletContext();

		log.info("id: " + _id);

		storeJokr(_id, userId, title, language, tags, framingBegin, framingEnd,
				creationTimestamp, part, isPublic);

		log.info("Upload completed.");

	}

	protected void storeJokr(String _id, String userId, String title,
			String language, String tags, String framingBegin,
			String framingEnd, String creationTimestamp, Part part, boolean isPublic)
			throws WebApplicationException {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
					"Problem with database.");
		}

		// check if user exists
		User user = null;
		try {
			user = (userId == null) ? null : db.find(User.class, userId);
		} catch (NoDocumentException e) {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Please create a user before uploading the file..");
		}

		Joke joke = new Joke();
		joke.set_id(_id);
		joke.setTitle(title);
		joke.setFramingBegin(Integer.valueOf(framingBegin));
		joke.setFramingEnd(Integer.valueOf(framingEnd));
		joke.setTags(tags);
		joke.setLikes(0);
		joke.setLanguage(language);
		joke.setCreationTimestamp(new Long(creationTimestamp));
		joke.setUploadTimestamp(System.currentTimeMillis());
		joke.setUserId(userId);
		joke.setUserName(user.getUserName());
		joke.setPublic(isPublic);
		
		db.save(joke);

		// attach the attachment object
		HashMap<String, Object> obj = db.find(HashMap.class, _id);
		// try {
		// saveAttachment(db, _id, part, "jokr.wav", obj);
		// } catch (IOException e) {
		// e.printStackTrace();
		// throw new RestsException(Response.Status.INTERNAL_SERVER_ERROR,
		// e.getMessage());
		// }

		// now coverting to mp3

		try {
			byte[] bytes = IOUtils.toByteArray(part.getInputStream());

			byte[] mp3 = ConvertPcmToMp3.encodePcmToMp3(bytes);

			byte[] cuttedMp3 = Arrays.copyOfRange(mp3, 4087 + 418,
					mp3.length - 1022);

			saveMp3Attachment(db, _id, cuttedMp3, "jokr_plain.mp3", obj);

			obj = db.find(HashMap.class, _id);

			if (laugh == null) {
				InputStream f = context
						.getResourceAsStream("/WEB-INF/audio/laugh1.mp3");
				laugh = IOUtils.toByteArray(f);
			}

			if (clapping == null) {
				InputStream f = context
						.getResourceAsStream("/WEB-INF/audio/clapping1.mp3");
				clapping = IOUtils.toByteArray(f);
			}

			byte[] finished = null;
			if (joke.getFramingBegin() == 1) {
				finished = ConcatMp3.concat(clapping, cuttedMp3);
			} else {
				finished = cuttedMp3;
			}

			if (joke.getFramingEnd() == 1) {
				byte[] withL = ConcatMp3.concat(finished, laugh);
				finished = withL;
			}

			saveMp3Attachment(db, _id, finished, "jokr.mp3", obj);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
					e.getMessage());
		}

	}

	private void saveAttachment(Database db, String id, Part part,
			String fileName, HashMap<String, Object> obj) throws IOException {
		if (part != null) {
			InputStream inputStream = part.getInputStream();
			try {
				db.saveAttachment(inputStream, fileName, "audio/wav", id,
						(String) obj.get("_rev"));
			} finally {
				inputStream.close();
			}
		}
	}

	private void saveMp3Attachment(Database db, String id, byte[] part,
			String fileName, HashMap<String, Object> obj) throws IOException {
		if (part != null) {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(part);
			try {
				db.saveAttachment(inputStream, fileName, "audio/mp3", id,
						(String) obj.get("_rev"));
			} finally {
				inputStream.close();
			}
		}
	}

	private Database getDB() {
		return CloudantClientMgr.getDB();
	}
}
