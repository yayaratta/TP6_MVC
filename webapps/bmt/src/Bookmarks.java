import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.util.ajax.JSON.Source;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Provides handling of tag-related requests.
 * 
 * @author Guillaume Halb, Benjamin Lebit, Ayutaya Rattanatray
 */
public class Bookmarks {
	/**
	 * Handles the request for the bookmark list.
	 * 
	 * @param req
	 *           the request
	 * @param resp
	 *           the response
	 * @param method
	 *           request method to appply
	 * @param requestPath
	 *           request path
	 * @param queryParams
	 *           query parameters
	 * @param user
	 *           the user
	 * @throws IOException
	 *            if the response cannot be written
	 * @throws SQLException 
	 */
	public static void handleBookmarkList(HttpServletRequest req, HttpServletResponse resp,
			Dispatcher.RequestMethod method, String[] requestPath,
			Map<String, List<String>> queryParams, User user) throws IOException, SQLException {
		// Rule-out PUT and DELETE requests
		System.out.println("Action: handleBookmarkList - " + method + "-" + queryParams);
		if (method == Dispatcher.RequestMethod.PUT || method == Dispatcher.RequestMethod.DELETE) {
			resp.setStatus(405);
			return;
		}

		// Handle GET
		if (method == Dispatcher.RequestMethod.GET) {
			// Get the Bookmark list
			List<Tag> tags = null;
			try {
				tags = TagDAO.getTags(user);
			} catch (SQLException ex) {
				resp.setStatus(500);
				return;
			}

			// Encode the tag list to JSON
			String json = "[";
			for (int i = 0, n = tags.size(); i < n; i++) {
				Tag tag = tags.get(i);
				json += tag.toJson();
				if (i < n - 1)
					json += ", ";
			}
			json += "]";

			// Send the response
			resp.setStatus(200);
			resp.setContentType("application/json");
			resp.getWriter().print(json);
			return;
		}

		// Handle POST
		
		if (method == Dispatcher.RequestMethod.POST) {
			// Get the tag list
			List<Tag> tags = null;
			try {
				tags = TagDAO.getTags(user);
				} catch (SQLException ex) {
					resp.setStatus(500);
					return;
				}
			
			//parse the query params
			JSONObject tagToAddJson = new JSONObject(queryParams.get("json").get(0));
			String tagName = (String) tagToAddJson.get("name");
			Tag tagToAdd = new Tag(tagName);
			
			
			//le tag existe deja
			if (TagDAO.getTagByName(tagToAdd.getName(), user)!=null)
			{
				resp.setStatus(304);
				return;
			} else 
			{
				TagDAO.saveTag(tagToAdd, user);
				resp.setStatus(201);
				return;
			}
			// TODO 1
		}

		// Other
		resp.setStatus(405);
	}
}
