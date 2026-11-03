#!/usr/bin/env python3

from urllib.parse import urlparse
import argparse
import base64
import http.client
import json
import os
import time
import sys

SLEEP_DURATION_BETWEEN_RETRIES = 60
MAXIMUM_NUMBER_OF_RETRIES = 5
MAXIMUM_SLEEP_WAITING_FOR_RATELIMIT_RESET = 5 * 60

def create_arg_parser():
    usage = "usage: %prog destination_directory [--github_branch_name=<branch_name>] [--source_filename=<source_filename>]"
    parser = argparse.ArgumentParser(
                    prog='download_clickhouse_sql_scripts_py3.py',
                    description='Downloads one or more .sql files (by default, only the cbioportal derived table population script) from the /src/main/resources/db_scripts/clickhouse directory of the cBioPortal github repository.')
    parser.add_argument('destination_directory', help = "local filesystem directory where downloads will be stored")
    parser.add_argument('--github_branch_name', default = "master")
    parser.add_argument('--source_filename', default = "populate_derived_tables.sql", help='Default is to download "populate_derived_tables.sql" but another filename can be specified, or use "*" to download all files ending in .sql')
    return parser

def exit_if_args_are_invalid(args):
    if not os.path.isdir(args.destination_directory):
        sys.exit("destination_directory argument (" + args.destination_directory + ") does not exist or is not a directory")
    if not args.source_filename == '*' and not args.source_filename.endswith('.sql'):
        sys.exit("source_filename argument (" + args.source_filename + ") must either be '*' or be a filename ending with .sql")

def request_failed(response):
    return response.status != http.HTTPStatus.OK

def ratelimit_is_available(response):
    remaining_requests = response.getheader('x-ratelimit-remaining')
    return not remaining_requests == None and int(remaining_requests) > 0

def get_ratelimit_reset_time(response):
    return response.getheader('x-ratelimit-reset')

def request_via_http_with_retry(connection_host_name, request_string, retry_limit):
    github_user_agent = f'python http.client(v{sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro})'
    github_headers = {"Host" : connection_host_name,
            "User-Agent" : github_user_agent,
            "Accept" : "application/vnd.github+json",
            "X-GitHub-Api-Version" : "2022-11-28"}
    for attempt_number in range(retry_limit):
        # make request, try to get response
        conn = http.client.HTTPSConnection(connection_host_name, timeout=16)
        conn.request("GET", request_string, headers = github_headers)
        response = conn.getresponse()
        if request_failed(response):
            if not ratelimit_is_available(response):
                ratelimit_reset_time = get_ratelimit_reset_time(response)
                if not ratelimit_reset_time == None:
                    required_sleep_duration = int(float(ratelimit_reset_time) - time.time() + 1)
                    if required_sleep_duration > MAXIMUM_SLEEP_WAITING_FOR_RATELIMIT_RESET:
                        print(f"rate limit reset occurs {required_sleep_duration} seconds from now. This is more than the maximum allowable sleep time of {MAXIMUM_SLEEP_WAITING_FOR_RATELIMIT_RESET} seconds. ... exiting")
                        sys.exit(1)
                    if attempt_number < retry_limit - 1:
                        print(f"because of a request rate limit, this program must now sleep for {required_sleep_duration} seconds until the next reset")
                        time.sleep(sleep_duration_seconds)
                        continue
                    else:
                        print(f'warning : request to host {connection_host_name} with url string {request_string} failed.')
                        continue
            if attempt_number < retry_limit - 1:
                sleep_duration_seconds = 60
                print(f"retrying request after {sleep_duration_seconds} seconds pause.")
                time.sleep(sleep_duration_seconds)
                continue
            else:
                # retries exhausted
                print(f"giving up after {retry_limit} requests failed")
                break
        else:
            return response
    # no response was obtained
    return None

def file_should_be_downloaded(github_filename, source_filename):
    if source_filename == '*':
        return github_filename.endswith('.sql')
    return github_filename == source_filename

def download_files_from_github(args):
    GITHUB_HOST_NAME = "api.github.com"
    GITHUB_API_PATH = "/repos/cBioPortal/cbioportal/contents/src/main/resources/db-scripts/clickhouse"
    github_query_string = f'ref={args.github_branch_name}'
    github_request_string = f'{GITHUB_API_PATH}?{github_query_string}'
    directory_content_response = request_via_http_with_retry(GITHUB_HOST_NAME, github_request_string, MAXIMUM_NUMBER_OF_RETRIES)
    if not directory_content_response:
        sys.exit(1)
    directory_content = json.loads(directory_content_response.read().decode("utf-8"))
    files_to_be_downloaded = [x for x in directory_content if file_should_be_downloaded(os.path.basename(urlparse(x["url"]).path).casefold(), args.source_filename.casefold())]
    destination_directory_path = os.path.normpath(args.destination_directory)
    downloaded_file_count = 0
    for github_file in files_to_be_downloaded:
        filename = os.path.basename(urlparse(github_file["url"]).path)
        print(f'attempting download of {github_file["url"]} for file {filename}')
        file_content_response = request_via_http_with_retry(GITHUB_HOST_NAME, github_file["url"], MAXIMUM_NUMBER_OF_RETRIES)
        if not file_content_response:
            print(f'download of {filename} content failed ... downloads incomplete ... exiting')
            sys.exit(1)
        file_content_response_data = json.loads(file_content_response.read().decode("utf-8"))
        file_data = base64.b64decode(file_content_response_data["content"]).decode()
        output_file_path = os.path.join(destination_directory_path, filename)
        with open(output_file_path, "w") as output_file:
            output_file.write(file_data)
        print(f'file {filename} written')
        downloaded_file_count = downloaded_file_count + 1
    if downloaded_file_count == 0:
        print(f'no files were download, using source_filename {args.source_filename}')
        sys.exit(1)
    desired_files_count = len(files_to_be_downloaded)
    if desired_files_count != downloaded_file_count:
        print(f'{desired_files_count} matching files were found in github, but only {downloaded_file_count} were successfully downloaded')
        sys.exit(1)
    
def main():
    parser = create_arg_parser()
    args = parser.parse_args()
    exit_if_args_are_invalid(args)
    download_files_from_github(args)

if __name__ == '__main__':
    main()
