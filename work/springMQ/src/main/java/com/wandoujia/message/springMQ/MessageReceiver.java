package com.wandoujia.message.springMQ;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

import javax.jms.JMSException;

import javax.jms.TextMessage;

public class MessageReceiver {

	// 这里用的是单元测试来写的，执行时需要引入junit4的jar包，如果嫌麻烦，直接改成main方法也行
	@Test
	public void Receive() throws JMSException {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		JmsTemplate template = (JmsTemplate) ctx.getBean("jmsTemplate");
		Destination destination = (Destination) ctx.getBean("destination");
		while (true) {
			TextMessage msg = (TextMessage) template.receive(destination);
			if (null != msg)
				System.out.println("收到消息内容为: " + msg.getText());
			else
				break;

		}
	}
}