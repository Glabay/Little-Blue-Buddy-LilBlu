package xyz.glabaystudios.web.model.ecomm;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@ToString
public class PegaProduct {

	private String productName;
	private double productPriceBase;
	private String productDescription;
	private String productWeight;
	private double productWidth;
	private double productLength;
	private double productHeight;

	private boolean onSale;
	private double listedPrice = 0.0;
	private double whatYouSave = 0.0;

	private final HashMap<String, List<String>> productOptions;
	private final HashMap<String, List<String>> productOptionPriceAdjustments;

	private final ArrayList<String> productImages = new ArrayList<>();


	public PegaProduct() {
		productOptions = new HashMap<>();
		productOptionPriceAdjustments = new HashMap<>();
	}

	public String getInfo() {
		String base = "Product: %n%s%nPrice: %n$%.2f%nDescription: %n%s%nWeight: %n%s%nDimensions: W=%.2f, L=%.2f, H=%.2f%nProduct Images: %n%s%nOptions: %n%s%nOption Adjustments: %n%s%n";

		StringBuilder optionBuilder = new StringBuilder();
		productOptions.keySet().forEach(option -> {
			optionBuilder.append("\n").append(option).append(":\n");
			productOptions.get(option).forEach(opt -> optionBuilder.append("\t").append(opt).append("\n"));
		});
		StringBuilder optionAdjustmentsBuilder = new StringBuilder();
		productOptionPriceAdjustments.keySet().forEach(option -> {
			optionAdjustmentsBuilder.append("\n").append(option).append(":\n");
			productOptionPriceAdjustments.get(option).forEach(opt -> optionAdjustmentsBuilder.append("\t").append(opt).append("\n"));
		});
		StringBuilder productImageList = new StringBuilder();
		productImages.forEach(imageLink -> productImageList.append(imageLink).append("\n"));

		return String.format(base,
				productName,
				productPriceBase,
				productDescription,
				productWeight,
				productWidth,
				productLength,
				productHeight,
				productImageList,
				optionBuilder,
				optionAdjustmentsBuilder);
	}
}
