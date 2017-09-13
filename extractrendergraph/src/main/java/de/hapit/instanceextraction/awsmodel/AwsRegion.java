package de.hapit.instanceextraction.awsmodel;

import lombok.Data;

import java.util.List;
import com.google.gson.annotations.SerializedName;

/**
 * Created by phemmer on 07.02.17.
 */
@Data
public class AwsRegion {

    @SerializedName("region")
    String name;

    @SerializedName("instanceTypes")
    List<AwsInstanceType> instanceTypeList;
}
