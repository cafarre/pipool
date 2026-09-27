package es.fdvcode.pipool.restsrv.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ReleConfigDto {

	private String nom;
	private Integer secondsDuradaCicles;
	private Double consumHora;
	private String unitatConsumHora;
	private Boolean rulesOn;
	private Boolean masterOnObligatori;
	private String idReleMaster;
	private Boolean mqttEnabled;
	private Boolean mqttConsumSensorEnabled;
	private Integer ordre;
	private Boolean enabled;

}
