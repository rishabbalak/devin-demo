package com.costcodemo.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The runnable application.
 *
 * <p>Hosts both tiers in one process: the 5250 terminal at {@code /wms} and the REST API
 * under {@code /api}. They share a single datasource on purpose — the point of the
 * reference build is that there is one system of record, not a modern copy of one.
 */
@SpringBootApplication
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
    }
}
