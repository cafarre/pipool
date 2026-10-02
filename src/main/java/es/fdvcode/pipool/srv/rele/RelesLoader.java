package es.fdvcode.pipool.srv.rele;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import es.fdvcode.pipool.common.ObjJsonPrinter;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.RuleCondicio;
import es.fdvcode.pipool.model.rele.RuleRele;
import es.fdvcode.pipool.model.rele.StateRele;
import jakarta.annotation.PostConstruct;

/**
 * 
 * @author cfarrema
 *
 */
@Component
public class RelesLoader {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final Map<String, Rele> mapReles = new HashMap<>();

	@Autowired 
	ObjJsonPrinter objJsonPrinter;

	@Value("${pipool.reles.file}")
	private String fileConfig;

	private abstract static class ReleConfigFileMixIn {
		@JsonIgnore
		abstract StateRele getCopyStateRele();
	}

	private abstract static class RuleReleConfigFileMixIn {
		@JsonIgnore
		abstract boolean isActivada();
		@JsonIgnore
		abstract Date getDtActivada();
		@JsonIgnore
		abstract Date getDtAturada();
	}

	private abstract static class RuleCondicioConfigFileMixIn {
		@JsonIgnore
		abstract String getValorDatoCondicio();
		@JsonIgnore
		abstract boolean isCumpleCondicio();
	}

	private static final String DEFAULT_RELES_JSON = """
[
  {
    "id": "rele_bomba",
    "nom": "Bomba Filtre",
    "enabled": true,
    "mqttType": "SWITCH",
    "mqttEnabled": true,
    "mqttConsumSensorEnabled": false,
    "ordre": 1,
    "gpioPin": 26,
    "gpioStateWhenReleOn": false,
    "secondsDuradaCicles": 10800,
    "calendars": [
      {
        "id": "ProgBombaEstiu",
        "nom": "Programació Bomba Estiu",
        "diaIni": 1,
        "mesIni": 6,
        "diaFin": 15,
        "mesFin": 9,
        "listFrangesHoraries": [
          {
            "tipus": "Normal",
            "activarRelesSlaves": true,
            "horaIni": 23,
            "minutIni": 0,
            "secondIni": 0,
            "duracioSeconds": 1770
          }
        ]
      },
      {
        "id": "ProgBombaTardor",
        "nom": "Programació Bomba Tardor",
        "diaIni": 16,
        "mesIni": 9,
        "diaFin": 15,
        "mesFin": 11,
        "listFrangesHoraries": [
          {
            "tipus": "Normal",
            "activarRelesSlaves": true,
            "horaIni": 23,
            "minutIni": 0,
            "secondIni": 0,
            "duracioSeconds": 1770
          }
        ]
      },
      {
        "id": "ProgBombaHivern",
        "nom": "Programació Bomba Hivern",
        "diaIni": 16,
        "mesIni": 11,
        "diaFin": 31,
        "mesFin": 3,
        "listFrangesHoraries": [
          {
            "tipus": "Normal",
            "activarRelesSlaves": true,
            "horaIni": 23,
            "minutIni": 0,
            "secondIni": 0,
            "duracioSeconds": 1770
          }
        ]
      },
      {
        "id": "ProgBombaPrimavera",
        "nom": "Programació Bomba Primavera",
        "diaIni": 1,
        "mesIni": 4,
        "diaFin": 31,
        "mesFin": 5,
        "listFrangesHoraries": [
          {
            "tipus": "Normal",
            "activarRelesSlaves": true,
            "horaIni": 23,
            "minutIni": 0,
            "secondIni": 0,
            "duracioSeconds": 1770
          }
        ]
      }
    ],
    "rules": [
      {
        "id": "AnticongelacioBomba",
        "activateOnTrue": true,
        "activarRelesSlaves": true,
        "segonsFinsProximaActivacio": 1800,
        "condicionsActivacio": [
          {
            "dato": "sonda_temp",
            "operand": "LE",
            "valor": 2.0
          }
        ],
        "condicionsDesactivacio": [
          {
            "dato": "sonda_temp",
            "operand": "GE",
            "valor": 3.0
          },
          {
            "dato": "DuracioSeconds",
            "operand": "GE",
            "valor": 900
          }
        ]
      },
      {
        "id": "Filtre-DosificacioBombaClor",
        "activateOnTrue": true,
        "activarRelesSlaves": true,
        "segonsFinsProximaActivacio": 30,
        "condicionsActivacio": [
          {
            "dato": "rele_bomba_clor",
            "operand": "E",
            "valor": 1
          }
        ],
        "condicionsDesactivacio": [
          {
            "dato": "DuracioSeconds",
            "operand": "GE",
            "valor": 1500
          }
        ]
      },
      {
        "id": "Filtre-DosificacioBombPH",
        "activateOnTrue": true,
        "activarRelesSlaves": true,
        "segonsFinsProximaActivacio": 30,
        "condicionsActivacio": [
          {
            "dato": "rele_bomba_acid",
            "operand": "E",
            "valor": 1
          }
        ],
        "condicionsDesactivacio": [
          {
            "dato": "DuracioSeconds",
            "operand": "GE",
            "valor": 1500
          }
        ]
      }
    ]
  },
  {
    "id": "rele_lfi",
    "nom": "Bomba LFI",
    "enabled": true,
    "mqttType": "SWITCH",
    "mqttEnabled": true,
    "mqttConsumSensorEnabled": false,
    "ordre": 2,
    "gpioPin": 19,
    "gpioStateWhenReleOn": false,
    "idReleMaster": "rele_bomba"
  },
  {
    "id": "rele_fan",
    "nom": "Ventilador Quadre",
    "enabled": true,
    "mqttType": "SWITCH",
    "mqttEnabled": true,
    "mqttConsumSensorEnabled": false,
    "ordre": 3,
    "gpioPin": 6,
    "gpioStateWhenReleOn": false,
    "rules": [
      {
        "id": "FanTempCpu",
        "activateOnTrue": true,
        "segonsFinsProximaActivacio": 0,
        "condicionsActivacio": [
          {
            "dato": "temp_cpu_rpi",
            "operand": "GE",
            "valor": 48.0
          }
        ],
        "condicionsDesactivacio": [
          {
            "dato": "temp_cpu_rpi",
            "operand": "LE",
            "valor": 47.0
          }
        ]
      }
    ]
  },
  {
    "id": "rele_llums",
    "nom": "Llums Piscina",
    "enabled": true,
    "mqttType": "LIGHT",
    "mqttEnabled": true,
    "mqttConsumSensorEnabled": false,
    "ordre": 4,
    "gpioPin": 13,
    "gpioStateWhenReleOn": false,
    "idReleMaster": "rele_llums_jardi"
  },
  {
    "id": "rele_llums_jardi",
    "nom": "Llums Jardi",
    "enabled": false,
    "mqttType": "LIGHT",
    "mqttEnabled": true,
    "mqttConsumSensorEnabled": false,
    "ordre": 5,
    "gpioPin": 5,
    "gpioStateWhenReleOn": false
  },
  {
    "id": "rele_bomba_clor",
    "nom": "Bomba Clor",
    "enabled": true,
    "mqttType": "SWITCH",
    "mqttEnabled": true,
    "mqttConsumSensorEnabled": true,
    "ordre": 6,
    "gpioPin": 21,
    "gpioStateWhenReleOn": false,
    "unitatConsumHora": "ml",
    "consumHora": 4500.0,
    "rulesOn": true,
    "rules": [
      {
        "id": "AugmentClor",
        "activateOnTrue": true,
        "activarRelesSlaves": false,
        "segonsFinsProximaActivacio": 1200,
        "condicionsActivacio": [
          {
            "dato": "sonda_orp",
            "operand": "L",
            "valor": 660.0
          },
          {
            "dato": "SegonsPiPoolArrancat",
            "operand": "GE",
            "valor": "700"
          },
          {
            "dato": "SegonsReleParat",
            "paramsDato": [
              {
                "camp": "idRele",
                "valor": "rele_bomba_acid"
              }
            ],
            "operand": "GE",
            "valor": "30"
          },
          {
            "dato": "SegonsReleActivat",
            "paramsDato": [
              {
                "camp": "idRele",
                "valor": "rele_bomba"
              }
            ],
            "operand": "GE",
            "valor": "700"
          },
          {
            "dato": "SegonsLecturaSonda",
            "paramsDato": [
              {
                "camp": "idSonda",
                "valor": "sonda_orp"
              }
            ],
            "operand": "LE",
            "valor": "30"
          },
          {
            "dato": "HoraActual",
            "operand": "G",
            "valor": "8"
          },
          {
            "dato": "HoraActual",
            "operand": "L",
            "valor": "23"
          }
        ],
        "condicionsDesactivacio": [
          {
            "dato": "VolumMlInjectats",
            "operand": "GE",
            "valor": 200
          },
          {
            "dato": "VolumMlAcumulatDia",
            "operand": "GE",
            "valor": 600
          }
        ]
      },
      {
        "id": "AugmentClorDiari",
        "activateOnTrue": true,
        "activarRelesSlaves": false,
        "segonsFinsProximaActivacio": 3600,
        "condicionsActivacio": [
          {
            "dato": "SegonsPiPoolArrancat",
            "operand": "GE",
            "valor": "120"
          },
          {
            "dato": "SegonsReleParat",
            "paramsDato": [
              {
                "camp": "idRele",
                "valor": "rele_bomba_acid"
              }
            ],
            "operand": "GE",
            "valor": "30"
          },
          {
            "dato": "SegonsReleActivat",
            "paramsDato": [
              {
                "camp": "idRele",
                "valor": "rele_bomba"
              }
            ],
            "operand": "GE",
            "valor": "120"
          },
          {
            "dato": "HoraActual",
            "operand": "GE",
            "valor": "23"
          },
          {
            "dato": "HoraActual",
            "operand": "LE",
            "valor": "23"
          }
        ],
        "condicionsDesactivacio": [
          {
            "dato": "VolumMlInjectats",
            "operand": "GE",
            "valor": 400
          },
          {
            "dato": "VolumMlAcumulatDia",
            "operand": "GE",
            "valor": 2000
          }
        ]
      }
    ]
  },
  {
    "id": "rele_bomba_acid",
    "nom": "Bomba PH-Minus",
    "enabled": true,
    "mqttType": "SWITCH",
    "mqttEnabled": true,
    "mqttConsumSensorEnabled": true,
    "ordre": 7,
    "gpioPin": 20,
    "gpioStateWhenReleOn": false,
    "unitatConsumHora": "ml",
    "consumHora": 4000.0,
    "rulesOn": true,
    "rules": [
      {
        "id": "PHMinusDia",
        "activateOnTrue": true,
        "activarRelesSlaves": false,
        "segonsFinsProximaActivacio": 1200,
        "condicionsActivacio": [
          {
            "dato": "sonda_ph",
            "operand": "G",
            "valor": 7.45
          },
          {
            "dato": "SegonsReleParat",
            "paramsDato": [
              {
                "camp": "idRele",
                "valor": "rele_bomba_clor"
              }
            ],
            "operand": "GE",
            "valor": "20"
          },
          {
            "dato": "SegonsReleActivat",
            "paramsDato": [
              {
                "camp": "idRele",
                "valor": "rele_bomba"
              }
            ],
            "operand": "GE",
            "valor": "700"
          },
          {
            "dato": "SegonsLecturaSonda",
            "paramsDato": [
              {
                "camp": "idSonda",
                "valor": "sonda_ph"
              }
            ],
            "operand": "LE",
            "valor": "30"
          },
          {
            "dato": "HoraActual",
            "operand": "GE",
            "valor": "7"
          },
          {
            "dato": "HoraActual",
            "operand": "LE",
            "valor": "23"
          }
        ],
        "condicionsDesactivacio": [
          {
            "dato": "VolumMlInjectats",
            "operand": "GE",
            "valor": 100
          },
          {
            "dato": "VolumMlAcumulatDia",
            "operand": "GE",
            "valor": 900
          }
        ]
      }
    ]
  }
]
""";

	/**
	 * spring constructor
	 */
	@PostConstruct
	public void initClass() {
		
		log.info("Inicialitza RELE's.");

		try {
			loadJsonFile();
		} catch (IOException e) {
			log.error("Error al llegir Json de Reles.", e);
			initDefaultMap();
		}
		
		log.info("S'han trobat {} reles definits.", mapReles.size());
	}

	/**
	 * default map: intenta carregar del recurs /default-config/reles.json
	 * i si falla, construeix la configuració per defecte programàticament.
	 */
	public void initDefaultMap() {
		log.info("Carreguem reles per defecte.");

		try (InputStream is = getClass().getResourceAsStream("/default-config/reles.json")) {
			if (is != null) {
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<List<Rele>> mapType = new TypeReference<List<Rele>>() {};
				List<Rele> list = mapper.readValue(is, mapType);
				if (list != null && !list.isEmpty()) {
					synchronized (mapReles) {
						this.mapReles.clear();
						for (Rele rele : list) {
							mapReles.put(rele.getId(), rele);
						}
					}
					log.info("Reles per defecte carregats correctament des de /default-config/reles.json. Total: {}", mapReles.size());
					return;
				}
			}
		} catch (Exception e) {
			log.warn("No s'ha pogut carregar el recurs /default-config/reles.json: {}", e.getMessage());
		}

		initDefaultProgrammaticMap();
	}

	public void initDefaultProgrammaticMap() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<List<Rele>> mapType = new TypeReference<List<Rele>>() {};
			List<Rele> list = mapper.readValue(DEFAULT_RELES_JSON, mapType);
			if (list != null) {
				synchronized (mapReles) {
					this.mapReles.clear();
					for (Rele rele : list) {
						mapReles.put(rele.getId(), rele);
					}
				}
				log.info("Reles per defecte carregats correctament des de la configuració de fallback integrada. Total: {}", mapReles.size());
			}
		} catch (Exception e) {
			log.error("Error crític carregant reles per defecte des de configuració integrada.", e);
		}
	}
	
	public Map<String, Rele> getReles() {
		if(mapReles==null) {
			initClass();
		}
		return mapReles;
	}
	
	/**
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void loadJsonFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		
		//JSON file to List Java
		TypeReference<List<Rele>> mapType = new TypeReference<List<Rele>>() {};
    	List<Rele> jsonToList = mapper.readValue(new File(fileConfig), mapType);

    	if(jsonToList!=null) {
    		synchronized (mapReles) {
        		this.mapReles.clear();
        		
        		for(Rele rele : jsonToList) {
        			mapReles.put(rele.getId(), rele);
        		}
			}
    	}
    	
    	if (objJsonPrinter != null) {
    		log.info("JSON de Rele llegit i carregat OK: {}", objJsonPrinter.print(mapReles));
    	} else {
    		log.info("JSON de Rele llegit i carregat OK. Total: {}", mapReles.size());
    	}
	}	

	/**
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public synchronized void writeJsonFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.addMixIn(Rele.class, ReleConfigFileMixIn.class);
		mapper.addMixIn(RuleRele.class, RuleReleConfigFileMixIn.class);
		mapper.addMixIn(RuleCondicio.class, RuleCondicioConfigFileMixIn.class);
		
		File targetFile = new File(fileConfig);
		File parentDir = targetFile.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}
		
		File tempFile = new File(targetFile.getAbsolutePath() + ".tmp");
		
		synchronized (mapReles) {
			mapper.writeValue(tempFile, mapReles.values());
		}
		
		Path tempPath = tempFile.toPath();
		Path targetPath = targetFile.toPath();
		try {
			Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			log.warn("ATOMIC_MOVE no suportat ({}), utilitzant reemplaçament estàndard.", e.getMessage());
			Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}

		log.info("JSON de Rele grabat OK de forma atòmica a: {}", fileConfig);
	}

}
