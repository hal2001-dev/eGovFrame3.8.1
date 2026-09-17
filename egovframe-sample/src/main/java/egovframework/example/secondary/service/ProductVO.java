package egovframework.example.secondary.service;

import java.io.Serializable;

/**
 * 두 번째 DB(sampledb2)의 상품(PRODUCT) VO.
 */
public class ProductVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 상품 ID */
	private String productId;

	/** 상품명 */
	private String productName;

	/** 가격 */
	private int price;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
}
