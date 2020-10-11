package org.dicontainer.di;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.handler.MessageContext.Scope;

import org.dicontainer.annotations.CustomQualifier;
import org.dicontainer.annotations.CustomScope;
import org.dicontainer.annotations.Inject;
import org.dicontainer.annotations.Service;
import org.dicontainer.constants.ScopeType;

public class DIContext {
	public static final String JAVA_BINARY_EXTENSION = ".class";
    private final Set<Object> serviceInstances = new HashSet<Object>();
    private Map<String, Object> serviceQualifierInstances;
    private static Set<Class<?>> allClassesInPackage;
    private Map<Class<?>, Object> SingletonserviceInstances = new HashMap<Class<?>, Object>();
    
    public DIContext()
    {
    	
    }
    
    
    public DIContext createContext(Class path) throws Exception {
    	String test  = path.getProtectionDomain().getCodeSource().getLocation().getFile();
    	allClassesInPackage = new HashSet<Class<?>>();
    	
   
    	File file = new File(test);
    	if (!file.isDirectory())
    	{
    		
    	}
    	for (File subfile: file.listFiles())
    	{
    		scanDir(subfile, "");
    	}
        return DIContext.createContextForPackage();
    }
    
    
    private void scanDir(File file, String packageName) throws ClassNotFoundException {
        if (file.isDirectory()) {
            packageName += file.getName() + ".";

            for (File innerFile : file.listFiles()) {
                this.scanDir(innerFile, packageName);
            }
        } else {
            if (!file.getName().endsWith(JAVA_BINARY_EXTENSION)) {
                return;
            }

            final String className = packageName + file.getName().replace(JAVA_BINARY_EXTENSION, "");

            this.allClassesInPackage.add(Class.forName(className, true, Thread.currentThread().getContextClassLoader()));
        }
    }
    


    public static DIContext createContextForPackage(String rootPackageName) throws Exception {
        Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
        for(Class<?> aClass : allClassesInPackage){
        	System.out.println("aClass = "+aClass.getName());
            if(aClass.isAnnotationPresent(Service.class)){
                serviceClasses.add(aClass);
            }
        }
        return new DIContext(serviceClasses);
    }
    
    public static DIContext createContextForPackage() throws Exception {
        Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
        for(Class<?> aClass : allClassesInPackage){
        	if(aClass.isAnnotationPresent(Service.class)){
                serviceClasses.add(aClass);
            }
            
        }
        
        return new DIContext(serviceClasses);
    }


    public DIContext(Collection<Class<?>> serviceClasses) throws Exception {
        // create an instance of each service class
    	serviceQualifierInstances = new HashMap<String, Object>();
        for(Class<?> serviceClass : serviceClasses){
        	Constructor<?> constructor = serviceClass.getConstructor();
            constructor.setAccessible(true);
            
            Object serviceInstance = constructor.newInstance();
            
            if((serviceClass.isAnnotationPresent(CustomScope.class)) && (serviceClass.getAnnotation(CustomScope.class).value().equals(ScopeType.SINGLETON)))
            {
            		if (SingletonserviceInstances.containsKey(serviceClass)){
		            	serviceInstance = SingletonserviceInstances.get(serviceClass);
            		}
            		else
            		{
            			SingletonserviceInstances.put(serviceClass, serviceInstance);
            		}
            }
            
        	if(serviceClass.isAnnotationPresent(CustomQualifier.class)){
                CustomQualifier c = serviceClass.getAnnotation(CustomQualifier.class);
                String val = c.value();
                serviceQualifierInstances.put(val, serviceInstance);
              }
            
            this.serviceInstances.add(serviceInstance);
        	
        }
        // wire them together
        for(Object serviceInstance : this.serviceInstances){

            for(Field field : serviceInstance.getClass().getDeclaredFields()){
                if(!field.isAnnotationPresent(Inject.class)){
                    continue;
                }
                
                Class<?> fieldType = field.getType();
                field.setAccessible(true);
                
                
                // find a suitable matching service instance
                for(Object matchPartner : this.serviceInstances){               	
                    if(fieldType.isInstance(matchPartner)){
                    	if (field.isAnnotationPresent(CustomQualifier.class))
                    	{
                    		CustomQualifier c = field.getAnnotation(CustomQualifier.class);
                    		field.set(serviceInstance, serviceQualifierInstances.get(c.value()));
                    	}
                    	else
                        field.set(serviceInstance, matchPartner);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getServiceInstance(Class<T> serviceClass){
        for(Object serviceInstance : this.serviceInstances){
            if(serviceClass.isInstance(serviceInstance)){
                return (T)serviceInstance;
            }
        }
        return null;
    }

}
