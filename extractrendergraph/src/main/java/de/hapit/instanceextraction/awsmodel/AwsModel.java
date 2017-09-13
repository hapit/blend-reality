package de.hapit.instanceextraction.awsmodel;

import lombok.Data;

import java.util.List;
import com.google.gson.annotations.SerializedName;

@Data
public class AwsModel {
    @SerializedName("size")
    String name;
    @SerializedName("vCPU")
    String vCpu;
    @SerializedName("ECU")
    String ecu;
    @SerializedName("memoryGiB")
    String memory;
    @SerializedName("storageGB")
    String storage;
    @SerializedName("valueColumns")
    List<AwsPrice> prices;

}
