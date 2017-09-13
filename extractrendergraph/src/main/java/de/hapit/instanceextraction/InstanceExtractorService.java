package de.hapit.instanceextraction;

import de.hapit.instanceextraction.awsmodel.AwsInstanceType;
import de.hapit.instanceextraction.awsmodel.AwsInstanceTypeInfo;
import de.hapit.instanceextraction.awsmodel.AwsModel;
import de.hapit.instanceextraction.awsmodel.AwsPrice;
import de.hapit.instanceextraction.awsmodel.AwsRegion;
import de.hapit.instanceextraction.model.Instance;
import de.hapit.instanceextraction.model.InstanceInfo;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class InstanceExtractorService {
    private static final Logger LOG = LoggerFactory.getLogger(InstanceExtractorService.class);
    // following link is in the source of "https://aws.amazon.com/ec2/pricing/on-demand/"
    private static final String AWS_URL = "https://a0.awsstatic.com/pricing/1/ec2/linux-od.min.js";
    private static final String AWS_REGION = "us-east-1";
    //    private final static List<String> AWS_TYPE_LIST = Arrays.asList(new String[]{"generalCurrentGen", "computeCurrentGen"}); // not added types: gpuCurrentGen, hiMemCurrentGen, storageCurrentGen
    private static final List<String> AWS_TYPE_LIST = Arrays.asList("generalCurrentGen", "computeCurrentGen", "gpuCurrentGen", "hiMemCurrentGen", "storageCurrentGen");

    public InstanceInfo extractInstanceInfoFromAWS(String fileName) throws IOException {
        String s = getInstanceJsonAsString();

        JsonArray instanceTypeListJson = new JsonArray();
        List<Instance> instanceList = new ArrayList<>();

        AwsInstanceTypeInfo instanceTypeInfo = new Gson().fromJson(s, AwsInstanceTypeInfo.class);

        for (AwsRegion region : instanceTypeInfo.getConfig().getRegions()) {
            if (AWS_REGION.equals(region.getName())) {
                for (AwsInstanceType instanceType : region.getInstanceTypeList()) {
                    if (AWS_TYPE_LIST.contains(instanceType.getName())) {
                        for (AwsModel awsModel : instanceType.getModelList()) {
                            instanceList.add(convertAwsModel(awsModel));
                        }
                    }
                }
                break;
            }
        }

        InstanceInfo instanceInfo = new InstanceInfo();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        instanceInfo.setDate(df.format(new Date()));
        instanceInfo.setInstanceTypes(instanceList);

        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("Writing AWS instance list to Json file: %s", fileName));
        }
        try (Writer writer = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(instanceInfo, writer);
        }

        return instanceInfo;
    }

    private Instance convertAwsModel(AwsModel awsModel) {
        Instance instance = new Instance();

        instance.setType(awsModel.getName().substring(0, awsModel.getName().indexOf('.')));
        instance.setName(awsModel.getName());
        instance.setVCpu(Integer.parseInt(awsModel.getVCpu()));
        instance.setMemory(Double.parseDouble(awsModel.getMemory()));
        instance.setStorage(awsModel.getStorage());

        String ecuString = awsModel.getEcu();
        double ecu = 0;
        if (!"variable".equals(ecuString)) {
            ecu = Double.parseDouble(ecuString);
        }
        instance.setEcu(ecu);

        for (AwsPrice awsPrice : awsModel.getPrices()) {
            if (awsPrice != null && "linux".equals(awsPrice.getName())) {
                String priceInUSD = awsPrice.getCurrency().getPriceInUSD();
                instance.setPrice(Double.parseDouble(priceInUSD));
            }
        }

        return instance;
    }

    private static String getInstanceJsonAsString() throws IOException {
        URL oracle = new URL(AWS_URL);
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
        String jsString = sb.toString();

        // remove js part to get json
        return jsString.substring(jsString.indexOf("callback(") + 9, jsString.length() - 2);
    }
}
