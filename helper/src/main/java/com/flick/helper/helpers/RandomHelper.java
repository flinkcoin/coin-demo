package com.flick.helper.helpers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class RandomHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RandomHelper.class);

    public static SecureRandom get() {
        SecureRandom instance = null;
        try {
            instance = SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("Stange error: {}!", ex);
        }
        return instance;
    }
}
