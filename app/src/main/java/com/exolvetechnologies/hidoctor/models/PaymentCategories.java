package com.exolvetechnologies.hidoctor.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ekeretepeter on 22/02/16.
 */
public class PaymentCategories {
    @SerializedName("chat_type")
    String name;

    @SerializedName("description")
    String description;

    @SerializedName("rate")
    String rate;

    @SerializedName("bundle_amount")
    String bundleAmount;

    public String getName() {
        return name.toUpperCase() + " Chat";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return "N" + description.substring(7);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getBundleAmount() {
        return bundleAmount;
    }

    public void setBundleAmount(String bundleAmount) {
        this.bundleAmount = bundleAmount;
    }
}
