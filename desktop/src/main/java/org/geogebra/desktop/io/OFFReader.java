package org.geogebra.desktop.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.geogebra.common.io.OFFHandler;
import org.geogebra.common.main.App;
import org.geogebra.common.util.opencsv.CSVException;
import org.geogebra.common.util.opencsv.CSVParser;

/**
 * Read OFF (Object File Format) file. Off file begins with "OFF" indicating it
 * is an off file, followed by one or more comments. Each comment line start
 * with '#'. Thereafter three integers in a single line representing
 * vertixCount(V), faceCount(F) and edgeCount(F) respectively. Since V + F - E =
 * 2, E can be ignored safely or it can be used for verification purpose. The
 * file is followed by V lines, each contains three doubles representing
 * coordinates x, y, and z respectively. Finally the file is followed by F
 * lines, every line represents for a face. Each line start with an integer N,
 * number of vertices in the face followed by N integers n1, n2, n3, ..., nN,
 * each integer represents an index of a vertex(0 based indexing)
 */
public class OFFReader {





	/**
	 * 
	 * @param stream
	 *            file input stream
	 * @throws IOException
	 *             input output exception
	 */
	public OFFReader() {


	}

	private void parse(BufferedReader in, OFFHandler handler)
			throws IOException, CSVException {

		String[] aux;
		String line = in.readLine();

		if (line != null) {
			CSVParser parser = new CSVParser(' ');
			// Skip comments and headers
			while (OFFHandler.isCommentOrOffHeader(line))
				line = in.readLine();
			aux = parser.parseLine(line);
			handler.setCounts(Integer.parseInt(aux[0]),
					Integer.parseInt(aux[1]), Integer.parseInt(aux[2]));


			// read all vertices
			while (handler.getVertices().size() < handler.getVertexCount()) {
				line = in.readLine();
				if (line == null) {
					return;
				}
				handler.addVertexLine(line);

			}

			while (handler.getFaces().size() < handler.getFaceCount()) {
				line = in.readLine();
				if (line == null) {
					return;
				}
				handler.addFaceLine(in.readLine());
			}
		}
	}



	private void exc(int k, OFFHandler handler) {
		throw new RuntimeException("Invalid vertex index " + k
				+ "(expected an integer between 0(inclusive) and "
				+ handler.getVertexCount() + "(exclusive))");
	}


	/**
	 * 
	 * @param file
	 *            off file
	 */
	public void parse(File file, OFFHandler handler) {
		try {
			InputStream stream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(stream));
			parse(br, handler);
			handler.updateAfterParsing();
			App.debug(String.format("Off file has ben load:(v=%d;e=%d;f=%d)",
					handler.getVertexCount(), handler.getEdgeCount(),
					handler.getEdgeCount()));
		} catch (FileNotFoundException e) {
			// It is unexpected as we already have checked existence GUI
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
 catch (CSVException e) {
			e.printStackTrace();
		}
	}

}