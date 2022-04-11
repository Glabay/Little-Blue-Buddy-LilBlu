package xyz.glabaystudios.web.model.ecomm.wix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WixMedia {

	@JsonProperty private String id;
	@JsonProperty private String url;
	@JsonProperty private String fullUrl;
	@JsonProperty private String altText;
	@JsonProperty private String thumbnailFullUrl;
	@JsonProperty private String mediaType;
	@JsonProperty private String videoType;

	@JsonProperty private String[] videoFiles;

	@JsonProperty private Long width;
	@JsonProperty private Long height;
	@JsonProperty private Long index;

	@JsonProperty private String title;
}
