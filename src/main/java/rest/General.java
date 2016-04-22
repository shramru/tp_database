package rest;

import org.json.JSONObject;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

/**
 * Created by vladislav on 20.03.16.
 */
@Singleton
@Path("/")
public class General {

    @POST
    @Path("clear")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            RestApplication.DATABASE.execUpdate("CALL clear");

            jsonResult.put("code", 0);
            jsonResult.put("response", "OK");
        } catch (SQLException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            RestApplication.DATABASE.execQuery("CALL status",
                    result -> {
                        final JSONObject response = new JSONObject();

                        result.next();
                        response.put("user", result.getInt("user"));
                        response.put("thread", result.getInt("thread"));
                        response.put("forum", result.getInt("forum"));
                        response.put("post", result.getInt("post"));

                        jsonResult.put("code", 0);
                        jsonResult.put("response", response);
                    });
        } catch (SQLException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
