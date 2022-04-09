package xyz.glabaystudios.web.model.ecomm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopifyVariant {
	@JsonProperty private Long id;

	@JsonProperty private String title;
	@JsonProperty private String option1;
	@JsonProperty private String option2;
	@JsonProperty private String option3;
	@JsonProperty private String sku;
	@JsonProperty private Boolean requires_shipping;
	@JsonProperty private Boolean taxable;
	@JsonProperty private String featured_image;
	@JsonProperty private Boolean available;
	@JsonProperty private String name;
	@JsonProperty private String public_title;
	@JsonProperty private String[] options;
	@JsonProperty private String price;
	@JsonProperty private String weight;
	@JsonProperty private String compare_at_price;
	@JsonProperty private String inventory_management;
	@JsonProperty private String barcode;
	@JsonProperty private Boolean requires_selling_plan;
	@JsonProperty private String[] selling_plan_allocations;

}
