package cn.njjy.group.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class IOHelper {
	private static final int BUFFER_SIZE = 8 * 1024;
	
	public static String toString(InputStream input) throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw);
		return sw.toString();
	}

	public static void copyFileToDirectory(File src, File dir)
			throws IOException {
		if (dir == null) {
			throw new NullPointerException("destination must not be null");
		}
		if (dir.exists() && dir.isDirectory() == false) {
			throw new IllegalArgumentException("destination is not directory");
		}
		copy(src, new File(dir, src.getName()));
	}
	
	public static void copy(InputStream input, Writer output) throws IOException {
		InputStreamReader in = new InputStreamReader(input);
		copy(in, output);
	}
	
	public static int copy(Reader input, Writer output) throws IOException {
		long count = 0;
		char[] buffer = new char[BUFFER_SIZE];
		int n = 0;
		while(-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		if(count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	public static void copy(File src, File dest) throws IOException {
		if (src == null) {
			throw new NullPointerException("source must not be null");
		}
		if (dest == null) {
			throw new NullPointerException("distination must not be null");
		}
		if (src.isDirectory()) {
			throw new IOException("source is a directory");
		}
		if (src.getCanonicalPath().equals(dest.getCanonicalPath())) {
			throw new IOException("source and destination are the same");
		}
		if (dest.getParentFile() != null
				&& dest.getParentFile().exists() == false) {
			if (dest.getParentFile().mkdirs() == false) {
				throw new IOException("destination directory cannot be created");
			}
		}
		if (dest.exists() && dest.canWrite() == false) {
			throw new IOException("destination exists but is read-only");
		}
		doCopy(src, dest);
	}

	public static void doCopy(File src, File dest) throws IOException {
		if (dest.exists() && dest.isDirectory()) {
			throw new IOException("destination is a derectory");
		}
		FileInputStream input = new FileInputStream(src);
		try {
			FileOutputStream output = new FileOutputStream(dest);
			try {
				copy(input, output);
			} finally {
				closeQuietly(output);
			}
		} finally {
			closeQuietly(input);
		}
		if (src.length() != dest.length()) {
			throw new IOException(
					"Failed to copy full content from src to destination");
		}
	}

	public static int copy(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return (int) count;
	}

	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File" + file + " is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File" + file + " cannot be read");
			}
		} else {
			throw new FileNotFoundException("File" + file + " does not exist");
		}
		return new FileInputStream(file);
	}

	public static FileOutputStream openOutputStream(File file)
			throws IOException {
		if (file == null){
			throw new IOException("File " + file + " is a Null");
		}else if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File " + file + " is a directory");
			}
			if (file.canWrite() == false) {
				throw new IOException("File " + file + " cannot be writen");
			}
		} else {
			File parent = file.getParentFile();
			if (parent != null && parent.exists() == false) {
				if (parent.mkdirs() == false) {
					throw new IOException("File " + file
							+ " could not be created");
				}
			}
		}
		return new FileOutputStream(file);
	}

	public static void closeQuietly(OutputStream output) {
		try {
			if (output != null) {
				output.close();
			}
		} catch (IOException e) {

		}
	}

	public static void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException e) {

		}
	}
}
