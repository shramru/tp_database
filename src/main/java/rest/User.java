package rest;

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
        JSONObject jsonObject = new JSONObject(input);

        String columns = String.format("username, about, name, email%s", jsonObject.has("isAnonymous") ? ", isAnonymous" : "");
        String values = String.format("'%s', '%s', '%s', '%s'%s",
                jsonObject.getString("username"), jsonObject.getString("about"), jsonObject.getString("name"), jsonObject.getString("email"),
                jsonObject.has("isAnonymous") ? ", " + (jsonObject.getBoolean("isAnonymous") ? '1' : '0') : "");

        try {
            RestApplication.DATABASE.execUpdate(String.format("INSERT INTO user (%s) VALUES (%s)", columns, values));
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK).entity("OK").build();
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("follow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response follow(final String input, @Context HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("unfollow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfollow(final String input, @Context HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("listFollowers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowers(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("listFollowing")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowing(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("updateProfile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(final String input, @Context HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).build();
    }
}
