package com.flick.node.configuration;

import org.aeonbits.owner.ConfigFactory;

public class Config{ 

    private static final FlinkConfig FLICK_CONFIG = ConfigFactory.create(FlinkConfig.class);

    public static FlinkConfig get() {
        return FLICK_CONFIG;

    }
}
