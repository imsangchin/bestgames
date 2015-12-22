/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: Apache Tomcat/7.0.42
 * Generated at: 2013-09-11 08:00:52 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import com.channel.conn.ConnDB;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.text.*;
import java.util.*;
import com.channel.dao.*;
import com.channel.dao.impl.*;
import com.channel.model.*;
import java.sql.ResultSet;

public final class in_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

private static Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}
	

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
        throws java.io.IOException, javax.servlet.ServletException {

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html;charset=UTF-8");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write('\n');
      out.write('\n');
      out.write('\n');

	if(session.getAttribute("auth") == null) {
		response.sendRedirect("login.jsp");
	}
		int sumCount = 0;
		double sumNdau = 0.0;
		double sumNdlu = 0.0;
		double appdownload = 0.0;
		double incomeSum = 0.0;
		DecimalFormat df2  = new DecimalFormat("0.00");
		
		int channelId = Integer.valueOf(request.getParameter("id"));
		ChannelDao channelDao = new ChannelDaoImpl();
		Channel channel = channelDao.queryChannelById(channelId);
		String count = request.getParameter("count");
		String name = (String)session.getAttribute("channelName" + count);
		SourceDao dao = new SourceDaoImpl();
		List<Source> list = dao.querySourceByChannelName(name);
		Collections.sort(list);
		Iterator<Source> it = list.iterator();

      out.write("\n");
      out.write("<!DOCTYPE html>\n");
      out.write("<html>\n");
      out.write("<head>\n");
      out.write("<meta charset=\"utf-8\">\n");
      out.write("<title>豌豆渠道后台</title>\n");
      out.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
      out.write("<meta name=\"description\" content=\"\">\n");
      out.write("<meta name=\"author\" content=\"\">\n");
      out.write("<meta name=\"keywords\" content=\"\">\n");
      out.write("<meta name=\"description\" content=\"\">\n");
      out.write("<link rel=\"stylesheet\"\n");
      out.write("\thref=\"http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css\" />\n");
      out.write("<link rel=\"stylesheet\" href=\"static/css/common.css\">\n");
      out.write("<link href=\"static/css/bootstrap.css\" rel=\"stylesheet\">\n");
      out.write("<link href=\"static/css/bootstrap-responsive.css\" rel=\"stylesheet\">\n");
      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("<link rel=\"apple-touch-icon-precomposed\" sizes=\"144x144\"\n");
      out.write("\thref=\"static/ico/apple-touch-icon-144-precomposed.png\">\n");
      out.write("<link rel=\"apple-touch-icon-precomposed\" sizes=\"114x114\"\n");
      out.write("\thref=\"static/ico/apple-touch-icon-114-precomposed.png\">\n");
      out.write("<link rel=\"apple-touch-icon-precomposed\" sizes=\"72x72\"\n");
      out.write("\thref=\"static/ico/apple-touch-icon-72-precomposed.png\">\n");
      out.write("<link rel=\"apple-touch-icon-precomposed\"\n");
      out.write("\thref=\"static/ico/apple-touch-icon-57-precomposed.png\">\n");
      out.write("<link rel=\"shortcut icon\" href=\"static/ico/favicon.png\">\n");
      out.write("<style type=\"text/css\">\n");
      out.write("#padding {\n");
      out.write("\ttext-align:left;\n");
      out.write("\tpadding-right: 10px;\n");
      out.write("\tpadding-bottom: 4px;\n");
      out.write("}\n");
      out.write("</style>\n");
      out.write("\n");
      out.write("<script type=\"text/javascript\">\n");
      out.write("\tfunction setUrl(n, i) {\n");
      out.write("\t\tstr = \"url\";\n");
      out.write("\t\tstr += i;\n");
      out.write("\t\tdocument.getElementById(str).href = n;\n");
      out.write("\t}\n");
      out.write("</script>\n");
      out.write("\n");
      out.write("</head>\n");
      out.write("<body>\n");
      out.write("\t<div id=\"wrapper\" class=\"displayn\" style=\"margin-top: 0px;\">\n");
      out.write("\t\t<div style=\"float: left;\">\n");
      out.write("\t\t\t<ul class=\"hd marginBT20\" style=\"margin: 0px;\">\n");
      out.write("\t\t\t\t<li><a\n");
      out.write("\t\t\t\t\thref=\"out.jsp?id=");
      out.print(Base64.encode(String.valueOf(channelId).getBytes()));
      out.write("&name=");
      out.print(Base64.encode(String.valueOf(name).getBytes()));
      out.write("\">对外后台地址</a>\n");
      out.write("\t\t\t\t</li>\n");
      out.write("\t\t\t\t<li id=\"edit-channel\" data-name=\"");
      out.print(name);
      out.write("\"\n");
      out.write("\t\t\t\t\tdata-id=\"");
      out.print(channelId);
      out.write("\">编辑网盟信息</li>\n");
      out.write("\t\t\t\t<li><label for=\"from\">From</label><input type=\"text\" id=\"from\"\n");
      out.write("\t\t\t\t\tname=\"from\" style=\"height: 15px;\" /><label for=\"to\">to</label><input\n");
      out.write("\t\t\t\t\ttype=\"text\" id=\"to\" name=\"to\" style=\"height: 15px;\" /><br>\n");
      out.write("\t\t\t\t<a id=\"import\" class=\"marginRT30\">导出CSV</a><span id=\"loading\" style=\"height: 30px; width : 30px;\"><img style=\"height: 30px; width : 30px;\"/></span>\n");
      out.write("\t\t\t\t</li>\n");
      out.write("\t\t\t</ul>\n");
      out.write("\t\t</div>\n");
      out.write("\t\t<div style=\"float: left;margin-left: 5%;\">\n");
      out.write("\t\t\t<ul>\n");
      out.write("\t\t\t\t<li id=\"user\" style=\"margin-bottom: 4px\">激活用户数：</li>\n");
      out.write("\t\t\t\t<li id=\"ndlu\" style=\"margin-bottom: 4px\">NDLU：</li>\n");
      out.write("\t\t\t\t<li id=\"ndau\" style=\"margin-bottom: 4px\">NDAU：</li>\n");
      out.write("\t\t\t\t<li id=\"appdownload\" style=\"margin-bottom: 4px\">首日人均App download*NDLU/总NDLU：</li>\n");
      out.write("\t\t\t\t<li id=\"income\" style=\"margin-bottom: 4px\">首日人均收入*NDLU等值/总NDLU：</li>\n");
      out.write("\t\t\t</ul>\n");
      out.write("\t\t</div>\n");
      out.write("\t\t<div style=\"float: right;\">\n");
      out.write("\t\t\t<table>\n");
      out.write("\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>渠道分类</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getChannelType());
      out.write("</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>备注</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getComment());
      out.write("</td>\n");
      out.write("\t\t\t\t</tr>\n");
      out.write("\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>联系人</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getContact());
      out.write("</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>财务流程</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getFinProcess());
      out.write("</td>\n");
      out.write("\t\t\t\t</tr>\n");
      out.write("\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>QQ</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getQq());
      out.write("</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>抬头</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getTitle());
      out.write("</td>\n");
      out.write("\t\t\t\t</tr>\n");
      out.write("\t\t\t\t\n");
      out.write("\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>Tel</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getTel());
      out.write("</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>公司帐号</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getAccount());
      out.write("</td>\n");
      out.write("\t\t\t\t</tr>\n");
      out.write("\t\t\t\t\n");
      out.write("\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>Email</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getEmail());
      out.write("</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>开户行</b>：</td>\n");
      out.write("\t\t\t\t\t<td id=\"padding\">");
      out.print(channel.getBank());
      out.write("</td>\n");
      out.write("\t\t\t\t</tr>\n");
      out.write("\t\t\t\t\n");
      out.write("\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t<td id=\"padding\"><b>地址</b>：</td>\n");
      out.write("\t\t\t\t\t<td colspan=\"3\" id=\"padding\">");
      out.print(channel.getAddress());
      out.write("</td>\n");
      out.write("\t\t\t\t</tr>\n");
      out.write("\n");
      out.write("\t\t\t</table>\n");
      out.write("\t\t</div>\n");
      out.write("\n");
      out.write("\n");
      out.write("\t\t<br>\n");
      out.write("\t\t<table id=\"data-table\" class=\"bd table table-striped\">\n");
      out.write("\n");
      out.write("\t\t\t<tr>\n");
      out.write("\t\t\t\t<th>网盟名称</th>\n");
      out.write("\t\t\t\t<th>User Source</th>\n");
      out.write("\t\t\t\t<th>渠道类型</th>\n");
      out.write("\t\t\t\t<th>目前价格</th>\n");
      out.write("\t\t\t\t<th>目前质量参数</th>\n");
      out.write("\t\t\t\t<th>激活用户</th>\n");
      out.write("\t\t\t\t<th>全量量级</th>\n");
      out.write("\t\t\t\t<th>备注信息</th>\n");
      out.write("\t\t\t\t<th>分包下载地址</th>\n");
      out.write("\t\t\t\t<th>在哪儿能找到</th>\n");
      out.write("\t\t\t\t<th colspan=\"2\">操作</th>\n");
      out.write("\t\t\t</tr>\n");
      out.write("\t\t\t");

				ConnDB conn = ConnDB.getConnection();

				SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

				DailyInfoDao infoDao = new DailyInfoDaoImpl();
				int i = 0;
				while (it.hasNext()) {
					String valueN = "0";
					String discount = "80";
					String price = "1.0";
			
      out.write("\n");
      out.write("\t\t\t");

				Source source = it.next();
			
      out.write("\n");
      out.write("\t\t\t<tr data=\"");
      out.print(source.getId());
      out.write("\">\n");
      out.write("\t\t\t\t<td>");
      out.print(name);
      out.write("</td>\n");
      out.write("\t\t\t\t<td><a href=\"./daily?action=in&num=");
      out.print(++i);
      out.write("\"><span\n");
      out.write("\t\t\t\t\t\tclass=\"span-name\" data=\"name\">");
      out.print(source.getSourceName());
      out.write("</span>\n");
      out.write("\t\t\t\t</a>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t");

					System.out
								.println("select discount,price from daily_info where id in (select max(id) from daily_info where source_id="
										+ source.getId() + ")");
						ResultSet rs2 = conn
								.query("select discount,price from daily_info where id in (select max(id) from daily_info where source_id="
										+ source.getId() + ")");
						while (rs2.next()) {
							discount = rs2.getString("discount");
							price = rs2.getString("price");
						}
				
      out.write("\n");
      out.write("\t\t\t\t<td><span data=\"type\">");
      out.print(source.getChannelType());
      out.write("</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t<td><span data=\"price\">¥ ");
      out.print(price);
      out.write("</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t<td><span data=\"percent\">");
      out.print(discount);
      out.write("%</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t");

					/* String ndauValue = infoDao.getMaxDateNdauBySourceId(source.getId()); */
						String valueA = "";
						String valueD = "";
						String valueI = "";
						
						ResultSet rs = conn
								.query("select ndlu,ndau,download_avg from ndau_ndlu where date='"
										+ sf.format(getDateBefore(new Date(), 1))
										+ "' and source_name='"
										+ source.getSourceName() + "'");
					ResultSet rs1 = conn
								.query("select income from income where date='"
										+ sf.format(getDateBefore(new Date(), 1))
										+ "' and source_name='"
										+ source.getSourceName() + "'");
						while (rs1.next()) {
							valueI = rs1.getString("income");
							
						}
						while (rs.next()) {
							valueN = rs.getString("ndlu");
							valueA = rs.getString("ndau");
							valueD = rs.getString("download_avg");
							
						}
						double ndlu = 0;
						if(discount.contains("%")) {
							
						} else {
						 ndlu = Double.parseDouble(discount)
								* Integer
										.parseInt(valueN == null || valueN.equals("") ? "0"
												: valueN) / 100;
						}
				
      out.write("\n");
      out.write("\t\t\t\t");

					int value = ndlu - (int) ndlu != 0.0 ? (int) ndlu + 1
								: (int) ndlu;
								sumCount += value;
				
      out.write("\n");
      out.write("\t\t\t\t<td><span data=\"level\">");
      out.print(value);
      out.write("</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t");

					source.setCurQuantity(String.valueOf(value));
						session.setAttribute("source" + i, source);
				
      out.write("\n");
      out.write("\t\t\t\t<td><span data=\"level-all\">");
      out.print(source.getFullQuantity());
      out.write("</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t<td><span data=\"comments\">");
      out.print(source.getComment());
      out.write("</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t<td><span data=\"pk-name\"><a\n");
      out.write("\t\t\t\t\t\thref=\"http://dl.wandoujia.com/files/phoenix/latest/wandoujia-");
      out.print(source.getSourceName());
      out.write(".apk\">下载</a>\n");
      out.write("\t\t\t\t</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t<td><span data=\"where\">");
      out.print(source.getLink());
      out.write("</span>\n");
      out.write("\t\t\t\t</td>\n");
      out.write("\t\t\t\t<td class=\"edit-data\">编辑</td>\n");
      out.write("\t\t\t\t<td class=\"dele-data\">删除</td>\n");
      out.write("\t\t\t</tr>\n");
      out.write("\t\t\t");

				sumNdlu += Integer.parseInt(valueN == null || valueN.equals("") ? "0": valueN);
				sumNdau += Integer.parseInt(valueA == null || valueA.equals("") ? "0": valueA);
				appdownload += Integer.parseInt(valueN == null || valueN.equals("") ? "0": valueN) *
				 Double.parseDouble(df2.format(Double.parseDouble(valueD == null || valueD.equals("") ? "0" : valueD)));
				 System.out.println(Double.parseDouble(valueI == null || valueI.equals("") ? "0" : valueI));
				 System.out.println(Integer.parseInt(valueN == null || valueN.equals("") ? "0": valueN));
				incomeSum += 
				Double.parseDouble(df2.format(Double.parseDouble(valueI == null || valueI.equals("") ? "0" : valueI) ));
				
				}
				
			
      out.write("\n");
      out.write("\t\t</table>\n");
      out.write("\t\t<script type=\"text/javascript\">\n");
      out.write("\t\t\tvar e = document.getElementById(\"ndlu\");\n");
      out.write("\t\t\te.innerHTML='NDLU：'+");
      out.print(sumNdlu);
      out.write(";\n");
      out.write("\t\t\t\n");
      out.write("\t\t\tvar e1 = document.getElementById(\"user\");\n");
      out.write("\t\t\te1.innerHTML='激活用户数：'+");
      out.print(sumCount);
      out.write(";\n");
      out.write("\t\t\t\n");
      out.write("\t\t\tvar e2 = document.getElementById(\"ndau\");\n");
      out.write("\t\t\te2.innerHTML='NDAU：'+");
      out.print(sumNdau);
      out.write(";\n");
      out.write("\t\t\t\n");
      out.write("\t\t\tvar e3 = document.getElementById(\"appdownload\");\n");
      out.write("\t\t\te3.innerHTML='首日人均App download*NDLU/总NDLU：'+");
      out.print(df2.format(appdownload / sumNdlu));
      out.write(";\n");
      out.write("\t\t\t\n");
      out.write("\t\t\tvar e4 = document.getElementById(\"income\");\n");
      out.write("\t\t\te4.innerHTML='首日人均收入*NDLU等值/总NDLU：'+");
      out.print(df2.format(incomeSum / sumNdlu));
      out.write(";\n");
      out.write("\t\t</script>\n");
      out.write("\t\t<div class=\"ft\">\n");
      out.write("\t\t\t<input type=\"text\" id=\"name\" name=\"name\" class=\"\" /> <label id=\"add\"\n");
      out.write("\t\t\t\tfor=\"name\" class=\"link\">增加分包</label>\n");
      out.write("\t\t</div>\n");
      out.write("\t</div>\n");
      out.write("\t<div id=\"dele-Channel\">\n");
      out.write("\t\t<div class=\"mask\"></div>\n");
      out.write("\t\t<div id=\"dele-Channel-wrapper\" class=\"wrapper\">\n");
      out.write("\t\t\t<p>确定要删除这个分包嘛？</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<button id=\"y-dele-btn\" type=\"button\">Apply</button>\n");
      out.write("\t\t\t\t<button id=\"n-dele-btn\" type=\"button\">Cancel</button>\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t</div>\n");
      out.write("\t</div>\n");
      out.write("\t<div id=\"edit-Channel\">\n");
      out.write("\t\t<div class=\"mask\"></div>\n");
      out.write("\t\t<form id=\"edit-Channel-wrapper\" class=\"wrapper\">\n");
      out.write("\t\t\t<p class=\"bold\">编辑渠道</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"channel_type\">渠道分类</label> <input type=\"text\"\n");
      out.write("\t\t\t\t\tid=\"channel_type\" name=\"channel_type\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"contact\">联系人</label> <input type=\"hidden\"\n");
      out.write("\t\t\t\t\tid=\"channel-name\" name=\"channel-name\" value=\"\" /> <input\n");
      out.write("\t\t\t\t\ttype=\"text\" id=\"contact\" name=\"contact\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"qq\">QQ</label> <input type=\"text\" id=\"qq\" name=\"qq\"\n");
      out.write("\t\t\t\t\tstyle=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"tel\">Tel</label> <input type=\"text\" id=\"tel\" name=\"tel\"\n");
      out.write("\t\t\t\t\tstyle=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"email\">Email</label> <input type=\"text\" id=\"email\"\n");
      out.write("\t\t\t\t\tname=\"email\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"address\">地址</label> <input type=\"text\" id=\"address\"\n");
      out.write("\t\t\t\t\tname=\"address\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"message\">备注</label> <input type=\"text\" id=\"comment\"\n");
      out.write("\t\t\t\t\tname=\"comment\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"progress\">财务流程</label> <input type=\"text\"\n");
      out.write("\t\t\t\t\tid=\"fin_process\" name=\"fin_process\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p class=\"bold\">财务信息</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"title\">抬头</label> <input type=\"text\" id=\"title\"\n");
      out.write("\t\t\t\t\tname=\"title\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"account\">公司账号</label> <input type=\"text\" id=\"account\"\n");
      out.write("\t\t\t\t\tname=\"account\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"bank\">开户行</label> <input type=\"text\" id=\"bank\"\n");
      out.write("\t\t\t\t\tname=\"bank\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<button id=\"y-edit-btn\" type=\"button\">Apply</button>\n");
      out.write("\t\t\t\t<button id=\"n-edit-btn\" type=\"button\">Cancel</button>\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t</form>\n");
      out.write("\t</div>\n");
      out.write("\t<div id=\"edit-sub\">\n");
      out.write("\t\t<div class=\"mask\"></div>\n");
      out.write("\t\t<form id=\"edit-sub-wrapper\" class=\"wrapper\">\n");
      out.write("\t\t\t<p class=\"bold\">编辑分包</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"name\">User Source</label> <input type=\"hidden\"\n");
      out.write("\t\t\t\t\tid=\"channelId\" name=\"channelId\" value=\"\" /> <input type=\"hidden\"\n");
      out.write("\t\t\t\t\tid=\"channelName\" name=\"channelName\" value=\"\" /> <input\n");
      out.write("\t\t\t\t\ttype=\"hidden\" id=\"subId\" name=\"subId\" value=\"\" /> <input\n");
      out.write("\t\t\t\t\ttype=\"text\" id=\"name\" name=\"name\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"type\">渠道类型</label> <input type=\"text\" id=\"type\"\n");
      out.write("\t\t\t\t\tname=\"type\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p style=\"display:none;\">\n");
      out.write("\t\t\t\t<label for=\"price\">目前价格(￥)</label> <input type=\"text\" id=\"price\"\n");
      out.write("\t\t\t\t\tname=\"price\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p style=\"display:none;\">\n");
      out.write("\t\t\t\t<label for=\"percent\">目前折算比例(%)</label> <input type=\"text\"\n");
      out.write("\t\t\t\t\tid=\"percent\" name=\"percent\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p style=\"display:none;\">\n");
      out.write("\t\t\t\t<label for=\"level\">目前量级</label> <input type=\"text\" id=\"level\"\n");
      out.write("\t\t\t\t\tname=\"level\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"level-all\">全量量级</label> <input type=\"text\"\n");
      out.write("\t\t\t\t\tid=\"level-all\" name=\"level-all\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"comments\">备注信息</label> <input type=\"text\" id=\"comments\"\n");
      out.write("\t\t\t\t\tname=\"comments\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p style=\"display:none;\">\n");
      out.write("\t\t\t\t<label for=\"pk-name\">分包下载地址</label> <input type=\"text\" id=\"pk-name\"\n");
      out.write("\t\t\t\t\tname=\"pk-name\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<label for=\"where\">在哪儿能找到</label> <input type=\"text\" id=\"where\"\n");
      out.write("\t\t\t\t\tname=\"where\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p style=\"display:none;\">\n");
      out.write("\t\t\t\t<label for=\"date\">开始结算时间</label> <input type=\"text\" id=\"date\"\n");
      out.write("\t\t\t\t\tname=\"date\" style=\"height: 15px;\" />\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t\t<p>\n");
      out.write("\t\t\t\t<button id=\"y-edits-btn\" type=\"button\">Apply</button>\n");
      out.write("\t\t\t\t<button id=\"n-edits-btn\" type=\"button\">Cancel</button>\n");
      out.write("\t\t\t</p>\n");
      out.write("\t\t</form>\n");
      out.write("\t</div>\n");
      out.write("\t<script src=\"http://code.jquery.com/jquery-1.9.1.js\"></script>\n");
      out.write("\t<script src=\"http://code.jquery.com/ui/1.10.3/jquery-ui.js\"></script>\n");
      out.write("\t<script src=\"static/js/global.js\"></script>\n");
      out.write("\t<script src=\"static/js/in.js\"></script>\n");
      out.write("</body>");
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
