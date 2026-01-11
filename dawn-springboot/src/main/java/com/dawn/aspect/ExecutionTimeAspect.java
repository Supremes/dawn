package com.dawn.aspect;

import com.dawn.constant.MetricsConstant;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
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
    private final MeterRegistry meterRegistry;

    ExecutionTimeAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Pointcut("execution(* com.dawn.controller.*.*(..))")
    public void executionPointCut() {}

    @Around("executionPointCut()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // 计数请求总数
        Counter.builder(MetricsConstant.REQUEST_COUNT)
                .description("Controller 请求数")
                .tag("status", "all")
                .register(meterRegistry)
                .increment();

        // 获取请求信息
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (HttpServletRequest) Objects.requireNonNull(requestAttributes).resolveReference(RequestAttributes.REFERENCE_REQUEST);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String uri = request.getRequestURI();
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        String desc = (apiOperation != null) ? apiOperation.value() : "No description";

        // 开始计时
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功的请求
            log.debug("Method URI: {}, Method name: {}, Swagger Desc: {}, returning with: {}", uri, methodName, desc, result);
            
            // 成功请求计数
            Counter.builder(MetricsConstant.REQUEST_OK_COUNT)
                    .description("Controller 请求成功总数")
                    .tag("method_name", methodName)
                    .tag("uri", uri)
                    .tag("status", "success")
                    .register(meterRegistry)
                    .increment();
            
            // 记录成功请求的执行时间
            sample.stop(Timer.builder(MetricsConstant.REQUEST_DURATION)
                    .description("Controller 请求执行时间")
                    .tag("method_name", methodName)
                    .tag("uri", uri)
                    .tag("status", "success")
                    .publishPercentiles(0.5, 0.95, 0.99)  // P50, P95, P99
                    .publishPercentileHistogram()
                    .register(meterRegistry));
            
            return result;
            
        } catch (Throwable e) {
            // 记录异常请求
            String exceptionType = e.getClass().getSimpleName();
            
            Counter.builder(MetricsConstant.REQUEST_EXCEPTION_COUNT)
                    .description("Controller 请求异常总数")
                    .tag("method_name", methodName)
                    .tag("uri", uri)
                    .tag("status", "error")
                    .tag("exception", exceptionType)
                    .register(meterRegistry)
                    .increment();
            
            // 记录失败请求的执行时间
            sample.stop(Timer.builder(MetricsConstant.REQUEST_DURATION)
                    .description("Controller 请求执行时间")
                    .tag("method_name", methodName)
                    .tag("uri", uri)
                    .tag("status", "error")
                    .tag("exception", exceptionType)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .register(meterRegistry));
            
            log.error("Method: {} threw exception: {}", methodName, exceptionType, e);
            throw e;
        }
    }

}
