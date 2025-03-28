package project.rest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ProductDao {
	instance;
	
private Map<Integer, Product> productsMap = new HashMap<Integer, Product>();
	
	private ProductDao() {
		
		Product product1 = new Product();
		product1.setProductid(1);
		product1.setName("The Egg");
		product1.setType("Power");
		product1.setYear(2025);
		product1.setCost(19000);
		product1.setCategoryid(12);
		
		productsMap.put(1, product1);
		
		Product product2 = new Product();
		product2.setProductid(2);
		product2.setName("Smart Lock");
		product2.setType("Device");
		product2.setYear(2024);
		product2.setCost(230);
		product2.setCategoryid(11);
		
		productsMap.put(2, product2);
	}
	
	public List<Product> getProducts(){
		List<Product> products = new ArrayList<Product>();
		products.addAll(productsMap.values());
		return products;
	}
	
	public Product getProduct(int productid) {
		return productsMap.get(productid);
	}
}
