use std::env;
use std::error::Error;
use std::time::Duration;

use futures_lite::stream::StreamExt;
use lapin::{BasicProperties, Channel, Connection, ConnectionProperties, options::*, publisher_confirm::Confirmation, types::FieldTable};
use lapin::protocol::constants::REPLY_SUCCESS;
use log::error;
use reqwest::StatusCode;
use tokio::sync::broadcast;
use tokio::sync::broadcast::error::TryRecvError;
use tokio::sync::broadcast::Sender;
use tokio::time;

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    let mut counter = 1;
    let service2_address = env::var("SERVER_URL").expect("Environment variable SERVER_URL not found");

    // Setup a RabbitMQ connection and open a channel
    let connection = setup_rabbitmq_connection().await?;
    let channel = setup_rabbitmq_channel(&connection).await?;

    let (state_sender, mut state_receiver) = broadcast::channel::<String>(1);
    listen_to_state_queue(channel.clone(), state_sender.clone()).await?;

    let mut is_paused = false;
    loop {
        // Check if a new state was received
        match state_receiver.try_recv() {
            Ok(state) => {
                match state.as_str() {
                    "PAUSED" => {
                        println!("Received PAUSED. Pausing...");
                        is_paused = true;
                    }
                    "RUNNING" => {
                        println!("Received RUNNING. Resuming...");
                        is_paused = false;
                    }
                    "SHUTDOWN" => {
                        println!("Received SHUTDOWN. Stopping...");
                        break;
                    }
                    &_ => {
                        error!("Received illegal state: {}", state)
                    }
                }
            }
            Err(TryRecvError::Empty) => {
                // No new state, continue with regular processing
            }
            Err(TryRecvError::Closed) => {
                println!("State channel is closed");
                break; // Exit the loop and stop the service
            }
            Err(TryRecvError::Lagged(count)) => {
                println!("State channel lagged behind by {} messages", count);
            }
        }

        if !is_paused {
            let timestamp = chrono::Utc::now().to_rfc3339_opts(chrono::SecondsFormat::Millis, true);
            // Format message (ex: "1 2022-10-01T06:35:01.373Z 192.168.2.22:8000")
            let message = format!("SND {} {} {}", counter, timestamp, &service2_address);
            // Write the message to the message queue
            send_to_queue(&channel, "message", &message).await?;
            // Send the message to service 2 and log the result to the log queue
            send_to_queue(&channel, "log", &match send_message(&service2_address, &message).await {
                Ok(status) => { format!("{} {}", status.as_u16(), timestamp) }
                Err(error) => error.to_string()
            }).await?;
            // Increase the counter
            counter += 1;
        }

        // Wait for 2 seconds before the next iteration
        time::sleep(Duration::from_secs(2)).await;
    }

    // Write "SND STOP" to the log queue
    send_to_queue(&channel, "log", "SND STOP").await.unwrap();
    // Close the channel and connection
    channel.close(REPLY_SUCCESS, "Stopping").await.unwrap();
    connection.close(REPLY_SUCCESS, "Stopping").await.unwrap();
    Ok(())
}

async fn send_message(address: &String, message: &str) -> Result<StatusCode, Box<dyn Error>> {
    let response = reqwest::Client::new().post(format!("http://{}", address)).body(message.to_string()).send().await?;

    return if !response.status().is_success() {
        Err(format!("HTTP request failed with status code: {}", response.status()).into())
    } else {
        Ok(response.status())
    }
}

async fn setup_rabbitmq_connection() -> Result<Connection, Box<dyn Error>> {
    let host = env::var("RABBIT_MQ_HOST").unwrap_or("localhost".to_string());
    let port = env::var("RABBIT_MQ_PORT").unwrap_or("5672".to_string());
    let username = env::var("RABBIT_MQ_USERNAME").unwrap_or("guest".to_string());
    let password = env::var("RABBIT_MQ_PASSWORD").unwrap_or("guest".to_string());

    let address = format!("amqp://{username}:{password}@{host}:{port}");

    // Connect to RabbitMQ
    let connection = Connection::connect(
        &address,
        ConnectionProperties::default()
    ).await?;

    return Ok(connection);
}

async fn setup_rabbitmq_channel(connection: &Connection) -> Result<Channel, Box<dyn Error>> {
    // Open a channel on the connection
    let channel = connection.create_channel().await?;

    // Declare both queues
    channel.queue_declare("message", QueueDeclareOptions { durable: true, ..Default::default() }, FieldTable::default()).await?;
    channel.queue_declare("log", QueueDeclareOptions { durable: true, ..Default::default() }, FieldTable::default()).await?;
    channel.queue_declare("service1-state", QueueDeclareOptions { durable: true, ..Default::default() }, FieldTable::default()).await?;

    return Ok(channel);
}

pub async fn send_to_queue(channel: &Channel, routing_key: &str, content: &str) -> Result<Confirmation, Box<dyn Error>> {
    let confirmation = channel.basic_publish(
        "exchange",
        routing_key,
        BasicPublishOptions::default(),
        content.to_string().as_ref(),
        BasicProperties::default()
    ).await?.await?;

    return Ok(confirmation);
}

async fn listen_to_state_queue(channel: Channel, state_sender: Sender<String>) -> Result<(), Box<dyn Error>> {
    let mut consumer = channel.basic_consume(
        "service1-state",
        "state-consumer",
        BasicConsumeOptions::default(),
        FieldTable::default()
    ).await?;

    tokio::spawn(async move {
        while let Some(payload) = consumer.next().await {
            let delivery = payload.unwrap();

            let state = String::from_utf8(delivery.data.clone()).unwrap();
            state_sender.send(state).unwrap();
            delivery.ack(BasicAckOptions::default()).await.unwrap();
        }
    });

    Ok(())
}