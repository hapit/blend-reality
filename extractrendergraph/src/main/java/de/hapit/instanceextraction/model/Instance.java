package de.hapit.instanceextraction.model;

import lombok.Data;

@Data
public class Instance {
    String type;
    String name;
    String storage;
    int vCpu;
    double ecu;
    double memory;
    double price;
}
