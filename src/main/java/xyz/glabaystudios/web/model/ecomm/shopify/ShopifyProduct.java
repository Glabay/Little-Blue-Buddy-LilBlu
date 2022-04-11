package xyz.glabaystudios.web.model.ecomm.shopify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopifyProduct {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("title") private String title;
	@JsonProperty private String handle;
	@JsonProperty private String description;
	@JsonProperty private String published_at;
	@JsonProperty private String created_at;
	@JsonProperty private String vendor;
	@JsonProperty private String type;

	@JsonProperty private String[] tags;

	@JsonProperty private String price;
	@JsonProperty private String price_min;
	@JsonProperty private String price_max;

	@JsonProperty private Boolean available;
	@JsonProperty private Boolean price_varies;

	@JsonProperty private String compare_at_price;
	@JsonProperty private String compare_at_price_min;
	@JsonProperty private String compare_at_price_max;

	@JsonProperty private Boolean compare_at_price_varies;

	@JsonProperty private ShopifyVariant[] variants;

	@JsonProperty private String[] images;
	@JsonProperty private String featured_image;
	@JsonProperty private String[] options;

	@JsonProperty private ShopifyMedia[] media;

	@JsonProperty private Boolean requires_selling_plan;
	@JsonProperty private String[] selling_plan_groups;
	@JsonProperty private String content;
}
