package xyz.glabaystudios.web.model.ecomm.wix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WixProduct {

	@JsonProperty private String id;
	@JsonProperty private String description;

	@JsonProperty private Boolean isVisible;

	@JsonProperty private String sku;
	@JsonProperty private String ribbon;
	@JsonProperty private String brand;

	@JsonProperty private Double price;
	@JsonProperty private Double comparePrice;
	@JsonProperty private Double discountedPrice;

	@JsonProperty private String formattedPrice;
	@JsonProperty private String formattedComparePrice;
	@JsonProperty private String formattedDiscountedPrice;
	@JsonProperty private String pricePerUnit;
	@JsonProperty private String formattedPricePerUnit;
	@JsonProperty private String pricePerUnitData;
	@JsonProperty private String seoTitle;
	@JsonProperty private String seoDescription;

	@JsonProperty private Long createVersion;

	@JsonProperty private String[] digitalProductFileItems;

	@JsonProperty private WixProductItem[] productItems;

	@JsonProperty private String name;

	@JsonProperty private Boolean isTrackingInventory;

	@JsonProperty private WixInventory inventory;

	@JsonProperty private Boolean isManageProductItems;
	@JsonProperty private Boolean isInStock;

	@JsonProperty private WixMedia[] media;

	@JsonProperty private String[] customTextFields;

	@JsonProperty private Long nextOptionsSelectionId;

	@JsonProperty private String[] options;

	@JsonProperty private String productType;
	@JsonProperty private String urlPart;

	@JsonProperty private WixProductAdditionInfo[] additionalInfo;

	@JsonProperty private WixSubscriptionList subscriptionPlans;

	@JsonProperty private WixDiscount discount;

	@JsonProperty private String currency;
	@JsonProperty private Double weight;
	@JsonProperty private String seoJson;
}
