package xyz.glabaystudios.web.model.ecomm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopifyMedia {

	@JsonProperty private String alt;
	@JsonProperty private Long id;
	@JsonProperty private Integer position;
	@JsonProperty private SitePlusImagePreview preview_image;
	@JsonProperty private Double aspect_ratio;
	@JsonProperty private Integer height;
	@JsonProperty private String media_type;
	@JsonProperty private String src;
	@JsonProperty private Integer width;


	@Data
	public static class SitePlusImagePreview {
		@JsonProperty private Double aspect_ratio;
		@JsonProperty private Integer height;
		@JsonProperty private Integer width;
		@JsonProperty private String src;
	}
}
