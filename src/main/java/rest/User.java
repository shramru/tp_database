package rest;

import db.Database;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;
import java.util.NoSuchElementException;
import static main.Helper.*;

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
    public Response create(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            final String values = String.format("'%s', '%s', '%s', '%s', '%s'",
                    jsonObject.get("username"), jsonObject.get("about"), jsonObject.get("name"), jsonObject.get("email"),
                    serializeBoolean(jsonObject.optBoolean("isAnonymous"))).replaceAll("'null'", "null");

            final int uID = database.execUpdate(
                    String.format("INSERT INTO user (username, about, name, email, isAnonymous) VALUES (%s)", values));
            jsonObject.put("id", uID);

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | ClassCastException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request, @Context Database database) throws SQLException {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String email = params.get("user")[0];
            final JSONObject response = new JSONObject();
            userDetails(database, email, response);

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

    public static void userDetails(Database database, String email, JSONObject response) throws SQLException {
        database.execQuery(String.format("SELECT * FROM user where email='%s'", email),
                result -> {
                    result.next();
                    userDetailstoJSON(database, result, response);
                });
    }

    public static void userDetailstoJSON(Database database, ResultSet resultSet, JSONObject response) throws SQLException {
        final String email = resultSet.getString("email");

        response.put("about", resultSet.getString("about") == null ? JSONObject.NULL : resultSet.getString("about"));
        response.put("email", email);
        response.put("id", resultSet.getInt("uID"));
        response.put("isAnonymous", resultSet.getBoolean("isAnonymous"));
        response.put("name", resultSet.getString("name") == null ? JSONObject.NULL : resultSet.getString("name"));
        response.put("username", resultSet.getString("username") == null ? JSONObject.NULL : resultSet.getString("username"));

        database.execQuery(
                String.format("SELECT follower FROM user_user WHERE followee='%s'", email),
                result -> {
                    final JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getString("follower"));

                    response.put("followers", jsonArray);
                });
        database.execQuery(
                String.format("SELECT followee FROM user_user WHERE follower='%s'", email),
                result -> {
                    final JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getString("followee"));

                    response.put("following", jsonArray);
                });
        database.execQuery(
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
    public Response follow(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String follower = jsonObject.getString("follower");
            final String followee = jsonObject.getString("followee");

            database.execUpdate(String.format("INSERT INTO user_user (follower, followee) VALUES ('%s', '%s')", follower, followee));

            final JSONObject response = new JSONObject();
            userDetails(database, follower, response);

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) {
                followDuplicate(database, input, jsonResult);
            } else {
                jsonResult.put("code", 4);
                jsonResult.put("response", "Unknown error");
            }
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    private static void followDuplicate(Database database, String input, JSONObject jsonResult) {
        try {
            final JSONObject jsonObject = new JSONObject(input);
            final JSONObject response = new JSONObject();
            userDetails(database, jsonObject.getString("follower"), response);

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e1) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        } catch (ParseException e1) {
            jsonResult.put("code", (e1.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        }
    }

    @POST
    @Path("unfollow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfollow(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String follower = jsonObject.getString("follower");
            final String followee = jsonObject.getString("followee");

            database.execUpdate(String.format("DELETE FROM user_user WHERE follower='%s' AND followee='%s'", follower, followee));

            final JSONObject response = new JSONObject();
            userDetails(database, follower, response);

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listFollowers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowers(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String query = String.format("SELECT u.* FROM user_user uu JOIN user u ON uu.follower=u.email WHERE followee='%s' %s %s %s",
                    params.get("user")[0],
                    (params.containsKey("since_id") ? String.format("AND uID >= %s", params.get("since_id")[0]) : ""),
                    String.format("ORDER BY name %s", ((params.containsKey("order") ? params.get("order")[0] : "desc"))),
                    (params.containsKey("limit") ? String.format("LIMIT %s", params.get("limit")[0]) : "")
            );

            database.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject user = new JSONObject();
                            userDetailstoJSON(database, result, user);
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
    public Response listFollowing(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String query = String.format("SELECT u.* FROM user_user uu JOIN user u ON uu.followee=u.email WHERE follower='%s' %s %s %s",
                    params.get("user")[0],
                    (params.containsKey("since_id") ? String.format("AND uID >= %s", params.get("since_id")[0]) : ""),
                    String.format("ORDER BY name %s", ((params.containsKey("order") ? params.get("order")[0] : "desc"))),
                    (params.containsKey("limit") ? String.format("LIMIT %s", params.get("limit")[0]) : "")
            );

            database.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject user = new JSONObject();
                            userDetailstoJSON(database, result, user);
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
    public Response listPosts(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String query = String.format("SELECT * FROM post WHERE user='%s' %s %s %s",
                    params.get("user")[0],
                    (params.containsKey("since") ? String.format("AND date >= '%s'", params.get("since")[0]) : ""),
                    String.format("ORDER BY date %s", ((params.containsKey("order") ? params.get("order")[0] : "desc"))),
                    (params.containsKey("limit") ? String.format("LIMIT %s", params.get("limit")[0]) : "")
            );

            database.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject post = new JSONObject();
                            Post.postDetailstoJSON(result, post);
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
    public Response updateProfile(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String email = jsonObject.getString("user");

            database.execUpdate(String.format("UPDATE user SET about='%s', name='%s' WHERE email='%s'",
                    jsonObject.get("about"), jsonObject.get("name"), email).replaceAll("'null'", "null")
            );

            final JSONObject response = new JSONObject();
            userDetails(database, email, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
