#!/usr/bin/env python
"""
Make screenshot of given url using selenium grid
"""
import selenium.webdriver as webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
import argparse
import time

def make_screenshot(selenium_hub_url, desired_capabilities, url, png, timeout_in_seconds):
    driver = webdriver.Remote(command_executor=selenium_hub_url,
                              desired_capabilities=desired_capabilities)

    driver.get(url)
    time.sleep(timeout_in_seconds)

    # Remove version info from footer
    driver.execute_script("""
    var footer = document.getElementById("footer");
    if (footer) {
        footer.style.display = "none";
    }
    """)

    # save screenshot
    driver.get_screenshot_as_file(png)
    driver.quit()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('selenium_hub_url', help="e.g. http://hub:4444/wd/hub")
    parser.add_argument('browser', type=str, choices=["chrome","firefox"],
                        help="selenium browser to use to take the screenshot")
    parser.add_argument('url', help="url to make the screenshot of")
    parser.add_argument('png', help="png to store the screenshot")
    parser.add_argument('timeout_in_seconds', type=int, help="keep searching for given id for x seconds")
    args = parser.parse_args()

    # set browser
    desired_capabilities = {
        "chrome": DesiredCapabilities.CHROME,
        "firefox": DesiredCapabilities.FIREFOX
    }[args.browser]

    make_screenshot(args.selenium_hub_url, desired_capabilities, args.url,
                    args.png, args.timeout_in_seconds)
