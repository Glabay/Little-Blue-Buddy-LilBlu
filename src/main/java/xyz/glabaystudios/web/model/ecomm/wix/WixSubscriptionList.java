package xyz.glabaystudios.web.model.ecomm.wix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WixSubscriptionList {

	@JsonProperty private String[] list;

	@JsonProperty private String oneTimePurchase;
}
