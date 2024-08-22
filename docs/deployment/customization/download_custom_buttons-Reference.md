# Custom Buttons for Data Tables

Custom Buttons can be defined which will conditionally appear in all group comparison data tables (with CopyDownloadControls - after the download button) to launch a custom URL. This can be used, for example, to launch a software application (that is installed on the user's system) with the data. This configuration can also customize new elements on the Visualize page.

## Configuration File

The Custom Buttons are defined in a JSON file on the classpath. Set `download_custom_buttons_json` to refer to the file (see [application.properties reference](application.properties-Reference.md#add-custom-buttons-to-data-tables)).

## JSON format

The JSON file contains an array where each entry defines one Custom Button. For example:

```
[
    {
        "id": "myurl",
        "name": "My Software",
        "tooltip": "Launch My Software with data (copied to clipboard)",
        "image_src": "https://mydomain.com/icon.png",
        "required_user_agent": "Win",
        "required_installed_font_family": "MyFont",
        "url_format": "myurl://?clipboard-Study={studyName}&-ImportDataLength={dataLength}",
        "visualize_title": "My Software",
        "visualize_href": "https://www.mydomain.com/",
        "visualize_description": "Custom software for analyzing group comparison data.",
        "visualize_image_src": "https://www.mydomain.com/preview.png"
    }
]
```

### General Properties
- **id**: A unique identifier.
- **name**: The display name that may be displayed to the user.

### Button Customization

- **tooltip**: The text that appears when the user hovers over the button.
- **image_src**: The image to display in the button.
- **required_platform**: (optional) This string must be present in the UserAgent for the button to be visible. For example, 'Win' indicates Windows.
- **required_installed_font_family**: (optional) A custom font to check is installed in the user's system for the button to be visible (see below).
- **url_format**: The custom URL that is launched. There are a few properties that can be accessed: `dataLength`, `studyName`.

### Visualize Page Customization (Optional)

The Custom Button will get it's own section in the Visualize page if visualize_href is defined.

- **visualize_title**: The text that appears at the top of the section.
- **visualize_href**: The link to use for clickable elements.
- **visualize_description**: The text that appears under the title.
- **visualize_image_src**: The image that appears under the description.

## Technical Notes

### Custom Font 

To display the button, the cBioPortal Frontend needs to know if the software is installed (more specifically, the custom URL protocol).
Since it is not possible for the cBioPortal Frontend code to query the user's browser to see if a custom URL protocol is supported, a workaround is used where the software installs a custom font, and the frontend code checks if that font is installed.

### Data Transmission

Currently, the code only supports sending the data via the Clipboard. _Future Improvements:_ It is possible to modify the code to support other data transmission vectors, such as sending the data to a specific URL or encoding into the URL.

### Software Setup

To modify software to leverage this:

- Modify your software to read data from clipboard.
- Modify your software or installer to register a URL protocol to launch.
- Modify your software or installer to install a custom font.