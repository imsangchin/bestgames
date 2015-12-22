package com.wandoujia.message.springMQ;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.springframework.jms.core.JmsTemplate;

import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;

import javax.jms.JMSException;

import javax.jms.Message;

import javax.jms.Session;

public class MessageSender implements MessageCreator{
    
    @Test
    public void send(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
    
        JmsTemplate template = (JmsTemplate) ctx.getBean("jmsTemplate");
    
        Destination destination = (Destination) ctx.getBean("destination");
    
        template.send(destination, this);//此处的this是一个MessageCreator的类型，实质上调用的是MessageCreator的createMessage的方法
        //也可以用内部类来写，会更清晰,就不需要实现MessageCreator
        
        /*
        template.send(destination, new MessageCreator(){

            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("sender发送消息！");
            }
            
        });*/
        System.out.println("发送JMS消息成功");
    }

    public Message createMessage(Session session) throws JMSException {
        return session.createTextMessage("sender发送消息！");
    }
    
    
    
    
}