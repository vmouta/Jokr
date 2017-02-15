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
import com.jokrapp.server.data.model.Joke;

@WebServlet("/fetchJoke")
public class JokeService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String mixedPath;

	private File jokeDir;

	/**
	 * 
	 * @param context
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		mixedPath = config.getServletContext().getRealPath("/WEB-INF/jokes");

		jokeDir = new File(mixedPath);
		if (!jokeDir.exists()) {
			jokeDir.mkdir();
		}
		if (!jokeDir.exists()) {
			throw new ServletException("upload dir doesn't exist");
		}
		if (!jokeDir.isDirectory()) {
			throw new ServletException("not a dir");
		}

		if (jokeDir.list() == null) {
			throw new ServletException("dir is empty");
		}

	}

	/**
	 * Upon receiving file upload submission, parses the request to read upload
	 * data and saves the file on disk.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String jokeId = request.getParameter("jokeId");

		if (jokeId == null) {
			throw new RestException(Response.Status.BAD_REQUEST,
					"Missing param: jokeId.");
		}

		String jokeString = jokeDir.getAbsolutePath() + File.separator + jokeId
				+ ".mp3";

		File audioFile = new File(jokeString);

		if (!audioFile.exists()) {

			Database db = null;
			try {
				db = getDB();
			} catch (Exception re) {
				re.printStackTrace();
				throw new WebApplicationException(
						Response.Status.INTERNAL_SERVER_ERROR);
			}

			Joke joke = null;

			try {
			
				// check if joke exists
				joke = db.find(Joke.class, jokeId, new Params().attachments());

			} catch (NoDocumentException e) {
				throw new RestException(Response.Status.BAD_REQUEST,
						"Joke doesn't exist.");
			}

			
			Attachment a = joke.get_attachments().get("jokr.mp3");
			FileOutputStream outs = null;
			try {
				outs = new FileOutputStream(audioFile);
				outs.write(Base64.getDecoder().decode(a.getData()));
				outs.close();
			} finally {
				outs.close();
			}
		}

		response.setHeader("Content-Length", audioFile.length()+"");
		response.setHeader("Content-Type", "audio/mp3");
		ServletOutputStream os = response.getOutputStream();
		FileInputStream fis = new FileInputStream(audioFile);
		
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