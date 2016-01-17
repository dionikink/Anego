/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package org.g29.anego2.data.models;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MeshObject {
    
    public enum BUFFER_TYPE {
        BUFFER_TYPE_VERTEX, BUFFER_TYPE_TEXTURE_COORD, BUFFER_TYPE_NORMALS, BUFFER_TYPE_INDICES
    }

    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;

    private int indicesNumber = 0;
    private int verticesNumber = 0;

    public void setVertices(double[] vertices) {
        mVertBuff = fillBuffer(vertices);
        verticesNumber = vertices.length / 3;
    }

    public void setTexCoords(double[] texCoords) {
        mTexCoordBuff = fillBuffer(texCoords);
    }

    public void setNormals(double[] normals) {
        mNormBuff = fillBuffer(normals);
    }

    public void setIndices(short[] indices) {
        mIndBuff = fillBuffer(indices);
        indicesNumber = indices.length;
    }
    
    public Buffer getVertices() {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_VERTEX);
    }
    
    
    public Buffer getTexCoords() {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_TEXTURE_COORD);
    }
    
    
    public Buffer getNormals() {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_NORMALS);
    }
    
    
    public Buffer getIndices() {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_INDICES);
    }

    protected Buffer fillBuffer(double[] array) {
        // Convert to floats because OpenGL doesn't work on doubles, and manually
        // casting each input value would take too much time.
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (double d : array)
            bb.putFloat((float) d);
        bb.rewind();
        
        return bb;
        
    }

    protected Buffer fillBuffer(float[] array) {
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();
        
        return bb;
        
    }
    
    protected Buffer fillBuffer(short[] array) {
        // Each short takes 2 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(2 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : array)
            bb.putShort(s);
        bb.rewind();
        
        return bb;
        
    }

    public int getNumObjectIndex()
    {
        return indicesNumber;
    }

    public int getNumObjectVertex()
    {
        return verticesNumber;
    }

    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
            default:
                break;

        }

        return result;
    }

}
