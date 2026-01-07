package com.dawn.aspect;

import com.dawn.constant.MetricsConstant;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    ExecutionTimeAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Pointcut("execution(* com.dawn.controller.*.*(..))")
    public void executionPointCut() {}

    @Before("executionPointCut()")
    public void executionBefore(){
        Counter.builder(MetricsConstant.REQUEST_COUNT)
                .description("Controller 请求数")
                .tag("status", "all")
                .register(meterRegistry)
                .increment();
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

        Counter.builder(MetricsConstant.REQUEST_OK_COUNT)  // 指标名称
                .description("Controller 请求成功总数")
                .tag("method_name", method.getName())    // 标签1：按方法名分类
                .tag("uri", Objects.requireNonNull(request).getRequestURI())                   // 标签2：按 URI 分类
                .tag("status", "success")          // 标签3：标记为成功
                .register(meterRegistry)           // 注册到容器
                .increment();                      // 计数 +1
    }

    @AfterThrowing(pointcut = "executionPointCut()", throwing = "e")
    public void countException(JoinPoint joinPoint, Throwable e) {
        String methodName = joinPoint.getSignature().getName();

        Counter.builder(MetricsConstant.REQUEST_EXCEPTION_COUNT) // 使用相同的指标名
                .description("Controller 请求异常总数")
                .tag("method_name", methodName)
                .tag("status", "error")           // 标签不同：标记为失败
                .tag("exception", e.getClass().getSimpleName()) // 记录是什么异常
                .register(meterRegistry)
                .increment();
    }

}
