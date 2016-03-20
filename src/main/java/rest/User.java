package rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Map;

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
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            String values = String.format("'%s', '%s', '%s', '%s', '%s'",
                    jsonObject.get("username"), jsonObject.get("about"), jsonObject.get("name"), jsonObject.get("email"),
                    jsonObject.has("isAnonymous") ? (jsonObject.getBoolean("isAnonymous") ? '1' : '0') : '0').replaceAll("'null'", "null");



            RestApplication.DATABASE.execUpdate(String.format("INSERT INTO user (username, about, name, email, isAnonymous) VALUES (%s)", values));
            RestApplication.DATABASE.execQuery(String.format("SELECT * FROM user where email='%s'", jsonObject.getString("email")),
                    result -> {
                        JSONObject response = new JSONObject();

                        result.next();
                        response.put("about", result.getString("about") == null ? JSONObject.NULL : result.getString("about"));
                        response.put("email", result.getString("email"));
                        response.put("id", result.getInt("uID"));
                        response.put("isAnonymous", result.getBoolean("isAnonymous"));
                        response.put("name", result.getString("name") == null ? JSONObject.NULL : result.getString("name"));
                        response.put("username", result.getString("username") == null ? JSONObject.NULL : result.getString("username"));

                        jsonResult.put("code", 0);
                        jsonResult.put("response", response);
                    });
        } catch (SQLException e) {
                jsonResult.put("code", 5);
                jsonResult.put("response", "User exists");
        } catch (JSONException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (RuntimeException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) throws SQLException {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        try {
            String email = params.get("user")[0];
            userDetails(email, jsonResult);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (JSONException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    private void userDetails(String email, JSONObject jsonResult) throws SQLException {
        JSONObject response = new JSONObject();

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
                String.format("SELECT u2.email FROM ((user u1 join user_user uu on u1.uID=uu.followee) join user u2 on uu.follower=u2.uID) WHERE u1.email='%s'",
                        email),
                result -> {
                    JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getString("email"));

                    response.put("followers", jsonArray);
                });
        RestApplication.DATABASE.execQuery(
                String.format("SELECT u2.email FROM ((user u1 join user_user uu on u1.uID=uu.follower) join user u2 on uu.followee=u2.uID) WHERE u1.email='%s'",
                        email),
                result -> {
                    JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getString("email"));

                    response.put("following", jsonArray);
                });
        RestApplication.DATABASE.execQuery(
                String.format("SELECT tID FROM user u JOIN user_thread ut ON u.uID=ut.uID WHERE email='%s'",
                        email),
                result -> {
                    JSONArray jsonArray = new JSONArray();
                    while (result.next())
                        jsonArray.put(result.getString("tID"));

                    response.put("subscriptions", jsonArray);
                });

        jsonResult.put("code", 0);
        jsonResult.put("response", response);
    }

    @POST
    @Path("follow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response follow(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("unfollow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfollow(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listFollowers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowers(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listFollowing")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowing(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("updateProfile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(final String input, @Context HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject(input);
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
