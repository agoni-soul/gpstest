package com.soul.network.encrypt;

public class IOTPWManager {
	static int switchTable[] = new int[]{
			8, 9, 10,11, 
            28,29,30,31,
            0 ,1, 2, 3,
			16,17,18,19,
            12,13,14,15,
			24,25,26,27,
			20,21,22,23,
			4 ,5 ,6 ,7
	};

	public static byte[] pwReplace(byte input[]){
		int newLen=32*(int)Math.ceil((double)input.length/32);
	    byte tmp[]=new byte[newLen];
	    System.arraycopy(input, 0, tmp, 0, input.length);
	    input=tmp;
	   
		int n = input.length;
		byte output[]=new byte[n];
		int i;
		
		for (i = 0; i < n; i++) {
			int switchIndexOffset=32*(i/32);
			int switchIndex=i%32;
			switch (switchIndex)
			{
			case 0:
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 1:	   
				output[switchIndexOffset+switchTable[switchIndex]] =(byte)(input[i]&0xff ^ 1); break;// ^
			case 2:	   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 3:	   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] + 3); break; // +
			case 4:	 
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 5:	
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i]&0xff ^ 5); break;// ^
			case 6:	  
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 7:	   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] - 7); break; // -
			case 8:	   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 9:	   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i]&0xff ^ 9); break;// ^
			case 10:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 11:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] + 11); break;// *
			case 12:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 13:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i]&0xff ^ 13); break;// ^
			case 14:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 15:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] - 15); break;// /
			case 16:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 17:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i]&0xff ^ 17); break;// ^
			case 18:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 19:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] + 19); break;// +
			case 20:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 21:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i]&0xff ^ 21); break;// ^
			case 22:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 23:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] - 29); break;// -
			case 24:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 25:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i]&0xff ^ 25); break;// ^
			case 26:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 27:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] + 27); break;// *
			case 28:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 29:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i]&0xff ^ 29); break;// ^
			case 30:   
				output[switchIndexOffset+switchTable[switchIndex]] = input[i]; break;
			case 31:   
				output[switchIndexOffset+switchTable[switchIndex]] = (byte)(input[i] - 31); break;// /
			default:
				break;
			}
		}
		return deleteArrayTailZero(output);
	}
	public static byte[] pwUnReplace(byte input[]){
		int newLen=32*(int)Math.ceil((double)input.length/32);
	    byte tmp[]=new byte[newLen];
	    System.arraycopy(input, 0, tmp, 0, input.length);
	    input=tmp;
	    
		int n = input.length;
		byte output[]=new byte[n];
		int i;

		for (i = 0; i < n; i++) {
			int switchIndexOffset=32*(i/32);
			int switchIndex=i%32;
			switch (switchIndex)
			{
			case 0:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 1:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff ^ 1); break;// ^
			case 2:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 3:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] - 3); break; // +
			case 4:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 5:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff ^ 5); break;// ^
			case 6:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 7:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] + 7); break; // -
			case 8:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 9:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff^ 9); break;// ^
			case 10:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 11:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] - 11); break;// *
			case 12:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 13:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff ^ 13); break;// ^
			case 14:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 15:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] + 15); break;// /
			case 16:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 17:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff^ 17); break;// ^
			case 18:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 19:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] - 19); break;// +
			case 20:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 21:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff^ 21); break;// ^
			case 22:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 23:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] + 29); break;// -
			case 24:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 25:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff^ 25); break;// ^
			case 26:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 27:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] - 27); break;// *
			case 28:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 29:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]]&0xff^ 29); break;// ^
			case 30:
				output[i] = input[switchIndexOffset+switchTable[switchIndex]]; break;
			case 31:
				output[i] = (byte)(input[switchIndexOffset+switchTable[switchIndex]] + 31); break;// /
			default:
				break;
			}
		}
		return deleteArrayTailZero(output);
	}
	
	private static byte[] deleteArrayTailZero(byte []b){
		int len;
		for(len=b.length-1;len>=0;len--){
			if(b[len]!=0) break;;
		}
		byte result[]=new byte[len+1];
		System.arraycopy(b, 0, result, 0, result.length);
		return result;
	}
	
	public static String encode(String pwd){
		String result=null;
		byte repPwd[]=pwUnReplace(pwd.getBytes());
		byte Ciphertext[] = {
				(byte)0x85,(byte)0x60,(byte)0x1D,(byte)0xB7,(byte)0x95,(byte)0xEE,(byte)0xED,(byte)0x3E,(byte)0xF3,(byte)0x3B,(byte)0xF8,(byte)0xE0,(byte)0x44,(byte)0xB2,(byte)0x30,(byte)0xC5,
				(byte)0xE4,(byte)0x5E,(byte)0x2E,(byte)0xF8,(byte)0xDC,(byte)0xB0,(byte)0xD,(byte)0xD9,(byte)0xAA,(byte)0xC9,(byte)0x8B,(byte)0xD,(byte)0x67,(byte)0x5A,(byte)0x52,(byte)0xC9,
				(byte)0xAA,(byte)0x20,(byte)0x6C,(byte)0x2D,(byte)0x5,(byte)0xE6,(byte)0xA0,(byte)0x71,(byte)0x24,(byte)0xF9,(byte)0xDE,(byte)0xF0,(byte)0xE4,(byte)0x3,(byte)0x7E,(byte)0xFF,
				(byte)0x14,(byte)0xF6,(byte)0xA9,(byte)0xCC,(byte)0xD6,(byte)0xEF,(byte)0xF2,(byte)0xD,(byte)0xC2,(byte)0xD,(byte)0x37,(byte)0xDC,(byte)0x35,(byte)0xB3,(byte)0x7D,(byte)0xBB,};
		IOTAES aes=new IOTAES("rockykaikainihao".getBytes());
		byte[] b64Map=aes.InvCipher(Ciphertext);
		result=IOTBase64.encode(new String(b64Map).toCharArray(), repPwd);
		return result;
	}
	public static String decode(String pwd){
		String result=null;
		byte Ciphertext[] = {
				(byte)0x85,(byte)0x60,(byte)0x1D,(byte)0xB7,(byte)0x95,(byte)0xEE,(byte)0xED,(byte)0x3E,(byte)0xF3,(byte)0x3B,(byte)0xF8,(byte)0xE0,(byte)0x44,(byte)0xB2,(byte)0x30,(byte)0xC5,
				(byte)0xE4,(byte)0x5E,(byte)0x2E,(byte)0xF8,(byte)0xDC,(byte)0xB0,(byte)0xD,(byte)0xD9,(byte)0xAA,(byte)0xC9,(byte)0x8B,(byte)0xD,(byte)0x67,(byte)0x5A,(byte)0x52,(byte)0xC9,
				(byte)0xAA,(byte)0x20,(byte)0x6C,(byte)0x2D,(byte)0x5,(byte)0xE6,(byte)0xA0,(byte)0x71,(byte)0x24,(byte)0xF9,(byte)0xDE,(byte)0xF0,(byte)0xE4,(byte)0x3,(byte)0x7E,(byte)0xFF,
				(byte)0x14,(byte)0xF6,(byte)0xA9,(byte)0xCC,(byte)0xD6,(byte)0xEF,(byte)0xF2,(byte)0xD,(byte)0xC2,(byte)0xD,(byte)0x37,(byte)0xDC,(byte)0x35,(byte)0xB3,(byte)0x7D,(byte)0xBB,};
		IOTAES aes=new IOTAES("rockykaikainihao".getBytes());
		byte[] b64Map=aes.InvCipher(Ciphertext);
		byte b[]=IOTBase64.decode(new String(b64Map).toCharArray(), pwd);
		result=new String(pwReplace(b));
		return result;
	}
}
