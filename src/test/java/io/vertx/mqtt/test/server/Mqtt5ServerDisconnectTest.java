/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.mqtt.test.server;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttEndpoint;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientException;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.packet.MqttReturnCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * MQTT server testing about clients disconnections
 */
@RunWith(VertxUnitRunner.class)
public class Mqtt5ServerDisconnectTest extends MqttServerBaseTest {

  private static final Logger log = LoggerFactory.getLogger(Mqtt5ServerDisconnectTest.class);

  @Before
  public void before(TestContext context) {

    this.setUp(context);
  }

  @After
  public void after(TestContext context) {

    this.tearDown(context);
  }

  @Test
  public void disconnect(TestContext context) {

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
      client.connect();
      client.disconnect();
      context.assertTrue(true);
    } catch (MqttException e) {
      context.assertTrue(false);
      e.printStackTrace();
    }
  }

  @Test
  public void bruteDisconnect(TestContext context) {

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
      client.connect();
      client.close();
      context.assertTrue(false);
    } catch (MqttException e) {
      context.assertEquals(e.getReasonCode(), (int)MqttClientException.REASON_CODE_CLIENT_CONNECTED);
      e.printStackTrace();
    }
  }

  @Override
  protected void endpointHandler(MqttEndpoint endpoint, TestContext context) {

    endpoint.disconnectHandler(v -> {

      log.info("MQTT remote client disconnected");
    });

    endpoint.closeHandler(v -> {

      log.info("MQTT remote client connection closed");
    });

    endpoint.accept(false);
  }
}
