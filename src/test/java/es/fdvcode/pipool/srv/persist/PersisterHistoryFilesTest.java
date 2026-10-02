package es.fdvcode.pipool.srv.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.srv.rele.RelesPersister;

class PersisterHistoryFilesTest {

	@TempDir
	File tempFolder;

	private File relesDir;
	private TestRelesPersister persister;

	private static class TestRelesPersister extends RelesPersister {
		private final String customBasePath;

		public TestRelesPersister(String customBasePath) {
			this.customBasePath = customBasePath;
		}

		@Override
		protected String getBasePath() {
			return customBasePath;
		}
	}

	@BeforeEach
	void setUp() {
		relesDir = new File(tempFolder, "reles");
		relesDir.mkdirs();
		// customBasePath ha d'acabar amb barra per concordar amb "data/"
		String basePath = tempFolder.getAbsolutePath() + File.separator;
		persister = new TestRelesPersister(basePath);
	}

	private void createDatFile(String dateStr, String releId, double consumRele) throws IOException {
		File file = new File(relesDir, "reles_" + dateStr + ".dat");
		try (PrintWriter out = new PrintWriter(new FileWriter(file, false))) {
			out.println("IDRELE;TIMESTAMP;IS_ON;GPIOPINHIGH;MODE;CAUSA;DESACTRELEMASTER;ACITVTEMPORAL;ACTIVPROGRAMADA;DESCRIPCIO;ACTRELEMASTER;ACTIVRULE;CONSUMHORA;CONSUMRELE");
			out.println(releId + ";01/01/2026-12:00:00;false;true;AUTO;INIT;false;;;desc;false;;4000.0;" + consumRele + ";");
		}
	}

	@Test
	void testLoadHistoryWhenNoFilesInLast4DaysTakesLatest4ExistingFiles() throws IOException {
		// Simulem fitxers antics de fa mesos (cap fitxer en els últims dies)
		createDatFile("2026-05-01", "rele_bomba_clor", 100.0);
		createDatFile("2026-05-02", "rele_bomba_clor", 200.0);
		createDatFile("2026-05-03", "rele_bomba_clor", 300.0);
		createDatFile("2026-05-04", "rele_bomba_clor", 400.0);
		createDatFile("2026-05-05", "rele_bomba_clor", 500.0);
		createDatFile("2026-05-06", "rele_bomba_clor", 600.0);

		// Demanem 4 dies d'història
		List<PersistibleRele> history = persister.loadHistory("rele_bomba_clor", 4);

		// Ha d'haver agafat els 4 més recents: 2026-05-03, 2026-05-04, 2026-05-05, 2026-05-06
		assertEquals(4, history.size(), "Ha de carregar exactament els 4 fitxers més recents que existeixen");
		assertEquals(300.0, history.get(0).getConsumRele());
		assertEquals(400.0, history.get(1).getConsumRele());
		assertEquals(500.0, history.get(2).getConsumRele());
		assertEquals(600.0, history.get(3).getConsumRele());
	}

	@Test
	void testLoadHistoryWithFewerFilesThanRequested() throws IOException {
		// Només 2 fitxers existents
		createDatFile("2026-08-10", "rele_bomba_acid", 150.0);
		createDatFile("2026-08-12", "rele_bomba_acid", 250.0);

		List<PersistibleRele> history = persister.loadHistory("rele_bomba_acid", 4);

		assertEquals(2, history.size(), "Ha de carregar tots els fitxers disponibles si n'hi ha menys de 4");
		assertEquals(150.0, history.get(0).getConsumRele());
		assertEquals(250.0, history.get(1).getConsumRele());
	}

	@Test
	void testLoadHistoryEmptyDirectory() {
		List<PersistibleRele> history = persister.loadHistory("rele_bomba_clor", 4);
		assertTrue(history.isEmpty(), "Si no hi ha fitxers, la llista ha de ser buida sense error");
	}
}
