package com.littt.blelibrary;

public class AndroidTool {
    /**
     * 数组转字符串
     * @param Bux
     * @param len
     * @return
     */
    public String ByteToStr(byte[] Bux, int len)
    {
        String s="";
        for(int i=0;i<len;i++)
        {
            s=s+ String.format("%02X ",Bux[i]);
        }
        return s;
    }
    public String ByteToStrA(byte[] Bux, int len)
    {
        String s="";
        for(int i=0;i<len;i++)
        {
            s=s+ String.format("%02X",Bux[i]);
        }
        return s;
    }
    int hexnumToint(String hex_num){
        int dec_num = Integer.parseInt(hex_num, 16);
        return dec_num;
    }

    public byte[]StrToByteArray(String s)
    {
        byte[]Bux;
        if(s==null)return null;
        int len=s.length();
        if(len<2) return null;
        int n=len/2;
        if(n*2!=len)
            return null;
        Bux=new byte[n];
        len=n;
        n=0;
        for(int i=0;i<len;i++)
        {
            char a=s.charAt(i*2);
            char b=s.charAt(i*2+1);
            int m1=getHexValue(a);
            int m2=getHexValue(b);
            Bux[n]=(byte)(m1*16+m2);
            n++;
        }
        return Bux;
    }
    int getHexValue(char ch)
    {
        if(ch >= '0' && ch <= '9'){
            return Integer.parseInt(String.valueOf(ch));
        }
        if ( (ch >= 'a'  && ch <= 'f') || (ch >= 'A' && ch <= 'F')) {
            switch (ch) {
                case 'a':
                case 'A':
                    return 10;
                case 'b':
                case 'B':
                    return 11;
                case 'c':
                case 'C':
                    return 12;
                case 'd':
                case 'D':
                    return 13;
                case 'e':
                case 'E':
                    return 14;
                case 'f':
                case 'F':
                    return 15;
            }
        }
        return -1;
    }

    /**
     * byte[]数组转换 string ,new String 去尾部处理
     * @param Bux
     * @param index
     * @param len
     * @return
     */
    String GetStringByByte(byte[]Bux, int index, int len)
    {
        if(Bux==null) return "";
        if(len<1) return "";

        byte[]Buxx=new byte[len];
        System.arraycopy(Bux,index,Buxx,0,len);
        int n=0;
        for(int i=0;i<len;i++)
        {
            if(Buxx[i]>20)
            {
                n++;
            }
        }
        if(n==0) return "" ;
        if(n>255)return "";
        byte[]Bua=new byte[n];
        n=0;
        for(int i=0;i<len;i++)
        {
            if(Buxx[i]>20)
            {
                Bua[n]=Buxx[i];
                n++;
            }
        }
        String s=new String(Bua);
        return s;
    }

    /**
     * 转换为 十六进制字符串
     * @param Bux
     * @param index
     * @param len
     * @return string
     */
    String GetStringByHexByte(byte[]Bux, int index, int len)
    {
        byte[]Buxx=new byte[len];
        System.arraycopy(Bux,index,Buxx,0,len);
        int n=len;
        if(n==0) return "" ;
        if(n>255)return "";
        byte[]Bua=new byte[n];
        String s="";
        for(int i=0;i<len;i++)
        {
          s=s+ String.format("%02X",Buxx[i]);
        }
        return s;
    }

    public int StrTwoToInt(String s ) {
        if(s==null) return 0;
        int len=s.length();
        if(len!=2) return 0;

        int n= Integer.valueOf(s,16);
        return n;
    }

    public byte GetXorCrc(byte[] bux, int n) {
        if(n==0) return 0;
        if(n>1024) return 0;
        if(bux==null) return 0;
        if(n>bux.length) return 0;
        byte crc=(byte)(bux[0]^0x3);
        for(int i=1;i<n;i++)
        {
            crc=(byte)(crc^bux[i]);
        }
        return crc;
    }
}
