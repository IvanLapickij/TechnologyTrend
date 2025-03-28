package project.rest;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/products")
public class ProductResource {
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Product> getProducts(){
        return ProductDAO.INSTANCE.getAllProducts();
    }
    
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProductById(@PathParam("id") int id){
        Product product = ProductDAO.INSTANCE.getProductById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addProduct(Product product) {
        ProductDAO.INSTANCE.addProduct(product);
        return Response.status(Response.Status.CREATED).build();
    }
    
    @DELETE
    @Path("{id}")
    public Response deleteProduct(@PathParam("id") int id) {
        ProductDAO.INSTANCE.deleteProduct(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
