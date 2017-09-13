package de.hapit.instanceextraction.awsmodel;

import lombok.Data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by phemmer on 07.02.17.
 */
@Data
public class AwsInstanceTypeInfo {

    @SerializedName("config")
    AwsInstanceTypeConfig config;
}
