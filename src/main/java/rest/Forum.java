package rest;

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
@Path("/forum")
public class Forum {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            String a = jsonObject.getString("name");

            String values = String.format("'%s', '%s', '%s'",
                    jsonObject.getString("name"), jsonObject.getString("short_name"), jsonObject.getString("user"));

            int fID = RestApplication.DATABASE.execUpdate(String.format("INSERT INTO forum (name, short_name, user) VALUES (%s)", values));
            jsonObject.put("id", fID);

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

    public static void forumDetails(String shortName, JSONObject response) throws SQLException {
        RestApplication.DATABASE.execQuery(String.format("SELECT fID, name, user FROM forum WHERE short_name='%s'", shortName),
                result -> {
                    result.next();
                    response.put("short_name", shortName);
                    response.put("id", result.getInt("fID"));
                    response.put("name", result.getString("name"));
                    response.put("user", result.getString("user"));
                });
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        try {
            String shortName = params.get("forum")[0];
            JSONObject response = new JSONObject();
            forumDetails(shortName, response);

            if (params.containsKey("related")) {
                JSONObject user = new JSONObject();
                User.userDetails(response.getString("user"), user);
                response.put("user", user);
            }

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

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

    @GET
    @Path("listThreads")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listThreads(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("listUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
