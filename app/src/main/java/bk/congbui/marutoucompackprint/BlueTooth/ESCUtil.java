package bk.congbui.marutoucompackprint.BlueTooth;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ESCUtil {
	public static BitSet dots;
	public static final byte ESC = 0x1B;// Escape
	public static final byte FS =  0x1C;// Text delimiter
	public static final byte GS =  0x1D;// Group separator
	public static final byte DLE = 0x10;// data link escape
	public static final byte EOT = 0x04;// End of transmission
	public static final byte ENQ = 0x05;// Enquiry character
	public static final byte SP =  0x20;// Spaces
	public static final byte HT =  0x09;// Horizontal list
	public static final byte LF =  0x0A;//Print and wrap (horizontal orientation)
	public static final byte CR =  0x0D;// Home key
	public static final byte FF =  0x0C;// Carriage control (print and return to the standard mode (in page mode))
	public static final byte CAN = 0x18;// Canceled (cancel print data in page mode)

	public static final byte[] ESC_FONT_COLOR_DEFAULT = new byte[] { 0x1B, 'r',0x00 };
	public static final byte[] FS_FONT_ALIGN = new byte[] { 0x1C, 0x21, 1, 0x1B, 0x21, 1 };
	public static final byte[] ESC_ALIGN_LEFT = new byte[] { 0x1b, 'a', 0x00 };
	public static final byte[] ESC_ALIGN_RIGHT = new byte[] { 0x1b, 'a', 0x02 };
	public static final byte[] ESC_ALIGN_CENTER = new byte[] { 0x1b, 'a', 0x01 };
	public static final byte[] ESC_CANCEL_BOLD = new byte[] { 0x1B, 0x45, 0 };


	public static final byte[] cc  = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
	public static final byte[] bb  = new byte[]{0x1B,0x21,0x06};  // 1- only bold text
	public static final byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
	public static final byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
	public static final byte[] bb4 = new byte[]{0x1B,0x21,(byte) 60};

	public static final byte[] ESC_BOLD = new byte[]{0x1C,0x57,0x01};
	public static final byte[] ESC_NORMAL = new byte[]{0x1C,0x57,0x00};

	public static byte[] SET_LINE_SPACING_24 = {0x1B, 0x33, 24};
	public static byte[] SET_LINE_SPACING_30 = {0x1B, 0x33, 30};


	public static int lineTable = 32;

	public static int isManyLine;


    public static byte[] nextLine(int lineNum) {
        byte[] result = new byte[lineNum];
        for (int i = 0; i < lineNum; i++) {
            result[i] = LF;
        }
        return result;
    }

	public static byte[] underlineOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 0;
		return result;
	}


	public static byte[] setCodeSystem(byte charset){
		byte[] result = new byte[3];
		result[0] = FS;
		result[1] = 0x43;
		result[2] = charset;
		return result;
	}

	/**
	 * 矩形印字時の高さの取得
	 *
	 * @param t_startheight [in] int    印字開始高さ
	 * @param t_endheight   [in] int    印字終了高さ
	 * @return int 終了-開始
	 */
	public static int getRectangleHeight(int t_startheight, int t_endheight) {
		return t_endheight - t_startheight;
	}

	public static byte[] createRectangle(int x, int y, int width, int height, byte weight)   {
		return commandSetLineBox(
				getLowLevel(x),
				getHighLevel(x),
				getLowLevel(y),
				getHighLevel(y),
				getLowLevel(width),
				getHighLevel(width),
				getLowLevel(height),
				getHighLevel(height),
				(byte) 0x01,
				weight);
	}
	/**
	 * LowLevel側の数値を取得する<br />
	 * 引数で与えられた数値を255で除算し、その余りを返す
	 *
	 * @param wkNum [in] int    数値
	 * @return byte 255で除算した余り
	 */
	public static byte getLowLevel(int wkNum) {
		int wkLowLevel = 0;
		if (wkNum > 0) {
			wkLowLevel = wkNum % 255;
		}
		return (byte)wkLowLevel;
	}

	/**
	 * HighLevel側の数値を取得する<br />
	 * 引数で与えられた数値を255で除算した結果を返す
	 *
	 * @param wkNum [in] int    数値
	 * @return byte 255で除算した結果
	 */
	public static byte getHighLevel(int wkNum) {
		int wkHighLevel = 0;
		if (wkNum > 0) {
			wkHighLevel = wkNum / 255;
		}
		return (byte)wkHighLevel;

	}

	/**
	 * ページモードにおけるライン＆ボックスの印字<br />
	 * ページモードにおいて線＆四角形を印字する。<br />
	 * 横（水平方向）の位置 ： wkXlow + wkXhigh * 256（dots）<br />
	 * 縦（垂直方向）の位置 ： wkYlow + wkYhigh * 256（dots）<br />
	 * 横（水平方向）の長さ : wkDxlow + wkDxhigh * 256(dots)<br />
	 * 縦（垂直方向）の長さ : wkDylow + wkDyhigh * 256(dots)<br />
	 * 印字モード： 0: 箱内を白色、1: 箱内を黒色、 2: 箱内を反転 線の太さ ： wkBold（ドット単位）<br />
	 *
	 * @param wkXlow
	 * @param wkXhigh
	 * @param wkYlow
	 * @param wkYhigh
	 * @param wkDxlow
	 * @param wkDxhigh
	 * @param wkDylow
	 * @param
	 * @param wkMode
	 * @param wkBold
	 * @return
	 */
	public static byte[] commandSetLineBox(byte wkXlow, byte wkXhigh, byte wkYlow, byte wkYhigh, byte wkDxlow, byte wkDxhigh, byte wkDylow,
										   byte wkDyhigh, byte wkMode, byte wkBold) {

		byte[] wkByte = new byte[] {
				0x1D, 0x57, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		};
		if (0 <= wkXlow || wkXlow <= 255) {
			wkByte[2] = wkXlow;
		}
		if (0 <= wkXhigh || wkXhigh <= 255) {
			wkByte[3] = wkXhigh;
		}
		if (0 <= wkYlow || wkYlow <= 255) {
			wkByte[4] = wkYlow;
		}
		if (0 <= wkYhigh || wkYhigh <= 255) {
			wkByte[5] = wkYhigh;
		}
		if (0 <= wkDxlow || wkDxlow <= 255) {
			wkByte[6] = wkDxlow;
		}
		if (0 <= wkDxhigh || wkDxhigh <= 255) {
			wkByte[7] = wkDxhigh;
		}
		if (0 <= wkDylow || wkDylow <= 255) {
			wkByte[8] = wkDylow;
		}
		if (0 <= wkDyhigh || wkDyhigh <= 255) {
			wkByte[9] = wkDyhigh;
		}
		if (0 <= wkMode || wkMode <= 255) {
			wkByte[10] = wkMode;
		}
		if (0 <= wkBold || wkBold <= 255) {
			wkByte[11] = wkBold;
		}

		return wkByte;
	}

	public static byte [] initARectangle(int first, int width, String[] textList,int manyLine) throws UnsupportedEncodingException {
		byte duongke;
		if (manyLine > 1){
			duongke = 0x18;
		}else {
			duongke = 0x08;
		}
		byte macdinh = 0x06;
		int w = width;
		int lenght = 48;
		byte[] data = new byte[100 * 3 ];
		int k = 0;
			if (textList[0].equals("first") == true){
				for (int l = 0; l < w; l++) {
					if (l==0){
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) duongke;
						data[k++] = (byte) 152;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else if(l == w - 1){
						data[k++] = (byte) 153;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else {
						if (l == lineTable){
							if (textList.length == 2){
								if (textList[1].equals("table")){
									data[k++] = (byte) 145;
									data[k++] = (byte) 0;
									data[k++] = (byte) 0;
								}
							}else {
								data[k++] = (byte) 149;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}
						}else {
							data[k++] = (byte) 149;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
						}

					}
				}
			}else if (textList[0].equals("last") == true){
				if (textList[1].equals("space")){
					for (int l = 0; l < (w); l++) {
						if (l==0){
							data[k++] = (byte) 0x1B;
							data[k++] = (byte) 0x21;
							data[k++] = (byte) duongke;
							data[k++] = (byte) 147;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
						}else if(l == w - 1){
							data[k++] = (byte) 146;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
						}else {
							if (l == lineTable){
								if (textList.length == 3){
									if (textList[2].equals("table")){
										data[k++] = (byte) 143;
										data[k++] = (byte) 0;
										data[k++] = (byte) 0;
									}
								}else {
									data[k++] = (byte) 149;
									data[k++] = (byte) 0;
									data[k++] = (byte) 0;
								}
							}else {
								data[k++] = (byte) 149;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}
						}
					}
				}else {
					for (int l = 0; l < (w); l++) {
						if(textList.length == 3 && textList[2].equals("table")){
							if (l==0){
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) duongke;
								data[k++] = (byte) 147;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}else if(l == w - 1){
								data[k++] = (byte) 146;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}else {
								if (l == lineTable){
									if (textList.length == 3){
										if (textList[2].equals("table")){
											data[k++] = (byte) 144;
											data[k++] = (byte) 0;
											data[k++] = (byte) 0;
										}
									}else {
										data[k++] = (byte) 149;
										data[k++] = (byte) 0;
										data[k++] = (byte) 0;
									}
								}else {
									data[k++] = (byte) 149;
									data[k++] = (byte) 0;
									data[k++] = (byte) 0;
								}
							}
						}else {
							if (l==0){
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) duongke;
								data[k++] = (byte) 154;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}else if(l == w - 1){
								data[k++] = (byte) 155;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}else {
								data[k++] = (byte) 149;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}
						}


					}
				}
			}else {
				if (textList.length == 2){
					byte[] textBytes1 = textList[0].getBytes("SJIS");
					if (first == 1){
						for (int i = 0; i < (w-2- textBytes1.length) / 2 ; i++) {
							textList[0] = " "+ textList[0];
						}
					}else if (first == 0){

					}
					textBytes1 = textList[0].getBytes("SJIS");
					w = lenght;
					for (int l = 0; l < w-2; l++) {
						if (l == 0){
							data[k++] = (byte) 0x1B;
							data[k++] = (byte) 0x21;
							data[k++] = (byte) duongke;
							data[k++] = (byte)(150);
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
							data[k++] = (byte) 32;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0x1B;
							data[k++] = (byte) 0x21;
							data[k++] = (byte) 0x06;
						}else if (l <= textBytes1.length){
							data[k++] =  textBytes1[l - 1];
						}else {
							if (l == lineTable-1){
									if (textList[1].equals("table")){
										data[k++] = (byte) 0x1B;
										data[k++] = (byte) 0x21;
										data[k++] = (byte) duongke;
										data[k++] = (byte) 150;
										data[k++] = (byte) 0;
										data[k++] = (byte) 0;
										data[k++] = (byte) 0x1B;
										data[k++] = (byte) 0x21;
										data[k++] = (byte) 0x06;
									}else {
										data[k++] = (byte) 32;
										data[k++] = (byte) 0;
										data[k++] = (byte) 0;
									}
							}else {
								data[k++] = (byte) 32;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}
						}
					}
					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) duongke;
					data[k++] = (byte)(150);
					data[k++] = (byte) 0;
					data[k++] = (byte) 0;
					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) 0x06;

				}else if (textList.length == 4){
					byte[] textBytes1 = textList[0].getBytes("SJIS");
					byte[] textBytes2 = textList[1].getBytes("SJIS");
					String[] textListChar1 = textList[0].split("");
					String[] textListChar2 = textList[1].split("");
					int lenght1 = 0;
					int lenght2 = 0;
					// set charactor in a line
					if (textList[2].equals("1")){
						for (int i = 0; i < textListChar1.length; i++) {

							if (textListChar1[i].getBytes().length == 3){
								lenght1 = lenght1 + 3;
							}else if (textListChar1[i].getBytes().length == 1){
								lenght1 = lenght1 + 1;
							}
						}
						for (int i = 0; i < textListChar2.length; i++) {

							if (textListChar2[i].getBytes().length == 3){
								lenght2 = lenght2 + 3;
							}else if (textListChar2[i].getBytes().length == 1){
								lenght2 = lenght2 + 1;
							}
						}
						w = 48  - lenght1 - lenght2 - 5 - textListChar1.length - textListChar2.length;
						macdinh = 0x30;
						duongke = 0x18;
					}else {
						for (int i = 0; i < textListChar1.length; i++) {
							if (textListChar1[i].getBytes().length == 3){
								lenght1 = lenght1 + 1;
							}
						}

						for (int i = 0; i < textListChar2.length; i++) {
							if (textListChar2[i].getBytes().length == 3){
								lenght2 = lenght2 + 1;
							}
						}
						w = 48  - lenght1 - lenght2 - 5 - textListChar1.length - textListChar2.length;
						macdinh = 0x06;
					}

					int firstSpace1 = 0;
					int firstSpace2 = 0;

					if (first == 28){
						firstSpace2 = 17 - textListChar2.length - lenght2;
						firstSpace1 = w - firstSpace2;
					}else if (first == 34 ){
						firstSpace1 = w;
						firstSpace2 = 0;
					} else {
						firstSpace1 = w;
						firstSpace2 = 0;
					}
					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) duongke;
					data[k++] = (byte)(150);
					data[k++] = (byte) 0;
					data[k++] = (byte) 0;
					data[k++] = (byte) 32;
					data[k++] = (byte) 0;
					data[k++] = (byte) 0;
					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) macdinh;
					data[k++] = 0x1C;
					data[k++] = 0x43;
					data[k++] = (byte) 1;

					for (int i = 0; i < textBytes1.length; i++) {
						data[k++] = textBytes1[i];
					}
					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) 0x06;

					for (int l = 0; l < firstSpace1; l++) {
						if (l == lineTable - lenght1 - textListChar1.length - 2){
							if (textList[3].equals("table")){
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) duongke;
								data[k++] = (byte) 150;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) 0x06;
							}else {
								data[k++] = (byte) 32;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}

						}else {
							data[k++] = (byte) 32;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
						}
					}

					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) macdinh;

					for (int i = 0; i < textBytes2.length; i++) {
						data[k++] = textBytes2[i];
					}

					for (int l = 0; l < firstSpace2; l++) {
						if (l == lineTable - lenght1 - textListChar1.length - 2){
							if (textList[3].equals("table")){
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) duongke;
								data[k++] = (byte) 150;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) 0x06;
							}else {
								data[k++] = (byte) 32;
								data[k++] = (byte) 0;
								data[k++] = (byte) 0;
							}

						}else {
							data[k++] = (byte) 32;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
						}
					}
					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) macdinh;

					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) duongke;
					data[k++] = (byte) 32;
					data[k++] = (byte) 0;
					data[k++] = (byte) 0;
					data[k++] = (byte) 32;
					data[k++] = (byte) 0;
					data[k++] = (byte) 0;
					data[k++] = (byte)(150);
					data[k++] = (byte) 0;
					data[k++] = (byte) 0;
					data[k++] = (byte) 0x1B;
					data[k++] = (byte) 0x21;
					data[k++] = (byte) macdinh;
				}
			}
			//data[k++] = 0x0A;
		return data;
	}


	public static byte [] initRyoShu(String[] text){
		String[] tienNhan ;
		String tienNhan1 = "領収金額";
		String chuki = "領　収　印";
		String giaTien = text[0].substring(11);

		BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_24);
		byte[] data = new byte[50 * 3 * 9 ];
		int k = 0;
		data[k++] = LF;
		data[k++] = LF;

		//    "ryoshu/spf/6円 , 50 , 10 , 0 , 100 , 400 , 173 , 70 , 180"

		int firstText = Integer.parseInt(text[4]) * 48 / 573 ;
		int firstRec = Integer.parseInt(text[5]) * 48 / 573 ;
		int line = Integer.parseInt(text[7]) /10 ;

		data[k++] = (byte) 0x1B;
		data[k++] = (byte) 0x21;
		data[k++] = (byte) 0x06;
		data[k++] = 0x1C;
		data[k++] = 0x43;
		data[k++] = (byte) 1;

		for (int i = 0; i < line; i++) {
			for (int j = 0; j < firstRec; j++) {
				if (i == 3 || i == 4 ){

					if (i==3){
						tienNhan = tienNhan1.split("");
					}else {
						tienNhan = giaTien.split("");
					}

					int space = 0;
					for (int l = 0; l < tienNhan.length; l++) {
						if (tienNhan[l].getBytes().length == 3){
							space = space + 4;
						}else if (tienNhan[l].getBytes().length == 1){
							space = space +2;
						}
					}

					if (j < firstText || j >= firstText + space){
						data[k++] = (byte) 32;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else {
						try {
							if (j - firstText < tienNhan.length){
								data[k++] = (byte) 0x1C;
								data[k++] = (byte) 0x57;
								data[k++] = (byte) 0x01;
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) 0x30;
								data[k++] = tienNhan[j - firstText].getBytes("SJIS")[0];
								data[k++] = tienNhan[j - firstText].getBytes("SJIS")[1];
								data[k++] = (byte) 0x1B;
								data[k++] = (byte) 0x21;
								data[k++] = (byte) 0x06;
								data[k++] = (byte) 0x1C;
								data[k++] = (byte) 0x57;
								data[k++] = (byte) 0x00;
							}
							//data[k++] = tienNhan[j - firstText].getBytes("SJIS")[1];
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}else {
					data[k++] = (byte) 32;
					data[k++] = (byte) 0;
					data[k++] = (byte) 0;
				}
			}
			// first = 33
			for (int j = firstRec; j < 48; j++) {
				if (i == 0){
					if (j==firstRec){
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x08;
						data[k++] = (byte) 152;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else if(j == 48 -  1){
						data[k++] = (byte) 153;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else {
						data[k++] = (byte) 149;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}

				} else if (i == 1){
					byte[] textBytes1 = new byte[0];
					int khoangtrang = 0;
					try {
						textBytes1 = chuki.getBytes("SJIS");
						khoangtrang = (48 - firstRec - textBytes1.length)/2;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					if (j == firstRec){
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x08;
						data[k++] = (byte)(150);
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;

						data[k++] = 0x1C;
						data[k++] = 0x43;
						data[k++] = (byte) 1;
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x06;
						for (int l = 0; l < khoangtrang -1; l++) {
							data[k++] = (byte) 32;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
						}
						data[k++] = (byte) 0x1C;
						data[k++] = (byte) 0x57;
						data[k++] = (byte) 0x00;
						for (int l = 0; l < textBytes1.length; l++) {
							data[k++] =  textBytes1[l];
						}
						for (int l = 0; l < khoangtrang; l++) {
							data[k++] = (byte) 32;
							data[k++] = (byte) 0;
							data[k++] = (byte) 0;
						}
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x08;
						data[k++] = (byte)(150);
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}

				} else if (i == 2){
					if (j == firstRec){
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x08;
						data[k++] = (byte) 147;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else if(j == 48 -  1){
						data[k++] = (byte) 146;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else {
						data[k++] = (byte) 149;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}
				} else if (i == line - 1){
					if (j == firstRec){
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x08;
						data[k++] = (byte) 154;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else if(j == 48 -  1){
						data[k++] = (byte) 155;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}else {
						data[k++] = (byte) 149;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}

				} else {
					if (j == firstRec){
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x50;
						data[k++] = (byte) 150;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x06;

					}else if(j == 48){
						continue;
					}else if(j == 48 -  1){
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x50;
						data[k++] = (byte) 150;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0x1B;
						data[k++] = (byte) 0x21;
						data[k++] = (byte) 0x06;

					}else {
						data[k++] = (byte) 32;
						data[k++] = (byte) 0;
						data[k++] = (byte) 0;
					}
				}
			}
		}

		String end = " 上記の道り領収致しました。有難うございました。";
		try {
			byte[] bytesEnd = end.getBytes("SJIS");
			for (int i = 0; i < bytesEnd.length; i++) {
				data[k++] = bytesEnd[i];
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

		}
		data[k++] = LF;
		data[k++] = LF;
		data[k++] = LF;
		data[k++] = LF;
		return  data;
	}

	public static byte[] printTable(String table) {
		byte[] data = new byte[48*5];
		String[] lineTable = table.split("/tbnl/");
		int first = Integer.parseInt(lineTable[0].substring(10));
		String [] tableLine;
		for (int i = 0; i < lineTable.length; i++) {
			tableLine = lineTable[i].split("/t/");

			if (tableLine[0].contains("table/spf/")){
				isManyLine = 0;
				tableLine = new String[]{"first","table"};
			}else if (tableLine[0].equals("0")){
				isManyLine = 0;
				tableLine = new String[]{"last","space","table"};
			}else if (tableLine[0].equals("rectangle12")){
				isManyLine = 0;
				tableLine = new String[]{"last","notspace","table"};
			}else if (tableLine[0].equals("rectangle0")){
				tableLine = new String[] {"last","notspace"};
			} else if (tableLine.length == 5){
				isManyLine++;
				tableLine = new String[]{tableLine[0],"table"};
			}else if (tableLine.length  > 5 ){
				String [] beforeTableLine = lineTable[i-1].split("/t/");
				if (beforeTableLine[0].equals("rectangle12")){
					isManyLine++;
					tableLine = new String[]{tableLine[0],tableLine[1].trim(),"0","nottable"};
				}else {
					isManyLine++;
					tableLine = new String[]{tableLine[0],tableLine[1].trim(),"0","table"};
				}
			}
			try {
				BluetoothUtil.sendData(initARectangle(0,48,tableLine,isManyLine));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.d("aaa",e.getMessage());
			}
		}
		return data;
	}

	public static int getCharactorOfString(String text,int size){
		int charactor = 0;
		String [] textList = text.split("");
		if (size == 0){
			for (int i = 0; i < textList.length; i++) {
				if (textList[i].getBytes().length == 3){
					charactor = charactor +1;
				}
			}
		}else if (size == 1){
			for (int i = 0; i < textList.length; i++) {
				if (textList[i].getBytes().length == 3){
					charactor = charactor + 3;
				}else if (textList[i].getBytes().length == 1){
					charactor = charactor + 1;
				}
			}
		}
		return charactor;
	}

	public static void print_image(String nameFile) throws IOException {
//		String  file = Environment.getExternalStoragePublicDirectory(
//				Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + nameFile;
		String  file = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "Heart.jpg";

		String abc = "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";

		byte[] decodedString = Base64.decode(abc, Base64.DEFAULT);
		Bitmap bmp1 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//		Bitmap bmp1 = BitmapFactory.decodeFile(file);
//		File fl = new File(file);
//		fl.delete();

		Bitmap bmp  = Bitmap.createScaledBitmap(bmp1, 584 ,bmp1.getHeight(),false);
		convertBitmap(bmp);
		BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_24);
		int offset = 0;
		int lenght = 0;
		List<Byte> bytes = new ArrayList<>();
		offset = 0;

		while (offset < bmp.getHeight()) {
			bytes.add((byte) 0x1B);
			bytes.add((byte) 0x2A);
			bytes.add((byte) 33);
			bytes.add((byte) 255);
			bytes.add((byte) 3);
			for (int x = 0; x < (1024) ; ++ x) {
				// Remember, 24 dots = 24 bits = 3 bytes.
				// The 'k' variable keeps track of which of those
				// three bytes that we're currently scribbling into.
				for (int k = 0; k < 3; ++ k) {
					byte slice = 0;
					// v < 10
					// A byte is 8 bits. The 'b' variable keeps track
					// of which bit in the byte we're recording.
					for (int b = 0; b < 8; ++b) {
						// Calculate the y position that we're currently
						// trying to draw. We take our offset, divide it
						// by 8 so we're talking about the y offset in
						// terms of bytes, add our current 'k' byte
						// offset to that, multiple by 8 to get it in terms
						// of bits again, and add our bit offset to it.
						int y = (((offset / 8) + k) * 8) + b;
						// Calculate the location of the pixel we want in the bit array.
						// It'll be at (y * width) + x.
						int i = (y * bmp.getWidth()) + x;
						// If the image (or this stripe of the image)
						// is shorter than 24 dots, pad with zero.
						boolean v = false;
						if (i  < dots.length()) {
							v = dots.get(i);
						}
						//7 - b
						// Finally, store our bit in the byte that we're currently
						// scribbling to. Our current 'b' is actually the exact
						// opposite of where we want it to be in the byte, so
						// subtract it from 7, shift our bit into place in a temp
						// byte, and OR it with the target byte to get it into there.
						slice |= (byte) ((v ? 1 : 0) << (7 - b));
					}
					bytes.add(slice);
//					BluetoothUtil.sendData(new byte[]{slice});
					lenght++;
				}
			}
			bytes.add((byte) 10);
//			BluetoothUtil.sendData(ESCUtil.FEED_LINE);
			offset += 24;

		}

		byte[] bytes1 = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			bytes1[i] = bytes.get(i);
		}
		BluetoothUtil.sendData(bytes1);
		BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_30);
	}

	public static  void convertBitmap(Bitmap inputBitmap) {
		int mWidth = inputBitmap.getWidth();
		int mHeight = inputBitmap.getHeight();
		convertArgbToGrayscale(inputBitmap, mWidth, mHeight);
	}

	public static void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height) {
		int pixel;
		int k = 0;
		int B = 0, G = 0, R = 0;
		dots = new BitSet();
		try {

			for (int x = 0; x < height; x++) {
				for (int y = 0; y < width; y++) {
					// get one pixel color
					pixel = bmpOriginal.getPixel(y, x);

					// retrieve color of all channels
					R = Color.red(pixel);
					G = Color.green(pixel);
					B = Color.blue(pixel);
					// take conversion up to one single value by calculating
					// pixel intensity.
					R = G = B = (int) (0.299 * R + 0.587 * G + 0.114 * B);
					// set bit into bitset, by calculating the pixel's luma
					if (R < 55) {
						dots.set(k);//this is the bitset that i'm printing
					}
					k++;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("aaa,", e.toString());
		}
	}

	//	public static byte [] initRyoShu(String[] text){
//		String[] tienNhan ;
//		String tienNhan1 = "領収金額";
//		String chuki = "領　収　印";
//		String giaTien = text[0].substring(11);
//
//		BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_24);
//		byte[] data = new byte[50 * 3 * 9 ];
//		int k = 0;
//		data[k++] = LF;
//		data[k++] = LF;
//
//		//    "ryoshu/spf/6円 , 50 , 10 , 0 , 100 , 400 , 173 , 70 , 180"
//
//		int firstText = Integer.parseInt(text[4]) * 48 / 573 ;
//		int firstRec = Integer.parseInt(text[5]) * 48 / 573 ;
//		int line = Integer.parseInt(text[7]) /10 ;
//
//		data[k++] = (byte) 0x1B;
//		data[k++] = (byte) 0x21;
//		data[k++] = (byte) 0x06;
//		data[k++] = 0x1C;
//		data[k++] = 0x43;
//		data[k++] = (byte) 1;
//
//		for (int i = 0; i < line; i++) {
//			for (int j = 0; j < firstRec; j++) {
//				 if (i == 3 || i == 4 ){
//
//					if (i==3){
//						tienNhan = tienNhan1.split("");
//					}else {
//						tienNhan = giaTien.split("");
//					}
//
//					int space = 0;
//					for (int l = 0; l < tienNhan.length; l++) {
//						if (tienNhan[l].getBytes().length == 3){
//							space = space + 4;
//						}else if (tienNhan[l].getBytes().length == 1){
//							space = space +2;
//						}
//					}
//
//					if (j < firstText || j >= firstText + space){
//						data[k++] = (byte) 32;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}else {
//						try {
//							if (j - firstText < tienNhan.length){
//								data[k++] = (byte) 0x1C;
//								data[k++] = (byte) 0x57;
//								data[k++] = (byte) 0x01;
//								data[k++] = (byte) 0x1B;
//								data[k++] = (byte) 0x21;
//								data[k++] = (byte) 0x30;
//								data[k++] = tienNhan[j - firstText].getBytes("SJIS")[0];
//								data[k++] = tienNhan[j - firstText].getBytes("SJIS")[1];
//								data[k++] = (byte) 0x1B;
//								data[k++] = (byte) 0x21;
//								data[k++] = (byte) 0x06;
//								data[k++] = (byte) 0x1C;
//								data[k++] = (byte) 0x57;
//								data[k++] = (byte) 0x00;
//							}
//							//data[k++] = tienNhan[j - firstText].getBytes("SJIS")[1];
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}else {
//					data[k++] = (byte) 32;
//					data[k++] = (byte) 0;
//					data[k++] = (byte) 0;
//				}
//			}
//				// first = 33
//			for (int j = firstRec; j < 48; j+=2) {
//				if (i == 0){
//						if (j == firstRec){
//							data[k++] = (byte) 0x1B;
//							data[k++] = (byte) 0x21;
//							data[k++] = (byte) 40;
//							data[k++] = (byte) 152;
//							data[k++] = (byte) 0;
//							data[k++] = (byte) 0;
//						}else if(j == 46 -  1){
//							data[k++] = (byte) 153;
//							data[k++] = (byte) 0;
//							data[k++] = (byte) 0;
//							data[k++] = (byte) 0x1B;
//							data[k++] = (byte) 0x21;
//							data[k++] = (byte) 0x06;
//						}else {
//							data[k++] = (byte) 149;
//							data[k++] = (byte) 0;
//							data[k++] = (byte) 0;
//						}
//
//				} else if (i == 1){
//					byte[] textBytes1 = new byte[0];
//					int khoangtrang = 0;
//					try {
//						textBytes1 = chuki.getBytes("SJIS");
//						 khoangtrang = (48 - firstRec - textBytes1.length)/4;
//					} catch (UnsupportedEncodingException e) {
//						e.printStackTrace();
//					}
//						if (j == firstRec){
//							data[k++] = (byte) 0x1B;
//							data[k++] = (byte) 0x21;
//							data[k++] = (byte) 40;
//							data[k++] = (byte)(150);
//							data[k++] = (byte) 0;
//							data[k++] = (byte) 0;
//
//							data[k++] = 0x1C;
//							data[k++] = 0x43;
//							data[k++] = (byte) 1;
////							data[k++] = (byte) 0x1B;
////							data[k++] = (byte) 0x21;
////							data[k++] = (byte) 0x06;
//							for (int l = 0; l < khoangtrang - 1; l++) {
//								data[k++] = (byte) 32;
//								data[k++] = (byte) 0;
//								data[k++] = (byte) 0;
//							}
//							data[k++] = (byte) 0x1C;
//							data[k++] = (byte) 0x57;
//							data[k++] = (byte) 0x00;
//							for (int l = 0; l < textBytes1.length; l++) {
//								data[k++] =  textBytes1[l];
//							}
//							for (int l = 0; l < khoangtrang; l++) {
//								data[k++] = (byte) 32;
//								data[k++] = (byte) 0;
//								data[k++] = (byte) 0;
//							}
//							data[k++] = (byte) 0x1B;
//							data[k++] = (byte) 0x21;
//							data[k++] = (byte) 40;
//							data[k++] = (byte)(150);
//							data[k++] = (byte) 0;
//							data[k++] = (byte) 0;
//							data[k++] = (byte) 0x1B;
//							data[k++] = (byte) 0x21;
//							data[k++] = (byte) 0x08;
//						}
//
//				} else if (i == 2){
//					if (j == firstRec){
//						data[k++] = (byte) 0x1B;
//						data[k++] = (byte) 0x21;
//						data[k++] = (byte) 0x08;
//						data[k++] = (byte) 147;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}else if(j == 48 -  1){
//						data[k++] = (byte) 146;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}else {
//						data[k++] = (byte) 149;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}
//				} else if (i == line - 1){
//					if (j == firstRec){
//						data[k++] = (byte) 0x1B;
//						data[k++] = (byte) 0x21;
//						data[k++] = (byte) 0x08;
//						data[k++] = (byte) 154;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}else if(j == 48 -  1){
//						data[k++] = (byte) 155;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}else {
//						data[k++] = (byte) 149;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}
//
//				} else {
//					if (j == firstRec){
//						data[k++] = (byte) 0x1B;
//						data[k++] = (byte) 0x21;
//						data[k++] = (byte) 0x50;
//						data[k++] = (byte) 150;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0x1B;
//						data[k++] = (byte) 0x21;
//						data[k++] = (byte) 0x06;
//
//					}else if(j == 48){
//						continue;
//					}else if(j == 48 -  1){
//						data[k++] = (byte) 0x1B;
//						data[k++] = (byte) 0x21;
//						data[k++] = (byte) 0x50;
//						data[k++] = (byte) 150;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0x1B;
//						data[k++] = (byte) 0x21;
//						data[k++] = (byte) 0x06;
//
//					}else {
//						data[k++] = (byte) 32;
//						data[k++] = (byte) 0;
//						data[k++] = (byte) 0;
//					}
//				}
//			}
//		}
//
//		String end = " 上記の道り領収致しました。有難うございました。";
//		try {
//			byte[] bytesEnd = end.getBytes("SJIS");
//			for (int i = 0; i < bytesEnd.length; i++) {
//				data[k++] = bytesEnd[i];
//			}
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//
//		}
//		data[k++] = LF;
//		data[k++] = LF;
//		data[k++] = LF;
//		data[k++] = LF;
//		return  data;
//	}
}