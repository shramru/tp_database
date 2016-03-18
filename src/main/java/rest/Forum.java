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
@Path("/forum")
public class Forum {

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

    @GET
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request) {
        return null;
    }

    @GET
    @Path("listThreads")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listThreads(@Context HttpServletRequest request) {
        return null;
    }

    @GET
    @Path("listUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@Context HttpServletRequest request) {
        return null;
    }
}
