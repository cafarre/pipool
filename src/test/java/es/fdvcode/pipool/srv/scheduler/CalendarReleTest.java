package es.fdvcode.pipool.srv.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import es.fdvcode.pipool.model.rele.CalendarRele;
import es.fdvcode.pipool.model.rele.FranjaHoraria;

class CalendarReleTest {

    @Test
    void testIsActiu_A_IniciAnyAnterior() {
		
		//Init obj calendar
		CalendarRele cal = new CalendarRele();
		cal.setDiaIni(1);
		cal.setMesIni(12);
		cal.setDiaFin(30);
		cal.setMesFin(3);
		cal.setId("Test");
		
		List<FranjaHoraria> listFrangesHoraries = new ArrayList<>();
		FranjaHoraria franja = new FranjaHoraria(10,0,0,3600);
		listFrangesHoraries.add(franja);
		cal.setListFrangesHoraries(listFrangesHoraries);

		//mock data actual
		LocalDateTime dateNow = LocalDateTime.of(2018, 1, 1, 8, 0);
		CalendarRele calMock = Mockito.spy(cal);
	    Mockito.when(calMock.getDataActual()).thenReturn(dateNow);		

		Boolean b = calMock.isActiu();
		
		assertTrue(b);
	}

    @Test
    void testIsActiu_B_IniciAnyActual() {
		
		//Init obj calendar
		CalendarRele cal = new CalendarRele();
		cal.setDiaIni(1);
		cal.setMesIni(2);
		cal.setDiaFin(30);
		cal.setMesFin(3);
		cal.setId("Test");
		
		List<FranjaHoraria> listFrangesHoraries = new ArrayList<>();
		FranjaHoraria franja = new FranjaHoraria(10,0,0,3600);
		listFrangesHoraries.add(franja);
		cal.setListFrangesHoraries(listFrangesHoraries);

		//mock data actual
		LocalDateTime dateNow = LocalDateTime.of(2018, 2, 10, 8, 0);
		CalendarRele calMock = Mockito.spy(cal);
	    Mockito.when(calMock.getDataActual()).thenReturn(dateNow);		

		Boolean b = calMock.isActiu();
		
		assertTrue(b);
	}

    @Test
    void testIsActiu_C_FiAnyPosterior() {
		
		//Init obj calendar
		CalendarRele cal = new CalendarRele();
		cal.setDiaIni(1);
		cal.setMesIni(12);
		cal.setDiaFin(30);
		cal.setMesFin(3);
		cal.setId("Test");
		
		List<FranjaHoraria> listFrangesHoraries = new ArrayList<>();
		FranjaHoraria franja = new FranjaHoraria(10,0,0,3600);
		listFrangesHoraries.add(franja);
		cal.setListFrangesHoraries(listFrangesHoraries);

		//mock data actual
		LocalDateTime dateNow = LocalDateTime.of(2018, 12, 10, 8, 0);
		CalendarRele calMock = Mockito.spy(cal);
	    Mockito.when(calMock.getDataActual()).thenReturn(dateNow);		

		Boolean b = calMock.isActiu();
		
		assertTrue(b);
	}

//	@Test
//	public void testProximaFranjaIniciAnyAnterior() {
//		
//		//mock data actual
//		LocalDateTime dateNow = LocalDateTime.of(2018, 1, 1, 8, 0); 
//		when(mock.getDataActual()).thenReturn(dateNow);
//		
//		//Init obj calendar
//		CalendarRele cal = new CalendarRele();
//		cal.setDiaIni(1);
//		cal.setMesIni(12);
//		cal.setDiaFin(30);
//		cal.setMesFin(3);
//		cal.setId("Test");
//		
//		List<FranjaHoraria> listFrangesHoraries = new ArrayList<>();
//		FranjaHoraria franja = new FranjaHoraria(10,0,0,3600);
//		listFrangesHoraries.add(franja);
//		cal.setListFrangesHoraries(listFrangesHoraries);
//		
//		ProximaFranja prox = cal.getProximaFranjaActiva();
//		
//		assertTrue(true);
//	}
}
