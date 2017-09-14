

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpiderByQueue {
	static Set<String> set=new HashSet<String>();
	static boolean b=false;
	static AtomicInteger summax=new AtomicInteger();
	static AtomicInteger count=new AtomicInteger();
	static BlockingQueue<String> urlqueue1=new ArrayBlockingQueue<String>(10000);
	static BlockingQueue<String> urlqueue2=new ArrayBlockingQueue<String>(10000);
	
	public static void main(String[] args) {
		spider1("http://www.stats.gov.cn", 0, 3, "统计");

	}
	//读取url并将数据存入txt并统计“统计出现的次数”
	public static void readUrl(String surl,BlockingQueue<String> urlqueue,String result){
		System.out.println("-----readUrl-----");
		if(b)return;
		BufferedReader br=null;
		try {
			int i=new Random().nextInt(500);
			Thread.sleep(500+i);
			URL url=new URL(surl);
			//获得输入流并对流功能加强
			InputStream in = url.openStream();
			System.out.println(summax);	
			if(summax.incrementAndGet()>1000){
				b=true;
				return;
			}

			InputStreamReader ir=new InputStreamReader(in,"UTF-8");
			br=new BufferedReader(ir);

			Pattern pattern2=Pattern.compile("统计");
			Pattern pattern=Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",Pattern.CASE_INSENSITIVE);

			while(true){
				System.out.println("---readline----"+surl);
				String line = br.readLine();
				if(line==null)break;
				//统计“统计”出现的次数
				Matcher matcher2 = pattern2.matcher(line);
				while(matcher2.find()){
					System.out.println("---readcount----");
					System.out.println(count+"---"+summax+surl);
					count.incrementAndGet();
				}
				//二次统计url
				Matcher matcher = pattern.matcher(line);
				while(matcher.find()){
					System.out.println("---readhttp----"+surl);
					String href = matcher.group(1);
//					if(href.startsWith("http")&&(!href.contentEquals(surl))&&set.add(href)){
//						urlqueue.put(href);	
//					}
					if(href.startsWith("../")){
						href=href.replace("../", surl.endsWith("/")?surl:surl+"/");
						if(set.add(href))urlqueue.put(href);
							
					}else if(href.startsWith("./")){
						href=href.replace("./", surl.endsWith("/")?surl:surl+"/");
						if(set.add(href))urlqueue.put(href);
							
					}

				}
			}

		} catch (Exception e) {
			System.out.println("无效的链接"+surl);
		}finally{

			try {
				if(br!=null)br.close();
				//if(out!=null)out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//分层读取文件
	public static void readhreftxt(BlockingQueue<String> urlqueue1,BlockingQueue<String> urlqueue2,String result){
		System.out.println("-----readhreftxt-----");
		if(b)return;
		//File file=new File(hreftxt1);
		BufferedReader br=null;
		try {
			while(!urlqueue1.isEmpty()){
				String poll = urlqueue1.poll();
				readUrl(poll,urlqueue2,result);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void spider1(String url,int maxsum,int tier,String result){
		
		readUrl(url, urlqueue1,result);
		for(int i=0;i<tier;i++){
			if(i%2==0){
				System.out.println(i+"tier");
				readhreftxt(urlqueue1, urlqueue2,result);
			}else{
				System.err.println(i+"tier");
				readhreftxt(urlqueue2, urlqueue1,result);
			}
		}
		System.out.println("----");
		System.out.println(count.get());
	}
	

}
