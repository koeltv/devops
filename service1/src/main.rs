use std::env;
use std::error::Error;
use std::time::Duration;

use amqprs::BasicProperties;
use amqprs::callbacks::{DefaultChannelCallback, DefaultConnectionCallback};
use amqprs::channel::{BasicPublishArguments, Channel, QueueBindArguments, QueueDeclareArguments};
use amqprs::connection::{Connection, OpenConnectionArguments};
use reqwest::StatusCode;
use tokio::time;

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    let mut counter = 1;
    let service2_address = env::var("SERVER_URL").expect("Environment variable SERVER_URL not found");

    // Setup a RabbitMQ connection and open a channel
    let connection = setup_rabbitmq_connection().await;
    let channel = setup_rabbitmq_channel(&connection).await;

    for _ in 0..20 {
        let timestamp = chrono::Utc::now().to_rfc3339_opts(chrono::SecondsFormat::Millis, true);
        // Format message (ex: "1 2022-10-01T06:35:01.373Z 192.168.2.22:8000")
        let message = format!("SND {} {} {}", counter, timestamp, &service2_address);
        // Write the message to the message queue
        send_to_queue(&channel, "message", &message).await.unwrap();
        // Send the message to service 2 and log the result to the log queue
        send_to_queue(&channel, "log", &match send_message(&service2_address, &message).await {
            Ok(status) => { format!("{} {}", status.as_u16(), timestamp) }
            Err(error) => error.to_string()
        }).await.unwrap();
        // Increase the counter
        counter += 1;
        // Wait for 2 seconds before the next iteration
        time::sleep(Duration::from_secs(2)).await;
    }

    // Write "SND STOP" to the log queue
    send_to_queue(&channel, "log", "SND STOP").await.unwrap();
    // Close the channel and connection
    channel.close().await.unwrap();
    connection.close().await.unwrap();
    // Wait until stopped
    time::sleep(Duration::MAX).await;
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

async fn setup_rabbitmq_connection() -> Connection {
    let rabbitmq_host = env::var("RABBIT_MQ_HOST").unwrap_or("localhost".to_string());
    let rabbitmq_port = env::var("RABBIT_MQ_PORT").unwrap_or("5672".to_string());
    let rabbitmq_username = env::var("RABBIT_MQ_USERNAME").unwrap_or("guest".to_string());
    let rabbitmq_password = env::var("RABBIT_MQ_PASSWORD").unwrap_or("guest".to_string());

    // Connect to RabbitMQ
    let connection = Connection::open(&OpenConnectionArguments::new(
        &*rabbitmq_host,
        rabbitmq_port.parse().unwrap(),
        &*rabbitmq_username,
        &*rabbitmq_password,
    )).await.unwrap();

    connection.register_callback(DefaultConnectionCallback).await.unwrap();

    return connection;
}

async fn setup_rabbitmq_channel(connection: &Connection) -> Channel {
    // Open a channel on the connection
    let channel = connection.open_channel(None).await.unwrap();
    channel.register_callback(DefaultChannelCallback).await.unwrap();

    // Declare both queues
    let (message_queue, _, _) = channel.queue_declare(QueueDeclareArguments::durable_client_named("message")).await.unwrap().unwrap();
    let (log_queue, _, _) = channel.queue_declare(QueueDeclareArguments::durable_client_named("log")).await.unwrap().unwrap();

    // Bind the queues to exchange
    const MAX_RETRIES: usize = 5;
    let mut retry_count = 0;
    loop {
        let message_queue_bound = match channel.queue_bind(QueueBindArguments::new(&message_queue, "exchange", &message_queue)).await {
            Ok(_) => { true }
            Err(err) => {
                eprintln!("Error setting up RabbitMQ {} channel: {}", &message_queue, err);
                false
            }
        };
        let log_queue_bound = match channel.queue_bind(QueueBindArguments::new(&log_queue, "exchange", &log_queue)).await {
            Ok(_) => { true }
            Err(err) => {
                eprintln!("Error setting up RabbitMQ {} channel: {}", &log_queue,  err);
                false
            }
        };
        if message_queue_bound && log_queue_bound { return channel }
        else { retry_count += 1 }

        if retry_count >= MAX_RETRIES { panic!("Unable to establish RabbitMQ channels."); }
    }
}

pub async fn send_to_queue(channel: &Channel, routing_key: &str, content: &str) -> Result<(), impl Error> {
    return channel.basic_publish(
        BasicProperties::default(),
        content.to_string().into_bytes(),
        BasicPublishArguments::new("exchange", routing_key),
    ).await;
}