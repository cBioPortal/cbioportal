# UptimeRobot Integration

## Configuration

Both configuration properties must be set to enable the integration:

```properties
uptime_robot_status_page_url=https://status.cbioportal.org
uptime_robot_api_key=YOUR_API_KEY_HERE
```

Add these to your `application.properties` file.

### Getting Your UptimeRobot API Key

1. Log in to your [UptimeRobot account](https://uptimerobot.com/)
2. Navigate to "Status Pages"
3. Open your status page settings
4. Look for the "Event Feed" or "API" section
5. Copy the API key (e.g., `RlrzpsmAn`)
6. Note your status page URL (e.g., `https://status.cbioportal.org`)

### Testing the Configuration

You can test your API endpoint manually:

```bash
curl https://status.cbioportal.org/api/getEventFeed/YOUR_API_KEY
```

You should receive a JSON response containing event data.

## Disabling the Integration

To disable the integration, simply remove or comment out both configuration properties:

```properties
# uptime_robot_status_page_url=https://status.cbioportal.org
# uptime_robot_api_key=YOUR_API_KEY_HERE
```

The integration will automatically disable if either property is missing.
