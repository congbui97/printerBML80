package bk.congbui.marutoucompackprint.BlueTooth;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;


import java.util.Hashtable;

public class BytesUtil {


	public static String getHexStringFromBytes(byte[] data) {
		if (data == null || data.length <= 0) {
			return null;
		}
		String hexString = "0123456789ABCDEF";
		int size = data.length * 2;
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < data.length; i++) {
			sb.append(hexString.charAt((data[i] & 0xF0) >> 4));
			sb.append(hexString.charAt((data[i] & 0x0F) >> 0));
		}
		return sb.toString();
	}

	private static byte RGB2Gray(int r, int g, int b) {
		return (false ? ((int) (0.29900 * r + 0.58700 * g + 0.11400 * b) > 200)
				: ((int) (0.29900 * r + 0.58700 * g + 0.11400 * b) < 200)) ? (byte) 1 : (byte) 0;
	}

	public static byte [] initARectangle(int w, int h, byte []textBytes){
		h = h+2;
		w = w+4;
		byte[] data = new byte[ 48 * h + 5];
		int k = 0;
		for (int j = 0; j < h; j++) {
			if (j == 0){
				for (int l = 0; l < (w); l++) {
					if (l==0){
						data[k++] = (byte) 152;
					}else if(l == w-1){
						data[k++] = (byte) 153;
					}else {
						data[k++] = (byte) 149;
					}
				}
			}else if (j==(h-1)){
				for (int l = 0; l < (w); l++) {
					if (l==0){
						data[k++] = (byte) 154;
					}else if(l == w-1){
						data[k++] = (byte) 155;
					}else {
						data[k++] = (byte) 149;
					}
				}
			}else {
				data[k++] = (byte)(150);
				for (int l = 0; l < (w-2); l++) {
					if (l == 0){
						data[k++] = (byte) 32;
					}else if (l <= textBytes.length){
						data[k++] = (byte) textBytes[l - 1];
					}else {
						data[k++] = (byte) 32;
					}
				}
				data[k++] = (byte)(150);
			}
			data[k++] = 0x0A;
		}
		data[k++] = 0x0A;
		return data;
	}




















}



