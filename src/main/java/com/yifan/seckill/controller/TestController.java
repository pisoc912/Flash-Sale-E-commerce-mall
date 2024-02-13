package com.yifan.seckill.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Controller
@Slf4j
public class TestController {

    @ResponseBody
    @RequestMapping("hello")
    public String hello() throws BlockException {
        String result;
        try(Entry entry = SphU.entry("HelloResource")){
            result = "Hello Sentinel";
            return result;
        } catch (BlockException blockException){
            log.error(blockException.toString());
            result="System busy, try later";
            return result;
        }
    }

    /**
     * Define current limiting rules
     * 1. Create a collection to store current limiting rules
     * 2. Create current limiting rules
     * 3. Put the current limiting rules into the collection
     * 4. Load current limiting rules
     * @PostConstruct is executed after the constructor of the current class is executed.
     */
    @PostConstruct
    public void seckillsFlow(){
        // 1. Create a collection to store current limiting rules
        List<FlowRule> rules = new ArrayList<>();

        // 2. Create current limiting rules
        FlowRule flowRule1 = new FlowRule();

        // Define resource
        flowRule1.setResource("seckills");

        // Define limiting rules,QPS Type
        flowRule1.setGrade(RuleConstant.FLOW_GRADE_QPS);

        // Define the number of requests passed by QPS per second
        flowRule1.setCount(1);

        FlowRule flowRule2 = new FlowRule();

        flowRule2.setGrade(RuleConstant.FLOW_GRADE_QPS);

        flowRule2.setCount(2);

        flowRule2.setResource("HelloResouces");

        // 3. Put the current limiting rules into the collection
        rules.add(flowRule1);
        rules.add(flowRule2);

        // 4. Load current limiting rules
        FlowRuleManager.loadRules(rules);
    }
}
