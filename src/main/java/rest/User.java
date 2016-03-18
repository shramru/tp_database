package rest;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Response create(@Context HttpServletRequest request) {
        return null;
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) {
        return null;
    }

    @POST
    @Path("follow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response follow(@Context HttpServletRequest request) {
        return null;
    }

    @POST
    @Path("unfollow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfollow(@Context HttpServletRequest request) {
        return null;
    }

    @GET
    @Path("listFollowers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowers(@Context HttpServletRequest request) {
        return null;
    }

    @GET
    @Path("listFollowing")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFollowing(@Context HttpServletRequest request) {
        return null;
    }

    @GET
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request) {
        return null;
    }

    @POST
    @Path("updateProfile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(@Context HttpServletRequest request) {
        return null;
    }
}
