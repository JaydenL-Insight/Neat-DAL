package com.insightsystems.dal;

import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

import org.junit.jupiter.api.*;

import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeatPadTests {
    private final String deviceIp = "192.168.0.157";
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
        device.controlProperty(new ControllableProperty("Updates#AutomaticUpdates", "true", ""));
        device.controlProperty(new ControllableProperty("Time#Use24HourTime", "false", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#AutoPairingVolume", "-36", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#CameraAntiBandingMode", "default", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#MusicModeEnabled", "false", ""));
        device.controlProperty(new ControllableProperty("System#ShareAnalytics", "true", ""));
    }

    @Test
    @Order(3)
    public void confirmExtendedPropertiesAreValid() throws Exception {
        ExtendedStatistics extStats = (ExtendedStatistics) device.getMultipleStatistics().get(0);
        Map<String, String> stats = extStats.getStatistics();

        Assertions.assertEquals(stats.get("Ethernet#802.1xAuthenticationState"), "EMPTY");
        Assertions.assertEquals(stats.get("System#Reboot"), "0");
        Assertions.assertEquals(stats.get("ProductName"), "Neat Pad");
        Assertions.assertEquals(stats.get("Time#Use24HourTime"), "false");
        Assertions.assertEquals(stats.get("Network#WiFiMacAddress"), "c4:63:fb:01:a9:8b");
        Assertions.assertEquals(stats.get("deviceOnline"), "true");
        Assertions.assertEquals(stats.get("Language#Language"), "en-US");
        Assertions.assertEquals(stats.get("Ethernet#Ipv4Address"), "192.168.0.157");
        Assertions.assertEquals(stats.get("PrimaryMode"), "msteams");
        Assertions.assertEquals(stats.get("Ethernet#Ipv6Address"), "fe80::533a:504c:62af:9417");
        Assertions.assertEquals(stats.get("isPaired"), "true");
        Assertions.assertEquals(stats.get("Network#NtpServer"), "time.neat.no");
        Assertions.assertEquals(stats.get("Network#Mode"), "Dual");
        Assertions.assertEquals(stats.get("ProductNameRaw"), "arran");
        Assertions.assertEquals(stats.get("Updates#AutomaticUpdates"), "true");
        Assertions.assertEquals(stats.get("AudioVideo#UsbAudioEnabled"), "false");
        Assertions.assertEquals(stats.get("Updates#UpdateChanel"), "stable");
        Assertions.assertEquals(stats.get("FirmwareVersion"), "NFA1.20230504.0202");
        Assertions.assertEquals(stats.get("PairedDeviceName"), "NB12041000369");
        Assertions.assertEquals(stats.get("Network#EthernetMacAddress"), "c4:63:fb:01:17:cc");
        Assertions.assertEquals(stats.get("Ethernet#SubnetMask"), "255.255.255.0");

        Assertions.assertEquals(stats.get("SerialNumber"), "NA12041001858");
        Assertions.assertEquals(stats.get("Ethernet#Gateway"), "192.168.0.254");
        Assertions.assertEquals(stats.get("AudioVideo#DeepNoiseSupressionEabled"), "false");

        Assertions.assertNull(stats.get("AudioVideo#MusicModeEnabled"));
        Assertions.assertNull(stats.get("AudioVideo#CameraAntiBandingMode"));
    }

    @Test
    @Order(4)
    public void confirmControllableProperties() throws Exception {
        device.getMultipleStatistics();
        device.controlProperty(new ControllableProperty("Updates#AutomaticUpdates", "false", ""));
        device.controlProperty(new ControllableProperty("Time#Use24HourTime", "true", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#AutoPairingVolume", "-40", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#CameraAntiBandingMode", "60", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#MusicModeEnabled", "true", ""));
        device.controlProperty(new ControllableProperty("System#ShareAnalytics", "false", ""));


        Thread.sleep(2000);

        ExtendedStatistics extStats = (ExtendedStatistics) device.getMultipleStatistics().get(0);
        Map<String, String> stats = extStats.getStatistics();

        Assertions.assertEquals(stats.get("Updates#AutomaticUpdates"), "false");
        Assertions.assertEquals(stats.get("Time#Use24HourTime"), "true");

        Assertions.assertNull(stats.get("System#ShareAnalytics"));
        Assertions.assertNull(stats.get("AudioVideo#AutoPairingVolume"));
        Assertions.assertNull(stats.get("AudioVideo#CameraAntiBandingMode"));
        Assertions.assertNull(stats.get("AudioVideo#MusicModeEnabled"));

        device.controlProperty(new ControllableProperty("Updates#AutomaticUpdates", "true", ""));
        device.controlProperty(new ControllableProperty("Time#Use24HourTime", "false", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#AutoPairingVolume", "-36", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#CameraAntiBandingMode", "default", ""));
        device.controlProperty(new ControllableProperty("AudioVideo#MusicModeEnabled", "false", ""));
        device.controlProperty(new ControllableProperty("System#ShareAnalytics", "true", ""));
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
        } catch (Exception e) {
            caughtException = e;
        } finally {
            Assertions.assertNotNull(caughtException);
            Assertions.assertEquals("Login credentials are invalid!", caughtException.getMessage());
        }
    }

    @Test
    @Order(6)
    public void testRebootControl() throws Exception {
        ControllableProperty cp = new ControllableProperty("System#Reboot", "1", "null");
        device.getMultipleStatistics();
        device.controlProperty(cp);
        Thread.sleep(2_000L);
        Exception thrown = null;
        try {
            device.getMultipleStatistics();
        } catch (Exception e) {
            thrown = e;
        } finally {
            Assertions.assertNotNull(thrown);
            Assertions.assertEquals("Cannot reach resource at HTTPS://" + deviceIp + ":443/api/v1/status", thrown.getMessage());
        }
    }
}
