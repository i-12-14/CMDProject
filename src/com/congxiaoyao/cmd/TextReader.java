package com.congxiaoyao.cmd;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 文本读取器，将文件以文本的形式读取
 * 每一行读取出的文本将以回调的形式通知外界
 * 使用者需在使用时复写类内onReadLine函数
 * 比scaner快一点
 * 提供内部类PathBuilder 可以安全的不用考虑正斜线还是反斜线的构建一个路径
 * 甚至是直接从剪切板获取其中保存的路径，当然不推荐使用，只是以备不时之需
 * @version 1.1
 * @date 2015.10.04
 * @author congxiaoyao
 */
public class TextReader {

	private int bufferSize;
	private File file;
	private BufferedReader reader;
	private FileInputStream fileInputStream;
	private InputStreamReader inputStreamReader;

	public TextReader(){}

	public TextReader(String path){
		file = new File(path);
		this.bufferSize = 8192;
		initReader();
	}

	public TextReader(String path,int bufferSize) {
		file = new File(path);
		this.bufferSize = bufferSize;
		initReader();
	}

	public void read(){
		String line = null;
		if(reader == null) return;
		try {
			while ((line = reader.readLine()) != null) {
				onReadLine(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeReader();
		onReadFinished();
	}

	public void onReadLine(String line){}

	public void onReadFinished(){}
	
	public void onError(Exception e) {
		e.printStackTrace();
	}

	public void setPath(String path) {
		file = new File(path);
		if(reader != null) closeReader();
		initReader();
	}

	public File getFile(){
		return file;
	}

	private void initReader(){
		try {
			String charsetName = codeString(file.getAbsolutePath());
			fileInputStream = new FileInputStream(file);
			inputStreamReader = new InputStreamReader(fileInputStream, charsetName);
			reader = new BufferedReader(inputStreamReader,bufferSize);
		} catch (Exception e) {
			onError(e);
		}
	}

	private void closeReader(){
		try {
			reader.close();
			inputStreamReader.close();
			fileInputStream.close();
		} catch (IOException e) {
			onError(e);
		}
	}

	public static String codeString(String fileName) throws Exception{
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
		int p = (bin.read() << 8) + bin.read();
		String code = null;
		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:  
			code = "Unicode";  
			break;  
		case 0xfeff:  
			code = "UTF-16BE";  
			break;  
		default:  
			code = "GBK";  
		}
		bin.close();
		return code;  
	} 

	public static class PathBuilder {

		private StringBuilder builder;

		public PathBuilder(char driveLetter){
			builder = new StringBuilder();
			builder.append(driveLetter+":\\");
		}

		/**
		 * 通过构造函数给定盘符后，此方法会将文件夹或文件追加在后面，支持链式编程
		 * @param folderName 可以是文件夹的名字也可以是文件名
		 * @return
		 */
		public PathBuilder append(String folderName){
			builder.append("\\").append(folderName);
			return this;
		}

		@Override
		public String toString() {
			return builder.toString();
		}

		/**
		 * 说白了就是获取剪切板上的内容，如果你想拿到某个文件的path，只要将其所在文件夹的目录
		 * 复制下来，参数传文件名字，即可通过此方法获取这个文件的path
		 * @param fileName 你懂得
		 * @return 字符串的path
		 */
		public static String getPathByAccessClipBoardAndFileName(String fileName){
			String path = getSysClipboardText() + "\\" + fileName;
			System.out.println(path);
			return path;
		}

		private static String getSysClipboardText() {
			String ret = "";
			Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
			// 获取剪切板中的内容
			Transferable clipTf = sysClip.getContents(null);
			if (clipTf != null) {
				// 检查内容是否是文本类型
				if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						ret = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return ret;
		}
	}

}
