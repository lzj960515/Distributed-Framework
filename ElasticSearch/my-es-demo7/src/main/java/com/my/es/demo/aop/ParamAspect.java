package com.my.es.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Aspect
@Component
public class ParamAspect {

/*    @Around(value = "@annotation(paramInsert)")
    public Object log(ProceedingJoinPoint joinPoint, ParamInsert paramInsert) throws Throwable {
        final Object proceed = joinPoint.proceed();
        return proceed;
    }*/

    @Before(value = "@annotation(paramInsert)")
    public void  methodBefore(JoinPoint joinPoint, ParamInsert paramInsert) {
        String methodName = joinPoint.getSignature().getName();
        final Object[] args = joinPoint.getArgs();

        System.out.println("执行目标方法 【" + methodName + "】 的【前置通知】，入参：" + Arrays.toString(joinPoint.getArgs()));
    }
}
