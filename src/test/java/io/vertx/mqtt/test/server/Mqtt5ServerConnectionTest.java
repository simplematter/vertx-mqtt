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

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServerOptions;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.packet.MqttReturnCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;

/**
 * MQTT server testing about clients connection
 */
@RunWith(VertxUnitRunner.class)
public class Mqtt5ServerConnectionTest extends MqttServerBaseTest {

  private static final Logger log = LoggerFactory.getLogger(Mqtt5ServerConnectionTest.class);

  private MqttConnectReturnCode expectedReturnCode;

  private static final String MQTT_USERNAME = "username";
  private static final String MQTT_PASSWORD = "password";
  private static final int MQTT_TIMEOUT_ON_CONNECT = 5;

  private MqttEndpoint endpoint;

  @Before
  public void before(TestContext context) {

    MqttServerOptions options = new MqttServerOptions();
    options.setTimeoutOnConnect(MQTT_TIMEOUT_ON_CONNECT);

    this.setUp(context, options);
  }

  @After
  public void after(TestContext context) {

    this.tearDown(context);
  }

  @Test
  public void accepted(TestContext context) {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
      client.connect();
    } catch (MqttException e) {
      context.fail(e);
    }
  }

  @Test
  public void acceptedClientIdAutoGenerated(TestContext context) {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "", persistence);
      client.connect();
    } catch (MqttException e) {
      context.fail(e);
    }
  }

  @Test
  public void refusedIdentifierRejected(TestContext context) {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID;

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
      client.connect();
      context.fail();
    } catch (MqttException e) {
      context.assertEquals(e.getReasonCode(), MqttReturnCode.RETURN_CODE_IDENTIFIER_NOT_VALID);
    }
  }

  @Test
  public void refusedServerUnavailable(TestContext context) {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE_5;

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
      client.connect();
      context.fail();
    } catch (MqttException e) {
      context.assertEquals(e.getReasonCode(), MqttReturnCode.RETURN_CODE_SERVER_UNAVAILABLE);
    }
  }

  @Test
  public void refusedBadUsernamePassword(TestContext context) {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD;

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttConnectionOptions options = new MqttConnectionOptions();
      options.setUserName("wrong_username");
      options.setPassword("wrong_password".getBytes(StandardCharsets.UTF_8));
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
      client.connect(options);
      context.fail();
    } catch (MqttException e) {
      context.assertEquals(e.getReasonCode(), MqttReturnCode.RETURN_CODE_BAD_USERNAME_OR_PASSWORD);
    }
  }

  @Test
  public void refusedNotAuthorized(TestContext context) {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED_5;

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
      client.connect();
      context.fail();
    } catch (MqttException e) {
      context.assertEquals(e.getReasonCode(), MqttReturnCode.RETURN_CODE_NOT_AUTHORIZED);
    }
  }

  @Test
  public void connectionAlreadyAccepted(TestContext context) throws Exception {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;

    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "12345", persistence);
    client.connect();

    try {
      // try to accept a connection already accepted
      this.endpoint.accept(false);
      context.fail();
    } catch (IllegalStateException e) {
      // Ok
    }
  }

  @Test
  public void refusedClientIdZeroBytes(TestContext context) {

    this.expectedReturnCode = MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID;

    try {
      MemoryPersistence persistence = new MemoryPersistence();
      MqttConnectionOptions options = new MqttConnectionOptions();
      options.setCleanStart(false);
      MqttClient client = new MqttClient(String.format("tcp://%s:%d", MQTT_SERVER_HOST, MQTT_SERVER_PORT), "", persistence);
      client.connect(options);
      context.fail();
    } catch (MqttException e) {
      context.assertEquals(e.getReasonCode(), MqttReturnCode.RETURN_CODE_IDENTIFIER_NOT_VALID);
      context.assertNotNull(rejection);
    }
  }

  @Test
  public void noConnectSent(TestContext context) {

    NetClient client = this.vertx.createNetClient();
    Async async = context.async();

    client.connect(MQTT_SERVER_PORT, MQTT_SERVER_HOST, done -> {

      if (done.succeeded()) {

        done.result().closeHandler(v -> {
          log.info("No CONNECT sent in " + MQTT_TIMEOUT_ON_CONNECT + " secs. Closing connection.");
          async.complete();
        });

      } else {
        context.fail();
      }
    });

    // check that the async is completed (so connection was closed by server) in
    // the specified timeout (+500 ms just for being sure)
    async.await(500 + MQTT_TIMEOUT_ON_CONNECT * 1000);
    if (!async.isCompleted())
      context.fail();
  }

  @Override
  protected void endpointHandler(MqttEndpoint endpoint, TestContext context) {

    MqttConnectReturnCode returnCode = this.expectedReturnCode;

    switch (this.expectedReturnCode) {

      case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:

        returnCode =
          (endpoint.auth().getUsername().equals(MQTT_USERNAME) &&
            endpoint.auth().getPassword().equals(MQTT_PASSWORD)) ?
            MqttConnectReturnCode.CONNECTION_ACCEPTED :
            MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
        break;

      case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:

        returnCode = endpoint.protocolVersion() == MqttVersion.MQTT_5.protocolLevel() ?
          MqttConnectReturnCode.CONNECTION_ACCEPTED :
          MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
        break;
    }

    log.info("return code = " + returnCode);

    if (returnCode == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
      log.info("client id = " + endpoint.clientIdentifier());
      endpoint.accept(false);
    } else {
      endpoint.reject(returnCode);
    }

    this.endpoint = endpoint;
  }
}
