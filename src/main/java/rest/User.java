package rest;

import org.json.JSONArray;
import org.json.JSONObject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by vladislav on 18.03.16.
 */
@Singleton
@Path("/user")
public class User {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            final String values = String.format("'%s', '%s', '%s', '%s', '%s'",
                    jsonObject.get("username"), jsonObject.get("about"), jsonObject.get("name"), jsonObject.get("email"),
                    jsonObject.has("isAnonymous") ? (jsonObject.getBoolean("isAnonymous") ? '1' : '0') : '0').replaceAll("'null'", "null");

            final int uID = RestApplication.DATABASE.execUpdate(String.format("INSERT INTO user (username, about, name, email, isAnonymous) VALUES (%s)", values));
            jsonObject.put("id", uID);

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
                jsonResult.put("code", 5);
                jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) throws SQLException {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String email = params.get("user")[0];
            final JSONObject response = new JSONObject();
            userDetails(email, response);

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (RuntimeException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    public static void userDetails(String email, JSONObject response) throws SQLException {
        RestApplication.DATABASE.execQuery(String.format("SELECT * FROM user where email='%s'", email),
                result -> {
                    result.next();
                    response.put("about", result.getString("about") == null ? JSONObject.NULL : result.getString("about"));
                    response.put("email", result.getString("email"));
                    response.put("id", result.getInt("uID"));
                    response.put("isAnonymous", result.getBoolean("isAnonymous"));
                    response.put("name", result.getString("name") == null ? JSONObject.NULL : result.getString("name"));
                    response.put("username", result.getString("username") == null ? JSONObject.NULL : result.getString("username"));
                });
        RestApplication.DATABASE.execQuery(
                String.format("SELECT follower FROM user_user WHERE followee='%s'", email),
                result -> {
                    final JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getString("follower"));

                    response.put("followers", jsonArray);
                });
        RestApplication.DATABASE.execQuery(
                String.format("SELECT followee FROM user_user WHERE follower='%s'", email),
                result -> {
                    final JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getString("followee"));

                    response.put("following", jsonArray);
                });
        RestApplication.DATABASE.execQuery(
                String.format("SELECT tID FROM user_thread WHERE user='%s'", email),
                result -> {
                    final JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getInt("tID"));

                    response.put("subscriptions", jsonArray);
                });
    }

    @POST
    @Path("follow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response follow(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String follower = jsonObject.getString("follower");
            final String followee = jsonObject.getString("followee");

            RestApplication.DATABASE.execUpdate(String.format("INSERT INTO user_user (follower, followee) VALUES ('%s', '%s')", follower, followee));

            final JSONObject response = new JSONObject();
            userDetails(follower, response);

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("unfollow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfollow(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String follower = jsonObject.getString("follower");
            final String followee = jsonObject.getString("followee");

            RestApplication.DATABASE.execUpdate(String.format("DELETE FROM user_user WHERE follower='%s' AND followee='%s'", follower, followee));

            final JSONObject response = new JSONObject();
            userDetails(follower, response);

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listFollowers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowers(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
           final String email = params.get("user")[0];
            String query = String.format("SELECT follower FROM user_user uu JOIN user u ON uu.follower=u.email WHERE followee='%s'%s ORDER BY name DESC", email,
                    (params.containsKey("since_id") ? String.format(" AND uID >= %s", params.get("since_id")[0]) : ""));
            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit"))
                query += " LIMIT " + params.get("limit")[0];

            RestApplication.DATABASE.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject user = new JSONObject();
                            userDetails(result.getString("follower"), user);
                            jsonArray.put(user);
                        }

                        jsonResult.put("code", 0);
                        jsonResult.put("response", jsonArray);
                    });
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (RuntimeException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listFollowing")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowing(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String email = params.get("user")[0];
            String query = String.format("SELECT followee FROM user_user uu JOIN user u ON uu.followee=u.email WHERE follower='%s'%s ORDER BY name DESC", email,
                    (params.containsKey("since_id") ? String.format(" AND uID >= %s", params.get("since_id")[0]) : ""));
            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit"))
                query += " LIMIT " + params.get("limit")[0];

            RestApplication.DATABASE.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject user = new JSONObject();
                            userDetails(result.getString("followee"), user);
                            jsonArray.put(user);
                        }

                        jsonResult.put("code", 0);
                        jsonResult.put("response", jsonArray);
                    });
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (RuntimeException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String email = params.get("user")[0];
            String query = String.format("SELECT pID FROM post WHERE user='%s'%s ORDER BY date DESC", email,
                    (params.containsKey("since") ? String.format(" AND date >= '%s'", params.get("since")[0]) : ""));
            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit"))
                query += " LIMIT " + params.get("limit")[0];

            RestApplication.DATABASE.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject post = new JSONObject();
                            Post.postDetails(result.getString("pID"), post);
                            jsonArray.put(post);
                        }

                        jsonResult.put("code", 0);
                        jsonResult.put("response", jsonArray);
                    });
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (RuntimeException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("updateProfile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String email = jsonObject.getString("user");

            RestApplication.DATABASE.execUpdate(String.format("UPDATE user SET about='%s', name='%s' WHERE email='%s'", jsonObject.get("about"), jsonObject.get("name"), email).replaceAll("'null'", "null"));

            final JSONObject response = new JSONObject();
            userDetails(email, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
