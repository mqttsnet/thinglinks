package com.mqttsnet.thinglinks.rule;

import com.mqttsnet.thinglinks.common.core.mqs.SelectorConfig;
import com.mqttsnet.thinglinks.rule.common.consumer.kafka.RuleTriggerMessageKafkaConsumer;
import com.mqttsnet.thinglinks.rule.common.consumer.rocketmq.RuleTriggerMessageRocketmqConsumer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
class ThingLinksConfiguration {

    @Bean
    public ThingLinksBeanDefinitionRegistrar thingLinksBeanDefinitionRegistrar(Environment environment) {
        return new ThingLinksBeanDefinitionRegistrar(environment);
    }
}

public class ThingLinksBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private final Environment environment;

    public ThingLinksBeanDefinitionRegistrar(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Class<?> beanClass = getConsumerClass();
        String beanName = beanClass.getSimpleName();
        registerBeanDefinition(registry, beanClass, beanName);
    }

    private Class<?> getConsumerClass() {
        boolean selectorKafka = Boolean.parseBoolean(environment.getProperty(SelectorConfig.selectorKafkaKey, "false"));
        return selectorKafka ? RuleTriggerMessageKafkaConsumer.class :
                RuleTriggerMessageRocketmqConsumer.class;
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass, String beanName) {
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(beanClass).getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
