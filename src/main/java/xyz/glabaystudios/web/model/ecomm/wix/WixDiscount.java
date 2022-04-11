package xyz.glabaystudios.web.model.ecomm.wix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WixDiscount {

	@JsonProperty private String mode;

	@JsonProperty private Long value;
}
