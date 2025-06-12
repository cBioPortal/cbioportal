import os
import shutil
import subprocess
import psutil
import time
import logging
import pandas as pd
import argparse
from concurrent.futures import ThreadPoolExecutor

# Configure logging
logging.basicConfig(filename='curl_monitoring.log', level=logging.INFO, format='%(asctime)s - %(message)s')

def log_resource_usage(process, data, study_id, interval, random_id=0):
    """Log CPU and memory usage of the given process and store it in a shared DataFrame."""
    try:
        while True:
            cpu_usage = process.cpu_percent(interval=interval)
            memory_info = process.memory_info()
            memory_usage_mb = memory_info.rss / (1024 * 1024)
            pid = process.pid  # Get the process ID
            
            # Log to file with PID
            logging.info(f'Study ID: {study_id}, PID: {pid}, CPU Usage: {cpu_usage:.3f}%, Memory Usage: {memory_usage_mb:.3f} MB')
            
            # Append to data list
            data.append({
                'Study_ID': study_id,
                'PID': pid,
                'Timestamp': time.time(),
                'CPU_Usage': round(cpu_usage, 3),
                'Memory_Usage_MB': round(memory_usage_mb, 3),
                'Random_ID': random_id
            })
    except psutil.NoSuchProcess:
        logging.info(f'Study ID: {study_id}, PID: {process.pid}, Process has terminated.')

def run_curl_command(command, study_id, interval, shared_data):
    """Run a curl command and log its resource usage."""
    # Record start time
    start_time = time.time()
    
    import numpy as np
    rand_id = np.random.randint(10000)

    shutil.rmtree('out/tmp', ignore_errors=True)
    os.makedirs('out/tmp', exist_ok=True)
    
    command.insert(1, '-o')
    command.insert(2, f'out/tmp/{rand_id}_{study_id}.zip')
    
    # Start the curl process
    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    psutil_process = psutil.Process(process.pid)

    # Monitor resource usage
    log_resource_usage(psutil_process, shared_data, study_id, interval, rand_id)
    
    # Wait for the process to complete
    stdout, stderr = process.communicate()
    
    # Record end time and calculate total export time
    end_time = time.time()
    total_time = end_time - start_time
    logging.info(f'Study ID: {study_id}, PID: {process.pid}, Total Export Time: {total_time:.2f} seconds, Random ID: {rand_id}')
    
    # Log process output
    if stdout:
        logging.info(f'Study ID: {study_id}, PID: {process.pid}, STDOUT: {stdout.decode()}')
    if stderr:
        logging.error(f'Study ID: {study_id}, PID: {process.pid}, STDERR: {stderr.decode()}')

def delete_zip_files(study_ids):
    """Delete all downloaded .zip files for the given study IDs."""
    for study_id in study_ids:
        zip_file = f"{study_id}.zip"
        if os.path.exists(zip_file):
            os.remove(zip_file)
            logging.info(f"Deleted file: {zip_file}")
        else:
            logging.warning(f"File not found, could not delete: {zip_file}")

if __name__ == '__main__':
    # Parse command-line arguments
    parser = argparse.ArgumentParser(description="Monitor CPU and memory usage of curl commands in parallel.")
    parser.add_argument('--study_ids', type=str, nargs='+', required=True, help="List of study IDs for the exports.")
    parser.add_argument('--interval', type=float, default=1.0, help="Interval (in seconds) for logging resource usage.")
    args = parser.parse_args()

    study_ids = args.study_ids
    interval = args.interval

    # Generate the output CSV filename by concatenating all study IDs
    output_csv = f"out/resource_usage/{'-'.join(study_ids)}.csv"

    # Move downloaded .zip files to a folder named after the CSVs
    output_folder = f"out/zips/{'-'.join(study_ids)}"
    
    log_file_name = f"out/logs/{'-'.join(study_ids)}.log"

    if len(output_csv) > 250:
        cropped_study_ids = [x[:8] for x in study_ids]
        output_csv = f"out/resource_usage/{'-'.join(cropped_study_ids)}.csv"
        output_folder = f"out/zips/{'-'.join(cropped_study_ids)}"
        log_file_name = f"out/logs/{'-'.join(cropped_study_ids)}.log"

    logging.info(f'Starting monitoring for Study IDs: {study_ids} with interval: {interval} seconds')
    logging.info(f'Output CSV file will be: {output_csv}')

    # Shared data list to store logs from all processes
    shared_data = []

    # Run commands in parallel
    with ThreadPoolExecutor() as executor:
        commands = [['curl', '-O', f'http://localhost:8081/export/study/{study_id}.zip'] for study_id in study_ids]
        futures = [executor.submit(run_curl_command, command, study_id, interval, shared_data) for command, study_id in zip(commands, study_ids)]

    # Wait for all processes to complete
    for future in futures:
        future.result()

    # Save all logs to a single CSV file
    df = pd.DataFrame(shared_data)
    df.to_csv(output_csv, index=False)

    logging.info(f'All resource usage data saved to {output_csv}')

    # Move all .zip files from the out/tmp folder to the output_folder
    os.makedirs(output_folder, exist_ok=True)
    for file_name in os.listdir('out/tmp'):
        if file_name.endswith('.zip'):
            shutil.move(os.path.join('out/tmp', file_name), os.path.join(output_folder, file_name))
            logging.info(f"Moved file {file_name} to {output_folder}")

    # Rename the log file to include the name of the CSVs
    os.makedirs(os.path.dirname(log_file_name), exist_ok=True)
    os.rename('curl_monitoring.log', log_file_name)
    logging.info(f"Log file renamed to {log_file_name}")

# python3 src/monitor.py --study_id ov_tcga_pub ov_tcga_pub ov_tcga_pub ov_tcga_pub ov_tcga_pub ov_tcga_pub ov_tcga_pub ov_tcga_pub --interval 2

# curl -o out/lala.zip -O http://localhost:8081/export/study/pancan_pcawg_2020.zip      

# curl -O http://localhost:8081/export/study/pancan_pcawg_2020.zip      