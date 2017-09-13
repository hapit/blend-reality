package de.hapit.instanceextraction.model;

import lombok.Data;

import java.util.List;

@Data
public class InstanceInfo {

    String date;
    List<Instance> instanceTypes;

}
