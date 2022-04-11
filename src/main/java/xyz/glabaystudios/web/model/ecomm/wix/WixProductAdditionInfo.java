package xyz.glabaystudios.web.model.ecomm.wix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WixProductAdditionInfo {

	@JsonProperty private String id;
	@JsonProperty private String title;
	@JsonProperty private String description;
	@JsonProperty private Long index;


}
