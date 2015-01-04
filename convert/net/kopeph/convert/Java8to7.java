package net.kopeph.convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/********************** WARNING **********************
 * Application specific tool.                        *
 * Many assumptions were made. Use at your own risk. *
 * See README.md for details.                        *
 *                                                   *
 * @author alexg                                     *
 *****************************************************/
public class Java8to7 {
	private static final String LAMBDA_COMMENT = "//$LAMBDA:";
	private static final String SPI_PKG = "net.kopeph.ld31.spi";
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final File IN_DIR = new File("src");
	private static final File OUT_DIR = new File("src-7");
	private static final String EXT = ".java";

	public static void main(String[] args) throws IOException {
		processDirectory(IN_DIR, OUT_DIR);
	}

	private static void processDirectory(File inDir, File outDir) throws IOException {
		if (!outDir.exists())
			outDir.mkdir();

		for (String filename : inDir.list()) {
			File inFile = new File(inDir, filename);
			File outFile = new File(outDir, filename);
			if (inFile.isDirectory())
				processDirectory(inFile, outFile);
			else if (filename.endsWith(EXT))
				processFile(inFile, outFile);
			else
				copyFile(inFile, outFile);
		}
	}

	private static void processFile(File inFile, File outFile) throws IOException {
		String src = readFile(inFile), interfaceSrc;
		int endlPos, lambdaPos = 0, commentPos, lambdaEndPos, lineBeginPos;
		int iStartPos, iEndPos, packagePos;
		int curlyCount;
		String className, classNameLoc, methodSignature, lambdaContents, lambdaExpansion;

		while ((lambdaPos = indexOf(src, lambdaPos, " ->", ")->")) != -1) {
			//Check to make sure the operator isn't in a string
			lineBeginPos = src.lastIndexOf('\n', lambdaPos);
			if (src.substring(lineBeginPos, lambdaPos + 1).split("\"").length % 2 == 0) {
				lambdaPos++;
				continue;
			}

			//Locate the comment telling which interface to use
			commentPos = src.lastIndexOf(LAMBDA_COMMENT, lambdaPos);
			if (commentPos == -1)
				throw new RuntimeException(
						"Required " + LAMBDA_COMMENT + " not found " + inFile(inFile, src, lambdaPos));

			//Cut out the interface name and open it
			endlPos = src.indexOf('\n', commentPos);
			className = src.substring(commentPos + LAMBDA_COMMENT.length(), endlPos);
			classNameLoc = (SPI_PKG + "." + className).replace('.', '/') + EXT;
			interfaceSrc = readFile(new File(IN_DIR, classNameLoc));

			//cut out method signature from the interface source
			iStartPos = interfaceSrc.lastIndexOf("public "); //XXX: cheap way to find the method
			iEndPos = interfaceSrc.lastIndexOf(';');
			methodSignature = interfaceSrc.substring(iStartPos, iEndPos).trim();

			//Cut out the lambda contents
			lambdaEndPos = lambdaPos + 8; //XXX: magic number
			curlyCount = 1;
			while (curlyCount > 0) {
				if (src.charAt(lambdaEndPos) == '{') curlyCount++;
				if (src.charAt(lambdaEndPos) == '}') curlyCount--;
				lambdaEndPos++;
			}
			lambdaContents = src.substring(lambdaPos + 4, lambdaEndPos);
			lambdaPos = src.lastIndexOf('(', lambdaPos);

			//Assemble
			lambdaExpansion = String.format("new %s() { @Override %s %s }",
					className, methodSignature, lambdaContents);

			//Replace with expansion
			src = replace(src, lambdaPos, lambdaEndPos, lambdaExpansion);
			src = replace(src, commentPos, endlPos, "");
		}
		//Remove the @FunctionalInterface annotation, becuase it doesn't exist pre-java 8
		src = src.replaceAll("@FunctionalInterface", "");

		//Add in import, just in case
		packagePos = src.indexOf(';') + 2;
		src = replace(src, packagePos, packagePos, "import " + SPI_PKG + ".*;");

		//Output changes to output dir
		writeFile(outFile, src);
	}

	private static String readFile(File file) throws IOException {
		StringBuilder out = new StringBuilder();
		String curLine;

		//this is why we can't have nice things
		try (BufferedReader r = new BufferedReader(
				                new InputStreamReader(
				                new FileInputStream(file), CHARSET))) {
			while ((curLine = r.readLine()) != null)
				out.append(curLine).append('\n');
		}

		return out.toString();
	}

	private static void writeFile(File file, String src) throws IOException {
		//this is why we can't have nice things
		try (BufferedWriter r = new BufferedWriter(
				                new OutputStreamWriter(
				                new FileOutputStream(file), CHARSET))) {
			r.write(src);
		}
	}

	/** Pulled From SO http://stackoverflow.com/a/115086/1204134 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}

	private static String inFile(File file, String src, int pos) {
		return String.format("in file: %s:%d",
				file.getPath(),
				src.substring(0, pos).split("\n").length);
	}

	private static int indexOf(String haystack, int stPos, String...needles) {
		int pos = haystack.length() + 1;
		for (String needle : needles) {
			int newPos = haystack.indexOf(needle, stPos);
			if (newPos != -1 && newPos < pos)
				pos = newPos;
		}
		return pos == haystack.length() + 1 ? -1 : pos;
	}

	private static String replace(String in, int startIndex, int endIndex, String with) {
		String before = in.substring(0, startIndex);
		String after = in.substring(endIndex);

		return before + with + after;
	}
}
