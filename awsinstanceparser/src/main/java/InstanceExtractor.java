import model.AwsInstanceType;
import model.AwsInstanceTypeInfo;
import model.AwsModel;
import model.AwsPrice;
import model.AwsRegion;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class InstanceExtractor {
    private final static Logger LOGGER = LoggerFactory.getLogger(InstanceExtractor.class);
    // following link is in the source of "https://aws.amazon.com/ec2/pricing/on-demand/"
    private final static String AWS_URL = "https://a0.awsstatic.com/pricing/1/ec2/linux-od.min.js";
    private final static String AWS_REGION = "us-east-1";
//    private final static List<String> AWS_TYPE_LIST = Arrays.asList(new String[]{"generalCurrentGen", "computeCurrentGen"}); // not added types: gpuCurrentGen, hiMemCurrentGen, storageCurrentGen
    private final static List<String> AWS_TYPE_LIST = Arrays.asList(new String[]{"generalCurrentGen", "computeCurrentGen", "gpuCurrentGen", "hiMemCurrentGen", "storageCurrentGen"});

    public static void main(String[] args) throws IOException {
        String s = getInstanceJsonAsString();

        JsonArray instanceTypeListJson = new JsonArray();

        AwsInstanceTypeInfo instanceTypeInfo = new Gson().fromJson(s, AwsInstanceTypeInfo.class);

        for (AwsRegion region : instanceTypeInfo.getConfig().getRegions()) {
            if (AWS_REGION.equals(region.getName())) {
                for (AwsInstanceType instanceType : region.getInstanceTypeList()) {
                    if (AWS_TYPE_LIST.contains(instanceType.getName())) {
                        for (AwsModel awsModel : instanceType.getModelList()) {
                            instanceTypeListJson.add(convertAwsModel(awsModel));
                        }
                    }
                }
                break;
            }
        }

        JsonObject instanceTypesJson = new JsonObject();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        instanceTypesJson.addProperty("date", df.format(new Date()));
        instanceTypesJson.add("instanceTypes", instanceTypeListJson);

        String fileName = "instanceTypes.json";
        LOGGER.info("Writing AWS instance list to Json file: " + fileName);
        try (Writer writer = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(instanceTypesJson, writer);
        }
    }

    private static JsonElement convertAwsModel(AwsModel awsModel) {
        JsonObject convertedModel = new JsonObject();

        convertedModel.addProperty("type", awsModel.getName().substring(0, awsModel.getName().indexOf('.')));
        convertedModel.addProperty("name", awsModel.getName());
        convertedModel.addProperty("vCPU", awsModel.getVCpu());
        convertedModel.addProperty("memory", awsModel.getMemory());
        convertedModel.addProperty("storage", awsModel.getStorage());
        convertedModel.addProperty("ecu", awsModel.getEcu());

        for (AwsPrice awsPrice : awsModel.getPrices()) {
            if (awsPrice.getName().equals("linux")) {
                String priceInUSD = awsPrice.getCurrency().getPriceInUSD();
                convertedModel.addProperty("price", priceInUSD);
            }
        }

        return convertedModel;
    }

    private static String getInstanceJsonAsString() throws IOException {
        URL oracle = new URL(AWS_URL);
        StringBuffer sb = new StringBuffer();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
        String jsString = sb.toString();

        // remove js part to get json
        String json = jsString.substring(jsString.indexOf("callback(") + 9, jsString.length() - 2);

        return json;
    }
}
