package project.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/products")
public class ProductResources {
	
	@GET
	@Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
	public List<Product> getProducts(){
		return ProductDao.instance.getProducts();
	}
	
	@GET
	@Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
	@Path("{productid}")
//	@Path("{prodid}")
//	public Product getBook(@PathParam("prodid") String productid){
	public Product getProduct(@PathParam("productid") String productid){//should productid should be prodid
		return ProductDao.instance.getProduct(Integer.parseInt(productid));
	}
}
