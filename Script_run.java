package test.record;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.junit.Test;

import test.record.getPredic;
import test.record.tomcat;
import test.record.train_to_model;

import com.memtest.Global;
import com.memtest.core.ContainerType;
import com.memtest.core.TomcatContainer;
import com.memtest.test.JmxWebContainerConnector;
import com.memtest.test.MemoryState;
import com.memtest.test.TestRecord;
import com.memtest.test.predictor.BloatPredictor;

public class Script_run {
	// boolean flag = true;
	@Test
	public void testDataCollection() throws Exception {

		startTomcat();
		String fm = new File("test\\data\\WorkSpace\\Project1\\VQWiki")
		.getAbsolutePath();
	    String[] scriptpath = {fm+"\\attach_files.py"};
		List<TestRecord> records = new ArrayList<TestRecord>();
		train_to_model tts = new train_to_model();
		JmxWebContainerConnector jmx = new JmxWebContainerConnector();
		MonitorThread monitor = new MonitorThread(jmx);
		
		new Thread(monitor).start();
		Date start = new Date();
		//将脚本运行15次获得usedmemory
		for (int i = 0; i <1000 ; i++) {
			
			tts.run_script(scriptpath[0]);
		}
			Date end = new Date();
			MemoryState state = jmx.getMemoryState();
			System.out.println(state.getUsedMemory());
			List<MemoryState> ms = new ArrayList<MemoryState>();
			ms.add(state);
			TestRecord testrecord = new TestRecord(TestRecord.createID(),ms, start, end);
			records.add(testrecord);
//			getPredic pre = new getPredic();
//			pre.getPre(records);
		
		

			tomcat.shutTomcat();
		
		

	}

	public class MonitorThread implements Runnable {
		volatile boolean monitorStop;
		private JmxWebContainerConnector jmx;

		public MonitorThread(JmxWebContainerConnector jmx) {
			this.jmx = jmx;
		}

		@Override
		public void run() {
			try {
				jmx.startProfiling(false);

				while (!monitorStop) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (InstanceNotFoundException | MBeanException
					| ReflectionException | IOException
					| MalformedObjectNameException e1) {
				System.err.println("Error: " + e1.getMessage());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void startTomcat() {
		try {
			Global.v().init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String hostURL = "http://localhost:8080";
		String location = Global.v().getOptions().getDefaultContainerLocation();
		TomcatContainer tomcat = new TomcatContainer(ContainerType.Default,
				hostURL, new File(location));

		tomcat.startContainer();
	}
}
