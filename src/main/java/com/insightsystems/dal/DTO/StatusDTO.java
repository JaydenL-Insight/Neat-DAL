package com.insightsystems.dal.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties
public class StatusDTO implements Serializable {
    public String ethernetMacAddress;
    public String firmwareVersion;
    public boolean isOnline;
    public boolean isPaired;
    public NetPropertiesDTO networkProperties;
    public String ntpServer;
    public String pairedDeviceName;
    public String primaryMode;
    public String product;
    public String serialNumber;
    public String wifiMacAddress;
}
