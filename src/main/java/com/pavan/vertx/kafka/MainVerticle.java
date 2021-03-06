/**
 * 
 */
package com.pavan.vertx.kafka;

import java.io.File;
import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author pk015603
 *
 */
public class MainVerticle extends AbstractVerticle {

	  public static void main(String[] args) {
	    Vertx vertx = Vertx.vertx();
	    vertx.deployVerticle(new MainVerticle());
	  }

	  private KafkaCluster kafkaCluster;

	  @Override
	  public void start() throws Exception {

	    // Kafka setup for the example
	    File dataDir = Testing.Files.createTestingDirectory("cluster");
	    dataDir.deleteOnExit();
	    kafkaCluster = new KafkaCluster()
	      .usingDirectory(dataDir)
	      .withPorts(2181, 9092)
	      .addBrokers(1)
	      .deleteDataPriorToStartup(true)
	      .startup();

	    // Deploy the dashboard
	    @SuppressWarnings("unchecked")
		JsonObject consumerConfig = new JsonObject((Map) kafkaCluster.useTo()
	      .getConsumerProperties("the_group", "the_client", OffsetResetStrategy.LATEST));
	    vertx.deployVerticle(
	      DashboardVerticle.class.getName(),
	      new DeploymentOptions().setConfig(consumerConfig)
	    );

	    // Deploy the metrics collector : 3 times
	    @SuppressWarnings("unchecked")
		JsonObject producerConfig = new JsonObject((Map) kafkaCluster.useTo()
	      .getProducerProperties("the_producer"));
	    vertx.deployVerticle(
	      MetricsVerticle.class.getName(),
	      new DeploymentOptions().setConfig(producerConfig).setInstances(1)
	    );
	  }

	  @Override
	  public void stop() throws Exception {
	    kafkaCluster.shutdown();
	  }
	}
