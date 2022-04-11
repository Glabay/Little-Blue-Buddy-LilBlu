package xyz.glabaystudios.web.model.ecomm.wix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WixProductItem {

	@JsonProperty private String id;

	@JsonProperty private Double price;
	@JsonProperty private Double comparePrice;

	@JsonProperty private String formattedPrice;
	@JsonProperty private String formattedComparePrice;
	@JsonProperty private String pricePerUnit;
	@JsonProperty private String formattedPricePerUnit;

	@JsonProperty private String[] optionsSelections;

	@JsonProperty private Boolean isVisible;

	@JsonProperty private WixInventory inventory;

	@JsonProperty private String sku;

	@JsonProperty private Double weight;
	@JsonProperty private Double surcharge;

	@JsonProperty private WixSubscriptionList subscriptionPlans;


}
