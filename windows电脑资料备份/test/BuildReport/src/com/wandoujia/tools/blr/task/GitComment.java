package com.wandoujia.tools.blr.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.wandoujia.tools.blr.common.BuildComment;


public class GitComment
{
	public GitComment()
	{
		
	}
	
	public void getComment(String fileName)
	{
		File file = new File(fileName);  
		//StringBuffer buffer = new StringBuffer();  
		List<BuildComment> builddata = new ArrayList<BuildComment>();
        
		if(file.exists()){  
            if(file.isFile()){  
                try{  
                    BufferedReader input = new BufferedReader (new FileReader(file));  
                    String text;  
                    while((text = input.readLine()) != null)
                    {
                    	BuildComment data = new BuildComment();
                    	if(text.contains("Update Version"))
                    	{
                    		
                    	}
                    	else
                    	{
                    		String[] content = text.split("#~");
                    		data.setVersion("Phoenix2_mixed");
                    		data.setName(content[0]);
                    		data.setDetail(content[1]);
                    		data.setTime(content[2]);
                    		data.setMD5(content[3]);
                    		builddata.add(data);
                    	}
                    }
                }
                catch(Exception e)
                {
                	
                }
            }
        }

		MailTask.sendMail("huwei.nwu@gmail.com", "Android Build Report", 
				buildContent("Android Developer",builddata), false);
	}
	
	private String buildContent(String developer, List<BuildComment> datas) {
        StringBuilder builder = new StringBuilder();
        int lineCnt = 0;
        String style = "";
        
        builder.append("Hi, " + developer + ":<br /><br />");
        builder.append("<b>下面是今天的编译日志！</b><br />");
        builder.append("<table id='report' style='border-collapse:colapse'>");
        builder.append("<tr style='background-color:#A7C942; color:#fff;font-size:1.4em; text-align:left;'>" +
                "<th style='border:1px solid #98bf21; padding:3px 7px 2px 7px; text-align:left;'>版本号</th>" +
                "<th style='border:1px solid #98bf21; padding:3px 7px 2px 7px; text-align:left;'>用户名</th>" +
                "<th style='border:1px solid #98bf21; padding:3px 7px 2px 7px; text-align:left;'>日志</th>" +
                "<th style='border:1px solid #98bf21; padding:3px 7px 2px 7px; text-align:left;'>MD5</th></tr>");
        for (BuildComment data : datas) {
            if (lineCnt % 2 == 1) {
                style = "font-size:1.2em; color:#000; background-color:#EAF2D3;";
            } else {
                style = "font-size:1.2em;";
            }
            builder.append("<tr style='" + style + "'>" +
                    "<td style='border:1px solid #98bf21; padding:3px 7px 2px 7px;'>" + data.getVersion() + "</td>" +
                    "<td style='border:1px solid #98bf21; padding:3px 7px 2px 7px;'>" + data.getName() + "</td>" +
                    "<td style='border:1px solid #98bf21; padding:3px 7px 2px 7px;'>" + data.getDetail() + "</td>" +
                    "<td style='border:1px solid #98bf21; padding:3px 7px 2px 7px;'>" + data.getMD5() + "</td></tr>");
            lineCnt++;
        }
        builder.append("</table>");
        //builder.append("<br /><br />.");
        return builder.toString();
    }
}