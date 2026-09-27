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
public class SondaConfigDto {

	private String nom;
	private Double minValor;
	private Double maxValor;
	private String idReleCorrector;
	private Integer ordre;
	private String unitats;
	private String haDeviceClass;

}
