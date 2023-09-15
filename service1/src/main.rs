use std::env;
use std::fs::File;
use std::io::Write;
use std::time::Duration;
use tokio::{fs, time};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let mut counter = 1;
    let service2_address = env::var("SERVER_URL").expect("Environment variable SERVER_URL not found");

    // Create the directory and file if they don't exists
    fs::create_dir_all("./logs").await?;
    let mut log_file = File::create("./logs/service1.log")?;
    // Ensure that the file is initially empty
    log_file.set_len(0)?;

    for _ in 0..20 {
        let timestamp = chrono::Utc::now().to_rfc3339_opts(chrono::SecondsFormat::Millis, true);
        // Format message (ex: "1 2022-10-01T06:35:01.373Z 192.168.2.22:8000")
        let message = format!("{} {} {}", counter, timestamp, &service2_address);
        // Write the message to the log file
        writeln!(log_file, "{}", message)?;
        // Send the message to service 2
        if let Err(err) = send_message(&service2_address, &message).await {
            // If sending fails, write the error message to the file
            writeln!(log_file, "Error sending message: {}", err)?;
        }
        // Increase the counter
        counter += 1;
        // Wait for 2 seconds before the next iteration
        time::sleep(Duration::from_secs(2)).await;
    }

    // Write "STOP" to the file and send it to service 2
    writeln!(log_file, "STOP")?;
    send_message(&service2_address, "STOP").await?;
    // Close the file and exit
    log_file.flush()?;
    Ok(())
}

async fn send_message(address: &String, message: &str) -> Result<(), Box<dyn std::error::Error>> {
    let response = reqwest::Client::new().post(format!("http://{}", address)).body(message.to_string()).send().await?;

    if !response.status().is_success() {
        return Err(format!("HTTP request failed with status code: {}", response.status()).into());
    }

    Ok(())
}
