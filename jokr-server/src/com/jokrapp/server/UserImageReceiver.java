package com.jokrapp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response;

import com.cloudant.client.api.Database;

@WebServlet("/addUserImage")
@MultipartConfig()
public class UserImageReceiver extends HttpServlet {

	private static final Logger log = Logger.getLogger(UserImageReceiver.class
			.getName());

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Part part = request.getPart("file");
		String userId = request.getParameter("userId");

		if (userId == null) {
			throw new ServletException("Incorrect parameters... :-(");
		}

		log.info("Receiving file... from: " + userId);

		Database db = null;
		try {
			db = CloudantClientMgr.getDB();
		} catch (Exception re) {
			re.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		// attach the attachment object
		HashMap<String, Object> obj = db.find(HashMap.class, userId);

		if (obj != null) {
			try {
				saveAttachment(db, userId, part, "profilePic.jpg", obj);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
						e.getMessage());
			}
		} else {
			throw new RestException(Response.Status.BAD_REQUEST,
					"User not found.");
		}
		log.info("Upload of image for user completed.");

	}

	private void saveAttachment(Database db, String id, Part part,
			String fileName, HashMap<String, Object> obj) throws IOException {
		if (part != null) {
			InputStream inputStream = part.getInputStream();
			try {
				db.saveAttachment(inputStream, fileName, "image/jpg", id,
						(String) obj.get("_rev"));
			} finally {
				inputStream.close();
			}
		}
	}

}
