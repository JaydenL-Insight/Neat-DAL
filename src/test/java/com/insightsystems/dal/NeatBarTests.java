package com.insightsystems.dal;


import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

import org.junit.jupiter.api.*;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeatBarTests {
    private final String deviceIp = "192.168.0.161";
    Neat device;


    @BeforeEach
    public void initialiseDevice() throws Exception {
        device = new Neat();
        device.setHost(deviceIp);
        device.setProtocol("HTTPS");
        device.setLogin("admin");
        device.setPassword("19881988");
        device.init();
    }

    @Test
    @Order(2)
    public void trySetDeviceControllablePropertiesToKnownValues() throws Exception {
        device.getMultipleStatistics();
        device.controlProperty(new ControllableProperty("Updates#AutomaticUpdates","true",""));
        device.controlProperty(new ControllableProperty("Time#Use24HourTime","false",""));
        device.controlProperty(new ControllableProperty("AudioVideo#AutoPairingVolume","-35",""));
        device.controlProperty(new ControllableProperty("AudioVideo#CameraAntiBandingMode","default",""));
        device.controlProperty(new ControllableProperty("AudioVideo#MusicModeEnabled","false",""));
        device.controlProperty(new ControllableProperty("System#ShareAnalytics","true",""));
    }

    @Test
    @Order(3)
    public void confirmExtendedPropertiesAreValid() throws Exception {
        ExtendedStatistics extStats = (ExtendedStatistics) device.getMultipleStatistics().get(0);
        Map<String,String> stats = extStats.getStatistics();

        Assertions.assertEquals(stats.get("Ethernet#802.1xAuthenticationState"),"EMPTY");
        Assertions.assertEquals(stats.get("AudioVideo#MusicModeEnabled"),"false");
        Assertions.assertEquals(stats.get("System#Reboot"),"0");
        Assertions.assertEquals(stats.get("ProductName"),"Neat Bar");
        Assertions.assertEquals(stats.get("Wifi#DetailedState"),"Unknown");
        Assertions.assertEquals(stats.get("Time#Use24HourTime"),"false");
        Assertions.assertEquals(stats.get("Network#WiFiMacAddress"),"c4:63:fb:01:9f:78");
        Assertions.assertEquals(stats.get("AudioVideo#AutoPairingVolume"),"-35");
        Assertions.assertEquals(stats.get("deviceOnline"),"true");
        Assertions.assertEquals(stats.get("WifiSignalStrength"),"NONE");
        Assertions.assertEquals(stats.get("Wifi#State"),"DISCONNECTED");
        Assertions.assertEquals(stats.get("System#AutoWake"),"true");
        Assertions.assertEquals(stats.get("Language#Language"),"en-US");
        Assertions.assertEquals(stats.get("Ethernet#Ipv4Address"),"192.168.0.161");
        Assertions.assertEquals(stats.get("Wifi#isDisconnected"),"true");
        Assertions.assertEquals(stats.get("PrimaryMode"),"msteams");
        Assertions.assertEquals(stats.get("Ethernet#Ipv6Address"),"fe80::b871:4094:9fa3:c5e4");
        Assertions.assertEquals(stats.get("isPaired"),"false");
        Assertions.assertEquals(stats.get("Network#NtpServer"),"time.neat.no");
        Assertions.assertEquals(stats.get("Wifi#isSaved"),"true");
        Assertions.assertEquals(stats.get("Network#Mode"),"Dual");
        Assertions.assertEquals(stats.get("Wifi#isConnecting"),"false");
        Assertions.assertEquals(stats.get("Wifi#isConnected"),"false");
        Assertions.assertEquals(stats.get("System#ShareAnalytics"),"true");
        Assertions.assertEquals(stats.get("ProductNameRaw"),"barra");
        Assertions.assertEquals(stats.get("Updates#AutomaticUpdates"),"true");
        Assertions.assertEquals(stats.get("AudioVideo#UsbAudioEnabled"),"false");
        Assertions.assertEquals(stats.get("Updates#UpdateChanel"),"stable");
        Assertions.assertEquals(stats.get("FirmwareVersion"),"NFB1.20230504.0019");
        Assertions.assertEquals(stats.get("PairedDeviceName"),"NB12041000369");
        Assertions.assertEquals(stats.get("Network#EthernetMacAddress"),"c4:63:fb:01:2b:4d");
        Assertions.assertEquals(stats.get("Ethernet#SubnetMask"),"255.255.255.0");
        Assertions.assertEquals(stats.get("Wifi#Band"),"UNKNOWN");
        Assertions.assertEquals(stats.get("AudioVideo#CameraAntiBandingMode"),"default");
        Assertions.assertEquals(stats.get("Wifi#Ssid"),"FluxNet");
        Assertions.assertEquals(stats.get("SerialNumber"),"NB12041000369");
        Assertions.assertEquals(stats.get("Ethernet#Gateway"),"192.168.0.254");
        Assertions.assertEquals(stats.get("Wifi#SsidHidden"),"false");
        Assertions.assertEquals(stats.get("AudioVideo#DeepNoiseSupressionEabled"),"false");
        Assertions.assertEquals(stats.get("Wifi#isConnectedOrConnecting"),"false");
    }

    @Test
    @Order(4)
    public void confirmControllableProperties() throws Exception {
        device.getMultipleStatistics();
        device.controlProperty(new ControllableProperty("Updates#AutomaticUpdates","false",""));
        device.controlProperty(new ControllableProperty("Time#Use24HourTime","true",""));
        device.controlProperty(new ControllableProperty("AudioVideo#AutoPairingVolume","-40",""));
        device.controlProperty(new ControllableProperty("AudioVideo#CameraAntiBandingMode","60",""));
        device.controlProperty(new ControllableProperty("AudioVideo#MusicModeEnabled","true",""));
        device.controlProperty(new ControllableProperty("System#ShareAnalytics","false",""));


        Thread.sleep(2000);

        ExtendedStatistics extStats = (ExtendedStatistics) device.getMultipleStatistics().get(0);
        Map<String,String> stats = extStats.getStatistics();

        Assertions.assertEquals(stats.get("Updates#AutomaticUpdates"),"false");
        Assertions.assertEquals(stats.get("Time#Use24HourTime"),"true");
        Assertions.assertEquals(stats.get("AudioVideo#AutoPairingVolume"),"-40");
        Assertions.assertEquals(stats.get("AudioVideo#CameraAntiBandingMode"),"60");
        Assertions.assertEquals(stats.get("AudioVideo#MusicModeEnabled"),"true");
        Assertions.assertEquals(stats.get("System#ShareAnalytics"),"false");

        device.controlProperty(new ControllableProperty("Updates#AutomaticUpdates","true",""));
        device.controlProperty(new ControllableProperty("Time#Use24HourTime","false",""));
        device.controlProperty(new ControllableProperty("AudioVideo#AutoPairingVolume","-36",""));
        device.controlProperty(new ControllableProperty("AudioVideo#CameraAntiBandingMode","default",""));
        device.controlProperty(new ControllableProperty("AudioVideo#MusicModeEnabled","false",""));
        device.controlProperty(new ControllableProperty("System#ShareAnalytics","true",""));
    }

    @RepeatedTest(10)
    @Order(4)
    public void loadTestDeviceApi() throws Exception {
        device.getMultipleStatistics();
    }

    @Test
    @Order(5)
    public void tryIncorrectCredentials() throws Exception {
        Neat wrongDevice = new Neat();
        wrongDevice.setHost(deviceIp);
        wrongDevice.setProtocol("HTTPS");
        wrongDevice.setLogin("admin");
        wrongDevice.setPassword("not19881988");
        wrongDevice.init();

        Exception caughtException = null;
        try {
            wrongDevice.getMultipleStatistics();
        } catch(Exception e){
            caughtException = e;
        } finally {
            Assertions.assertNotNull(caughtException);
            Assertions.assertEquals("Login credentials are invalid!",caughtException.getMessage());
        }
    }

    @Test
    @Order(6)
    public void testRebootControl() throws Exception {
        ControllableProperty cp = new ControllableProperty("System#Reboot","1","null");
        device.getMultipleStatistics();
        device.controlProperty(cp);
        Thread.sleep(2_000L);
        Exception thrown = null;
        try{
            device.getMultipleStatistics();
        } catch (Exception e){
            thrown = e;
        } finally {
            Assertions.assertNotNull(thrown);
            Assertions.assertEquals("Cannot reach resource at HTTPS://"+deviceIp+":443/api/v1/status",thrown.getMessage());
        }
    }
}
