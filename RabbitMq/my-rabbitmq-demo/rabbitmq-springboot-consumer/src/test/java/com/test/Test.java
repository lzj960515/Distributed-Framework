package com.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public class Test {

    static SymmetricCrypto symmetricCrypto = new SymmetricCrypto(SymmetricAlgorithm.AES, readFile("D:\\file-test\\key"));

    public static void main(String[] args) {
        /*
        //随机生成密钥
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
        writeFile(key, "D:\\file-test\\key");
        */
        encryptFile("D:\\file-test\\789.png","D:\\file-test\\encrypt\\789.png");
        decryptFile("D:\\file-test\\encrypt\\789.png","D:\\file-test\\decrypt\\789.png");

    }

    public static SymmetricCrypto aes(){
        return symmetricCrypto;
    }


    public static void encryptFile(String sourceFile, String destFile){
        //读取文件字节码
        byte[] fileData = readFile(sourceFile);
        //加密
        byte[] encrypt = aes().encrypt(fileData);
        //写入文件
        writeFile(encrypt, destFile);
    }

    public static void decryptFile(String sourceFile, String destFile){
        //读取文件字节码
        byte[] fileData = readFile(sourceFile);
        //加密
        byte[] encrypt = aes().decrypt(fileData);
        //写入文件
        writeFile(encrypt, destFile);
    }

    public static byte[] readFile(String filename){
        return FileUtil.readBytes(filename);
    }

    public static void writeFile(byte[] fileData, String filename){
        FileUtil.writeBytes(fileData, filename);
    }

}
