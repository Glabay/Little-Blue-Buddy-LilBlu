package xyz.glabaystudios.web.model.ecomm.wix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WixInventory {

	@JsonProperty private String status;

	@JsonProperty private Double quantity;
}
