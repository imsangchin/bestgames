package log.com;

import java.util.ArrayList;

public class LogManagement {

	private static Process process;

	private static LogManagement instance = new LogManagement();

	public static LogManagement getInstance() {
		return instance;
	}

	/**
	 * ������־
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
	 * ֹͣ��logcat��־�ļ�������ļ���
	 */
	public void StopLog() {
		try {
			process.destroy();
		} catch (Exception e) {

		}
	}
}