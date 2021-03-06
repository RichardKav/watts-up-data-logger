/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.tango.wattsup;

import java.io.File;
import java.io.IOException;
import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.event.WattsUpMemoryCleanEvent;
import wattsup.jsdk.core.event.WattsUpStopLoggingEvent;
import wattsup.jsdk.core.listener.WattsUpDataAvailableListener;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.listener.WattsUpMemoryCleanListener;
import wattsup.jsdk.core.listener.WattsUpStopLoggingListener;
import wattsup.jsdk.core.meter.WattsUp;

/**
 * This logs WattsUp meter data to disk in a continuous fashion.
 *
 */
public class Logger {

    private static WattsUp meter;

    public static void main(String[] args) throws IOException {
        final WattsUpLogger logger = new WattsUpLogger(new File("Dataset.csv"), false);
        new Thread(logger).start();
        String port = "COM9";
        if (args.length > 0) {
            port = args[0];
        }
        //Note: negative numbers for the schedule duration makes it run forever
        meter = new WattsUp(new WattsUpConfig().withPort(port).scheduleDuration(-1).withInternalLoggingInterval(1).withExternalLoggingInterval(1));
        System.out.println("WattsUp Meter Created");

        meter.registerListener(new WattsUpDataAvailableListener() {
            @Override
            public void processDataAvailable(final WattsUpDataAvailableEvent event) {
                WattsUpPacket[] values = event.getValue();
                logger.printToFile(values);
            }
        });

        meter.registerListener(new WattsUpMemoryCleanListener() {
            @Override
            public void processWattsUpReset(WattsUpMemoryCleanEvent event) {
                System.out.println("Memory Just Cleaned");
            }
        });

        meter.registerListener(new WattsUpStopLoggingListener() {
            @Override
            public void processStopLogging(WattsUpStopLoggingEvent event) {
                System.out.println("Logging Stopped");
                logger.stop();
            }
        });

        meter.registerListener(new WattsUpDisconnectListener() {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event) {
                System.out.println("Application Exiting Due to Disconnect");
                logger.stop();
            }
        });

        System.out.println("WattsUp Meter Connecting");
        meter.connect(true);
        meter.setLoggingModeSerial(1);
        System.out.println("WattsUp Meter Connected " + meter.isConnected());

    }
}
