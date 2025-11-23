package com.dawn.aspect;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Slf4j
@Component
public class ExecutionTimeAspect {
    @Pointcut("execution(* com.dawn.controller.*.*(..))")
    public void executionPointCut() {}

    @Before("executionPointCut()")
    public void executionBefore(){
        log.debug("Logging before method execution");
    }

    @AfterReturning(pointcut = "executionPointCut()", returning = "result")
    public void executionAfter(JoinPoint joinPoint, Object result) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (HttpServletRequest) Objects.requireNonNull(requestAttributes).resolveReference(RequestAttributes.REFERENCE_REQUEST);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        String desc = (apiOperation != null) ? apiOperation.value() : "No description";
        log.debug("Method URI: {} , Method name: {}, Swagger Desc: {}, returning with : {}", Objects.requireNonNull(request).getRequestURI(), method.getName(), desc, result);
    }

}
