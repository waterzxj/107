package room107.aop;

import java.lang.reflect.Method;

import lombok.extern.apachecommons.CommonsLog;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import room107.util.web.DebugUtils;

/**
 * @author WangXiao
 * need add <aop:aspectj-autoproxy expose-proxy="true" /> into spring properties.
 */
@CommonsLog
@Aspect
public class MockAspect {

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void service() {
    }

    @Around("service()")
    public Object mock(ProceedingJoinPoint joinPoint) throws Throwable {
        if (DebugUtils.isDebugEnabled()) {
            Object target = joinPoint.getTarget();
            if (target != null) {
                // find mock class
                String mockClassName = target.getClass().getCanonicalName()
                        + "Mock";
                Class<?> mockClass = null;
                try {
                    mockClass = Class.forName(mockClassName);
                } catch (ClassNotFoundException e) { // don't mock
                    log.debug("No mock class: " + mockClassName);
                    return joinPoint.proceed();
                }
                Object mockObject = mockClass.newInstance();
                String methodName = joinPoint.getSignature().getName();
                Method[] methods = mockClass.getMethods();
                for (Method method : methods) {
                    if (method.getName().equals(methodName)) {
                        try {
                            return method.invoke(mockObject,
                                    joinPoint.getArgs());
                        } catch (IllegalArgumentException e) {
                            continue; // meet overloading
                        } catch (Exception e) {
                            throw e.getCause();
                        }
                    }
                }
                log.debug("No mock method: " + joinPoint.getSignature());
            }
        }
        return joinPoint.proceed();
    }

}
