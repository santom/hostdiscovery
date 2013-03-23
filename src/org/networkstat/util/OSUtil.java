package org.networkstat.util;

public class OSUtil {
	
	public enum OS {
		LINUX,
		WINDOWS,
		MAC
	}
	
	private static String os = System.getProperty("os.name").toLowerCase();
	
	public static OS getOS() {
	   if(isWindows()) return OS.WINDOWS;
	   if(isMac()) return OS.MAC;
	   if(isUnix()) return OS.LINUX;
	   return null;
	}
	
	public static boolean isWindows() {
		 
		return (os.indexOf("win") >= 0);
 
	}
 
	public static boolean isMac() {
 
		return (os.indexOf("mac") >= 0);
 
	}
 
	public static boolean isUnix() {
 
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 );
 
	}
 
	public static boolean isSolaris() {
 
		return (os.indexOf("sunos") >= 0);
 
	}

}
