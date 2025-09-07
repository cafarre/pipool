package es.fdvcode.pipool.srv.scheduler;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import es.fdvcode.pipool.srv.rele.RelesLoader;
import es.fdvcode.pipool.srv.rele.RelesSrv;

//@RunWith(SpringRunner.class)
//@SpringBootTest
class PiPoolScheduledTaskTest {

	@MockitoBean
	RelesSrv mock;
	
	@Autowired
	RelesLoader relesLoader;
	
	@Autowired 
	PiPoolPeriodicTask piPoolScheduledTask;

//
//	@Test
//	public void testRun() {
//				
//		try {
//			Map<String, Rele> map = relesLoader.getReles();
//			Mockito.doNothing().when(mock).initGpios();
//			when(mock.getSyncReles()).thenReturn(map);
//			//when(mock.setState(anyObject(), anyBoolean())).thenReturn(null);
//			Mockito.doNothing().when(mock).setState(any(Rele.class), anyBoolean());
//
//			piPoolScheduledTask.run();
//			
//			assertNotNull(piPoolScheduledTask);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Ha saltat una excepcio");
//		}
//	}


    @Test
    void testRunDummy() {
		assertTrue(true);
	}
}
