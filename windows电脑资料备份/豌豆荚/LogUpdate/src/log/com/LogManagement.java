package log.com;

import java.util.ArrayList;

public class LogManagement {

	private static Process process;

	private static LogManagement instance = new LogManagement();

	public static LogManagement getInstance() {
		return instance;
	}

	/**
	 * 捕获日志
	 */
	public void getLog(String Filename) {
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