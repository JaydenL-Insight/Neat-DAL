package com.insightsystems.dal;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.ping.Pingable;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.insightsystems.dal.DTO.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.insightsystems.dal.NeatConstants.*;

public class Neat extends RestCommunicator implements Monitorable, Pingable, Controller {
    private String token = "";
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private long rateLimitTimeout = 0;
    public Neat(){
        this.setBaseUri(BASE_URI);
        this.setAuthenticationScheme(AuthenticationScheme.None);
        this.setTrustAllCertificates(true);
    }
    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        ExtendedStatistics extStats = new ExtendedStatistics();
        Map<String,String> stats = new HashMap<>();
        List<AdvancedControllableProperty> controls = new ArrayList<>();

        //Check if we are currently on a timeout for too many bad requests
        if (rateLimitTimeout > System.currentTimeMillis()) {
            if (this.logger.isWarnEnabled()){
                this.logger.warn("Rate limit active. Returning early.");
            }
            stats.put("Error","Too many invalid requests!");
            extStats.setStatistics(stats);
            return Collections.singletonList(extStats);
        }

        reentrantLock.lock();
        if (token.isEmpty()){
            authenticate();
        }
        try {
            StatusDTO status;

            try{
                status = doGet(STATUS_URI, StatusDTO.class);
            } catch (FailedLoginException e){ //Authenticate and retry
                if (this.logger.isDebugEnabled()){
                    this.logger.debug("Unauthorised, attempting manual authentication.");
                }
                authenticate();
                status = doGet(STATUS_URI, StatusDTO.class);
            }

            if (status == null) {
                throw new RuntimeException("Received status object is null. None or invalid properties received.");
            }

            stats.put("deviceOnline", String.valueOf(status.isOnline));
            stats.put("isPaired", String.valueOf(status.isPaired));
            stats.put("FirmwareVersion", status.firmwareVersion);
            stats.put("PrimaryMode", status.primaryMode);
            stats.put("SerialNumber", status.serialNumber);
            stats.put("PairedDeviceName", status.pairedDeviceName);

            stats.put("ProductNameRaw", status.product);
            if (PRODUCT_MAP.containsKey(status.product)) {
                stats.put("ProductName", PRODUCT_MAP.get(status.product));
            }

            stats.put("Network#EthernetMacAddress", status.ethernetMacAddress);
            stats.put("Network#WiFiMacAddress", status.wifiMacAddress);
            stats.put("Network#NtpServer", status.ntpServer);

            if (status.networkProperties != null) {
                stats.put("Network#Mode", status.networkProperties.mode);
                if (status.networkProperties.ethernet != null) {
                    stats.put("Ethernet#802.1xAuthenticationState", status.networkProperties.ethernet.dot1xState);
                    stats.put("Ethernet#Gateway", status.networkProperties.ethernet.gateway);
                    stats.put("Ethernet#Ipv4Address", status.networkProperties.ethernet.ip4Address);
                    stats.put("Ethernet#Ipv6Address", status.networkProperties.ethernet.ip6Address);
                    stats.put("Ethernet#SubnetMask", status.networkProperties.ethernet.subnetMask);
                } else if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Ethernet property is null or missing from status request");
                }

                if (status.networkProperties.wifi != null) {
                    stats.put("Wifi#isSaved", String.valueOf(status.networkProperties.wifi.isSaved));
                    stats.put("Wifi#Ssid", status.networkProperties.wifi.ssid);
                    stats.put("WifiSignalStrength", status.networkProperties.wifi.signalStrength);
                    if (status.networkProperties.wifi.network != null) {
                        stats.put("Wifi#Band", status.networkProperties.wifi.network.band);
                        stats.put("Wifi#DetailedState", status.networkProperties.wifi.network.detailedState);
                        stats.put("Wifi#SsidHidden", String.valueOf(status.networkProperties.wifi.network.hidden$1));
                        stats.put("Wifi#isConnected", String.valueOf(status.networkProperties.wifi.network.isConnected));
                        stats.put("Wifi#isConnectedOrConnecting", String.valueOf(status.networkProperties.wifi.network.isConnectedOrConnecting));
                        stats.put("Wifi#isDisconnected", String.valueOf(status.networkProperties.wifi.network.isDisconnected));
                        stats.put("Wifi#isConnecting", String.valueOf(status.networkProperties.wifi.network.isConnecting));
                        stats.put("Wifi#State", status.networkProperties.wifi.network.state);
                    } else if (this.logger.isTraceEnabled()) {
                        this.logger.trace("Wifi.network property is null or missing from status request");
                    }
                } else if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Wifi property is null or missing from status request");
                }

            } else if (this.logger.isWarnEnabled()) {
                this.logger.warn("Unable to retrieve network properties. Object is null or property name is incorrect");
            }

            AudioVideoDTO av = null;

            try {
                av = doGet(AV_URI, AudioVideoDTO.class);
            } catch (Exception ignored) {
            } //Ignore exception in the case that this device does not support av settings

            if (av != null) {
                if (av.autoPairingVolume != null) {
                    addSlider(stats, controls, "AudioVideo#AutoPairingVolume", -50, -30, av.autoPairingVolume);
                }

                stats.put("AudioVideo#DeepNoiseSupressionEabled", String.valueOf(av.deepNoiseSuppressionEnabled));
                stats.put("AudioVideo#UsbAudioEnabled", String.valueOf(av.usbAudioDevicesEnabled));
            }

            SystemDTO system = doGet(SYSTEM_URI, SystemDTO.class);
            if (!status.product.equals("arran")) {
                addSwitch(stats, controls, "System#ShareAnalytics", Boolean.FALSE.equals(system.shareAnalyticsDisabled));
                if (av != null) {
                    addDropdown(stats, controls, "AudioVideo#CameraAntiBandingMode", ANTIBANDING_MODES, av.cameraAntiBanding);
                    addSwitch(stats, controls, "AudioVideo#MusicModeEnabled", Boolean.TRUE.equals(av.musicModeEnabled));
                }
            }
            if (status.product.equals("barra")) { //This property is only supported by specific products
                addSwitch(stats, controls, "System#AutoWake", Boolean.TRUE.equals(system.autoWakeEnabled));
            }

            UpdatesDTO updates = doGet(UPDATES_URI, UpdatesDTO.class);
            if (updates.type != null) {
                boolean automaticUpdates = updates.type.equalsIgnoreCase("AUTOMATIC");
                addSwitch(stats, controls, "Updates#AutomaticUpdates", automaticUpdates);
                if (automaticUpdates) {
                    addDropdown(stats, controls, "Updates#UpdateChanel", UPDATE_CHANNEL_MAP, updates.channel);
                } else {
                    stats.put("Updates#UpdateChanel", updates.channel);
                }
            }

            TimeDTO time = doGet(TIME_URI, TimeDTO.class);
            if (time.timezone != null) {
                addDropdown(stats, controls, "Time#Timezone", TIMEZONE_MAP, time.timezone.id);
            }
            addSwitch(stats, controls, "Time#Use24HourTime", Boolean.TRUE.equals(time.use24HourTimeFormat));

            LanguageDTO language = doGet(LANGUAGE_URI, LanguageDTO.class);
            addDropdown(stats, controls, "Language#Language", LANGUAGE_MAP, language.languageCode);

            //Add Reboot control
            AdvancedControllableProperty.Button reboot = new AdvancedControllableProperty.Button();
            reboot.setGracePeriod(20_000L);
            reboot.setLabel("Reboot Device");
            reboot.setLabelPressed("Rebooting Device...");
            stats.put("System#Reboot", "0");
            controls.add(new AdvancedControllableProperty("System#Reboot", new Date(), reboot, "0"));

            extStats.setStatistics(stats);
        } finally{
            reentrantLock.unlock();
        }
        return Collections.singletonList(extStats);
    }

    @SuppressWarnings("SameParameterValue")
    private static void addSlider(Map<String, String> stats, List<AdvancedControllableProperty> controls, String name, int min, int max, int currentValue) {
        AdvancedControllableProperty.Slider slider = new AdvancedControllableProperty.Slider();
        slider.setLabelStart(String.valueOf(min));
        slider.setLabelStart(String.valueOf(max));
        slider.setRangeStart((float) min);
        slider.setRangeEnd((float) max);

        stats.put(name, String.valueOf(currentValue));
        controls.add(new AdvancedControllableProperty(name,new Date(),slider,(float)currentValue));
    }
    private static void addDropdown(Map<String,String> stats,List<AdvancedControllableProperty> controls,String name,Map<String,String> map, String currentValue){
        AdvancedControllableProperty.DropDown dropdown = new AdvancedControllableProperty.DropDown();
        dropdown.setLabels(map.keySet().toArray(new String[0]));
        dropdown.setOptions(map.values().toArray(new String[0]));

        stats.put(name,currentValue);
        controls.add(new AdvancedControllableProperty(name,new Date(),dropdown,currentValue));
    }

    private static void addSwitch(Map<String,String> stats,List<AdvancedControllableProperty> controls,String name,boolean currentValue){
        AdvancedControllableProperty.Switch toggle = new AdvancedControllableProperty.Switch();
        toggle.setLabelOn("On");
        toggle.setLabelOff("Off");
        stats.put(name,currentValue ? "true":"false");
        controls.add(new AdvancedControllableProperty(name,new Date(),toggle,currentValue ? "true":"false"));
    }

    @Override
    protected void authenticate() throws Exception {
        MessageDigest digester = MessageDigest.getInstance("SHA-512");
        byte[] usernameHash = digester.digest(this.getLogin().getBytes(StandardCharsets.UTF_8));
        byte[] passwordHash = digester.digest(this.getPassword().getBytes(StandardCharsets.UTF_8));
        String body = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", bytesToHexString(usernameHash),bytesToHexString(passwordHash));

        try {
            String authResponse = doPost(LOGIN_URI, body);
            if (!authResponse.isEmpty()) {
                token = authResponse;
            }
        } catch (CommandFailureException e){
            if (e.getMessage().contains("404")) {
                throw new FailedLoginException("Login credentials are invalid!");
            } else if (e.getMessage().contains("429")){
                rateLimitTimeout = System.currentTimeMillis() + 300_000L; // Set timeout for 5 minutes
            }
            throw e;
        }
    }

    @Override
    protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) {
        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT){
            headers.add("Content-Type","application/json;charset=UTF-8");
            headers.add("Accept","application/json, text/plain, */*");
        }
        if (!token.isEmpty()){
            headers.add("Authorization","Bearer " + token);
        }
        headers.add("Host",this.getHost());
        return headers;
    }

    private static String bytesToHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder(hash.length *2);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws Exception {
        Neat dev = new Neat();
        dev.setHost("192.168.0.157");
        dev.setProtocol("HTTPS");
        dev.setLogin("admin");
        dev.setPassword("19881988");
        dev.init();
        dev.authenticate();
        dev.getMultipleStatistics().forEach((d)-> ((ExtendedStatistics)d).getStatistics().forEach((k,v)-> System.out.println(k+ ": " + v)));
    }

    @Override
    public void controlProperty(ControllableProperty cp) throws Exception {
        //Check if we are currently on a timeout for too many bad requests
        if (rateLimitTimeout > System.currentTimeMillis()) {
            if (this.logger.isWarnEnabled()){
                this.logger.warn("Rate limit active. Returning early.");
            }
            return;
        }

        reentrantLock.lock();
        try {
            switch (cp.getProperty()) {
                case "System#Reboot":
                    doPut(REBOOT_URI, null);
                    break;
                case "Language#Language":
                    doPut(LANGUAGE_URI, new LanguageDTO(cp.getValue().toString()));
                    break;
                case "Time#Use24HourTime":
                    doPut(TIME_URI, new TimeDTO(cp.getValue().toString().equalsIgnoreCase("true")));
                    break;
                case "Time#Timezone":
                    doPut(TIME_URI, new TimeDTO(cp.getValue().toString()));
                    break;
                case "Updates#UpdateChanel":
                    doPut(UPDATES_URI, new UpdatesDTO(cp.getValue().toString(), null));
                    break;
                case "Updates#AutomaticUpdates":
                    doPut(UPDATES_URI, new UpdatesDTO(null, UPDATE_TYPE_MAP.get(cp.getValue().toString().equalsIgnoreCase("true"))));
                    break;
                case "System#AutoWake":
                    doPut(SYSTEM_URI, new SystemDTO(cp.getValue().toString().equalsIgnoreCase("true"), null));
                    break;
                case "System#ShareAnalytics":
                    doPut(SYSTEM_URI, new SystemDTO(null, cp.getValue().toString().equalsIgnoreCase("false")));
                    break;
                case "AudioVideo#AutoPairingVolume":
                    doPut(AV_URI, new AudioVideoDTO((int)Float.parseFloat(cp.getValue().toString())));
                    break;
                case "AudioVideo#CameraAntiBandingMode":
                    doPut(AV_URI, new AudioVideoDTO(cp.getValue().toString()));
                    break;
                case "AudioVideo#MusicModeEnabled":
                    doPut(AV_URI, new AudioVideoDTO(cp.getValue().toString().equalsIgnoreCase("true")));
                    break;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void controlProperties(List<ControllableProperty> list) throws Exception {
        for (ControllableProperty cp : list)
            controlProperty(cp);
    }
}