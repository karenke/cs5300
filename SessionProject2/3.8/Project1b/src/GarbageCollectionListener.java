import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Timestamp;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class GarbageCollectionListener implements ServletContextListener {
	private ExecutorService execService = Executors.newFixedThreadPool(1);
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		execService.shutdown();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		execService.submit(new GarbageCollector());
	}
	
	class GarbageCollector implements Runnable {
		@Override
		public void run() {
			System.out.println("garbage collector daemon called!");
			// TODO Auto-generated method stub
			while (true) {
				Timestamp time = new Timestamp(System.currentTimeMillis());
			//	System.out.println("Heartbeat " + time.toString());
				if (SessionServlet.global != null) {
					Iterator<String> itr = SessionServlet.global.keySet().iterator();
					while(itr.hasNext()){
						String cur = itr.next();
						if (SessionServlet.global.get(cur).getExpiration().compareTo(time) <= 0) {
							SessionServlet.global.remove(cur);
							System.out.println("Session " + cur + " is removed!!");
							
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
