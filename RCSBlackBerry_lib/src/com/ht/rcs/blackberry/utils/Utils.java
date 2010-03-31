/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Utils.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.NumberUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {

    /** The debug. */
    private static Debug debug = new Debug("Utils", DebugLevel.VERBOSE);

    /**
     * Sleep.
     * 
     * @param millis
     *            the millis
     */
    public static void Sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            DbgTrace(e);
        }
    }

    /**
     * Dbg trace.
     * 
     * @param e
     *            the e
     */
    public static void DbgTrace(Exception e) {
        e.printStackTrace();
    }

    /**
     * Definizione delle funzioni helper comuni.
     * 
     * @param c
     *            the c
     * @return the int
     */
    public static int HEX(char c) {
        int ret = (char) ((c) <= '9' ? (c) - '0' : (c) <= 'F' ? (c) - 'A' + 0xA
                : (c) - 'a' + 0xA);
        return ret;
    }

    /**
     * ASCII.
     * 
     * @param c
     *            the c
     * @return the char
     */
    public static char ASCII(int c) {
        return (char) ((c) <= 9 ? (c) + '0' : (c) + 'A' - 0xA);
    }

    /**
     * Crc.
     * 
     * @param buffer
     *            the buffer
     * @return the int
     */
    public static int crc(byte[] buffer) {
        return crc(buffer, 0, buffer.length);
    }

    /**
     * Crc.
     * 
     * @param buffer
     *            the buffer
     * @return the int
     */
    public static int crc(char[] buffer) {
        return crc(CharArrayToByteArray(buffer), 0, buffer.length);
    }

    /**
     * Crc.
     * 
     * @param buffer
     *            the buffer
     * @param start
     *            the start
     * @param len
     *            the len
     * @return the int
     */
    public static int crc(byte[] buffer, int start, int len) {
        // CRC
        int conf_hash;
        long temp_hash = 0;

        for (int i = start; i < (len - start); i++) {
            temp_hash++;

            byte b = (byte) buffer[i];

            if (b != 0)
                temp_hash *= b;

            conf_hash = (int) (temp_hash >> 32);

            temp_hash = temp_hash & 0xFFFFFFFFL;
            temp_hash ^= conf_hash;
            temp_hash = temp_hash & 0xFFFFFFFFL;
        }

        conf_hash = (int) temp_hash;
        return conf_hash;
    }

    /**
     * Gets the index.
     * 
     * @param buffer
     *            the buffer
     * @param message
     *            the message
     * @return the index
     */
    public static int getIndex(char[] buffer, String message) {
        char[] token = new char[message.length()];

        message.getChars(0, message.length(), token, 0);

        int pos = -1;

        for (int i = 0; i < buffer.length; i++) {
            if (Arrays.equals(buffer, i, token, 0, token.length)) {
                pos = i;
                break;
            }
        }

        return pos;
    }

    /**
     * Gets the index.
     * 
     * @param buffer
     *            the buffer
     * @param token
     *            the token
     * @return the index
     */
    public static int getIndex(byte[] buffer, byte[] token) {
        int pos = -1;

        for (int i = 0; i < buffer.length; i++) {
            if (Arrays.equals(buffer, i, token, 0, token.length)) {
                pos = i;
                break;
            }
        }

        return pos;
    }

    /**
     * Char array to byte array.
     * 
     * @param message
     *            the message
     * @return the byte[]
     */
    public static byte[] CharArrayToByteArray(char[] message) {
        byte[] payload = new byte[message.length];

        for (int i = 0; i < message.length; i++) {
            payload[i] = (byte) (message[i] & 0xFF);
        }

        return payload;
    }

    /**
     * Byte array to char array.
     * 
     * @param message
     *            the message
     * @return the char[]
     */
    public static char[] ByteArrayToCharArray(byte[] message) {
        char[] payload = new char[message.length];

        for (int i = 0; i < message.length; i++) {
            payload[i] = (char) (message[i] & 0xFF);
        }

        return payload;
    }

    /**
     * Copy.
     * 
     * @param dest
     *            the dest
     * @param src
     *            the src
     * @param len
     *            the len
     */
    public static void Copy(byte[] dest, byte[] src, int len) {
        Copy(dest, 0, src, 0, len);
    }

    /**
     * Copy.
     * 
     * @param dest
     *            the dest
     * @param offsetDest
     *            the offset dest
     * @param src
     *            the src
     * @param offsetSrc
     *            the offset src
     * @param len
     *            the len
     */
    public static void Copy(byte[] dest, int offsetDest, byte[] src,
            int offsetSrc, int len) {
        Check.requires(dest.length >= offsetDest + len, "wrong dest len");
        Check.requires(src.length >= offsetSrc + len, "wrong src len");

        for (int i = 0; i < len; i++) {
            dest[i + offsetDest] = src[i + offsetSrc];
        }
    }

    /**
     * Int to byte array.
     * 
     * @param value
     *            the value
     * @return the byte[]
     */
    public static final byte[] intToByteArray(int value) {
        byte[] result = new byte[4];
        DataBuffer databuffer = new DataBuffer(result, 0, 4, false);
        databuffer.writeInt(value);
        return result;
    }

	public static byte[] longToByteArray(long value) {
		byte[] result = new byte[4];
        DataBuffer databuffer = new DataBuffer(result, 0, 4, false);
        databuffer.writeLong(value);
        return result;
	}
	
    /**
     * Byte array to int.
     * 
     * @param buffer
     *            the buffer
     * @param offset
     *            the offset
     * @return the int
     */
    public static final int byteArrayToInt(byte[] buffer, int offset) {

        Check.requires(buffer.length >= offset + 4, "short buffer");

        DataBuffer databuffer = new DataBuffer(buffer, offset, 4, false);
        int value = 0;

        try {
            value = databuffer.readInt();
        } catch (EOFException e) {
            debug.error("Cannot read int from buffer at offset:" + offset);
        }

        return value;

    }

    /**
     * Hex string to byte array.
     * 
     * @param wchar
     *            the wchar
     * @return the byte[]
     */
    public static byte[] HexStringToByteArray(String wchar) {
        Check.requires(wchar.length() % 2 == 0, "Odd inputt");
        byte[] ret = new byte[wchar.length() / 2];

        for (int i = 0; i < ret.length; i++) {
            char first = wchar.charAt(i * 2);
            char second = wchar.charAt(i * 2 + 1);

            int value = NumberUtilities.hexDigitToInt(first) << 4;
            value += NumberUtilities.hexDigitToInt(second);

            Check.asserts(value >= 0 && value < 256,
                    "HexStringToByteArray: wrong value");

            ret[i] = (byte) value;
        }

        return ret;
    }

    /**
     * Int to char array.
     * 
     * @param value
     *            the value
     * @return the char[]
     */
    public static char[] intToCharArray(int value) {
        return ByteArrayToCharArray(intToByteArray(value));
    }


}