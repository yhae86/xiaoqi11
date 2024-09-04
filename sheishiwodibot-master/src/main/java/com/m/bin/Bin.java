package com.m.bin;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.m.Main;

public class Bin {

	 public static String getProjectPath() throws UnsupportedEncodingException {
		 URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();

		 String filePath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);

		 if (filePath.endsWith(".jar")) {
		 filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);

		 }

		 File file = new File(filePath);

		 filePath = file.getAbsolutePath();

		 return filePath;

		 }

		 public static String getRealPath() {
		 String realPath = Objects.requireNonNull(Main.class.getClassLoader().getResource("")).getFile();

		 File file = new File(realPath);

		 realPath = file.getAbsolutePath();

		 try {
		 realPath = URLDecoder.decode(realPath, StandardCharsets.UTF_8);

		 } catch (Exception e) {
		 e.printStackTrace();

		 }

		 return realPath;

		 }
	public static String stringSub(String string,int end){
	 	return null==string?null:string.length()>end?string.substring(0,end):string;
	}

}
