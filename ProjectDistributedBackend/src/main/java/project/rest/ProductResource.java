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
    
    @GET
    @Path("name/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProductByName(@PathParam("name") String name){
        List<Product> products = ProductDAO.INSTANCE.getProductByName(name);
        if (products.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(products).build();
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addProduct(Product product) {
        ProductDAO.INSTANCE.addProduct(product);
        return Response.status(Response.Status.CREATED).build();
    }
    
    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateProduct(@PathParam("id") int id, Product updatedProduct) {
        // Retrieve the existing product
        Product existingProduct = ProductDAO.INSTANCE.getProductById(id);
        if (existingProduct == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Update fields based on the provided updatedProduct data.
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setType(updatedProduct.getType());
        existingProduct.setYear(updatedProduct.getYear());
        existingProduct.setCost(updatedProduct.getCost());
        existingProduct.setCategoryid(updatedProduct.getCategoryid());
        
        // Save the updated product to the database
        ProductDAO.INSTANCE.updateProduct(existingProduct);
        
        return Response.ok(existingProduct).build();
    } 
    
    @DELETE
    @Path("{id}")
    public Response deleteProduct(@PathParam("id") int id) {
        ProductDAO.INSTANCE.deleteProduct(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
