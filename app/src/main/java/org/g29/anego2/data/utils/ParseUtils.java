package org.g29.anego2.data.utils;

import android.util.Log;


import org.g29.anego2.data.models.MeshObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Dion on 12-1-2016.
 */
public class ParseUtils {

    public static final String TAG = "ParseObject";

    private static final int VERTICES_LINE = 30;
    private static final int TEX_COORDS_LINE = 34;
    private static final int NORMALS_LINE = 38;
    private static final int INDICES_LINE = 42;

    public static MeshObject ParseObject(MeshObject object, InputStream in) {
        Log.d("DataObjects", "Starting data parse");
        MeshObject result = getObjectDataFromFile(object, in);

        return result;
    }

    public static MeshObject getObjectDataFromFile(MeshObject object, InputStream in) {
        String verticesLine = readSingleLineFromFile(in, VERTICES_LINE);
        String texCoordsLine = readSingleLineFromFile(in, TEX_COORDS_LINE);
        String normalsLine = readSingleLineFromFile(in, NORMALS_LINE);
        String indicesLine = readSingleLineFromFile(in, INDICES_LINE);

        object.setVertices(parseDoubleArrayFromHeader(verticesLine));
        object.setTexCoords(parseDoubleArrayFromHeader(texCoordsLine));
        object.setNormals(parseDoubleArrayFromHeader(normalsLine));
        object.setIndices(parseShortArrayFromHeader(indicesLine));

        return object;
    }

    public static double[] parseDoubleArrayFromHeader(String lineFromHeader) {
        lineFromHeader = lineFromHeader.replaceAll(",$", "");
        lineFromHeader = lineFromHeader.trim();
        String[] splitLine = lineFromHeader.split("\\s*,\\s*");

        double[] doubles = new double[splitLine.length];
        for(int i = 0; i < doubles.length; i++) {
            doubles[i] = Double.parseDouble(splitLine[i]);
        }

        return doubles;
    }

    public static short[] parseShortArrayFromHeader(String lineFromHeader) {
        lineFromHeader = lineFromHeader.replaceAll(",$", "");
        lineFromHeader = lineFromHeader.trim();
        String[] splitLine = lineFromHeader.split("\\s*,\\s*");

        short[] shorts = new short[splitLine.length];
        for(int i = 0; i < shorts.length; i++) {
            shorts[i] = Short.parseShort(splitLine[i]);
        }

        return shorts;
    }

    // Reads a line from a file
    public static String readSingleLineFromFile(InputStream in, int lineNumber) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = "";

        try {
            for(int i = 0; i < lineNumber - 1; ++i) {
                br.readLine();
            }

            line = br.readLine();
        } catch (IOException e) {
            Log.e("LineParseError", "Could not read line");
        }

        try {
            in.reset();
        } catch(IOException e) {
            Log.e("LineParser", "Could not reset InputStream");
        }
        return line;
    }

}
