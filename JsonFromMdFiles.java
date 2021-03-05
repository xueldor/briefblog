package com.xueldor.myarticle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * GenArticleIndex.jar���ߵ�Դ����
 * myarticleĿ¼��ƽ��Ŀ¼ִ�� java -jar GenArticleIndex.jar
 */
public class JsonFromMdFiles {
	private static String dir = "myarticle";
	private static String[] suffix = new String[] {".md", ".txt"};
	
	private static StringBuilder sbJson = new StringBuilder();
	
	public static void main(String[] args) {
		File dirFile = new File(dir);
		
		sbJson.append("var indexes = ");
		searchFile(dirFile);
		System.out.println(sbJson.toString());
		//write to file
		BufferedWriter  fWriter = null;
		try {
			fWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("articleIndexes.js"), StandardCharsets.UTF_8));
			fWriter.write(sbJson.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (fWriter != null) {
				try {
					fWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	public static void searchFile(File f) {
		String[] files = f.list();
		List<String> fArrayList = Arrays.asList(files);
		Collections.sort(fArrayList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				//���谴��01-name�ĸ�ʽ���Ǵ˸���ʽ�ķŵ�����档
				int ai = Integer.MAX_VALUE;
				int bi = Integer.MAX_VALUE;
				
				int o1index = o1.indexOf('-');
				if(o1.indexOf('-') > 0) {
					String a = o1.substring(0, o1index);
					try {
						ai = Integer.parseInt(a);
					}catch (Exception e) {
					}
				}
				int o2index = o2.indexOf('-');
				if(o2.indexOf('-') > 0) {
					String b = o2.substring(0, o2index);
					try {
						bi = Integer.parseInt(b);
					}catch (Exception e) {
					}
				}
				return ai - bi;
			}
		});
		//ƴ��json��fArrayList�����飬����'append ['
		sbJson.append('[');
		String path = f.getPath() + '/';
		for (String string : fArrayList) {
			File sub = new File(path + string);
			if(sub.isDirectory()) {
				//���Բ�������Ч�ļ���Ŀ¼
				if(effectiveChildNum(sub) > 0) {
					sbJson.append("{\"" +path.replaceAll("\\\\", "/") + string + "\":");
					searchFile(sub);
					sbJson.append("},");
				}
			}else if(sub.isFile()){
				//���Ժ�׺����suffix�涨��׺���ļ�
				for (String suf : suffix) {
					if(string.endsWith(suf)) {
						sbJson.append("{\"" +path.replaceAll("\\\\", "/") + string + "\":0},");
						break;
					}
				}
			}
		}

		if(sbJson.charAt(sbJson.length() - 1) == ',') {
			sbJson.deleteCharAt(sbJson.length() - 1);
		}
		sbJson.append(']');
	}
	
	private static int effectiveChildNum(File dir) {
		int total = 0;
		
		ArrayList<File> stack = new ArrayList<>();//����ջ�����ݹ�
		stack.add(dir);
		for(int j = 0;j < stack.size(); j++) {
			File cur = stack.get(j);
			File[] fs = cur.listFiles();
			if (fs != null) {
				for(int i = 0; i < fs.length; i++) {
					File each = fs[i];
					if(each.isFile()) {
						String simpName = each.getName();
						for (String suf : suffix) {
							if(simpName.endsWith(suf)) {
								total++;
								break;
							}
						}
					}else if (each.isDirectory()) {
						//����listʱ��ֻ��ĩβ�����Ԫ���ǿ��Եġ�
						stack.add(each);
					}
				}
			}
		}
		return total;
	}

}