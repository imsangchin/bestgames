package log.com;

import java.util.ArrayList;

public class LogManagement {
	// private static String phoneID=android.os.Build.ID;
	private static Process process;
	// private static String FileName = "log";

	private static LogManagement instance = new LogManagement();

	public static LogManagement getInstance() {
		return instance;
	}

	/**
	 * 
	*/
	public void Log(final String Filename) {
		Thread th = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				getLog(Filename);

			}
		});
		th.start();
	}

	/**
	 * 捕获日志
	 */
	private void getLog(String Filename) {
		System.out.println("--------func start--------");
		try {
			ArrayList<String> cmdLine = new ArrayList<String>();
			cmdLine.add("logcat");
			cmdLine.add("-f");
			cmdLine.add(Filename);

			process = Runtime.getRuntime().exec(
					cmdLine.toArray(new String[cmdLine.size()]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 停止将logcat日志文件输出到文件中
	 */
	public void StopLog() {
		try {
			process.destroy();
		} catch (Exception e) {

		}
	}
}