package com.martin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.BarrierSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.store.MessageGroup;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.springframework.integration.IntegrationMessageHeaderAccessor.CORRELATION_ID;

@IntegrationComponentScan @EnableIntegration
@ComponentScan
public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(Main.class);
        ctx.refresh();

        ctx.getBean(UpCaseService.class).upCaseStrings(Arrays.asList("1", "2", "3", "4", "5")).forEach(s -> log.info("Output: {}", s));
        ctx.close();
    }

    @Bean
    public IntegrationFlow upcase() {
        return f->f
                .split()
                .log()
                .barrier(500, new Consumer<BarrierSpec>() {
                    @Override
                    public void accept(BarrierSpec spec) {
                        spec.correlationStrategy(new HeaderAttributeCorrelationStrategy(CORRELATION_ID));
                        spec.outputProcessor(new MessageGroupProcessor() {
                            @Override
                            public Object processMessageGroup(MessageGroup messageGroup) {
                                log.info("barrier outputProcessor");
                                return messageGroup.getMessages();
                            }
                        });
                        spec.order(1);
                    }
                })
                .transform(s -> s + "!")
                .log()
                .aggregate()
                ;
    }


}
