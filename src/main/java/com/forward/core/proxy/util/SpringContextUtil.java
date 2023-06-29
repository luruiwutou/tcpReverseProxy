package com.forward.core.proxy.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Locale;


/**
 * spring工具类 方便在非spring管理环境中获取bean
 *
 * */
@Component
public class SpringContextUtil implements ApplicationContextAware {
  private static ApplicationContext context;
 
  @Override
  public void setApplicationContext(ApplicationContext contex)
    throws BeansException
  {
	  SpringContextUtil.context = contex;
  }

  public static ApplicationContext getApplicationContext() {
      return context; 
  } 
  
  public static Object getBean(String beanName) {
    return context.getBean(beanName);
  }
 
  public static String getMessage(String key) {
    return context.getMessage(key, null, Locale.getDefault());
  }
}