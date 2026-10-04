package es.fdvcode.pipool.srv.rele;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;

import es.fdvcode.pipool.model.rele.CalendarRele;
import es.fdvcode.pipool.model.rele.FranjaHoraria;
import es.fdvcode.pipool.model.rele.Rele;
import es.fdvcode.pipool.model.rele.RuleRele;
import es.fdvcode.pipool.model.sonda.Sonda;
import es.fdvcode.pipool.mqtt.homeassistant.PipoolEntitiesMqttSrv;
import es.fdvcode.pipool.restsrv.v1.dto.ReleConfigDto;
import es.fdvcode.pipool.restsrv.v1.dto.SondaConfigDto;
import es.fdvcode.pipool.srv.ItemNotFoundException;
import es.fdvcode.pipool.srv.sonda.SondesLoader;
import es.fdvcode.pipool.srv.sonda.SondesQuerySrv;
import es.fdvcode.pipool.srv.sonda.SondesSrv;
import es.fdvcode.pipool.srv.sonda.atlasi2c.impl.FactorySondaAtlas;

class ConfigUpdateAndAtomicWriteTest {

	@TempDir
	File tempFolder;

	@Test
	void testRelesLoaderAtomicWrite() throws Exception {
		File configFile = new File(tempFolder, "reles_test.json");
		RelesLoader loader = new RelesLoader();
		
		Field fileConfigField = RelesLoader.class.getDeclaredField("fileConfig");
		fileConfigField.setAccessible(true);
		fileConfigField.set(loader, configFile.getAbsolutePath());

		loader.initDefaultMap();
		loader.writeJsonFile();

		assertTrue(configFile.exists(), "El fitxer reles_test.json ha d'existir");
		File tempFile = new File(configFile.getAbsolutePath() + ".tmp");
		assertFalse(tempFile.exists(), "El fitxer temporal .tmp ha d'haver estat mogut de forma atòmica");
		assertTrue(configFile.length() > 0, "El fitxer no ha d'estar buit");
	}

	@Test
	void testSondesLoaderAtomicWrite() throws Exception {
		File configFile = new File(tempFolder, "sondes_test.json");
		SondesLoader loader = new SondesLoader();

		Field fileConfigField = SondesLoader.class.getDeclaredField("fileConfig");
		fileConfigField.setAccessible(true);
		fileConfigField.set(loader, configFile.getAbsolutePath());

		loader.initDefaultMap();
		loader.writeJsonFile();

		assertTrue(configFile.exists(), "El fitxer sondes_test.json ha d'existir");
		File tempFile = new File(configFile.getAbsolutePath() + ".tmp");
		assertFalse(tempFile.exists(), "El fitxer temporal .tmp ha d'haver estat mogut de forma atòmica");
		assertTrue(configFile.length() > 0, "El fitxer no ha d'estar buit");
	}

	@Test
	void testRelesSrvUpdateCalendarsAndConfig() throws Exception {
		File configFile = new File(tempFolder, "reles_srv_test.json");
		RelesLoader loader = new RelesLoader();
		Field fileConfigField = RelesLoader.class.getDeclaredField("fileConfig");
		fileConfigField.setAccessible(true);
		fileConfigField.set(loader, configFile.getAbsolutePath());
		loader.initDefaultMap();

		RelesQuerySrv querySrv = new RelesQuerySrv(loader, null, null);
		RelesSrv srv = new RelesSrv(querySrv, loader, null, null, null, null);

		// 1. Prova dades invàlides
		assertThrows(IllegalArgumentException.class, () -> srv.updateCalendars("rele_bomba", null));

		List<CalendarRele> invalidCals = new ArrayList<>();
		CalendarRele badCal = new CalendarRele();
		badCal.setMesIni(15); // mes invàlid
		invalidCals.add(badCal);
		assertThrows(IllegalArgumentException.class, () -> srv.updateCalendars("rele_bomba", invalidCals));

		// 2. Prova actualització correcta de calendaris
		List<CalendarRele> validCals = new ArrayList<>();
		CalendarRele cal = new CalendarRele();
		cal.setId("TestCal");
		cal.setNom("Calendari Prova");
		cal.setDiaIni(1);
		cal.setMesIni(6);
		cal.setDiaFin(30);
		cal.setMesFin(9);
		List<FranjaHoraria> franges = new ArrayList<>();
		franges.add(new FranjaHoraria(10, 30, 0, 3600));
		cal.setListFrangesHoraries(franges);
		validCals.add(cal);

		Rele releUpdated = srv.updateCalendars("rele_bomba", validCals);
		assertNotNull(releUpdated);
		assertEquals(1, releUpdated.getCalendars().size());
		assertEquals("TestCal", releUpdated.getCalendars().get(0).getId());

		// 3. Prova actualització de configuració
		ReleConfigDto dto = new ReleConfigDto();
		dto.setNom("Bomba Actualitzada");
		dto.setConsumHora(1250.0);
		dto.setSecondsDuradaCicles(7200);

		Rele releConfigUpdated = srv.updateConfig("rele_bomba", dto);
		assertEquals("Bomba Actualitzada", releConfigUpdated.getNom());
		assertEquals(1250.0, releConfigUpdated.getConsumHora());
		assertEquals(7200, releConfigUpdated.getSecondsDuradaCicles());

		// 4. Prova actualització de regles
		List<RuleRele> rules = new ArrayList<>();
		RuleRele rule = new RuleRele();
		rules.add(rule);
		Rele releRulesUpdated = srv.updateRules("rele_bomba", rules);
		assertEquals(1, releRulesUpdated.getRules().size());
	}

	@Test
	void testSondesSrvUpdateConfig() throws Exception {
		File configFile = new File(tempFolder, "sondes_srv_test.json");
		SondesLoader loader = new SondesLoader();
		Field fileConfigField = SondesLoader.class.getDeclaredField("fileConfig");
		fileConfigField.setAccessible(true);
		fileConfigField.set(loader, configFile.getAbsolutePath());
		loader.initDefaultMap();

		SondesQuerySrv querySrv = new SondesQuerySrv(null, null, loader, null, null, null);
		SondesSrv srv = new SondesSrv(null, null, querySrv, loader, null);

		// Prova rang invàlid min > max
		SondaConfigDto badDto = new SondaConfigDto();
		badDto.setMinValor(10.0);
		badDto.setMaxValor(5.0);
		assertThrows(IllegalArgumentException.class, () -> srv.updateConfig("sonda_ph", badDto));

		// Prova actualització correcta
		SondaConfigDto goodDto = new SondaConfigDto();
		goodDto.setNom("Sonda PH Piscina");
		goodDto.setMinValor(6.8);
		goodDto.setMaxValor(7.8);
		goodDto.setIdReleCorrector("rele_acid");

		Sonda sonda = srv.updateConfig("sonda_ph", goodDto);
		assertEquals("Sonda PH Piscina", sonda.getNom());
		assertEquals(6.8, sonda.getMinValor());
		assertEquals(7.8, sonda.getMaxValor());
		assertEquals("rele_acid", sonda.getIdReleCorrector());
	}

	@Test
	void testDisabledReleCannotActivate() throws Exception {
		File configFile = new File(tempFolder, "reles_disabled_test.json");
		RelesLoader loader = new RelesLoader();
		Field fileConfigField = RelesLoader.class.getDeclaredField("fileConfig");
		fileConfigField.setAccessible(true);
		fileConfigField.set(loader, configFile.getAbsolutePath());
		loader.initDefaultMap();

		RelesQuerySrv querySrv = new RelesQuerySrv(loader, null, null);
		RelesSrv srv = new RelesSrv(querySrv, loader, null, null, null, null);

		Rele rele = querySrv.getRele("rele_bomba");
		assertTrue(rele.isEnabled(), "Per defecte el relé ha d'estar habilitat");

		// Deshabilitem el relé
		srv.disableRele("rele_bomba");
		assertFalse(rele.isEnabled(), "El relé ha d'estar deshabilitat");
		assertFalse(rele.getCopyStateRele().isOn(), "El relé deshabilitat ha d'estar en OFF");

		// 1. Intent d'activació manual ha de fallar
		assertThrows(IllegalStateException.class, () -> srv.setStateManual("rele_bomba", true));

		// 2. Intent d'activació temporal ha de fallar
		FranjaHoraria fr = new FranjaHoraria(10, 0, 0, 1800);
		assertThrows(IllegalStateException.class, () -> srv.setOnTemporal(rele, fr));

		// 3. Activació per HA ha de ser rebutjada i romandre OFF
		srv.setStateHA(rele.getCopyStateRele(), true);
		assertFalse(rele.getCopyStateRele().isOn());

		// 4. Activació per Master ha de ser rebutjada
		Rele master = new Rele();
		srv.setOnMaster(rele, master);
		assertFalse(rele.getCopyStateRele().isOn());

		// 5. Verifiquem que teActivacionsActives retorna false
		assertFalse(rele.getCopyStateRele().teActivacionsActives());

		// 6. Habilitem novament el relé
		srv.enableRele("rele_bomba");
		assertTrue(rele.isEnabled(), "El relé ha de tornar a estar habilitat");
	}

	@Test
	void testNoRuntimeStatePersistedInJson() throws Exception {
		File configFile = new File(tempFolder, "reles_clean_persistence_test.json");
		RelesLoader loader = new RelesLoader();
		Field fileConfigField = RelesLoader.class.getDeclaredField("fileConfig");
		fileConfigField.setAccessible(true);
		fileConfigField.set(loader, configFile.getAbsolutePath());
		loader.initDefaultMap();

		Rele releBomba = loader.getReles().get("rele_bomba");
		assertNotNull(releBomba);
		// Assignem valors en calent / runtime
		releBomba.setUltimResultatEvalCondicions(new es.fdvcode.pipool.model.rele.ResultatEvalCondicions(false, "TEST_KO"));
		if (releBomba.getRules() != null && !releBomba.getRules().isEmpty()) {
			RuleRele rule = releBomba.getRules().get(0);
			rule.activar();
			if (rule.getCondicionsActivacio() != null && !rule.getCondicionsActivacio().isEmpty()) {
				rule.getCondicionsActivacio().get(0).setValorDatoCondicio("999");
				rule.getCondicionsActivacio().get(0).setCumpleCondicio(true);
			}
		}

		// Persistim a fitxer
		loader.writeJsonFile();

		String jsonContent = Files.readString(configFile.toPath());

		// Comprovem que cap propietat en calent no s'ha escrit al fitxer JSON
		assertFalse(jsonContent.contains("ultimResultatEvalCondicions"), "No s'ha de persistir ultimResultatEvalCondicions");
		assertFalse(jsonContent.contains("\"stateRele\""), "No s'ha de persistir stateRele");
		assertFalse(jsonContent.contains("secondsActivatAvui"), "No s'ha de persistir secondsActivatAvui");
		assertFalse(jsonContent.contains("consumUltimaActivacio"), "No s'ha de persistir consumUltimaActivacio");
		assertFalse(jsonContent.contains("consumAvui"), "No s'ha de persistir consumAvui");
		assertFalse(jsonContent.contains("consumTotalRele"), "No s'ha de persistir consumTotalRele");
		assertFalse(jsonContent.contains("consumAcumulatHistoric"), "No s'ha de persistir consumAcumulatHistoric");
		assertFalse(jsonContent.contains("consumPendentConsolidar"), "No s'ha de persistir consumPendentConsolidar");
		assertFalse(jsonContent.contains("\"activada\""), "No s'ha de persistir activada de RuleRele");
		assertFalse(jsonContent.contains("valorDatoCondicio"), "No s'ha de persistir valorDatoCondicio");
		assertFalse(jsonContent.contains("cumpleCondicio"), "No s'ha de persistir cumpleCondicio");

		// Comprovem que es pot llegir de nou sense cap problema
		RelesLoader newLoader = new RelesLoader();
		fileConfigField.set(newLoader, configFile.getAbsolutePath());
		newLoader.loadJsonFile();
		assertEquals(7, newLoader.getReles().size(), "S'han de carregar els 7 relés correctament");
	}

	@Test
	void testInitDefaultMapMatchesFullConfiguration() throws Exception {
		RelesLoader relesLoader = new RelesLoader();
		relesLoader.initDefaultMap();
		assertEquals(7, relesLoader.getReles().size(), "S'han de carregar els 7 relés per defecte");
		assertTrue(relesLoader.getReles().containsKey("rele_bomba"));
		assertTrue(relesLoader.getReles().containsKey("rele_lfi"));
		assertTrue(relesLoader.getReles().containsKey("rele_fan"));
		assertTrue(relesLoader.getReles().containsKey("rele_llums"));
		assertTrue(relesLoader.getReles().containsKey("rele_llums_jardi"));
		assertTrue(relesLoader.getReles().containsKey("rele_bomba_clor"));
		assertTrue(relesLoader.getReles().containsKey("rele_bomba_acid"));

		Rele bombaClor = relesLoader.getReles().get("rele_bomba_clor");
		assertNotNull(bombaClor.getRules());
		assertFalse(bombaClor.getRules().isEmpty(), "rele_bomba_clor ha de tenir regles configurades");

		SondesLoader sondesLoader = new SondesLoader();
		sondesLoader.initDefaultMap();
		assertEquals(5, sondesLoader.getSondes().size(), "S'han de carregar les 5 sondes per defecte");
		assertEquals("rele_bomba_clor", sondesLoader.getSondes().get("sonda_orp").getIdReleCorrector());
		assertEquals("rele_bomba_acid", sondesLoader.getSondes().get("sonda_ph").getIdReleCorrector());
		assertEquals("rele_fan", sondesLoader.getSondes().get("temp_cpu_rpi").getIdReleCorrector());
	}

	@Test
	void testResultatEvalCondicionsDeserializeSafe() throws Exception {
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		String json = "{\"resultatOK\":false,\"motiu\":\"TEST_MOTIU\",\"ruleActivada\":true,\"condicionsActivacio\":[]}";
		es.fdvcode.pipool.model.rele.ResultatEvalCondicions res = mapper.readValue(json, es.fdvcode.pipool.model.rele.ResultatEvalCondicions.class);
		assertNotNull(res);
		assertFalse(res.isResultatOK());
		assertEquals("TEST_MOTIU", res.getMotiu());
		assertTrue(res.isRuleActivada());
		assertNotNull(res.getCondicionsActivacio());
	}

	@Test
	void testStateReleJsonIgnoreActivadorReleMaster() throws Exception {
		RelesLoader loader = new RelesLoader();
		loader.initDefaultMap();
		Rele rele = loader.getReles().get("rele_bomba");
		es.fdvcode.pipool.model.rele.StateRele state = rele.getCopyStateRele();
		RuleRele rule = rele.getRules().get(0);
		state.setActivadorReleMaster(rule);

		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		String json = mapper.writeValueAsString(state);
		assertFalse(json.contains("activadorReleMaster"), "StateRele serialized JSON must NOT contain activadorReleMaster to prevent Jackson issues in clients");
	}

}
