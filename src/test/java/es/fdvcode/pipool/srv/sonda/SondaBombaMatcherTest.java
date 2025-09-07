package es.fdvcode.pipool.srv.sonda;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import es.fdvcode.pipool.model.rele.PersistibleRele;
import es.fdvcode.pipool.model.sonda.PersistibleSonda;
import org.junit.jupiter.api.Test;

class SondaBombaMatcherTest {

    @Test
    void testBombaActiva_OK() {
		
		PersistibleSonda sonda = new PersistibleSonda("SondaORP;05/06/2018-08:12:28;678.0"); 
		List<PersistibleRele> histBomba = new ArrayList<>();		
		
		PersistibleRele rele = new PersistibleRele("ReleBomba;05/06/2018-08:00:01;true;false;AUTO;false;;8:0:0-->81:8;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-09:38:21;false;true;AUTO;false;;;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=false i EstatPin=true.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-11:00:01;true;false;AUTO;false;;11:0:0-->103:55;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		
		SondaBombaMatcher matcher = new SondaBombaMatcher(); 
		boolean result = matcher.bombaActiva(sonda, histBomba);
		
		assertTrue(result);
	}

    @Test
    void testBombaActiva_OK2() {
		
		PersistibleSonda sonda = new PersistibleSonda("SondaORP;05/06/2018-11:12:28;678.0"); 
		List<PersistibleRele> histBomba = new ArrayList<>();		
		
		PersistibleRele rele = new PersistibleRele("ReleBomba;05/06/2018-08:00:01;false;false;AUTO;false;;8:0:0-->81:8;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-09:38:21;true;true;AUTO;true;;;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=false i EstatPin=true.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-11:00:01;true;true;AUTO;true;;11:0:0-->103:55;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-11:30:01;false;false;AUTO;true;;11:0:0-->103:55;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-11:35:01;true;false;AUTO;true;;11:0:0-->103:55;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);

		
		SondaBombaMatcher matcher = new SondaBombaMatcher(); 
		boolean result = matcher.bombaActiva(sonda, histBomba);
		
		assertTrue(result);
	}

    @Test
    void testBombaActiva_NOK() {
		
		PersistibleSonda sonda = new PersistibleSonda("SondaORP;05/06/2018-09:40:28;678.0"); 
		List<PersistibleRele> histBomba = new ArrayList<>();		
		
		PersistibleRele rele = new PersistibleRele("ReleBomba;05/06/2018-08:00:01;false;false;AUTO;false;;8:0:0-->81:8;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-09:38:21;true;true;AUTO;true;;;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=false i EstatPin=true.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-11:00:01;true;true;AUTO;true;;11:0:0-->103:55;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-11:30:01;false;false;AUTO;true;;11:0:0-->103:55;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);
		rele = new PersistibleRele("ReleBomba;05/06/2018-11:35:01;true;false;AUTO;true;;11:0:0-->103:55;RELESRV SET STATE AUTO -> S'ha establert el Rele=ReleBomba amb Mode=AUTO, EstatRele=true i EstatPin=false.;false;;");
		histBomba.add(rele);

		
		SondaBombaMatcher matcher = new SondaBombaMatcher(); 
		boolean result = matcher.bombaActiva(sonda, histBomba);

        assertFalse(result);
	}
}
