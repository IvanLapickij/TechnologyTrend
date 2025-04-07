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

    // GET all products wrapped in ProductsWrapper for XML parsing
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ProductsWrapper getProducts() {
        List<Product> productList = ProductDAO.INSTANCE.getAllProducts();
        ProductsWrapper wrapper = new ProductsWrapper();
        wrapper.setProducts(productList);
        return wrapper;
    }

    // GET porduct by productID
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

    // GET products by name
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


    // PUT/update product by ID
    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateProduct(@PathParam("id") int id, Product updatedProduct) {
        Product existingProduct = ProductDAO.INSTANCE.getProductById(id);
        if (existingProduct == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // updating fields
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
    
    //POST/ addProduct
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addProduct(Product product, @Context UriInfo uriInfo) {
        int newId = ProductDAO.INSTANCE.getNextAvailableId();
        product.setProductid(newId);
        ProductDAO.INSTANCE.addProduct(product);

        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(newId)).build();
        return Response.created(uri).build();
    }


    // DELETE by productID
    @DELETE
    @Path("{id}")
    public Response deleteProduct(@PathParam("id") int id) {
        ProductDAO.INSTANCE.deleteProduct(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    //DELETE ALL
    @DELETE
    @Path("/all")
    public Response deleteAllProducts() {
        ProductDAO.INSTANCE.deleteAll();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
