package com.jokrapp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.lightcouch.Attachment;
import org.lightcouch.NoDocumentException;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Params;
import com.jokrapp.server.data.model.User;

@WebServlet("/fetchUserPic")
public class UserPicService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String mixedPath;

	private File userPicDir;

	/**
	 * 
	 * @param context
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		mixedPath = config.getServletContext().getRealPath("/WEB-INF/userPics");

		userPicDir = new File(mixedPath);
		if (!userPicDir.exists()) {
			userPicDir.mkdir();
		}
		if (!userPicDir.exists()) {
			throw new ServletException("upload dir doesn't exist");
		}
		if (!userPicDir.isDirectory()) {
			throw new ServletException("not a dir");
		}

		if (userPicDir.list() == null) {
			throw new ServletException("dir is empty");
		}

	}

	/**
	 * Upon receiving file upload submission, parses the request to read upload
	 * data and saves the file on disk.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String userId = request.getParameter("userId");

		if (userId == null) {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: userId.");
		}

		String jokeString = userPicDir.getAbsolutePath() + File.separator
				+ userId + ".mp3";

		File userFile = new File(jokeString);

		if (!userFile.exists()) {

			Database db = null;
			try {
				db = getDB();
			} catch (Exception re) {
				re.printStackTrace();
				throw new WebApplicationException(
						Response.Status.INTERNAL_SERVER_ERROR);
			}

			User user = null;

			try {
				// check if user exists
				user = db.find(User.class, userId, new Params().attachments());
			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"User doesn't exist.");
			}

			if (user.get_attachments() != null) {
				Attachment a = user.get_attachments().get("profilePic.jpg");
			
				if (a == null) {
					throw new RestException(Response.Status.NOT_FOUND,
							"User has no pic yet.");
				}
			
				FileOutputStream outs = null;
				try {
					outs = new FileOutputStream(userFile);
					outs.write(Base64.getDecoder().decode(a.getData()));
					outs.close();
				} finally {
					outs.close();
				}
			} else {
				throw new RestException(Response.Status.NOT_FOUND,
						"User has no pic yet.");
			}
			
		}

		ServletOutputStream os = response.getOutputStream();
		FileInputStream fis = new FileInputStream(userFile);
		try {
			IOUtils.copy(fis, os);
			os.flush();
		} finally {
			os.close();
			fis.close();
		}
	}

	private Database getDB() {
		return CloudantClientMgr.getDB();
	}

}