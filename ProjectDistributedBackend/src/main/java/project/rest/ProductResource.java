package project.rest;

import java.net.URI;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/products")
public class ProductResource {

    // Return all products wrapped in ProductsWrapper for XML parsing
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ProductsWrapper getProducts() {
        List<Product> productList = ProductDAO.INSTANCE.getAllProducts();
        ProductsWrapper wrapper = new ProductsWrapper();
        wrapper.setProducts(productList);
        return wrapper;
    }

    // Get a single product by ID
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProductById(@PathParam("id") int id) {
        Product product = ProductDAO.INSTANCE.getProductById(id);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(product).build();
    }

    // Get products by name (can return multiple matches)
    @GET
    @Path("name/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProductByName(@PathParam("name") String name){
        List<Product> products = ProductDAO.INSTANCE.getProductByName(name);
        if (products.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ProductsWrapper wrapper = new ProductsWrapper();
        wrapper.setProducts(products);
        return Response.ok(wrapper).build();
    }


    // Update an existing product by ID
    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateProduct(@PathParam("id") int id, Product updatedProduct) {
        Product existingProduct = ProductDAO.INSTANCE.getProductById(id);
        if (existingProduct == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Update fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setType(updatedProduct.getType());
        existingProduct.setYear(updatedProduct.getYear());
        existingProduct.setCost(updatedProduct.getCost());
        existingProduct.setCategoryName(updatedProduct.getCategoryName());

        if (updatedProduct.getCompany() != null) {
            existingProduct.setCompany(updatedProduct.getCompany());
        }

        ProductDAO.INSTANCE.updateProduct(existingProduct);
        return Response.ok(existingProduct).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addProduct(Product product, @Context UriInfo uriInfo) {
        int newId = ProductDAO.INSTANCE.getNextAvailableId();
        product.setProductid(newId);
        ProductDAO.INSTANCE.addProduct(product);

        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(newId)).build();
        return Response.created(uri).build();
    }


    // Delete a product by ID
    @DELETE
    @Path("{id}")
    public Response deleteProduct(@PathParam("id") int id) {
        ProductDAO.INSTANCE.deleteProduct(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    @DELETE
    @Path("/all")
    public Response deleteAllProducts() {
        ProductDAO.INSTANCE.deleteAll();  // You’ll need to add this method to DAO
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
