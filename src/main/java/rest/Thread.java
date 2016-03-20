package rest;

import org.json.JSONObject;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by vladislav on 18.03.16.
 */
@Singleton
@Path("/thread")
public class Thread {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@Context HttpServletRequest request) {
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
    @Path("open")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response open(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response close(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("restore")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restore(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response subscribe(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("unsubscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unsubscribe(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("vote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response vote(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        JSONObject jsonObject = new JSONObject(input);
        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
