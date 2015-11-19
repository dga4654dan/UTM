package com.dc.utm.init;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class CommonInitParamInstacncesInitialize {

	protected final Object commonParam;
	
	@SuppressWarnings("rawtypes")
	protected final ArrayList<Class> classList = new ArrayList<Class>();
	
	public CommonInitParamInstacncesInitialize(Object commonParam) {
		
		this.commonParam = commonParam;
	}
	
	@SuppressWarnings("rawtypes")
	public void addClass(Class c) {
		
		classList.add(c);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void startInit() throws SecurityException, NoSuchMethodException, 
		IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		Class commInitParamClass = commonParam.getClass();
		
		Constructor constructor;
		ArrayList<Object> instanceList = new ArrayList<Object>();
		for( Class c : classList ) {
			
			constructor = c.getConstructor( commInitParamClass );
			instanceList.add( constructor.newInstance(commonParam) );
			
		}
		
		for( Object obj : instanceList ) {
			
			if( obj instanceof IInitializeOverListener ) {
				
				( (IInitializeOverListener)obj ).initializeOver(commonParam);
			}
			
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		
		CommonInitParamInstacncesInitialize i = new CommonInitParamInstacncesInitialize("123456");
		i.addClass(Test.class);
		
		i.startInit();
	}
	
}

class Test implements IInitializeOverListener<String> {
	
	public Test(String t) {
		
		System.out.println(t);
	}

	@Override
	public void initializeOver(String t) {

		System.out.println(t);
	}
	
	
}




