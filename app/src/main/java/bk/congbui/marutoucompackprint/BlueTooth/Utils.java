package bk.congbui.marutoucompackprint.BlueTooth;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    // UNICODE 0x23 = #
    public static final byte[] UNICODE_TEXT = new byte[] {0x23, 0x23, 0x23,
            0x23, 0x23, 0x23,0x23, 0x23, 0x23,0x23, 0x23, 0x23,0x23, 0x23, 0x23,
            0x23, 0x23, 0x23,0x23, 0x23, 0x23,0x23, 0x23, 0x23,0x23, 0x23, 0x23,
            0x23, 0x23, 0x23};

    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray = { "0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111" };


    public static byte[] decodeBitmap(Bitmap bmp){
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> list = new ArrayList<String>(); //binaryString list
        StringBuffer sb;
        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;
        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }
        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160)
                    sb.append("0");
                else
                    sb.append("1");
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }

        List<String> bmpHexList = binaryListToHexStringList(list);
        String commandHexString = "1D763000";
        String widthHexString = Integer
                .toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8
                        : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", " width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 2) {
            Log.e("decodeBitmap error", " height is too large");
            return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString+widthHexString+heightHexString);
        commandList.addAll(bmpHexList);

        return hexList2Byte(commandList);
    }

    public static List<String> binaryListToHexStringList(List<String> list) {
        List<String> hexList = new ArrayList<String>();
        for (String binaryStr : list) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);

                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            hexList.add(sb.toString());
        }
        return hexList;

    }

    public static String myBinaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }

        return hex;
    }

    public static byte[] hexList2Byte(List<String> list) {
        List<byte[]> commandList = new ArrayList<byte[]>();

        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        byte[] bytes = sysCopy(commandList);
        return bytes;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

//    public static void print_image(String nameFile) throws IOException {
//        BluetoothUtil.sendData(ESCUtil.alignCenter());
//        BluetoothUtil.sendData(ESCUtil.nextLine(3));
//        String  file = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + nameFile;
//        Bitmap bmp1 = BitmapFactory.decodeFile(file);
//        File fl = new File(file);
//        fl.delete();
//        Log.d("aaa,","width: " + bmp1.getWidth());
//        Log.d("aaa,","height: " + bmp1.getHeight());
//
//        Bitmap bmp  = Bitmap.createScaledBitmap(bmp1, 584 ,(584 * bmp1.getHeight() / bmp1.getWidth()),false);
//        convertBitmap(bmp);
//        BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_24);
//        int offset = 0;
//        int lenght = 0;
//        List<Byte> bytes = new ArrayList<>();
//        offset = 0;
//        bytes = new ArrayList<>();
//        byte[] bytes2 = new byte[3];
//        boolean flag = false;
//        while (offset < bmp.getHeight()) {
//
//            //		BluetoothUtil.sendData(ESCUtil.SELECT_BIT_IMAGE_MODE);
//            bytes.add((byte) 0x1B);
//            bytes.add((byte) 0x2A);
//            bytes.add((byte) 33);
//            bytes.add((byte) 255);
//            bytes.add((byte) 3);
//
//            for (int x = 0; x < (1024) ; ++ x) {
//                for (int k = 0; k < 3; ++ k) {
//                    byte slice = 0;
//                    for (int b = 0; b < 10; ++b) {
//                        int y = (((offset / 8) + k) * 8) + b;
//                        int i = (y * bmp.getWidth()) + x;
//                        boolean v = false;
//                        if (i  < dots.length()) {
//                            v = dots.get(i);
//                        }
//                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
//
//                    }
//                    //				bytes.add(slice);
//                    bytes2[k] = slice;
////					BluetoothUtil.sendData(new byte[]{slice});
//                    lenght++;
//                }
//                if (bytes2[0] != 0 || bytes2[1] != 0 || bytes2[2] != 0){
//                    bytes.add(bytes2[0]);
//                    bytes.add(bytes2[1]);
//                    bytes.add(bytes2[2]);
//                    flag = false;
//                }else {
//                    if (flag == false){
//                        bytes.add(bytes2[0]);
//                        bytes.add(bytes2[1]);
//                        bytes.add(bytes2[2]);
//                        flag = true;
//                    }
//                }
//            }
//            bytes.add((byte) 10);
////			BluetoothUtil.sendData(ESCUtil.FEED_LINE);
//
//            offset += 24;
//        }
//        Log.d("aaa,","size :  " + bytes.size());
//        Log.d("aaa,","dots lenght: "  + dots.length());
//        byte[] bytes1 = new byte[bytes.size()];
//        for (int i = 0; i < bytes.size(); i++) {
//            Log.d("aaa,",bytes.get(i)+"");
//            bytes1[i] = bytes.get(i);
//        }
//
//        BluetoothUtil.sendData(bytes1);
//        BluetoothUtil.sendData(ESCUtil.SET_LINE_SPACING_30);
//    }

}
